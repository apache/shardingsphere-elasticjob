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

package com.dangdang.ddframe.job.lite.internal.sharding;

import com.dangdang.ddframe.job.lite.api.strategy.JobShardingStrategy;
import com.dangdang.ddframe.job.lite.api.strategy.JobShardingStrategyFactory;
import com.dangdang.ddframe.job.lite.api.strategy.JobShardingStrategyOption;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.lite.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.lite.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.lite.internal.server.ServerService;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodePath;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.lite.internal.storage.TransactionExecutionCallback;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.util.concurrent.BlockUtils;
import com.dangdang.ddframe.job.util.config.ShardingItems;
import com.dangdang.ddframe.job.util.env.LocalHostService;
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
    
    public ShardingService(final CoordinatorRegistryCenter regCenter, final String jobName) {
        this.jobName = jobName;
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        leaderElectionService = new LeaderElectionService(regCenter, jobName);
        configService = new ConfigurationService(regCenter, jobName);
        serverService = new ServerService(regCenter, jobName);
        executionService = new ExecutionService(regCenter, jobName);
        jobNodePath = new JobNodePath(jobName);
    }
    
    /**
     * 设置需要重新分片的标记.
     */
    public void setReshardingFlag() {
        jobNodeStorage.createJobNodeIfNeeded(ShardingNode.NECESSARY);
    }
    
    /**
     * 判断是否需要重分片.
     * 
     * @return 是否需要重分片
     */
    public boolean isNeedSharding() {
        return jobNodeStorage.isJobNodeExisted(ShardingNode.NECESSARY);
    }
    
    /**
     * 如果需要分片且当前节点为主节点, 则作业分片.
     * 如果当前无可用节点则不分片.
     */
    public void shardingIfNecessary() {
        List<String> availableShardingServers = serverService.getAvailableShardingServers();
        if (availableShardingServers.isEmpty()) {
            clearShardingInfo();
            return;
        }
        if (!isNeedSharding()) {
            return;
        }
        if (!leaderElectionService.isLeader()) {
            blockUntilShardingCompleted();
            return;
        }
        LiteJobConfiguration liteJobConfig = configService.load(false);
        if (liteJobConfig.isMonitorExecution()) {
            waitingOtherJobCompleted();
        }
        log.debug("Job '{}' sharding begin.", jobName);
        jobNodeStorage.fillEphemeralJobNode(ShardingNode.PROCESSING, "");
        clearShardingInfo();
        JobShardingStrategy jobShardingStrategy = JobShardingStrategyFactory.getStrategy(liteJobConfig.getJobShardingStrategyClass());
        JobShardingStrategyOption option = new JobShardingStrategyOption(jobName, liteJobConfig.getTypeConfig().getCoreConfig().getShardingTotalCount());
        jobNodeStorage.executeInTransaction(new PersistShardingInfoTransactionExecutionCallback(jobShardingStrategy.sharding(availableShardingServers, option)));
        log.debug("Job '{}' sharding complete.", jobName);
    }
    
    private void blockUntilShardingCompleted() {
        while (!leaderElectionService.isLeader() && (jobNodeStorage.isJobNodeExisted(ShardingNode.NECESSARY) || jobNodeStorage.isJobNodeExisted(ShardingNode.PROCESSING))) {
            log.debug("Job '{}' sleep short time until sharding completed.", jobName);
            BlockUtils.waitingShortTime();
        }
    }
    
    private void waitingOtherJobCompleted() {
        while (executionService.hasRunningItems()) {
            log.debug("Job '{}' sleep short time until other job completed.", jobName);
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
        return ShardingItems.toItemList(jobNodeStorage.getJobNodeDataDirectly(ShardingNode.getShardingNode(ip)));
    }
    
    /**
     * 查询是否存在没有运行状态并且含有分片节点的作业服务器.
     * 
     * @return 是否存在没有运行状态并且含有分片节点的作业服务器
     */
    public boolean hasNotRunningShardingNode() {
        for (String each : this.serverService.getAllServers()) {
            if (this.jobNodeStorage.isJobNodeExisted(ShardingNode.getShardingNode(each)) 
                && !this.serverService.hasStatusNode(each)) {
                return true;
            }
        }
        return false;
    }
    
    @RequiredArgsConstructor
    class PersistShardingInfoTransactionExecutionCallback implements TransactionExecutionCallback {
        
        private final Map<String, List<Integer>> shardingItems;
        
        @Override
        public void execute(final CuratorTransactionFinal curatorTransactionFinal) throws Exception {
            for (Entry<String, List<Integer>> entry : shardingItems.entrySet()) {
                curatorTransactionFinal.create().forPath(jobNodePath.getFullPath(ShardingNode.getShardingNode(entry.getKey())), ShardingItems.toItemsString(entry.getValue()).getBytes()).and();
            }
            curatorTransactionFinal.delete().forPath(jobNodePath.getFullPath(ShardingNode.NECESSARY)).and();
            curatorTransactionFinal.delete().forPath(jobNodePath.getFullPath(ShardingNode.PROCESSING)).and();
        }
    }
}
