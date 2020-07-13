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

package org.apache.shardingsphere.elasticjob.cloud.executor;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.Protos;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.executor.ElasticJobExecutor;
import org.apache.shardingsphere.elasticjob.executor.JobFacade;
import org.apache.shardingsphere.elasticjob.infra.exception.JobSystemException;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.plugins.management.ShutdownHookPlugin;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Daemon task scheduler.
 */
@RequiredArgsConstructor
public final class DaemonTaskScheduler {
    
    private static final String ELASTIC_JOB_DATA_MAP_KEY = "elasticJob";
    
    private static final String ELASTIC_JOB_TYPE_DATA_MAP_KEY = "elasticJobType";
    
    private static final String JOB_FACADE_DATA_MAP_KEY = "jobFacade";
    
    private static final String EXECUTOR_DRIVER_DATA_MAP_KEY = "executorDriver";
    
    private static final String TASK_ID_DATA_MAP_KEY = "taskId";
    
    private static final ConcurrentHashMap<String, Scheduler> RUNNING_SCHEDULERS = new ConcurrentHashMap<>(1024, 1);
    
    private final ElasticJob elasticJob;
    
    private final String elasticJobType;
    
    private final JobConfiguration jobConfig;
    
    private final JobFacade jobFacade;
    
    private final ExecutorDriver executorDriver;
    
    private final Protos.TaskID taskId;
    
    /**
     * Init the job.
     */
    public void init() {
        JobDetail jobDetail = JobBuilder.newJob(DaemonJob.class).withIdentity(jobConfig.getJobName()).build();
        jobDetail.getJobDataMap().put(ELASTIC_JOB_DATA_MAP_KEY, elasticJob);
        jobDetail.getJobDataMap().put(ELASTIC_JOB_TYPE_DATA_MAP_KEY, elasticJobType);
        jobDetail.getJobDataMap().put(JOB_FACADE_DATA_MAP_KEY, jobFacade);
        jobDetail.getJobDataMap().put(EXECUTOR_DRIVER_DATA_MAP_KEY, executorDriver);
        jobDetail.getJobDataMap().put(TASK_ID_DATA_MAP_KEY, taskId);
        try {
            scheduleJob(initializeScheduler(), jobDetail, taskId.getValue(), jobConfig.getCron());
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
    }
    
    private Scheduler initializeScheduler() throws SchedulerException {
        StdSchedulerFactory factory = new StdSchedulerFactory();
        factory.initialize(getBaseQuartzProperties());
        return factory.getScheduler();
    }
    
    private Properties getBaseQuartzProperties() {
        Properties result = new Properties();
        result.put("org.quartz.threadPool.class", org.quartz.simpl.SimpleThreadPool.class.getName());
        result.put("org.quartz.threadPool.threadCount", "1");
        result.put("org.quartz.scheduler.instanceName", taskId.getValue());
        if (!jobConfig.isMisfire()) {
            result.put("org.quartz.jobStore.misfireThreshold", "1");
        }
        result.put("org.quartz.plugin.shutdownhook.class", ShutdownHookPlugin.class.getName());
        result.put("org.quartz.plugin.shutdownhook.cleanShutdown", Boolean.TRUE.toString());
        return result;
    }
    
    private void scheduleJob(final Scheduler scheduler, final JobDetail jobDetail, final String triggerIdentity, final String cron) {
        try {
            if (!scheduler.checkExists(jobDetail.getKey())) {
                scheduler.scheduleJob(jobDetail, createTrigger(triggerIdentity, cron));
            }
            scheduler.start();
            RUNNING_SCHEDULERS.putIfAbsent(scheduler.getSchedulerName(), scheduler);
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
    }
    
    private CronTrigger createTrigger(final String triggerIdentity, final String cron) {
        return TriggerBuilder.newTrigger().withIdentity(triggerIdentity).withSchedule(CronScheduleBuilder.cronSchedule(cron).withMisfireHandlingInstructionDoNothing()).build();
    }
    
    /**
     * Shutdown scheduling the task.
     *
     * @param taskID task id
     */
    public static void shutdown(final Protos.TaskID taskID) {
        Scheduler scheduler = RUNNING_SCHEDULERS.remove(taskID.getValue());
        if (null != scheduler) {
            try {
                scheduler.shutdown();
            } catch (final SchedulerException ex) {
                throw new JobSystemException(ex);
            }
        }
    }
    
    /**
     * Daemon job.
     */
    public static final class DaemonJob implements Job {
        
        @Setter
        private ElasticJob elasticJob;
    
        @Setter
        private String elasticJobType;
        
        @Setter
        private CloudJobFacade jobFacade;
        
        @Setter
        private ExecutorDriver executorDriver;
    
        @Setter
        private Protos.TaskID taskId;
        
        private volatile ElasticJobExecutor jobExecutor;
        
        @Override
        public void execute(final JobExecutionContext context) {
            ShardingContexts shardingContexts = jobFacade.getShardingContexts();
            int jobEventSamplingCount = shardingContexts.getJobEventSamplingCount();
            int currentJobEventSamplingCount = shardingContexts.getCurrentJobEventSamplingCount();
            if (jobEventSamplingCount > 0 && ++currentJobEventSamplingCount < jobEventSamplingCount) {
                shardingContexts.setCurrentJobEventSamplingCount(currentJobEventSamplingCount);
                jobFacade.getShardingContexts().setAllowSendJobEvent(false);
                getJobExecutor().execute();
            } else {
                jobFacade.getShardingContexts().setAllowSendJobEvent(true);
                executorDriver.sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskId).setState(Protos.TaskState.TASK_RUNNING).setMessage("BEGIN").build());
                getJobExecutor().execute();
                executorDriver.sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskId).setState(Protos.TaskState.TASK_RUNNING).setMessage("COMPLETE").build());
                shardingContexts.setCurrentJobEventSamplingCount(0);
            }
        }
        
        private ElasticJobExecutor getJobExecutor() {
            if (null == jobExecutor) {
                createJobExecutor();
            }
            return jobExecutor;
        }
        
        private synchronized void createJobExecutor() {
            if (null != jobExecutor) {
                return;
            }
            jobExecutor = null == elasticJob
                    ? new ElasticJobExecutor(elasticJobType, jobFacade.loadJobConfiguration(true), jobFacade)
                    : new ElasticJobExecutor(elasticJob, jobFacade.loadJobConfiguration(true), jobFacade);
        }
    }
}
