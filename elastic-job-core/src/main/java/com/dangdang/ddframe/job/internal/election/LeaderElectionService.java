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

import com.dangdang.ddframe.job.api.config.JobConfiguration;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.server.ServerService;
import com.dangdang.ddframe.job.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.internal.storage.LeaderExecutionCallback;
import com.dangdang.ddframe.job.internal.reg.BlockUtils;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 选举主节点的服务.
 * 
 * @author zhangliang
 */
@Slf4j
public class LeaderElectionService {
    
    private final LocalHostService localHostService = new LocalHostService();
    
    private final ServerService serverService;
    
    private final JobNodeStorage jobNodeStorage;
    
    public LeaderElectionService(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration) {
        jobNodeStorage = new JobNodeStorage(coordinatorRegistryCenter, jobConfiguration);
        serverService = new ServerService(coordinatorRegistryCenter, jobConfiguration);
    }
    
    /**
     * 强制选举主节点.
     */
    public void leaderForceElection() {
        jobNodeStorage.executeInLeader(ElectionNode.LATCH, new LeaderElectionExecutionCallback(true));
    }
    
    /**
     * 选举主节点.
     */
    public void leaderElection() {
        jobNodeStorage.executeInLeader(ElectionNode.LATCH, new LeaderElectionExecutionCallback(false));
    }
    
    /**
     * 判断当前节点是否是主节点.
     * 
     * <p>
     * 如果主节点正在选举中而导致取不到主节点, 则阻塞至主节点选举完成再返回.
     * </p>
     * 
     * @return 当前节点是否是主节点
     */
    public Boolean isLeader() {
        String localHostIp = localHostService.getIp();
        while (!hasLeader() && !serverService.getAvailableServers().isEmpty()) {
            log.info("Elastic job: leader node is electing, waiting for 100 ms at server '{}'", localHostIp);
            BlockUtils.waitingShortTime();
        }
        return localHostIp.equals(jobNodeStorage.getJobNodeData(ElectionNode.LEADER_HOST));
    }
    
    /**
     * 判断是否已经有主节点.
     * 
     * <p>
     * 仅为选举监听使用.
     * 程序中其他地方判断是否有主节点应使用{@code isLeader() }方法.
     * </p>
     * 
     * @return 是否已经有主节点
     */
    public boolean hasLeader() {
        return jobNodeStorage.isJobNodeExisted(ElectionNode.LEADER_HOST);
    }
    
    /**
     * 删除主节点供重新选举.
     */
    public void removeLeader() {
        jobNodeStorage.removeJobNodeIfExisted(ElectionNode.LEADER_HOST);
    }
    
    @RequiredArgsConstructor
    class LeaderElectionExecutionCallback implements LeaderExecutionCallback {
        
        private final boolean isForceElect;
    
        @Override
        public void execute() {
            if (!jobNodeStorage.isJobNodeExisted(ElectionNode.LEADER_HOST) && (isForceElect || serverService.isAvailableServer(localHostService.getIp()))) {
                jobNodeStorage.fillEphemeralJobNode(ElectionNode.LEADER_HOST, localHostService.getIp());
            }
        }
    }
}
