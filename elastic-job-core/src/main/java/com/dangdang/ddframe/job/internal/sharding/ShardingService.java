/**
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

package com.dangdang.ddframe.job.internal.sharding;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.env.RealLocalHostService;
import com.dangdang.ddframe.job.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.internal.server.ServerService;
import com.dangdang.ddframe.job.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.internal.util.BlockUtils;
import com.dangdang.ddframe.job.internal.util.ItemUtils;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;

import lombok.extern.slf4j.Slf4j;

/**
 * 作业分片服务.
 * 
 * @author zhangliang
 */
@Slf4j
public final class ShardingService {
    
    private final JobNodeStorage jobNodeStorage;
    
    private final LocalHostService localHostService = new RealLocalHostService();
    
    private final LeaderElectionService leaderElectionService;
    
    private final ConfigurationService configService;
    
    private final ServerService serverService;
    
    private final ExecutionService executionService;
    
    public ShardingService(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration) {
        jobNodeStorage = new JobNodeStorage(coordinatorRegistryCenter, jobConfiguration);
        leaderElectionService = new LeaderElectionService(coordinatorRegistryCenter, jobConfiguration);
        configService = new ConfigurationService(coordinatorRegistryCenter, jobConfiguration);
        serverService = new ServerService(coordinatorRegistryCenter, jobConfiguration);
        executionService = new ExecutionService(coordinatorRegistryCenter, jobConfiguration);
    }
    
    /**
     * 设置需要重新分片的标记.
     */
    public void setReshardingFlag() {
        jobNodeStorage.createJobNodeIfNeeded(ShardingNode.NECESSARY);
    }
    
    /**判断是否需要重分片.
     * 
     * @return 是否需要重分片
     */
    public boolean isNeedSharding() {
        return jobNodeStorage.isJobNodeExisted(ShardingNode.NECESSARY);
    }
    
    /**
     * 如果需要分片且当前节点为主节点, 则作业分片.
     */
    public void shardingIfNecessary() {
        if (!isNeedSharding()) {
            return;
        }
        if (!leaderElectionService.isLeader()) {
            blockUntilShardingCompleted();
            return;
        }
        if (configService.isMonitorExecution()) {
            waitingOtherJobCompleted();
        }
        log.debug("Elastic job: sharding begin.");
        jobNodeStorage.fillEphemeralJobNode(ShardingNode.PROCESSING, "");
        clearShardingInfo();
        JobShardingStrategy jobShardingStrategy = new AverageAllocationJobShardingStrategy();
        persistShardingInfo(jobShardingStrategy.sharding(serverService.getAvailableServers(), configService.getShardingTotalCount()));
        jobNodeStorage.removeJobNodeIfExisted(ShardingNode.NECESSARY);
        jobNodeStorage.removeJobNodeIfExisted(ShardingNode.PROCESSING);
        log.debug("Elastic job: sharding completed.");
    }
    
    private void clearShardingInfo() {
        for (String each : serverService.getAllServers()) {
            jobNodeStorage.removeJobNodeIfExisted(ShardingNode.getShardingNode(each));
        }
    }
    
    private void persistShardingInfo(final Map<String, List<Integer>> shardingItems) {
        for (Entry<String, List<Integer>> entry : shardingItems.entrySet()) {
            jobNodeStorage.replaceJobNode(ShardingNode.getShardingNode(entry.getKey()), ItemUtils.toItemsString(entry.getValue()));
        }
    }
    
    private void blockUntilShardingCompleted() {
        while (jobNodeStorage.isJobNodeExisted(ShardingNode.NECESSARY) || jobNodeStorage.isJobNodeExisted(ShardingNode.PROCESSING)) {
            log.debug("Sleep short time until sharding completed.");
            BlockUtils.waitingShortTime();
        }
    }
    
    private void waitingOtherJobCompleted() {
        while (executionService.hasRunningItems()) {
            log.debug("Sleep short time until other job completed.");
            BlockUtils.waitingShortTime();
        }
    }
    
    /**
     * 获取运行在本作业服务器的分片序列号.
     * 
     * @return 运行在本作业服务器的分片序列号
     */
    public List<Integer> getLocalHostShardingItems() {
        String ip = localHostService.getIp();
        if (!jobNodeStorage.isJobNodeExisted(ShardingNode.getShardingNode(ip))) {
            return Collections.<Integer>emptyList();
        }
        return ItemUtils.toItemList(jobNodeStorage.getJobNodeDataDirectly(ShardingNode.getShardingNode(ip)));
    }
}
