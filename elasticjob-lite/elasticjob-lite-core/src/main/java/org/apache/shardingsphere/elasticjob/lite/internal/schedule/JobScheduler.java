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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandlerPropertiesValidator;
import org.apache.shardingsphere.elasticjob.executor.ElasticJobExecutor;
import org.apache.shardingsphere.elasticjob.infra.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListenerFactory;
import org.apache.shardingsphere.elasticjob.infra.spi.ElasticJobServiceLoader;
import org.apache.shardingsphere.elasticjob.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeService;
import org.apache.shardingsphere.elasticjob.lite.internal.setup.JobClassNameProviderFactory;
import org.apache.shardingsphere.elasticjob.lite.internal.setup.SetUpFacade;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.SimpleThreadPool;

import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Job scheduler.
 */
public final class JobScheduler {
    
    static {
        ElasticJobServiceLoader.registerTypedService(JobErrorHandlerPropertiesValidator.class);
    }
    
    private static final String JOB_EXECUTOR_DATA_MAP_KEY = "jobExecutor";
    
    @Getter
    private final CoordinatorRegistryCenter regCenter;
    
    @Getter
    private final JobConfiguration jobConfig;
    
    private final SetUpFacade setUpFacade;
    
    private final SchedulerFacade schedulerFacade;
    
    private final LiteJobFacade jobFacade;
    
    private final ElasticJobExecutor jobExecutor;
    
    @Getter
    private final JobScheduleController jobScheduleController;
    
    public JobScheduler(final CoordinatorRegistryCenter regCenter, final ElasticJob elasticJob, final JobConfiguration jobConfig) {
        Preconditions.checkArgument(null != elasticJob, "Elastic job cannot be null.");
        this.regCenter = regCenter;
        Collection<ElasticJobListener> jobListeners = getElasticJobListeners(jobConfig);
        setUpFacade = new SetUpFacade(regCenter, jobConfig.getJobName(), jobListeners);
        String jobClassName = JobClassNameProviderFactory.getProvider().getJobClassName(elasticJob);
        this.jobConfig = setUpFacade.setUpJobConfiguration(jobClassName, jobConfig);
        schedulerFacade = new SchedulerFacade(regCenter, jobConfig.getJobName());
        jobFacade = new LiteJobFacade(regCenter, jobConfig.getJobName(), jobListeners, findTracingConfiguration().orElse(null));
        validateJobProperties();
        jobExecutor = new ElasticJobExecutor(elasticJob, this.jobConfig, jobFacade);
        setGuaranteeServiceForElasticJobListeners(regCenter, jobListeners);
        jobScheduleController = createJobScheduleController();
    }
    
    public JobScheduler(final CoordinatorRegistryCenter regCenter, final String elasticJobType, final JobConfiguration jobConfig) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(elasticJobType), "Elastic job type cannot be null or empty.");
        this.regCenter = regCenter;
        Collection<ElasticJobListener> jobListeners = getElasticJobListeners(jobConfig);
        setUpFacade = new SetUpFacade(regCenter, jobConfig.getJobName(), jobListeners);
        this.jobConfig = setUpFacade.setUpJobConfiguration(elasticJobType, jobConfig);
        schedulerFacade = new SchedulerFacade(regCenter, jobConfig.getJobName());
        jobFacade = new LiteJobFacade(regCenter, jobConfig.getJobName(), jobListeners, findTracingConfiguration().orElse(null));
        validateJobProperties();
        jobExecutor = new ElasticJobExecutor(elasticJobType, this.jobConfig, jobFacade);
        setGuaranteeServiceForElasticJobListeners(regCenter, jobListeners);
        jobScheduleController = createJobScheduleController();
    }
    
    private Collection<ElasticJobListener> getElasticJobListeners(final JobConfiguration jobConfig) {
        return jobConfig.getJobListenerTypes().stream()
                .map(type -> ElasticJobListenerFactory.createListener(type).orElseThrow(() -> new IllegalArgumentException(String.format("Can not find job listener type '%s'.", type))))
                .collect(Collectors.toList());
    }
    
    private Optional<TracingConfiguration<?>> findTracingConfiguration() {
        return jobConfig.getExtraConfigurations().stream().filter(each -> each instanceof TracingConfiguration).findFirst().map(extraConfig -> (TracingConfiguration<?>) extraConfig);
    }
    
    private void validateJobProperties() {
        validateJobErrorHandlerProperties();
    }
    
    private void validateJobErrorHandlerProperties() {
        if (null != jobConfig.getJobErrorHandlerType()) {
            ElasticJobServiceLoader.newTypedServiceInstance(JobErrorHandlerPropertiesValidator.class, jobConfig.getJobErrorHandlerType(), jobConfig.getProps())
                    .ifPresent(validator -> validator.validate(jobConfig.getProps()));
        }
    }
    
    private void setGuaranteeServiceForElasticJobListeners(final CoordinatorRegistryCenter regCenter, final Collection<ElasticJobListener> elasticJobListeners) {
        GuaranteeService guaranteeService = new GuaranteeService(regCenter, jobConfig.getJobName());
        for (ElasticJobListener each : elasticJobListeners) {
            if (each instanceof AbstractDistributeOnceElasticJobListener) {
                ((AbstractDistributeOnceElasticJobListener) each).setGuaranteeService(guaranteeService);
            }
        }
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
