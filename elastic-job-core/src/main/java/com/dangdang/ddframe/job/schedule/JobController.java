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

package com.dangdang.ddframe.job.schedule;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.dangdang.ddframe.job.api.AbstractElasticJob;
import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.exception.JobException;
import com.dangdang.ddframe.job.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.internal.execution.ExecutionContextService;
import com.dangdang.ddframe.job.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.internal.failover.FailoverService;
import com.dangdang.ddframe.job.internal.listener.ListenerManager;
import com.dangdang.ddframe.job.internal.server.ServerService;
import com.dangdang.ddframe.job.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.internal.statistics.StatisticsService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;

/**
 * 作业控制器.
 * 
 * @author zhangliang, caohao
 */
public class JobController {
    
    private final JobConfiguration jobConfiguration;
    
    private final ListenerManager listenerManager;
    
    private final ConfigurationService configService;
    
    private final LeaderElectionService leaderElectionService;
    
    private final ServerService serverService;
    
    private final ShardingService shardingService;
    
    private final ExecutionContextService executionContextService;
    
    private final ExecutionService executionService;
    
    private final FailoverService failoverService;
    
    private final StatisticsService statisticsService;
    
    private Scheduler scheduler;
    
    private JobDetail jobDetail;
    
    private Trigger trigger;
    
    public JobController(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration) {
        this.jobConfiguration = jobConfiguration;
        listenerManager = new ListenerManager(coordinatorRegistryCenter, jobConfiguration);
        configService = new ConfigurationService(coordinatorRegistryCenter, jobConfiguration);
        leaderElectionService = new LeaderElectionService(coordinatorRegistryCenter, jobConfiguration);
        serverService = new ServerService(coordinatorRegistryCenter, jobConfiguration);
        shardingService = new ShardingService(coordinatorRegistryCenter, jobConfiguration);
        executionContextService = new ExecutionContextService(coordinatorRegistryCenter, jobConfiguration);
        executionService = new ExecutionService(coordinatorRegistryCenter, jobConfiguration);
        failoverService = new FailoverService(coordinatorRegistryCenter, jobConfiguration);
        statisticsService = new StatisticsService(coordinatorRegistryCenter, jobConfiguration);
    }
    
    /**
     * 初始化作业.
     */
    public void init() {
        registerElasticEnv();
        jobDetail = createJobDetail();
        trigger = createTrigger();
        try {
            scheduler = initializeScheduler(jobDetail.getKey().toString());
            scheduleJob();
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }
        JobRegistry.getInstance().addJob(jobConfiguration.getJobName(), this);
    }
    
    private void registerElasticEnv() {
        listenerManager.startAllListeners();
        leaderElectionService.leaderElection();
        configService.persistJobConfiguration();
        serverService.persistServerOnline();
        serverService.clearJobStopedStatus();
        statisticsService.startProcessCountJob();
        shardingService.setReshardingFlag();
    }
    
    private JobDetail createJobDetail() {
        JobDetail result = JobBuilder.newJob(jobConfiguration.getJobClass()).withIdentity(jobConfiguration.getJobName()).build();
        result.getJobDataMap().put("configService", configService);
        result.getJobDataMap().put("shardingService", shardingService);
        result.getJobDataMap().put("executionContextService", executionContextService);
        result.getJobDataMap().put("executionService", executionService);
        result.getJobDataMap().put("failoverService", failoverService);
        return result;
    }
    
    private Trigger createTrigger() {
        return TriggerBuilder
                .newTrigger()
                .withIdentity(jobConfiguration.getJobName() + "_Trigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(configService.getCron()).withMisfireHandlingInstructionFireAndProceed())
                .build();
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
        result.put("org.quartz.scheduler.instanceName", jobName + "_Scheduler");
        prepareEnvironments(result);
        return result;
    }
    
    protected void prepareEnvironments(final Properties props) {
    }
    
    private void scheduleJob() throws SchedulerException {
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
        Date result = null;
        List<? extends Trigger> triggers;
        try {
            triggers = scheduler.getTriggersOfJob(jobDetail.getKey());
        } catch (final SchedulerException ex) {
            return result;
        }
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
            for (JobExecutionContext each : scheduler.getCurrentlyExecutingJobs()) {
                if (each.getJobInstance() instanceof AbstractElasticJob) {
                    ((AbstractElasticJob) each.getJobInstance()).stop();
                }
            }
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
            scheduler.shutdown();
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }
    }
}
