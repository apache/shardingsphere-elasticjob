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
import com.dangdang.ddframe.job.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.internal.execution.ExecutionContextService;
import com.dangdang.ddframe.job.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.internal.failover.FailoverService;
import com.dangdang.ddframe.job.internal.offset.OffsetService;
import com.dangdang.ddframe.job.internal.server.ServerService;
import com.dangdang.ddframe.job.internal.sharding.ShardingService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;

import java.util.List;

/**
 * 为调度器提供内部服务的门面类.
 * 
 * @author zhangliang
 */
public class JobFacade {
    
    private final ConfigurationService configService;
    
    private final ShardingService shardingService;
    
    private final ServerService serverService;
    
    private final ExecutionContextService executionContextService;
    
    private final ExecutionService executionService;
    
    private final FailoverService failoverService;
    
    private final OffsetService offsetService;
    
    private final List<ElasticJobListener> elasticJobListeners;
    
    public JobFacade(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration, final List<ElasticJobListener> elasticJobListeners) {
        configService = new ConfigurationService(coordinatorRegistryCenter, jobConfiguration);
        shardingService = new ShardingService(coordinatorRegistryCenter, jobConfiguration);
        serverService = new ServerService(coordinatorRegistryCenter, jobConfiguration);
        executionContextService = new ExecutionContextService(coordinatorRegistryCenter, jobConfiguration);
        executionService = new ExecutionService(coordinatorRegistryCenter, jobConfiguration);
        failoverService = new FailoverService(coordinatorRegistryCenter, jobConfiguration);
        offsetService = new OffsetService(coordinatorRegistryCenter, jobConfiguration);
        this.elasticJobListeners = elasticJobListeners;
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
     * 获取脚本型作业执行命令行.
     *
     * <p>
     * 仅脚本作业有效.
     * </p>
     *
     * @return 脚本型作业执行命令行
     */
    public String getScriptCommandLine() {
        return configService.getScriptCommandLine();
    }

    /**
     * 检查本机与注册中心的时间误差秒数是否在允许范围.
     */
    public void checkMaxTimeDiffSecondsTolerable() {
        configService.checkMaxTimeDiffSecondsTolerable();
    }
    
    /**
     * 如果需要失效转移, 则设置作业失效转移.
     */
    public void failoverIfNecessary() {
        if (configService.isFailover() && !serverService.isJobPausedManually()) {
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
        boolean isFailover = configService.isFailover();
        if (isFailover) {
            List<Integer> failoverItems = failoverService.getLocalHostFailoverItems();
            if (!failoverItems.isEmpty()) {
                return executionContextService.getJobExecutionShardingContext(failoverItems);
            }
        }
        shardingService.shardingIfNecessary();
        List<Integer> shardingItems = shardingService.getLocalHostShardingItems();
        if (isFailover) {
            shardingItems.removeAll(failoverService.getLocalHostTakeOffItems());
        }
        return executionContextService.getJobExecutionShardingContext(shardingItems);
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
     * @param shardingItems 任务分片项集合
     * @return 作业是否需要执行错过的任务
     */
    public boolean isExecuteMisfired(final List<Integer> shardingItems) {
        return isEligibleForJobRunning() && configService.isMisfire() && !executionService.getMisfiredJobItems(shardingItems).isEmpty();
    }
    
    /**
     * 判断作业是否符合继续运行的条件.
     * 
     * <p>如果作业停止或需要重分片则作业将不会继续运行.</p>
     * 
     * @return 作业是否符合继续运行的条件
     */
    public boolean isEligibleForJobRunning() {
        return !serverService.isJobPausedManually() && !shardingService.isNeedSharding();
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
     * 清理作业上次运行时信息.
     * 只会在主节点进行.
     */
    public void cleanPreviousExecutionInfo() {
        executionService.cleanPreviousExecutionInfo();
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
