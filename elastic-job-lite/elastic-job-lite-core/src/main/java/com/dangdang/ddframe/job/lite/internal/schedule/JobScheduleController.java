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

package com.dangdang.ddframe.job.lite.internal.schedule;

import com.dangdang.ddframe.job.exception.JobSystemException;
import lombok.RequiredArgsConstructor;
import org.quartz.*;

import java.text.ParseException;

/**
 * 作业调度控制器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class JobScheduleController {

    private final Scheduler scheduler;

    private final JobDetail jobDetail;

    private final String triggerIdentity;

    /**
     * 调度作业.
     *
     * @param cron CRON表达式
     */
    public void scheduleJob(final String cron) {
        try {
            String jobName = jobDetail.getName();
            String jobGroup = jobDetail.getGroup();
            if (scheduler.getJobDetail(jobName, jobGroup) == null) {
                CronTrigger trigger = createTrigger(cron);
                scheduler.scheduleJob(jobDetail, trigger);
            }
            scheduler.start();
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
    }

    /**
     * 重新调度作业.
     *
     * @param cron CRON表达式
     */
    public synchronized void rescheduleJob(final String cron) {
        try {
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerIdentity, null);
            if (!scheduler.isShutdown()
                    && trigger != null
                    && !cron.equals(trigger.getCronExpression())) {
                CronTrigger newTrigger = createTrigger(cron);
                scheduler.rescheduleJob(triggerIdentity, null, newTrigger);
            }
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
    }

    private CronTrigger createTrigger(final String cron) {
        CronTrigger trigger = null;
        try {
            trigger = new CronTrigger(triggerIdentity, null, cron);
            // 设置 misfire 策略为 "不立即触发，等待下次调度"
            trigger.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
            return trigger;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断作业是否暂停.
     *
     * @return 作业是否暂停
     */
    public synchronized boolean isPaused() {
        try {
            if (scheduler.isShutdown()) {
                return false;
            }
            int state = scheduler.getTriggerState(triggerIdentity, null);
            return state == Trigger.STATE_PAUSED;
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
    }

    /**
     * 暂停作业.
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
     * 恢复作业.
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
     * 立刻启动作业.
     */
    public synchronized void triggerJob() {
        try {
            if (!scheduler.isShutdown()) {
                // scheduler.triggerJob(jobDetail.getKey());
                scheduler.triggerJob(jobDetail.getName(), jobDetail.getGroup());
            }
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
    }

    /**
     * 关闭调度器.
     */
    public synchronized void shutdown() {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
    }
}
