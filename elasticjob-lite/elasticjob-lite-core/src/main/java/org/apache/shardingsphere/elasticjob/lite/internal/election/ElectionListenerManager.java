/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.lite.internal.election;

import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.shardingsphere.elasticjob.lite.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.listener.AbstractJobListener;
import org.apache.shardingsphere.elasticjob.lite.internal.listener.AbstractListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerNode;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerService;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerStatus;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;

import java.util.Objects;

/**
 * Election listener manager.
 */
public final class ElectionListenerManager extends AbstractListenerManager {
    
    private final String jobName;
    
    private final LeaderNode leaderNode;
    
    private final ServerNode serverNode;
    
    private final LeaderService leaderService;
    
    private final ServerService serverService;
    
    public ElectionListenerManager(final CoordinatorRegistryCenter regCenter, final String jobName) {
        super(regCenter, jobName);
        this.jobName = jobName;
        leaderNode = new LeaderNode(jobName);
        serverNode = new ServerNode(jobName);
        leaderService = new LeaderService(regCenter, jobName);
        serverService = new ServerService(regCenter, jobName);
    }
    
    @Override
    public void start() {
        addDataListener(new LeaderElectionJobListener());
        addDataListener(new LeaderAbdicationJobListener());
    }
    
    class LeaderElectionJobListener extends AbstractJobListener {
        
        @Override
        protected void dataChanged(final String path, final Type eventType, final String data) {
            if (!JobRegistry.getInstance().isShutdown(jobName) && (isActiveElection(path, data) || isPassiveElection(path, eventType))) {
                leaderService.electLeader();
            }
        }
        
        private boolean isActiveElection(final String path, final String data) {
            return !leaderService.hasLeader() && isLocalServerEnabled(path, data);
        }
        
        private boolean isPassiveElection(final String path, final Type eventType) {
            JobInstance jobInstance = JobRegistry.getInstance().getJobInstance(jobName);
            if (Objects.isNull(jobInstance)) {
                return false;
            }
            return isLeaderCrashed(path, eventType) && serverService.isAvailableServer(jobInstance.getIp());
        }
        
        private boolean isLeaderCrashed(final String path, final Type eventType) {
            return leaderNode.isLeaderInstancePath(path) && Type.NODE_REMOVED == eventType;
        }
        
        private boolean isLocalServerEnabled(final String path, final String data) {
            return serverNode.isLocalServerPath(path) && !ServerStatus.DISABLED.name().equals(data);
        }
    }
    
    class LeaderAbdicationJobListener extends AbstractJobListener {
        
        @Override
        protected void dataChanged(final String path, final Type eventType, final String data) {
            if (leaderService.isLeader() && isLocalServerDisabled(path, data)) {
                leaderService.removeLeader();
            }
        }
        
        private boolean isLocalServerDisabled(final String path, final String data) {
            return serverNode.isLocalServerPath(path) && ServerStatus.DISABLED.name().equals(data);
        }
    }
}
