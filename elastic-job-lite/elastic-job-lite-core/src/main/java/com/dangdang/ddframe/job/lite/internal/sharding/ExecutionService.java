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

import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.lite.internal.election.LeaderService;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.util.concurrent.BlockUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 执行作业的服务.
 * 
 * @author zhangliang
 * @author caohao
 */
public class ExecutionService {
    
    private final String jobName;
    
    private final JobNodeStorage jobNodeStorage;
    
    private final ConfigurationService configService;
    
    private final LeaderService leaderService;
    
    public ExecutionService(final CoordinatorRegistryCenter regCenter, final String jobName) {
        this.jobName = jobName;
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        configService = new ConfigurationService(regCenter, jobName);
        leaderService = new LeaderService(regCenter, jobName);
    }
    
    /**
     * 注册作业启动信息.
     * 
     * @param shardingContexts 分片上下文
     */
    public void registerJobBegin(final ShardingContexts shardingContexts) {
        JobRegistry.getInstance().setJobRunning(jobName, true);
        if (!configService.load(true).isMonitorExecution()) {
            return;
        }
        for (int each : shardingContexts.getShardingItemParameters().keySet()) {
            jobNodeStorage.fillEphemeralJobNode(ShardingNode.getRunningNode(each), "");
        }
    }
    
    /**
     * 清理作业上次运行时信息.
     * 只会在主节点进行.
     */
    public void cleanPreviousExecutionInfo() {
        if (!configService.load(true).isMonitorExecution()) {
            return;
        }
        if (leaderService.isLeaderUntilBlock()) {
            jobNodeStorage.fillEphemeralJobNode(ShardingNode.CLEANING, "");
            List<Integer> items = getAllItems();
            for (int each : items) {
                jobNodeStorage.removeJobNodeIfExisted(ShardingNode.getCompletedNode(each));
            }
            jobNodeStorage.removeJobNodeIfExisted(ShardingNode.CLEANING);
        }
        while (jobNodeStorage.isJobNodeExisted(ShardingNode.CLEANING)) {
            BlockUtils.waitingShortTime();
        }
    }
    
    /**
     * 注册作业完成信息.
     * 
     * @param shardingContexts 分片上下文
     */
    public void registerJobCompleted(final ShardingContexts shardingContexts) {
        JobRegistry.getInstance().setJobRunning(jobName, false);
        if (!configService.load(true).isMonitorExecution()) {
            return;
        }
        for (int each : shardingContexts.getShardingItemParameters().keySet()) {
            jobNodeStorage.removeJobNodeIfExisted(ShardingNode.getRunningNode(each));
            jobNodeStorage.createJobNodeIfNeeded(ShardingNode.getCompletedNode(each));
        }
    }
    
    /**
     * 清除分配分片序列号的运行状态.
     * 
     * <p>
     * 用于作业服务器恢复连接注册中心而重新上线的场景, 先清理上次运行时信息.
     * </p>
     * 
     * @param items 需要清理的分片项列表
     */
    public void clearRunningInfo(final List<Integer> items) {
        for (int each : items) {
            jobNodeStorage.removeJobNodeIfExisted(ShardingNode.getRunningNode(each));
        }
    }
    
    /**
     * 判断分片项中是否还有执行中的作业.
     *
     * @param items 需要判断的分片项列表
     * @return 分片项中是否还有执行中的作业
     */
    public boolean hasRunningItems(final Collection<Integer> items) {
        if (!configService.load(true).isMonitorExecution()) {
            return false;
        }
        for (int each : items) {
            if (jobNodeStorage.isJobNodeExisted(ShardingNode.getRunningNode(each))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 判断是否还有执行中的作业.
     *
     * @return 是否还有执行中的作业
     */
    public boolean hasRunningItems() {
        return hasRunningItems(getAllItems());
    }
    
    private List<Integer> getAllItems() {
        int shardingTotalCount = configService.load(true).getTypeConfig().getCoreConfig().getShardingTotalCount();
        List<Integer> result = new ArrayList<>(shardingTotalCount);
        for (int i = 0; i < shardingTotalCount; i++) {
            result.add(i);
        }
        return result;
    }
    
    /**
     * 如果当前分片项仍在运行则设置任务被错过执行的标记.
     * 
     * @param items 需要设置错过执行的任务分片项
     * @return 是否错过本次执行
     */
    public boolean misfireIfRunning(final Collection<Integer> items) {
        if (!hasRunningItems(items)) {
            return false;
        }
        for (int each : items) {
            jobNodeStorage.createJobNodeIfNeeded(ShardingNode.getMisfireNode(each));
        }
        return true;
    }
    
    /**
     * 获取标记被错过执行的任务分片项.
     * 
     * @param items 需要获取标记被错过执行的任务分片项
     * @return 标记被错过执行的任务分片项
     */
    public List<Integer> getMisfiredJobItems(final Collection<Integer> items) {
        List<Integer> result = new ArrayList<>(items.size());
        for (int each : items) {
            if (jobNodeStorage.isJobNodeExisted(ShardingNode.getMisfireNode(each))) {
                result.add(each);
            }
        }
        return result;
    }
    
    /**
     * 清除任务被错过执行的标记.
     * 
     * @param items 需要清除错过执行的任务分片项
     */
    public void clearMisfire(final Collection<Integer> items) {
        for (int each : items) {
            jobNodeStorage.removeJobNodeIfExisted(ShardingNode.getMisfireNode(each));
        }
    }
    
    /**
     * 获取禁用的任务分片项.
     *
     * @param items 需要获取禁用的任务分片项
     * @return 禁用的任务分片项
     */
    public List<Integer> getDisabledItems(final List<Integer> items) {
        List<Integer> result = new ArrayList<>(items.size());
        for (int each : items) {
            if (jobNodeStorage.isJobNodeExisted(ShardingNode.getDisabledNode(each))) {
                result.add(each);
            }
        }
        return result;
    }
    
    /**
     * 判断该分片是否已完成.
     * 
     * @param item 运行中的分片路径
     * @return 该分片是否已完成
     */
    public boolean isCompleted(final int item) {
        return jobNodeStorage.isJobNodeExisted(ShardingNode.getCompletedNode(item));
    }
}
