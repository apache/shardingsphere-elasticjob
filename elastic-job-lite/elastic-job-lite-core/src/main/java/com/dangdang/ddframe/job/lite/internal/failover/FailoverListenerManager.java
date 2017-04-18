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

package com.dangdang.ddframe.job.lite.internal.failover;

import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationNode;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.lite.internal.config.LiteJobConfigurationGsonFactory;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractJobListener;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractListenerManager;
import com.dangdang.ddframe.job.lite.internal.sharding.ExecutionService;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingNode;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;

/**
 * 失效转移监听管理器.
 * 
 * @author zhangliang
 */
public final class FailoverListenerManager extends AbstractListenerManager {
    
    private final ConfigurationService configService;
    
    private final ExecutionService executionService;
    
    private final ShardingService shardingService;
    
    private final FailoverService failoverService;
    
    private final ConfigurationNode configNode;
    
    private final ShardingNode shardingNode;
    
    private final FailoverNode failoverNode;
    
    public FailoverListenerManager(final CoordinatorRegistryCenter regCenter, final String jobName) {
        super(regCenter, jobName);
        configService = new ConfigurationService(regCenter, jobName);
        executionService = new ExecutionService(regCenter, jobName);
        shardingService = new ShardingService(regCenter, jobName);
        failoverService = new FailoverService(regCenter, jobName);
        configNode = new ConfigurationNode(jobName);
        shardingNode = new ShardingNode(jobName);
        failoverNode = new FailoverNode(jobName);
    }
    
    @Override
    public void start() {
        addDataListener(new JobCrashedJobListener());
        addDataListener(new FailoverJobCrashedJobListener());
        addDataListener(new FailoverSettingsChangedJobListener());
    }
    
    private void failover(final Integer item, final Type eventType) {
        if (!isJobCrashAndNeedFailover(item, eventType)) {
            return;
        }
        failoverService.setCrashedFailoverFlag(item);
        if (!executionService.hasRunningItems(shardingService.getLocalShardingItems())) {
            failoverService.failoverIfNecessary();
        }
    }
    
    private boolean isJobCrashAndNeedFailover(final Integer item, final Type eventType) {
        LiteJobConfiguration jobConfig = configService.load(true);
        boolean isFailover = null != jobConfig && jobConfig.isFailover();
        return null != item && Type.NODE_REMOVED == eventType && !executionService.isCompleted(item) && isFailover;
    }
    
    class JobCrashedJobListener extends AbstractJobListener {
        
        @Override
        protected void dataChanged(final String path, final Type eventType, final String data) {
            failover(shardingNode.getItemByRunningItemPath(path), eventType);
        }
    }
    
    class FailoverJobCrashedJobListener extends AbstractJobListener {
        
        @Override
        protected void dataChanged(final String path, final Type eventType, final String data) {
            failover(failoverNode.getItemByExecutionFailoverPath(path), eventType);
        }
    }
    
    class FailoverSettingsChangedJobListener extends AbstractJobListener {
        
        @Override
        protected void dataChanged(final String path, final Type eventType, final String data) {
            if (configNode.isConfigPath(path) && Type.NODE_UPDATED == eventType && !LiteJobConfigurationGsonFactory.fromJson(data).isFailover()) {
                failoverService.removeFailoverInfo();
            }
        }
    }
}
