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

package com.dangdang.ddframe.job.cloud.producer;

import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.state.ready.ReadyService;
import com.dangdang.ddframe.job.api.exception.JobSystemException;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.SimpleThreadPool;

import java.util.Collection;
import java.util.Properties;

/**
 * 发布任务作业的调度器.
 *
 * @author caohao
 */
class TaskProducerScheduler {
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final Scheduler scheduler;
    
    TaskProducerScheduler(final CoordinatorRegistryCenter regCenter) {
        this.regCenter = regCenter;
        scheduler = getScheduler();
    }
    
    private Scheduler getScheduler() {
        StdSchedulerFactory factory = new StdSchedulerFactory();
        try {
            factory.initialize(getQuartzProperties());
            return factory.getScheduler();
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
    }
    
    private Properties getQuartzProperties() {
        Properties result = new Properties();
        result.put("org.quartz.threadPool.class", SimpleThreadPool.class.getName());
        result.put("org.quartz.threadPool.threadCount", Integer.toString(Runtime.getRuntime().availableProcessors() * 2));
        result.put("org.quartz.scheduler.instanceName", "ELASTIC_JOB_CLOUD");
        return result;
    }
    
    void startup(final Collection<CloudJobConfiguration> jobConfigs) {
        try {
            for (CloudJobConfiguration each : jobConfigs) {
                scheduleJob(each);
            }
            scheduler.start();
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
    }
    
    //TODO 并发优化
    synchronized void register(final CloudJobConfiguration jobConfig) {
        JobKey jobKey = buildJobKey(jobConfig.getTypeConfig().getCoreConfig().getCron());
        TaskProducerJobContext.getInstance().put(jobKey, jobConfig.getJobName());
        try {
            if (!scheduler.checkExists(jobKey)) {
                scheduleJob(jobConfig);
            }
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
    }
    
    private void scheduleJob(final CloudJobConfiguration jobConfig) throws SchedulerException {
        JobDetail jobDetail = buildJobDetail(jobConfig.getTypeConfig().getCoreConfig().getCron());
        TaskProducerJobContext.getInstance().put(jobDetail.getKey(), jobConfig.getJobName());
        jobDetail.getJobDataMap().put("readyService", new ReadyService(regCenter));
        scheduler.scheduleJob(jobDetail, buildTrigger(jobConfig.getTypeConfig().getCoreConfig().getCron()));
    }
    
    void deregister(final CloudJobConfiguration jobConfig) {
        TaskProducerJobContext.getInstance().remove(jobConfig.getJobName());
        if (!TaskProducerJobContext.getInstance().contains(buildJobKey(jobConfig.getTypeConfig().getCoreConfig().getCron()))) {
            try {
                scheduler.unscheduleJob(TriggerKey.triggerKey(jobConfig.getTypeConfig().getCoreConfig().getCron()));
            } catch (final SchedulerException ex) {
                throw new JobSystemException(ex);
            }
        }
    }
    
    void shutdown() {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
    }
    
    private Trigger buildTrigger(final String cron) {
        return TriggerBuilder.newTrigger().withIdentity(cron)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron)
                        .withMisfireHandlingInstructionDoNothing()).build();
    }
    
    private JobDetail buildJobDetail(final String cron) {
        return JobBuilder.newJob(TaskProducerJob.class).withIdentity(cron).build();
    }
    
    private JobKey buildJobKey(final String cron) {
        return JobKey.jobKey(cron);
    }
}
