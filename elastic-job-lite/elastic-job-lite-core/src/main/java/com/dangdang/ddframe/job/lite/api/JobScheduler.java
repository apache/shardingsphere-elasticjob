/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.lite.api;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.script.ScriptJob;
import com.dangdang.ddframe.job.event.JobEventBus;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.exception.JobConfigurationException;
import com.dangdang.ddframe.job.exception.JobSystemException;
import com.dangdang.ddframe.job.executor.JobExecutorFactory;
import com.dangdang.ddframe.job.executor.JobFacade;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.internal.executor.JobExecutor;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.lite.internal.schedule.LiteJobFacade;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import lombok.Setter;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.plugins.management.ShutdownHookPlugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

/**
 * 作业调度器.
 * 
 * @author zhangliang
 * @author caohao
 */
public class JobScheduler {
    
    public static final String ELASTIC_JOB_DATA_MAP_KEY = "elasticJob";
    
    private static final String JOB_FACADE_DATA_MAP_KEY = "jobFacade";
    
    private final JobExecutor jobExecutor;
    
    private final JobFacade jobFacade;
    
    public JobScheduler(final CoordinatorRegistryCenter regCenter, final LiteJobConfiguration liteJobConfig, final ElasticJobListener... elasticJobListeners) {
        this(regCenter, liteJobConfig, Collections.<JobEventConfiguration>emptyList(), elasticJobListeners);
    }
    
    public JobScheduler(final CoordinatorRegistryCenter regCenter, final LiteJobConfiguration liteJobConfig, final Collection<JobEventConfiguration> jobEventConfigs, 
                        final ElasticJobListener... elasticJobListeners) {
        JobEventBus jobEventBus = new JobEventBus(jobEventConfigs.toArray(new JobEventConfiguration[jobEventConfigs.size()]));
        jobExecutor = new JobExecutor(regCenter, liteJobConfig, elasticJobListeners);
        jobFacade = new LiteJobFacade(regCenter, liteJobConfig.getJobName(), Arrays.asList(elasticJobListeners), jobEventBus);
    }
    
    /**
     * 初始化作业.
     */
    public void init() {
        jobExecutor.init();
        JobDetail jobDetail = JobBuilder.newJob(LiteJob.class).withIdentity(jobExecutor.getLiteJobConfig().getJobName()).build();
        try {
            if (!jobExecutor.getLiteJobConfig().getTypeConfig().getJobClass().equals(ScriptJob.class.getCanonicalName())) {
                jobDetail.getJobDataMap().put(ELASTIC_JOB_DATA_MAP_KEY, Class.forName(jobExecutor.getLiteJobConfig().getTypeConfig().getJobClass()).newInstance());
            }
        } catch (final ReflectiveOperationException ex) {
            throw new JobConfigurationException("Elastic-Job: Job class '%s' can not initialize.", jobExecutor.getLiteJobConfig().getTypeConfig().getJobClass());
        }
        jobDetail.getJobDataMap().put(JOB_FACADE_DATA_MAP_KEY, jobFacade);
        JobScheduleController jobScheduleController;
        try {
            jobScheduleController = new JobScheduleController(initializeScheduler(jobDetail.getKey().toString()), jobDetail, 
                    jobExecutor.getSchedulerFacade(), jobExecutor.getLiteJobConfig().getJobName());
            jobScheduleController.scheduleJob(jobExecutor.getSchedulerFacade().loadJobConfiguration().getTypeConfig().getCoreConfig().getCron());
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
        JobRegistry.getInstance().addJobScheduleController(jobExecutor.getLiteJobConfig().getJobName(), jobScheduleController);
    }
    
    private Scheduler initializeScheduler(final String jobName) throws SchedulerException {
        StdSchedulerFactory factory = new StdSchedulerFactory();
        factory.initialize(getBaseQuartzProperties(jobName));
        Scheduler result = factory.getScheduler();
        result.getListenerManager().addTriggerListener(jobExecutor.getSchedulerFacade().newJobTriggerListener());
        return result;
    }
    
    private Properties getBaseQuartzProperties(final String jobName) {
        Properties result = new Properties();
        result.put("org.quartz.threadPool.class", org.quartz.simpl.SimpleThreadPool.class.getName());
        result.put("org.quartz.threadPool.threadCount", "1");
        result.put("org.quartz.scheduler.instanceName", jobName);
        if (!jobExecutor.getSchedulerFacade().loadJobConfiguration().getTypeConfig().getCoreConfig().isMisfire()) {
            result.put("org.quartz.jobStore.misfireThreshold", "1");
        }
        result.put("org.quartz.plugin.shutdownhook.class", ShutdownHookPlugin.class.getName());
        result.put("org.quartz.plugin.shutdownhook.cleanShutdown", Boolean.TRUE.toString());
        prepareEnvironments(result);
        return result;
    }
    
    protected void prepareEnvironments(final Properties props) {
    }
    
    /**
     * 停止作业调度.
     */
    public void shutdown() {
        JobRegistry.getInstance().getJobScheduleController(jobExecutor.getLiteJobConfig().getJobName()).shutdown();
    }
    
    /**
     * Lite调度作业.
     * 
     * @author zhangliang
     */
    public static final class LiteJob implements Job {
        
        @Setter
        private ElasticJob elasticJob;
        
        @Setter
        private JobFacade jobFacade;
        
        @Override
        public void execute(final JobExecutionContext context) throws JobExecutionException {
            JobExecutorFactory.getJobExecutor(elasticJob, jobFacade).execute();
        }
    }
}
