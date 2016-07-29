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

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.internal.executor.JobFacade;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJobConfiguration;
import com.dangdang.ddframe.job.lite.api.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.lite.internal.execution.ExecutionContextService;
import com.dangdang.ddframe.job.lite.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.lite.internal.failover.FailoverService;
import com.dangdang.ddframe.job.lite.internal.server.ServerService;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;

import java.util.Collection;
import java.util.List;

/**
 * 为调度器提供内部服务的门面类.
 * 
 * @author zhangliang
 */
public class LiteJobFacade implements JobFacade {
    
    private final ConfigurationService configService;
    
    private final ShardingService shardingService;
    
    private final ServerService serverService;
    
    private final ExecutionContextService executionContextService;
    
    private final ExecutionService executionService;
    
    private final FailoverService failoverService;
    
    private final List<ElasticJobListener> elasticJobListeners;
    
    public LiteJobFacade(final CoordinatorRegistryCenter regCenter, final LiteJobConfiguration liteJobConfig, final List<ElasticJobListener> elasticJobListeners) {
        configService = new ConfigurationService(regCenter, liteJobConfig);
        shardingService = new ShardingService(regCenter, liteJobConfig);
        serverService = new ServerService(regCenter, liteJobConfig);
        executionContextService = new ExecutionContextService(regCenter, liteJobConfig);
        executionService = new ExecutionService(regCenter, liteJobConfig);
        failoverService = new FailoverService(regCenter, liteJobConfig);
        this.elasticJobListeners = elasticJobListeners;
    }
    
    @Override
    public LiteJobConfiguration loadJobConfiguration() {
        return configService.load();
    }
    
    @Override
    public void checkMaxTimeDiffSecondsTolerable() {
        configService.checkMaxTimeDiffSecondsTolerable();
    }
    
    @Override
    public void failoverIfNecessary() {
        if (configService.isFailover() && !serverService.isJobPausedManually()) {
            failoverService.failoverIfNecessary();
        }
    }
    
    @Override
    public void registerJobBegin(final ShardingContext shardingContext) {
        executionService.registerJobBegin(shardingContext);
    }
    
    @Override
    public void registerJobCompleted(final ShardingContext shardingContext) {
        executionService.registerJobCompleted(shardingContext);
        if (configService.isFailover()) {
            failoverService.updateFailoverComplete(shardingContext.getShardingItems().keySet());
        }
    }
    
    @Override
    public ShardingContext getShardingContext() {
        boolean isFailover = configService.isFailover();
        if (isFailover) {
            List<Integer> failoverShardingItems = failoverService.getLocalHostFailoverItems();
            if (!failoverShardingItems.isEmpty()) {
                return executionContextService.getJobShardingContext(failoverShardingItems);
            }
        }
        shardingService.shardingIfNecessary();
        List<Integer> shardingItems = shardingService.getLocalHostShardingItems();
        if (isFailover) {
            shardingItems.removeAll(failoverService.getLocalHostTakeOffItems());
        }
        return executionContextService.getJobShardingContext(shardingItems);
    }
    
    @Override
    public boolean misfireIfNecessary(final Collection<Integer> shardingItems) {
        return executionService.misfireIfNecessary(shardingItems);
    }
    
    @Override
    public void clearMisfire(final Collection<Integer> shardingItems) {
        executionService.clearMisfire(shardingItems);
    }
    
    @Override
    public boolean isExecuteMisfired(final Collection<Integer> shardingItems) {
        return isEligibleForJobRunning() && configService.load().getJobTypeConfig().getCoreConfig().isMisfire() && !executionService.getMisfiredJobItems(shardingItems).isEmpty();
    }
    
    @Override
    public boolean isEligibleForJobRunning() {
        LiteJobConfiguration liteJobConfig = configService.load();
        if (liteJobConfig.getJobTypeConfig() instanceof DataflowJobConfiguration) {
            return !serverService.isJobPausedManually() && !shardingService.isNeedSharding() && ((DataflowJobConfiguration) liteJobConfig.getJobTypeConfig()).isStreamingProcess();    
        }
        return !serverService.isJobPausedManually() && !shardingService.isNeedSharding();
    }
    
    @Override
    public boolean isNeedSharding() {
        return shardingService.isNeedSharding();
    }
    
    @Override
    public void cleanPreviousExecutionInfo() {
        executionService.cleanPreviousExecutionInfo();
    }
    
    @Override
    public void beforeJobExecuted(final ShardingContext shardingContext) {
        for (ElasticJobListener each : elasticJobListeners) {
            each.beforeJobExecuted(shardingContext);
        }
    }
    
    @Override
    public void afterJobExecuted(final ShardingContext shardingContext) {
        for (ElasticJobListener each : elasticJobListeners) {
            each.afterJobExecuted(shardingContext);
        }
    }
}
