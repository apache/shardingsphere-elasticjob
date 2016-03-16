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

package com.dangdang.ddframe.job.internal.server;

import com.dangdang.ddframe.job.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.internal.sharding.ShardingService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.api.JobScheduler;
import com.dangdang.ddframe.job.internal.listener.AbstractJobListener;
import com.dangdang.ddframe.job.internal.listener.AbstractListenerManager;
import com.dangdang.ddframe.job.internal.schedule.JobRegistry;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;

/**
 * 作业控制监听管理器.
 * 
 * @author zhangliang
 */
public class JobOperationListenerManager extends AbstractListenerManager {
    
    private final String jobName;
    
    private final ServerNode serverNode;
    
    private final ServerService serverService;
    
    private final ShardingService shardingService;
    
    private final ExecutionService executionService;
    
    public JobOperationListenerManager(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration) {
        super(coordinatorRegistryCenter, jobConfiguration);
        jobName = jobConfiguration.getJobName();
        serverNode = new ServerNode(jobName);
        serverService = new ServerService(coordinatorRegistryCenter, jobConfiguration);
        shardingService = new ShardingService(coordinatorRegistryCenter, jobConfiguration);
        executionService = new ExecutionService(coordinatorRegistryCenter, jobConfiguration);
    }
    
    @Override
    public void start() {
        addConnectionStateListener(new ConnectionLostListener());
        addDataListener(new JobStoppedStatusJobListener());
    }
    
    class ConnectionLostListener implements ConnectionStateListener {
        
        @Override
        public void stateChanged(final CuratorFramework client, final ConnectionState newState) {
            JobScheduler jobScheduler = JobRegistry.getInstance().getJobScheduler(jobName);
            if (ConnectionState.LOST == newState) {
                jobScheduler.stopJob();
            } else if (ConnectionState.RECONNECTED == newState) {
                serverService.persistServerOnline();
                executionService.clearRunningInfo(shardingService.getLocalHostShardingItems());
                if (!serverService.isJobStoppedManually()) {
                    jobScheduler.resumeJob();
                }
            }
        }
    }
    
    class JobStoppedStatusJobListener extends AbstractJobListener {
        
        @Override
        protected void dataChanged(final CuratorFramework client, final TreeCacheEvent event, final String path) {
            if (!serverNode.isJobStoppedPath(path)) {
                return;
            }
            JobScheduler jobScheduler = JobRegistry.getInstance().getJobScheduler(jobName);
            if (null == jobScheduler) {
                return;
            }
            if (Type.NODE_ADDED == event.getType()) {
                jobScheduler.stopJob();
            }
            if (Type.NODE_REMOVED == event.getType()) {
                jobScheduler.resumeJob();
                serverService.clearJobStoppedStatus();
            }
        }
    }
}
