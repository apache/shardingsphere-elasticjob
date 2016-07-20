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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;

import com.dangdang.ddframe.job.lite.api.config.JobConfiguration;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationNode;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.lite.internal.execution.ExecutionNode;
import com.dangdang.ddframe.job.lite.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractJobListener;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractListenerManager;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;

/**
 * 失效转移监听管理器.
 * 
 * @author zhangliang
 */
public class FailoverListenerManager extends AbstractListenerManager {
    
    private final ConfigurationService configService;
    
    private final ExecutionService executionService;
    
    private final ShardingService shardingService;
    
    private final FailoverService failoverService;
    
    private final ConfigurationNode configNode;
    
    private final ExecutionNode executionNode;
    
    private final FailoverNode failoverNode;
    
    public FailoverListenerManager(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration) {
        super(coordinatorRegistryCenter, jobConfiguration);
        configService = new ConfigurationService(coordinatorRegistryCenter, jobConfiguration);
        executionService = new ExecutionService(coordinatorRegistryCenter, jobConfiguration);
        shardingService = new ShardingService(coordinatorRegistryCenter, jobConfiguration);
        failoverService = new FailoverService(coordinatorRegistryCenter, jobConfiguration);
        configNode = new ConfigurationNode(jobConfiguration.getJobName());
        executionNode = new ExecutionNode(jobConfiguration.getJobName());
        failoverNode = new FailoverNode(jobConfiguration.getJobName());
    }
    
    @Override
    public void start() {
        addDataListener(new JobCrashedJobListener());
        addDataListener(new FailoverJobCrashedJobListener());
        addDataListener(new FailoverSettingsChangedJobListener());
    }
    
    private void failover(final Integer item, final TreeCacheEvent event) {
        if (!isJobCrashAndNeedFailover(item, event)) {
            return;
        }
        failoverService.setCrashedFailoverFlag(item);
        if (!executionService.hasRunningItems(shardingService.getLocalHostShardingItems())) {
            failoverService.failoverIfNecessary();
        }
    }
    
    private boolean isJobCrashAndNeedFailover(final Integer item, final TreeCacheEvent event) {
        return null != item && Type.NODE_REMOVED == event.getType() && !executionService.isCompleted(item) && configService.isFailover();
    }
    
    class JobCrashedJobListener extends AbstractJobListener {
        
        @Override
        protected void dataChanged(final CuratorFramework client, final TreeCacheEvent event, final String path) {
            failover(executionNode.getItemByRunningItemPath(path), event);
        }
    }
    
    class FailoverJobCrashedJobListener extends AbstractJobListener {
        
        @Override
        protected void dataChanged(final CuratorFramework client, final TreeCacheEvent event, final String path) {
            failover(failoverNode.getItemByExecutionFailoverPath(path), event);
        }
    }
    
    class FailoverSettingsChangedJobListener extends AbstractJobListener {
        
        @Override
        protected void dataChanged(final CuratorFramework client, final TreeCacheEvent event, final String path) {
            if (configNode.isFailoverPath(path) && Type.NODE_UPDATED == event.getType()) {
                if (!Boolean.valueOf(new String(event.getData().getData()))) {
                    failoverService.removeFailoverInfo();
                }
            }
        }
    }
}
