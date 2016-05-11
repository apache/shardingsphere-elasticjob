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

package com.dangdang.ddframe.job.internal.election;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.internal.listener.AbstractJobListener;
import com.dangdang.ddframe.job.internal.listener.AbstractListenerManager;
import com.dangdang.ddframe.job.internal.server.ServerNode;
import com.dangdang.ddframe.job.internal.sharding.ShardingService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
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
    
    private final ShardingService shardingService;
    
    private final ElectionNode electionNode;
    
    private final ServerNode serverNode;
    
    public ElectionListenerManager(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration) {
        super(coordinatorRegistryCenter, jobConfiguration);
        leaderElectionService = new LeaderElectionService(coordinatorRegistryCenter, jobConfiguration);
        shardingService = new ShardingService(coordinatorRegistryCenter, jobConfiguration);
        electionNode = new ElectionNode(jobConfiguration.getJobName());
        serverNode = new ServerNode(jobConfiguration.getJobName());
    }
    
    @Override
    public void start() {
        addDataListener(new LeaderElectionJobListener());
    }
    
    class LeaderElectionJobListener extends AbstractJobListener {
        
        @Override
        protected void dataChanged(final CuratorFramework client, final TreeCacheEvent event, final String path) {
            EventHelper eventHelper = new EventHelper(path, event);
            if ((eventHelper.isLeaderCrashed() || eventHelper.isServerEnabled() || eventHelper.isServerResumed()) && !leaderElectionService.hasLeader()) {
                log.debug("Elastic job: leader crashed, elect a new leader now.");
                leaderElectionService.leaderElection();
                shardingService.setReshardingFlag();
                log.debug("Elastic job: leader election completed.");
                return;
            }
            if ((eventHelper.isServerDisabled() || eventHelper.isServerPaused() || eventHelper.isServerShutdown()) && leaderElectionService.isLeader()) {
                leaderElectionService.removeLeader();
            }
        }
        
        @RequiredArgsConstructor
        final class EventHelper {
            
            private final String path;
            
            private final TreeCacheEvent event;
    
            boolean isLeaderCrashed() {
                return electionNode.isLeaderHostPath(path) && Type.NODE_REMOVED == event.getType();
            }
    
            boolean isServerEnabled() {
                return serverNode.isLocalServerDisabledPath(path) && Type.NODE_REMOVED == event.getType();
            }
    
            boolean isServerResumed() {
                return serverNode.isLocalJobPausedPath(path) && Type.NODE_REMOVED == event.getType();
            }
    
            boolean isServerDisabled() {
                return serverNode.isLocalServerDisabledPath(path) && Type.NODE_ADDED == event.getType();
            }
    
            boolean isServerPaused() {
                return serverNode.isLocalJobPausedPath(path) && Type.NODE_ADDED == event.getType();
            }
    
            boolean isServerShutdown() {
                return serverNode.isLocalJobShutdownPath(path) && Type.NODE_ADDED == event.getType();
            }
        }
    }
}
