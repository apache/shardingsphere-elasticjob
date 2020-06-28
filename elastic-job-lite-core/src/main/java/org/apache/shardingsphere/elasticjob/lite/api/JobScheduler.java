/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.lite.api;

import org.apache.shardingsphere.elasticjob.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.api.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.api.script.ScriptJob;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.lite.executor.JobFacade;
import org.apache.shardingsphere.elasticjob.lite.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeService;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobShutdownHookPlugin;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.LiteJob;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.LiteJobFacade;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.SchedulerFacade;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.tracing.api.TracingConfiguration;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.SimpleThreadPool;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Job scheduler.
 */
public final class JobScheduler {
    
    private static final String ELASTIC_JOB_DATA_MAP_KEY = "elasticJob";
    
    private static final String JOB_FACADE_DATA_MAP_KEY = "jobFacade";
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final ElasticJob elasticJob;
    
    private final JobConfiguration jobConfig;
    
    private final SchedulerFacade schedulerFacade;
    
    private final JobFacade jobFacade;
    
    public JobScheduler(final CoordinatorRegistryCenter regCenter, final ElasticJob elasticJob, final JobConfiguration jobConfig, final ElasticJobListener... elasticJobListeners) {
        this.regCenter = regCenter;
        this.elasticJob = elasticJob;
        this.jobConfig = jobConfig;
        JobRegistry.getInstance().addJobInstance(jobConfig.getJobName(), new JobInstance());
        List<ElasticJobListener> elasticJobListenerList = Arrays.asList(elasticJobListeners);
        setGuaranteeServiceForElasticJobListeners(regCenter, elasticJobListenerList);
        schedulerFacade = new SchedulerFacade(regCenter, jobConfig.getJobName(), elasticJobListenerList);
        jobFacade = new LiteJobFacade(regCenter, jobConfig.getJobName(), Arrays.asList(elasticJobListeners));
    }
    
    public JobScheduler(final CoordinatorRegistryCenter regCenter, final ElasticJob elasticJob, final JobConfiguration jobConfig, final TracingConfiguration tracingConfig,
                        final ElasticJobListener... elasticJobListeners) {
        this.regCenter = regCenter;
        this.elasticJob = elasticJob;
        this.jobConfig = jobConfig;
        JobRegistry.getInstance().addJobInstance(jobConfig.getJobName(), new JobInstance());
        List<ElasticJobListener> elasticJobListenerList = Arrays.asList(elasticJobListeners);
        setGuaranteeServiceForElasticJobListeners(regCenter, elasticJobListenerList);
        schedulerFacade = new SchedulerFacade(regCenter, jobConfig.getJobName(), elasticJobListenerList);
        jobFacade = new LiteJobFacade(regCenter, jobConfig.getJobName(), Arrays.asList(elasticJobListeners), tracingConfig);
    }
    
    private void setGuaranteeServiceForElasticJobListeners(final CoordinatorRegistryCenter regCenter, final List<ElasticJobListener> elasticJobListeners) {
        GuaranteeService guaranteeService = new GuaranteeService(regCenter, jobConfig.getJobName());
        for (ElasticJobListener each : elasticJobListeners) {
            if (each instanceof AbstractDistributeOnceElasticJobListener) {
                ((AbstractDistributeOnceElasticJobListener) each).setGuaranteeService(guaranteeService);
            }
        }
    }
    
    /**
     * Initialize job.
     */
    public void init() {
        JobConfiguration jobConfigFromRegCenter = schedulerFacade.updateJobConfiguration(null == elasticJob ? ScriptJob.class.getName() : elasticJob.getClass().getName(), jobConfig);
        JobRegistry.getInstance().setCurrentShardingTotalCount(jobConfigFromRegCenter.getJobName(), jobConfigFromRegCenter.getShardingTotalCount());
        JobScheduleController jobScheduleController = new JobScheduleController(createScheduler(), createJobDetail(elasticJob), jobConfigFromRegCenter.getJobName());
        JobRegistry.getInstance().registerJob(jobConfigFromRegCenter.getJobName(), jobScheduleController, regCenter);
        schedulerFacade.registerStartUpInfo(!jobConfigFromRegCenter.isDisabled());
        jobScheduleController.scheduleJob(jobConfigFromRegCenter.getCron());
    }
    
    private Scheduler createScheduler() {
        Scheduler result;
        try {
            StdSchedulerFactory factory = new StdSchedulerFactory();
            factory.initialize(getQuartzProps());
            result = factory.getScheduler();
            result.getListenerManager().addTriggerListener(schedulerFacade.newJobTriggerListener());
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
        return result;
    }
    
    private Properties getQuartzProps() {
        Properties result = new Properties();
        result.put("org.quartz.threadPool.class", SimpleThreadPool.class.getName());
        result.put("org.quartz.threadPool.threadCount", "1");
        result.put("org.quartz.scheduler.instanceName", jobConfig.getJobName());
        result.put("org.quartz.jobStore.misfireThreshold", "1");
        result.put("org.quartz.plugin.shutdownhook.class", JobShutdownHookPlugin.class.getName());
        result.put("org.quartz.plugin.shutdownhook.cleanShutdown", Boolean.TRUE.toString());
        return result;
    }
    
    private JobDetail createJobDetail(final ElasticJob elasticJob) {
        JobDetail result = JobBuilder.newJob(LiteJob.class).withIdentity(jobConfig.getJobName()).build();
        result.getJobDataMap().put(JOB_FACADE_DATA_MAP_KEY, jobFacade);
        if (null != elasticJob && !elasticJob.getClass().getName().equals(ScriptJob.class.getName())) {
            result.getJobDataMap().put(ELASTIC_JOB_DATA_MAP_KEY, elasticJob);
        }
        return result;
    }
    
   /**
    * Shutdown job.
    */
    public void shutdown() { 
        schedulerFacade.shutdownInstance();
    }
}
