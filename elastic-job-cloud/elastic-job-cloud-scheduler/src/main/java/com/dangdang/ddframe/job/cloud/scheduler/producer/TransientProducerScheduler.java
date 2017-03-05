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

package com.dangdang.ddframe.job.cloud.scheduler.producer;

import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.state.ready.ReadyService;
import com.dangdang.ddframe.job.exception.JobSystemException;
import lombok.Setter;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.plugins.management.ShutdownHookPlugin;
import org.quartz.simpl.SimpleThreadPool;

import java.util.List;
import java.util.Properties;

/**
 * 发布瞬时作业任务的调度器.
 *
 * @author caohao
 */
class TransientProducerScheduler {
    
    private final TransientProducerRepository repository;
    
    private final ReadyService readyService;
    
    private Scheduler scheduler;
    
    TransientProducerScheduler(final ReadyService readyService) {
        repository = new TransientProducerRepository();
        this.readyService = readyService;
    }
    
    void start() {
        scheduler = getScheduler();
        try {
            scheduler.start();
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
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
        result.put("org.quartz.scheduler.instanceName", "ELASTIC_JOB_CLOUD_TRANSIENT_PRODUCER");
        result.put("org.quartz.plugin.shutdownhook.class", ShutdownHookPlugin.class.getName());
        result.put("org.quartz.plugin.shutdownhook.cleanShutdown", Boolean.TRUE.toString());
        return result;
    }
    
    // TODO 并发优化
    synchronized void register(final CloudJobConfiguration jobConfig) {
        String cron = jobConfig.getTypeConfig().getCoreConfig().getCron();
        JobKey jobKey = buildJobKey(cron);
        repository.put(jobKey, jobConfig.getJobName());
        try {
            if (!scheduler.checkExists(jobKey)) {
                scheduler.scheduleJob(buildJobDetail(jobKey), buildTrigger(jobKey.getName()));
            }
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
    }
    
    private JobDetail buildJobDetail(final JobKey jobKey) {
        JobDetail result = JobBuilder.newJob(ProducerJob.class).withIdentity(jobKey).build();
        result.getJobDataMap().put("repository", repository);
        result.getJobDataMap().put("readyService", readyService);
        return result;
    }
    
    private Trigger buildTrigger(final String cron) {
        return TriggerBuilder.newTrigger().withIdentity(cron).withSchedule(CronScheduleBuilder.cronSchedule(cron).withMisfireHandlingInstructionDoNothing()).build();
    }
    
    void deregister(final CloudJobConfiguration jobConfig) {
        repository.remove(jobConfig.getJobName());
        String cron = jobConfig.getTypeConfig().getCoreConfig().getCron();
        if (!repository.containsKey(buildJobKey(cron))) {
            try {
                scheduler.unscheduleJob(TriggerKey.triggerKey(cron));
            } catch (final SchedulerException ex) {
                throw new JobSystemException(ex);
            }
        }
    }
    
    private JobKey buildJobKey(final String cron) {
        return JobKey.jobKey(cron);
    }
    
    void shutdown() {
        try {
            if (null != scheduler && !scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
        repository.removeAll();
    }
    
    @Setter
    public static final class ProducerJob implements Job {
        
        private TransientProducerRepository repository;
        
        private ReadyService readyService;
        
        @Override
        public void execute(final JobExecutionContext context) throws JobExecutionException {
            List<String> jobNames = repository.get(context.getJobDetail().getKey());
            for (String each : jobNames) {
                readyService.addTransient(each);
            }
        }
    }
}
