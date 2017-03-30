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

package com.dangdang.ddframe.job.lite.internal.election;

import com.dangdang.ddframe.job.lite.internal.listener.AbstractJobListener;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractListenerManager;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.server.ServerNode;
import com.dangdang.ddframe.job.lite.internal.server.ServerService;
import com.dangdang.ddframe.job.lite.internal.server.ServerStatus;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;

/**
 * 主节点选举监听管理器.
 * 
 * @author zhangliang
 */
public class ElectionListenerManager extends AbstractListenerManager {
    
    private final String jobName;
    
    private final ElectionNode electionNode;
    
    private final ServerNode serverNode;
    
    private final LeaderService leaderService;
    
    private final ServerService serverService;
    
    public ElectionListenerManager(final CoordinatorRegistryCenter regCenter, final String jobName) {
        super(regCenter, jobName);
        this.jobName = jobName;
        electionNode = new ElectionNode(jobName);
        serverNode = new ServerNode(jobName);
        leaderService = new LeaderService(regCenter, jobName);
        serverService = new ServerService(regCenter, jobName);
    }
    
    @Override
    public void start() {
        addDataListener(new LeaderElectionJobListener());
    }
    
    class LeaderElectionJobListener extends AbstractJobListener {
        
        @Override
        protected void dataChanged(final CuratorFramework client, final TreeCacheEvent event, final String path) {
            if (isLeaderCrashed(event, path) && serverService.isAvailableServer(JobRegistry.getInstance().getJobInstance(jobName).getIp())
                    || !leaderService.hasLeader() && isLocalServerEnabled(event, path)) {
                leaderService.electLeader();
            } else if (leaderService.isLeader() && isLocalServerDisabled(event, path)) {
                leaderService.removeLeader();
            }
        }
        
        private boolean isLeaderCrashed(final TreeCacheEvent event, final String path) {
            return electionNode.isLeaderInstancePath(path) && Type.NODE_REMOVED == event.getType();
        }
        
        private boolean isLocalServerEnabled(final TreeCacheEvent event, final String path) {
            return serverNode.isLocalServerPath(path) && !ServerStatus.DISABLED.name().equals(new String(event.getData().getData()));
        }
        
        private boolean isLocalServerDisabled(final TreeCacheEvent event, final String path) {
            return serverNode.isLocalServerPath(path) && ServerStatus.DISABLED.name().equals(new String(event.getData().getData()));
        }
    }
}
