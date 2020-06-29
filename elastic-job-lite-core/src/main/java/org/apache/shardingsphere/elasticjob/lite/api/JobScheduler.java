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
import org.apache.shardingsphere.elasticjob.lite.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeService;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobShutdownHookPlugin;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.LiteJob;
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
    
    private static final String REG_CENTER_DATA_MAP_KEY = "regCenter";
    
    private static final String ELASTIC_JOB_DATA_MAP_KEY = "elasticJob";
    
    private static final String JOB_CONFIG_DATA_MAP_KEY = "jobConfig";
    
    private static final String JOB_LISTENERS_DATA_MAP_KEY = "elasticJobListeners";
    
    private static final String TRACING_CONFIG_DATA_MAP_KEY = "tracingConfig";
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final ElasticJob elasticJob;
    
    private final JobConfiguration jobConfig;
    
    private final List<ElasticJobListener> elasticJobListeners;
    
    private final TracingConfiguration tracingConfig;
    
    private final SchedulerFacade schedulerFacade;
    
    public JobScheduler(final CoordinatorRegistryCenter regCenter, final ElasticJob elasticJob, final JobConfiguration jobConfig, final ElasticJobListener... elasticJobListeners) {
        this.regCenter = regCenter;
        this.elasticJob = elasticJob;
        this.jobConfig = jobConfig;
        this.elasticJobListeners = Arrays.asList(elasticJobListeners);
        tracingConfig = null;
        JobRegistry.getInstance().addJobInstance(jobConfig.getJobName(), new JobInstance());
        setGuaranteeServiceForElasticJobListeners(regCenter, this.elasticJobListeners);
        schedulerFacade = new SchedulerFacade(regCenter, jobConfig.getJobName(), this.elasticJobListeners);
    }
    
    public JobScheduler(final CoordinatorRegistryCenter regCenter, final ElasticJob elasticJob, final JobConfiguration jobConfig, final TracingConfiguration tracingConfig,
                        final ElasticJobListener... elasticJobListeners) {
        this.regCenter = regCenter;
        this.elasticJob = elasticJob;
        this.jobConfig = jobConfig;
        this.elasticJobListeners = Arrays.asList(elasticJobListeners);
        this.tracingConfig = tracingConfig;
        JobRegistry.getInstance().addJobInstance(jobConfig.getJobName(), new JobInstance());
        setGuaranteeServiceForElasticJobListeners(regCenter, this.elasticJobListeners);
        schedulerFacade = new SchedulerFacade(regCenter, jobConfig.getJobName(), this.elasticJobListeners);
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
        JobScheduleController jobScheduleController = new JobScheduleController(createScheduler(), createJobDetail(jobConfigFromRegCenter), jobConfigFromRegCenter.getJobName());
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
    
    private JobDetail createJobDetail(final JobConfiguration jobConfig) {
        JobDetail result = JobBuilder.newJob(LiteJob.class).withIdentity(jobConfig.getJobName()).build();
        result.getJobDataMap().put(REG_CENTER_DATA_MAP_KEY, regCenter);
        result.getJobDataMap().put(JOB_CONFIG_DATA_MAP_KEY, jobConfig);
        result.getJobDataMap().put(JOB_LISTENERS_DATA_MAP_KEY, elasticJobListeners);
        result.getJobDataMap().put(TRACING_CONFIG_DATA_MAP_KEY, tracingConfig);
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
