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

package com.dangdang.ddframe.job.cloud.internal.sharding;

import com.dangdang.ddframe.job.cloud.api.config.JobConfiguration;
import com.dangdang.ddframe.job.cloud.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.cloud.internal.env.LocalHostService;
import com.dangdang.ddframe.job.cloud.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.cloud.internal.reg.BlockUtils;
import com.dangdang.ddframe.job.cloud.internal.reg.ItemUtils;
import com.dangdang.ddframe.job.cloud.internal.server.ServerService;
import com.dangdang.ddframe.job.cloud.internal.sharding.strategy.JobShardingStrategy;
import com.dangdang.ddframe.job.cloud.internal.sharding.strategy.JobShardingStrategyFactory;
import com.dangdang.ddframe.job.cloud.internal.sharding.strategy.JobShardingStrategyOption;
import com.dangdang.ddframe.job.cloud.internal.storage.JobNodePath;
import com.dangdang.ddframe.job.cloud.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.cloud.internal.storage.TransactionExecutionCallback;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 作业分片服务.
 * 
 * @author zhangliang
 */
@Slf4j
public class ShardingService {
    
    private final String jobName;
    
    private final JobNodeStorage jobNodeStorage;
    
    private final LocalHostService localHostService = new LocalHostService();
    
    private final LeaderElectionService leaderElectionService;
    
    private final ConfigurationService configService;
    
    private final ServerService serverService;
    
    private final ExecutionService executionService;

    private final JobNodePath jobNodePath;
    
    public ShardingService(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration) {
        jobName = jobConfiguration.getJobName();
        jobNodeStorage = new JobNodeStorage(coordinatorRegistryCenter, jobConfiguration);
        leaderElectionService = new LeaderElectionService(coordinatorRegistryCenter, jobConfiguration);
        configService = new ConfigurationService(coordinatorRegistryCenter, jobConfiguration);
        serverService = new ServerService(coordinatorRegistryCenter, jobConfiguration);
        executionService = new ExecutionService(coordinatorRegistryCenter, jobConfiguration);
        jobNodePath = new JobNodePath(jobConfiguration.getJobName());
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
        JobShardingStrategy jobShardingStrategy = JobShardingStrategyFactory.getStrategy(configService.getJobShardingStrategyClass());
        JobShardingStrategyOption option = new JobShardingStrategyOption(jobName, configService.getShardingTotalCount(), configService.getShardingItemParameters());
        jobNodeStorage.executeInTransaction(new PersistShardingInfoTransactionExecutionCallback(jobShardingStrategy.sharding(serverService.getAvailableServers(), option)));
        log.debug("Elastic job: sharding completed.");
    }
    
    private void blockUntilShardingCompleted() {
        while (!leaderElectionService.isLeader() && (jobNodeStorage.isJobNodeExisted(ShardingNode.NECESSARY) || jobNodeStorage.isJobNodeExisted(ShardingNode.PROCESSING))) {
            log.debug("Elastic job: sleep short time until sharding completed.");
            BlockUtils.waitingShortTime();
        }
    }
    
    private void waitingOtherJobCompleted() {
        while (executionService.hasRunningItems()) {
            log.debug("Elastic job: sleep short time until other job completed.");
            BlockUtils.waitingShortTime();
        }
    }
    
    private void clearShardingInfo() {
        for (String each : serverService.getAllServers()) {
            jobNodeStorage.removeJobNodeIfExisted(ShardingNode.getShardingNode(each));
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
            return Collections.emptyList();
        }
        return ItemUtils.toItemList(jobNodeStorage.getJobNodeDataDirectly(ShardingNode.getShardingNode(ip)));
    }
    
    @RequiredArgsConstructor
    class PersistShardingInfoTransactionExecutionCallback implements TransactionExecutionCallback {
        
        private final Map<String, List<Integer>> shardingItems;
        
        @Override
        public void execute(final CuratorTransactionFinal curatorTransactionFinal) throws Exception {
            for (Entry<String, List<Integer>> entry : shardingItems.entrySet()) {
                curatorTransactionFinal.create().forPath(jobNodePath.getFullPath(ShardingNode.getShardingNode(entry.getKey())), ItemUtils.toItemsString(entry.getValue()).getBytes()).and();
            }
            curatorTransactionFinal.delete().forPath(jobNodePath.getFullPath(ShardingNode.NECESSARY)).and();
            curatorTransactionFinal.delete().forPath(jobNodePath.getFullPath(ShardingNode.PROCESSING)).and();
        }
    }
}
