/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.schedule;

import com.dangdang.ddframe.job.cloud.task.ready.ReadyJobQueueService;
import com.dangdang.ddframe.job.cloud.job.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.exception.JobException;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Joiner;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Properties;

/**
 * 云任务调度器.
 *
 * @author zhangliang
 */
public final class CloudTaskScheduler {
    
    private final CloudJobConfiguration task;
    
    private final CoordinatorRegistryCenter registryCenter;
    
    private final Scheduler scheduler;
    
    public CloudTaskScheduler(final CloudJobConfiguration task, final CoordinatorRegistryCenter registryCenter) {
        this.task = task;
        this.registryCenter = registryCenter;
        scheduler = getScheduler();
        startup();
    }
    
    private Scheduler getScheduler() {
        StdSchedulerFactory factory = new StdSchedulerFactory();
        try {
            factory.initialize(getQuartzProperties());
            return factory.getScheduler();
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }
    }
    
    private Properties getQuartzProperties() {
        Properties result = new Properties();
        result.put("org.quartz.threadPool.class", org.quartz.simpl.SimpleThreadPool.class.getName());
        result.put("org.quartz.threadPool.threadCount", Integer.toString(Runtime.getRuntime().availableProcessors() * 2));
        result.put("org.quartz.scheduler.instanceName", createIdentity("Scheduler"));
        return result;
    }
    
    /**
     * 启动调度作业.
     */
    public void startup() {
        JobDetail jobDetail = JobBuilder.newJob(CloudTaskEnqueueJob.class).withIdentity(task.getJobName()).build();
        jobDetail.getJobDataMap().put("jobName", task.getJobName());
        jobDetail.getJobDataMap().put("readyJobQueueService", new ReadyJobQueueService(registryCenter));
        try {
            if (!scheduler.checkExists(jobDetail.getKey())) {
                Trigger trigger = TriggerBuilder.newTrigger().withIdentity(createIdentity("Trigger"))
                        .withSchedule(CronScheduleBuilder.cronSchedule(task.getCron()).withMisfireHandlingInstructionDoNothing()).build();
                scheduler.scheduleJob(jobDetail, trigger);
            }
            scheduler.start();
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }
    }
    
    private String createIdentity(final String identityAppendix) {
        return Joiner.on("_").join(task.getJobName(), identityAppendix);
    }
    
    /**
     * 停止调度作业.
     */
    public void shutdown() {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }
    }
}
