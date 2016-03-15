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

package com.dangdang.ddframe.job.internal.schedule;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.api.listener.AbstractDistributeOnceElasticJobListener;
import com.dangdang.ddframe.job.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.internal.execution.ExecutionContextService;
import com.dangdang.ddframe.job.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.internal.failover.FailoverService;
import com.dangdang.ddframe.job.internal.guarantee.GuaranteeService;
import com.dangdang.ddframe.job.internal.listener.ListenerManager;
import com.dangdang.ddframe.job.internal.monitor.MonitorService;
import com.dangdang.ddframe.job.internal.offset.OffsetService;
import com.dangdang.ddframe.job.internal.server.ServerService;
import com.dangdang.ddframe.job.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.internal.statistics.StatisticsService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;

import java.util.List;

/**
 * 为调度器提供内部服务的门面类.
 * 
 * @author zhangliang
 */
public class SchedulerFacade {
    
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
    
    private final ListenerManager listenerManager;
    
    private final List<ElasticJobListener> elasticJobListeners;
    
    public SchedulerFacade(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration, final List<ElasticJobListener> elasticJobListeners) {
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
        this.elasticJobListeners = elasticJobListeners;
        setGuaranteeServiceForElasticJobListeners(coordinatorRegistryCenter, jobConfiguration);
        listenerManager = new ListenerManager(coordinatorRegistryCenter, jobConfiguration, this.elasticJobListeners);
    }
    
    private void setGuaranteeServiceForElasticJobListeners(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration) {
        GuaranteeService guaranteeService = new GuaranteeService(coordinatorRegistryCenter, jobConfiguration);
        for (ElasticJobListener each : elasticJobListeners) {
            if (each instanceof AbstractDistributeOnceElasticJobListener) {
                ((AbstractDistributeOnceElasticJobListener) each).setGuaranteeService(guaranteeService);
            }
        }
    }
    
    /**
     * 注册Elastic-Job启动信息.
     */
    public void registerStartUpInfo() {
        listenerManager.startAllListeners();
        leaderElectionService.leaderElection();
        configService.persistJobConfiguration();
        serverService.persistServerOnline();
        serverService.clearJobStoppedStatus();
        statisticsService.startProcessCountJob();
        shardingService.setReshardingFlag();
        monitorService.listen();
    }
    
    /**
     * 释放作业占用的资源.
     */
    public void releaseJobResource() {
        monitorService.close();
        statisticsService.stopProcessCountJob();
    }
    
    /**
     * 恢复因服务器崩溃而停止的作业信息.
     */
    public void resumeCrashedJobInfo() {
        serverService.persistServerOnline();
        executionService.clearRunningInfo(shardingService.getLocalHostShardingItems());
    }
    
    /**
     * 清除停止作业的标记.
     */
    public void clearJobStoppedStatus() {
        serverService.clearJobStoppedStatus();
    }
    
    /**
     * 判断是否是手工停止的作业.
     *
     * @return 是否是手工停止的作业
     */
    public boolean isJobStoppedManually() {
        return serverService.isJobStoppedManually();
    }

    /**
     * 获取作业名称.
     *
     * @return 作业名称
     */
    public String getJobName() {
        return configService.getJobName();
    }
    
    /**
     * 获取作业启动时间的cron表达式.
     *
     * @return 作业启动时间的cron表达式
     */
    public String getCron() {
        return configService.getCron();
    }
    
    /**
     * 获取是否开启misfire.
     *
     * @return 是否开启misfire
     */
    public boolean isMisfire() {
        return configService.isMisfire();
    }

    /**
     * 获取同时处理数据的并发线程数.
     *
     * <p>
     * 不能小于1.
     * 仅ThroughputDataFlow作业有效.
     * </p>
     *
     * @return 同时处理数据的并发线程数
     */
    public int getConcurrentDataProcessThreadCount() {
        return configService.getConcurrentDataProcessThreadCount();
    }
    
    /**
     * 检查本机与注册中心的时间误差秒数是否在允许范围.
     */
    public void checkMaxTimeDiffSecondsTolerable() {
        configService.checkMaxTimeDiffSecondsTolerable();
    }
    
    /**
     * 获取作业触发监听器.
     * 
     * @return 作业触发监听器
     */
    public JobTriggerListener newJobTriggerListener() {
        return new JobTriggerListener(executionService, shardingService);
    }
    
    /**
     * 如果需要失效转移, 则设置作业失效转移.
     * 
     * @param stopped 作业是否需要停止
     */
    public void failoverIfNecessary(final boolean stopped) {
        if (configService.isFailover() && !stopped) {
            failoverService.failoverIfNecessary();
        }
    }
    
    /**
     * 注册作业启动信息.
     *
     * @param shardingContext 作业运行时分片上下文
     */
    public void registerJobBegin(final JobExecutionMultipleShardingContext shardingContext) {
        executionService.registerJobBegin(shardingContext);
    }
    
    /**
     * 注册作业完成信息.
     *
     * @param shardingContext 作业运行时分片上下文
     */
    public void registerJobCompleted(final JobExecutionMultipleShardingContext shardingContext) {
        executionService.registerJobCompleted(shardingContext);
        if (configService.isFailover()) {
            failoverService.updateFailoverComplete(shardingContext.getShardingItems());
        }
    }
    
    /**
     * 获取当前作业服务器运行时分片上下文.
     *
     * @return 当前作业服务器运行时分片上下文
     */
    public JobExecutionMultipleShardingContext getShardingContext() {
        shardingService.shardingIfNecessary();
        return executionContextService.getJobExecutionShardingContext();
    }
    
    /**
     * 设置任务被错过执行的标记.
     *
     * @param shardingItems 需要设置错过执行的任务分片项
     * @return 是否满足misfire条件
     */
    public boolean misfireIfNecessary(final List<Integer> shardingItems) {
        return executionService.misfireIfNecessary(shardingItems);
    }
    
    /**
     * 清除任务被错过执行的标记.
     *
     * @param shardingItems 需要清除错过执行的任务分片项
     */
    public void clearMisfire(final List<Integer> shardingItems) {
        executionService.clearMisfire(shardingItems);
    }
    
    /**
     * 判断作业是否需要执行错过的任务.
     * 
     * @param stopped 作业是否需要停止
     * @param shardingItems 任务分片项集合
     * @return 作业是否需要执行错过的任务
     */
    public boolean isExecuteMisfired(final boolean stopped, final List<Integer> shardingItems) {
        return isEligibleForJobRunning(stopped) && configService.isMisfire() && !executionService.getMisfiredJobItems(shardingItems).isEmpty();
    }
    
    /**
     * 判断作业是否符合继续运行的条件.
     * 
     * <p>如果作业停止或需要重分片则作业将不会继续运行.</p>
     * 
     * @param stopped 作业是否需要停止
     * @return 作业是否符合继续运行的条件
     */
    public boolean isEligibleForJobRunning(final boolean stopped) {
        return !stopped && !shardingService.isNeedSharding();
    }
    
    /**判断是否需要重分片.
     *
     * @return 是否需要重分片
     */
    public boolean isNeedSharding() {
        return shardingService.isNeedSharding();
    }
    
    /**
     * 更新数据处理位置.
     *
     * @param item 分片项
     * @param offset 数据处理位置
     */
    public void updateOffset(final int item, final String offset) {
        offsetService.updateOffset(item, offset);
    }

    /**
     * 作业执行前的执行的方法.
     *
     * @param shardingContext 分片上下文
     */
    public void beforeJobExecuted(final JobExecutionMultipleShardingContext shardingContext) {
        for (ElasticJobListener each : elasticJobListeners) {
            each.beforeJobExecuted(shardingContext);
        }
    }
    
    /**
     * 作业执行后的执行的方法.
     *
     * @param shardingContext 分片上下文
     */
    public void afterJobExecuted(final JobExecutionMultipleShardingContext shardingContext) {
        for (ElasticJobListener each : elasticJobListeners) {
            each.afterJobExecuted(shardingContext);
        }
    }
}
