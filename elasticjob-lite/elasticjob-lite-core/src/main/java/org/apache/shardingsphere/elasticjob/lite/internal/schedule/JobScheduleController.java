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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.infra.exception.JobSystemException;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

/**
 * Job schedule controller.
 */
@RequiredArgsConstructor
public final class JobScheduleController {
    
    private final Scheduler scheduler;
    
    private final JobDetail jobDetail;
    
    private final String triggerIdentity;
    
    /**
     * Schedule job.
     * 
     * @param cron CRON expression
     */
    public void scheduleJob(final String cron) {
        try {
            if (!scheduler.checkExists(jobDetail.getKey())) {
                scheduler.scheduleJob(jobDetail, createCronTrigger(cron));
            }
            scheduler.start();
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
    }
    
    /**
     * Reschedule job.
     * 
     * @param cron CRON expression
     */
    public synchronized void rescheduleJob(final String cron) {
        try {
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(TriggerKey.triggerKey(triggerIdentity));
            if (!scheduler.isShutdown() && null != trigger && !cron.equals(trigger.getCronExpression())) {
                scheduler.rescheduleJob(TriggerKey.triggerKey(triggerIdentity), createCronTrigger(cron));
            }
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
    }
    
    /**
     * Reschedule OneOff job.
     */
    public synchronized void rescheduleJob() {
        try {
            SimpleTrigger trigger = (SimpleTrigger) scheduler.getTrigger(TriggerKey.triggerKey(triggerIdentity));
            if (!scheduler.isShutdown() && null != trigger) {
                scheduler.rescheduleJob(TriggerKey.triggerKey(triggerIdentity), createOneOffTrigger());
            }
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
    }
    
    private Trigger createCronTrigger(final String cron) {
        return TriggerBuilder.newTrigger().withIdentity(triggerIdentity).withSchedule(CronScheduleBuilder.cronSchedule(cron).withMisfireHandlingInstructionDoNothing()).build();
    }
    
    /**
     * Judge job is pause or not.
     * 
     * @return job is pause or not
     */
    public synchronized boolean isPaused() {
        try {
            return !scheduler.isShutdown() && Trigger.TriggerState.PAUSED == scheduler.getTriggerState(new TriggerKey(triggerIdentity));
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
    }
    
    /**
     * Pause job.
     */
    public synchronized void pauseJob() {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.pauseAll();
            }
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
    }
    
    /**
     * Resume job.
     */
    public synchronized void resumeJob() {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.resumeAll();
            }
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
    }
    
    /**
     * Trigger job.
     */
    public synchronized void triggerJob() {
        try {
            if (scheduler.isShutdown()) {
                return;
            }
            if (!scheduler.checkExists(jobDetail.getKey())) {
                scheduler.scheduleJob(jobDetail, createOneOffTrigger());
            } else {
                scheduler.triggerJob(jobDetail.getKey());
            }
            if (!scheduler.isStarted()) {
                scheduler.start();
            }
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
    }
    
    private Trigger createOneOffTrigger() {
        return TriggerBuilder.newTrigger().withIdentity(triggerIdentity).withSchedule(SimpleScheduleBuilder.simpleSchedule()).build();
    }
    
    /**
     * Shutdown scheduler.
     */
    public synchronized void shutdown() {
        shutdown(false);
    }
    
    /**
     * Shutdown scheduler graceful.
     * @param isCleanShutdown if wait jobs complete
     */
    public synchronized void shutdown(final boolean isCleanShutdown) {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.shutdown(isCleanShutdown);
            }
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
    }
}
