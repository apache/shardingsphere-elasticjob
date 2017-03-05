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

package com.dangdang.ddframe.job.lite.internal.server;

import com.dangdang.ddframe.job.lite.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractJobListener;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractListenerManager;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;

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
    
    public JobOperationListenerManager(final CoordinatorRegistryCenter regCenter, final String jobName) {
        super(regCenter, jobName);
        this.jobName = jobName;
        serverNode = new ServerNode(jobName);
        serverService = new ServerService(regCenter, jobName);
        shardingService = new ShardingService(regCenter, jobName);
        executionService = new ExecutionService(regCenter, jobName);
    }
    
    @Override
    public void start() {
        addConnectionStateListener(new ConnectionLostListener());
        addDataListener(new JobTriggerStatusJobListener());
        addDataListener(new JobPausedStatusJobListener());
        addDataListener(new JobShutdownStatusJobListener());
    }
    
    class ConnectionLostListener implements ConnectionStateListener {
        
        @Override
        public void stateChanged(final CuratorFramework client, final ConnectionState newState) {
            JobScheduleController jobScheduleController = JobRegistry.getInstance().getJobScheduleController(jobName);
            if (ConnectionState.LOST == newState) {
                jobScheduleController.pauseJob();
            } else if (ConnectionState.RECONNECTED == newState) {
                serverService.persistServerOnline(serverService.isLocalhostServerEnabled());
                executionService.clearRunningInfo(shardingService.getLocalHostShardingItems());
                if (!serverService.isJobPausedManually()) {
                    jobScheduleController.resumeJob();
                }
            }
        }
    }
    
    class JobTriggerStatusJobListener extends AbstractJobListener {
        
        @Override
        protected void dataChanged(final CuratorFramework client, final TreeCacheEvent event, final String path) {
            if (Type.NODE_ADDED != event.getType() || !serverNode.isLocalJobTriggerPath(path)) {
                return;
            }
            serverService.clearJobTriggerStatus();
            JobScheduleController jobScheduleController = JobRegistry.getInstance().getJobScheduleController(jobName);
            if (null == jobScheduleController) {
                return;
            }
            if (serverService.isLocalhostServerReady()) {
                jobScheduleController.triggerJob();
            }
        }
    }
    
    class JobPausedStatusJobListener extends AbstractJobListener {
        
        @Override
        protected void dataChanged(final CuratorFramework client, final TreeCacheEvent event, final String path) {
            if (!serverNode.isLocalJobPausedPath(path)) {
                return;
            }
            JobScheduleController jobScheduleController = JobRegistry.getInstance().getJobScheduleController(jobName);
            if (null == jobScheduleController) {
                return;
            }
            if (Type.NODE_ADDED == event.getType()) {
                jobScheduleController.pauseJob();
            }
            if (Type.NODE_REMOVED == event.getType()) {
                jobScheduleController.resumeJob();
                serverService.clearJobPausedStatus();
            }
        }
    }
    
    class JobShutdownStatusJobListener extends AbstractJobListener {
        
        @Override
        protected void dataChanged(final CuratorFramework client, final TreeCacheEvent event, final String path) {
            if (!serverNode.isLocalJobShutdownPath(path)) {
                return;
            }
            JobScheduleController jobScheduleController = JobRegistry.getInstance().getJobScheduleController(jobName);
            if (null != jobScheduleController && Type.NODE_ADDED == event.getType()) {
                jobScheduleController.shutdown();
                serverService.processServerShutdown();
            }
        }
    }
}
