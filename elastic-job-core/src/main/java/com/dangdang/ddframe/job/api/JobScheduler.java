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

package com.dangdang.ddframe.job.api;

import com.dangdang.ddframe.job.api.listener.AbstractDistributeOnceElasticJobListener;
import com.dangdang.ddframe.job.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.exception.JobException;
import com.dangdang.ddframe.job.internal.guarantee.GuaranteeService;
import com.dangdang.ddframe.job.internal.schedule.JobFacade;
import com.dangdang.ddframe.job.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.internal.schedule.SchedulerFacade;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * 作业调度器.
 * 
 * @author zhangliang
 * @author caohao
 */
@Slf4j
public class JobScheduler {
    
    private static final String SCHEDULER_INSTANCE_NAME_SUFFIX = "Scheduler";
    
    private static final String CRON_TRIGGER_IDENTITY_SUFFIX = "Trigger";
    
    private final String jobName;
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final SchedulerFacade schedulerFacade;
    
    private final JobFacade jobFacade;
    
    private final JobDetail jobDetail;
    
    private Scheduler scheduler;
    
    public JobScheduler(final CoordinatorRegistryCenter regCenter, final JobConfiguration jobConfig, final ElasticJobListener... elasticJobListeners) {
        jobName = jobConfig.getJobName();
        this.regCenter = regCenter;
        List<ElasticJobListener> elasticJobListenerList = Arrays.asList(elasticJobListeners);
        setGuaranteeServiceForElasticJobListeners(regCenter, jobConfig, elasticJobListenerList);
        schedulerFacade = new SchedulerFacade(regCenter, jobConfig, elasticJobListenerList);
        jobFacade = new JobFacade(regCenter, jobConfig, elasticJobListenerList);
        jobDetail = JobBuilder.newJob(jobConfig.getJobClass()).withIdentity(jobName).build();
    }
    
    private void setGuaranteeServiceForElasticJobListeners(final CoordinatorRegistryCenter regCenter, final JobConfiguration jobConfig, final List<ElasticJobListener> elasticJobListeners) {
        GuaranteeService guaranteeService = new GuaranteeService(regCenter, jobConfig);
        for (ElasticJobListener each : elasticJobListeners) {
            if (each instanceof AbstractDistributeOnceElasticJobListener) {
                ((AbstractDistributeOnceElasticJobListener) each).setGuaranteeService(guaranteeService);
            }
        }
    }
    
    /**
     * 初始化作业.
     */
    public void init() {
        log.debug("Elastic job: job controller init, job name is: {}.", jobName);
        schedulerFacade.clearPreviousServerStatus();
        regCenter.addCacheData("/" + jobName);
        schedulerFacade.registerStartUpInfo();
        jobDetail.getJobDataMap().put("jobFacade", jobFacade);
        try {
            scheduler = initializeScheduler(jobDetail.getKey().toString());
            scheduleJob(createTrigger(schedulerFacade.getCron()));
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }
        JobRegistry.getInstance().addJobScheduler(jobName, this);
    }
    
    private Scheduler initializeScheduler(final String jobName) throws SchedulerException {
        StdSchedulerFactory factory = new StdSchedulerFactory();
        factory.initialize(getBaseQuartzProperties(jobName));
        Scheduler result = factory.getScheduler();
        result.getListenerManager().addTriggerListener(schedulerFacade.newJobTriggerListener());
        return result;
    }
    
    private Properties getBaseQuartzProperties(final String jobName) {
        Properties result = new Properties();
        result.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        result.put("org.quartz.threadPool.threadCount", "1");
        result.put("org.quartz.scheduler.instanceName", Joiner.on("_").join(jobName, SCHEDULER_INSTANCE_NAME_SUFFIX));
        if (!schedulerFacade.isMisfire()) {
            result.put("org.quartz.jobStore.misfireThreshold", "1");
        }
        prepareEnvironments(result);
        return result;
    }
    
    protected void prepareEnvironments(final Properties props) {
    }
    
    private CronTrigger createTrigger(final String cronExpression) {
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
        if (schedulerFacade.isMisfire()) {
            cronScheduleBuilder = cronScheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
        } else {
            cronScheduleBuilder = cronScheduleBuilder.withMisfireHandlingInstructionDoNothing();
        }
        return TriggerBuilder.newTrigger()
                .withIdentity(Joiner.on("_").join(jobName, CRON_TRIGGER_IDENTITY_SUFFIX))
                .withSchedule(cronScheduleBuilder).build();
    }
    
    private void scheduleJob(final CronTrigger trigger) throws SchedulerException {
        if (!scheduler.checkExists(jobDetail.getKey())) {
            scheduler.scheduleJob(jobDetail, trigger);
        }
        scheduler.start();
    }
    
    /**
     * 获取下次作业触发时间.
     * 
     * @return 下次作业触发时间
     */
    public Date getNextFireTime() {
        List<? extends Trigger> triggers;
        try {
            triggers = scheduler.getTriggersOfJob(jobDetail.getKey());
        } catch (final SchedulerException ex) {
            return null;
        }
        Date result = null;
        for (Trigger each : triggers) {
            Date nextFireTime = each.getNextFireTime();
            if (null == nextFireTime) {
                continue;
            }
            if (null == result) {
                result = nextFireTime;
            } else if (nextFireTime.getTime() < result.getTime()) {
                result = nextFireTime;
            }
        }
        return result;
    }
    
    /**
     * 停止作业.
     */
    public void stopJob() {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.pauseAll();
            }
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }
    }
    
    /**
     * 恢复作业.
     */
    public void resumeJob() {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.resumeAll();
            }
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }
    }
    
    /**
     * 立刻启动作业.
     */
    public void triggerJob() {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.triggerJob(jobDetail.getKey());
            }
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }
    }
    
    /**
     * 关闭调度器.
     */
    public void shutdown() {
        schedulerFacade.releaseJobResource();
        try {
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }
    }
    
    /**
     * 重新调度作业.
     */
    public void rescheduleJob(final String cronExpression) {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.rescheduleJob(TriggerKey.triggerKey(Joiner.on("_").join(jobName, CRON_TRIGGER_IDENTITY_SUFFIX)), createTrigger(cronExpression));
            }
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        } 
    }
    
    /**
     * 设置作业字段属性.
     * 
     * @param fieldName 字段名称
     * @param fieldValue 字段值
     */
    public void setField(final String fieldName, final Object fieldValue) {
        jobDetail.getJobDataMap().put(fieldName, fieldValue);
    }
}
