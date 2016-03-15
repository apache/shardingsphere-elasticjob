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

package com.dangdang.ddframe.job.internal.sharding;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.internal.server.ServerService;
import com.dangdang.ddframe.job.internal.sharding.strategy.JobShardingStrategy;
import com.dangdang.ddframe.job.internal.sharding.strategy.JobShardingStrategyFactory;
import com.dangdang.ddframe.job.internal.sharding.strategy.JobShardingStrategyOption;
import com.dangdang.ddframe.job.internal.storage.JobNodePath;
import com.dangdang.ddframe.job.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.internal.storage.TransactionExecutionCallback;
import com.dangdang.ddframe.job.internal.util.BlockUtils;
import com.dangdang.ddframe.job.internal.util.ItemUtils;
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
        jobNodeStorage.executeInTransaction(new ClearShardingInfoInfoTransactionExecutionCallback());
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
    
    class ClearShardingInfoInfoTransactionExecutionCallback implements TransactionExecutionCallback {
        
        @Override
        public void execute(final CuratorTransactionFinal curatorTransactionFinal) throws Exception {
            for (String each : serverService.getAllServers()) {
                String shardingNode = jobNodePath.getFullPath(ShardingNode.getShardingNode(each));
                curatorTransactionFinal.check().forPath(shardingNode).and().delete().forPath(shardingNode).and();
            }
        }
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
