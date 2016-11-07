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
import com.dangdang.ddframe.job.lite.internal.server.ServerNode;
import com.dangdang.ddframe.job.lite.internal.server.ServerService;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;

/**
 * 主节点选举监听管理器.
 * 
 * @author zhangliang
 */
@Slf4j
public class ElectionListenerManager extends AbstractListenerManager {
    
    private final LeaderElectionService leaderElectionService;
    
    private final ServerService serverService;
    
    private final ElectionNode electionNode;
    
    private final ServerNode serverNode;
    
    
    public ElectionListenerManager(final CoordinatorRegistryCenter regCenter, final String jobName) {
        super(regCenter, jobName);
        leaderElectionService = new LeaderElectionService(regCenter, jobName);
        serverService = new ServerService(regCenter, jobName);
        electionNode = new ElectionNode(jobName);
        serverNode = new ServerNode(jobName);
    }
    
    @Override
    public void start() {
        addDataListener(new LeaderElectionJobListener());
    }
    
    class LeaderElectionJobListener extends AbstractJobListener {
        
        @Override
        protected void dataChanged(final CuratorFramework client, final TreeCacheEvent event, final String path) {
            EventHelper eventHelper = new EventHelper(path, event);
            if (eventHelper.isLeaderCrashedOrServerOn() && !leaderElectionService.hasLeader() && !serverService.getAvailableServers().isEmpty()) {
                log.debug("Leader crashed, elect a new leader now.");
                leaderElectionService.leaderElection();
                log.debug("Leader election completed.");
                return;
            }
            if (eventHelper.isServerOff() && leaderElectionService.isLeader()) {
                leaderElectionService.removeLeader();
            }
        }
        
        @RequiredArgsConstructor
        final class EventHelper {
            
            private final String path;
            
            private final TreeCacheEvent event;
            
            boolean isLeaderCrashedOrServerOn() {
                return isLeaderCrashed() || isServerEnabled() || isServerResumed();
            }
            
            private boolean isLeaderCrashed() {
                return electionNode.isLeaderHostPath(path) && Type.NODE_REMOVED == event.getType();
            }
            
            private boolean isServerEnabled() {
                return serverNode.isLocalServerDisabledPath(path) && Type.NODE_REMOVED == event.getType();
            }
            
            private boolean isServerResumed() {
                return serverNode.isLocalJobPausedPath(path) && Type.NODE_REMOVED == event.getType();
            }
            
            boolean isServerOff() {
                return isServerDisabled() || isServerPaused() || isServerShutdown();
            }
            
            private boolean isServerDisabled() {
                return serverNode.isLocalServerDisabledPath(path) && Type.NODE_ADDED == event.getType();
            }
            
            private boolean isServerPaused() {
                return serverNode.isLocalJobPausedPath(path) && Type.NODE_ADDED == event.getType();
            }
            
            private boolean isServerShutdown() {
                return serverNode.isLocalJobShutdownPath(path) && Type.NODE_ADDED == event.getType();
            }
        }
    }
}
