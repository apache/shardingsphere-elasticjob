/**
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
import com.dangdang.ddframe.job.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.internal.execution.ExecutionContextService;
import com.dangdang.ddframe.job.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.internal.failover.FailoverService;
import com.dangdang.ddframe.job.internal.guarantee.GuaranteeService;
import com.dangdang.ddframe.job.internal.listener.ListenerManager;
import com.dangdang.ddframe.job.internal.monitor.MonitorService;
import com.dangdang.ddframe.job.internal.offset.OffsetService;
import com.dangdang.ddframe.job.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.internal.schedule.JobTriggerListener;
import com.dangdang.ddframe.job.internal.server.ServerService;
import com.dangdang.ddframe.job.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.internal.statistics.StatisticsService;
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
    
    private static final String CRON_TRIGGER_INDENTITY_SUFFIX = "Trigger";
    
    private final JobConfiguration jobConfiguration;
    
    private final CoordinatorRegistryCenter coordinatorRegistryCenter;
    
    private final ListenerManager listenerManager;
    
    private final ConfigurationService configService;
    
    private final LeaderElectionService leaderElectionService;
    
    private final ServerService serverService;
    
    private final ShardingService shardingService;
    
    private final ExecutionContextService executionContextService;
    
    private final ExecutionService executionService;
    
    private final FailoverService failoverService;
    
    private final StatisticsService statisticsService;
    
    private final OffsetService offsetService;
    
    private final MonitorService monitorService;

    private List<ElasticJobListener> elasticJobListeners;
    
    private Scheduler scheduler;
    
    private JobDetail jobDetail;
    
    public JobScheduler(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration, final ElasticJobListener... elasticJobListeners) {
        this.jobConfiguration = jobConfiguration;
        this.coordinatorRegistryCenter = coordinatorRegistryCenter;
        this.elasticJobListeners = Arrays.asList(elasticJobListeners);
        setGuaranteeServiceForElasticJobListeners();
        listenerManager = new ListenerManager(coordinatorRegistryCenter, jobConfiguration, this.elasticJobListeners);
        configService = new ConfigurationService(coordinatorRegistryCenter, jobConfiguration);
        leaderElectionService = new LeaderElectionService(coordinatorRegistryCenter, jobConfiguration);
        serverService = new ServerService(coordinatorRegistryCenter, jobConfiguration);
        shardingService = new ShardingService(coordinatorRegistryCenter, jobConfiguration);
        executionContextService = new ExecutionContextService(coordinatorRegistryCenter, jobConfiguration);
        executionService = new ExecutionService(coordinatorRegistryCenter, jobConfiguration);
        failoverService = new FailoverService(coordinatorRegistryCenter, jobConfiguration);
        statisticsService = new StatisticsService(coordinatorRegistryCenter, jobConfiguration);
        offsetService = new OffsetService(coordinatorRegistryCenter, jobConfiguration);
        monitorService = new MonitorService(coordinatorRegistryCenter, jobConfiguration);
        jobDetail = JobBuilder.newJob(jobConfiguration.getJobClass()).withIdentity(jobConfiguration.getJobName()).build();
    }
    
    private void setGuaranteeServiceForElasticJobListeners() {
        GuaranteeService guaranteeService = new GuaranteeService(coordinatorRegistryCenter, jobConfiguration);
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
        log.debug("Elastic job: job controller init, job name is: {}.", jobConfiguration.getJobName());
        coordinatorRegistryCenter.addCacheData("/" + jobConfiguration.getJobName());
        registerElasticEnv();
        fillJobDetail();
        try {
            scheduler = initializeScheduler(jobDetail.getKey().toString());
            scheduleJob(createTrigger(configService.getCron()));
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }
        JobRegistry.getInstance().addJobScheduler(jobConfiguration.getJobName(), this);
    }
    
    private void registerElasticEnv() {
        listenerManager.startAllListeners();
        leaderElectionService.leaderElection();
        configService.persistJobConfiguration();
        serverService.persistServerOnline();
        serverService.clearJobStopedStatus();
        statisticsService.startProcessCountJob();
        shardingService.setReshardingFlag();
        monitorService.listen();
    }
    
    private void fillJobDetail() {
        jobDetail.getJobDataMap().put("configService", configService);
        jobDetail.getJobDataMap().put("shardingService", shardingService);
        jobDetail.getJobDataMap().put("executionContextService", executionContextService);
        jobDetail.getJobDataMap().put("executionService", executionService);
        jobDetail.getJobDataMap().put("failoverService", failoverService);
        jobDetail.getJobDataMap().put("offsetService", offsetService);
        jobDetail.getJobDataMap().put("elasticJobListeners", elasticJobListeners);
    }
    
    private Scheduler initializeScheduler(final String jobName) throws SchedulerException {
        StdSchedulerFactory factory = new StdSchedulerFactory();
        factory.initialize(getBaseQuartzProperties(jobName));
        Scheduler result = factory.getScheduler();
        result.getListenerManager().addTriggerListener(new JobTriggerListener(executionService, shardingService));
        return result;
    }
    
    private Properties getBaseQuartzProperties(final String jobName) {
        Properties result = new Properties();
        result.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        result.put("org.quartz.threadPool.threadCount", "1");
        result.put("org.quartz.scheduler.instanceName", Joiner.on("_").join(jobName, SCHEDULER_INSTANCE_NAME_SUFFIX));
        if (!configService.isMisfire()) {
            result.put("org.quartz.jobStore.misfireThreshold", "1");
        }
        prepareEnvironments(result);
        return result;
    }
    
    protected void prepareEnvironments(final Properties props) {
    }
    
    private CronTrigger createTrigger(final String cronExpression) {
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
        if (configService.isMisfire()) {
            cronScheduleBuilder = cronScheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
        } else {
            cronScheduleBuilder = cronScheduleBuilder.withMisfireHandlingInstructionDoNothing();
        }
        return TriggerBuilder.newTrigger()
                .withIdentity(Joiner.on("_").join(jobConfiguration.getJobName(), CRON_TRIGGER_INDENTITY_SUFFIX))
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
            JobRegistry.getInstance().getJobInstance(jobConfiguration.getJobName()).stop();
            scheduler.pauseAll();
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }
    }
    
    /**
     * 恢复手工停止的作业.
     */
    public void resumeManualStopedJob() {
        try {
            if (scheduler.isShutdown()) {
                return;
            }
            JobRegistry.getInstance().getJobInstance(jobConfiguration.getJobName()).resume();
            scheduler.resumeAll();
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }
        serverService.clearJobStopedStatus();
    }
    
    /**
     * 恢复因服务器崩溃而停止的作业.
     * 
     * <p>
     * 不会恢复手工设置停止运行的作业.
     * </p>
     */
    public void resumeCrashedJob() {
        serverService.persistServerOnline();
        executionService.clearRunningInfo(shardingService.getLocalHostShardingItems());
        if (serverService.isJobStopedManually()) {
            return;
        }
        JobRegistry.getInstance().getJobInstance(jobConfiguration.getJobName()).resume();
        try {
            scheduler.resumeAll();
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }
    }
    
    /**
     * 立刻启动作业.
     */
    public void triggerJob() {
        try {
            scheduler.triggerJob(jobDetail.getKey());
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }
    }
    
    /**
     * 关闭调度器.
     */
    public void shutdown() {
        try {
            monitorService.close();
            statisticsService.stopProcessCountJob();
            scheduler.shutdown();
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }
    }
    
    /**
     * 重新调度作业.
     */
    public void rescheduleJob(final String cronExpression) {
        try {
            scheduler.rescheduleJob(TriggerKey.triggerKey(Joiner.on("_").join(jobConfiguration.getJobName(), CRON_TRIGGER_INDENTITY_SUFFIX)), createTrigger(cronExpression));
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
