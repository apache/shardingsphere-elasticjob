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

package org.apache.shardingsphere.elasticjob.lite.internal.schedule;

import lombok.Getter;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.executor.ElasticJobExecutor;
import org.apache.shardingsphere.elasticjob.infra.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListenerFactory;
import org.apache.shardingsphere.elasticjob.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeService;
import org.apache.shardingsphere.elasticjob.lite.internal.setup.JobClassNameProviderFactory;
import org.apache.shardingsphere.elasticjob.lite.internal.setup.SetUpFacade;
import org.apache.shardingsphere.elasticjob.lite.internal.util.ParameterUtils;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.SimpleThreadPool;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Job scheduler.
 */
public final class JobScheduler {
    
    private static final String JOB_EXECUTOR_DATA_MAP_KEY = "jobExecutor";
    
    @Getter
    private final CoordinatorRegistryCenter regCenter;
    
    private final String elasticJobType;
    
    @Getter
    private final JobConfiguration jobConfig;
    
    private final SetUpFacade setUpFacade;
    
    private final SchedulerFacade schedulerFacade;
    
    private final LiteJobFacade jobFacade;
    
    private final ElasticJobExecutor jobExecutor;
    
    @Getter
    private final JobScheduleController jobScheduleController;
    
    public JobScheduler(final CoordinatorRegistryCenter regCenter, final ElasticJob elasticJob, final JobConfiguration jobConfig) {
        this(regCenter, elasticJob, jobConfig, null);
    }
    
    public JobScheduler(final CoordinatorRegistryCenter regCenter, final ElasticJob elasticJob, final JobConfiguration jobConfig, final TracingConfiguration<?> tracingConfig) {
        this.regCenter = regCenter;
        elasticJobType = null;
        final Collection<ElasticJobListener> elasticJobListeners = jobConfig.getJobListenerTypes().stream()
                .map(elasticJobTypeWithParameter -> lookupElasticJobListener(jobConfig.getJobName(), elasticJobTypeWithParameter)).collect(Collectors.toList());
        setUpFacade = new SetUpFacade(regCenter, jobConfig.getJobName(), elasticJobListeners);
        schedulerFacade = new SchedulerFacade(regCenter, jobConfig.getJobName());
        jobFacade = new LiteJobFacade(regCenter, jobConfig.getJobName(), elasticJobListeners, tracingConfig);
        jobExecutor = null == elasticJob ? new ElasticJobExecutor(elasticJobType, jobConfig, jobFacade) : new ElasticJobExecutor(elasticJob, jobConfig, jobFacade);
        String jobClassName = JobClassNameProviderFactory.getProvider().getJobClassName(elasticJob);
        this.jobConfig = setUpFacade.setUpJobConfiguration(jobClassName, jobConfig);
        jobScheduleController = createJobScheduleController();
    }
    
    public JobScheduler(final CoordinatorRegistryCenter regCenter, final String elasticJobType, final JobConfiguration jobConfig) {
        this(regCenter, elasticJobType, jobConfig, null);
    }
    
    public JobScheduler(final CoordinatorRegistryCenter regCenter, final String elasticJobType, final JobConfiguration jobConfig, final TracingConfiguration<?> tracingConfig) {
        this.regCenter = regCenter;
        this.elasticJobType = elasticJobType;
        final Collection<ElasticJobListener> elasticJobListeners = jobConfig.getJobListenerTypes().stream()
                .map(elasticJobTypeWithParameter -> lookupElasticJobListener(jobConfig.getJobName(), elasticJobTypeWithParameter)).collect(Collectors.toList());
        setUpFacade = new SetUpFacade(regCenter, jobConfig.getJobName(), elasticJobListeners);
        schedulerFacade = new SchedulerFacade(regCenter, jobConfig.getJobName());
        jobFacade = new LiteJobFacade(regCenter, jobConfig.getJobName(), elasticJobListeners, tracingConfig);
        jobExecutor = new ElasticJobExecutor(elasticJobType, jobConfig, jobFacade);
        this.jobConfig = setUpFacade.setUpJobConfiguration(elasticJobType, jobConfig);
        jobScheduleController = createJobScheduleController();
    }
    
    private ElasticJobListener lookupElasticJobListener(final String jobName, final String jobListenerTypeWithParameter) {
        String[] split = jobListenerTypeWithParameter.split("\\?");
        String jobListenerType = split[0];
        ElasticJobListener listener = ElasticJobListenerFactory.getListener(jobListenerType);
        if (!(listener instanceof AbstractDistributeOnceElasticJobListener)) {
            return listener;
        }
        Map<String, String> parameters = 1 < split.length ? ParameterUtils.parseQuery(split[1]) : Collections.emptyMap();
        return configureGuaranteeService(jobName, parameters, listener);
    }
    
    private ElasticJobListener configureGuaranteeService(final String jobName, final Map<String, String> parameters, final ElasticJobListener listener) {
        GuaranteeService guaranteeService = new GuaranteeService(regCenter, jobName);
        long startedTimeoutMilliseconds = Long.parseLong(parameters.getOrDefault("startedTimeoutMilliseconds", "0"));
        long completedTimeoutMilliseconds = Long.parseLong(parameters.getOrDefault("completedTimeoutMilliseconds", "0"));
        ((AbstractDistributeOnceElasticJobListener) listener).addGuaranteeService(guaranteeService, jobName, startedTimeoutMilliseconds, completedTimeoutMilliseconds);
        return listener;
    }
    
    private JobScheduleController createJobScheduleController() {
        JobScheduleController result = new JobScheduleController(createScheduler(), createJobDetail(), getJobConfig().getJobName());
        JobRegistry.getInstance().registerJob(getJobConfig().getJobName(), result);
        registerStartUpInfo();
        return result;
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
        result.put("org.quartz.scheduler.instanceName", getJobConfig().getJobName());
        result.put("org.quartz.jobStore.misfireThreshold", "1");
        result.put("org.quartz.plugin.shutdownhook.class", JobShutdownHookPlugin.class.getName());
        result.put("org.quartz.plugin.shutdownhook.cleanShutdown", Boolean.TRUE.toString());
        return result;
    }
    
    private JobDetail createJobDetail() {
        JobDetail result = JobBuilder.newJob(LiteJob.class).withIdentity(getJobConfig().getJobName()).build();
        result.getJobDataMap().put(JOB_EXECUTOR_DATA_MAP_KEY, jobExecutor);
        return result;
    }
    
    private void registerStartUpInfo() {
        JobRegistry.getInstance().registerRegistryCenter(jobConfig.getJobName(), regCenter);
        JobRegistry.getInstance().addJobInstance(jobConfig.getJobName(), new JobInstance());
        JobRegistry.getInstance().setCurrentShardingTotalCount(jobConfig.getJobName(), jobConfig.getShardingTotalCount());
        setUpFacade.registerStartUpInfo(!jobConfig.isDisabled());
    }
    
   /**
    * Shutdown job.
    */
    public void shutdown() {
        schedulerFacade.shutdownInstance();
        jobExecutor.shutdown();
    }
}
