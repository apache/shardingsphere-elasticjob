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

package com.dangdang.ddframe.job.internal.execution;

import com.dangdang.ddframe.job.api.config.JobConfiguration;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.internal.server.ServerService;
import com.dangdang.ddframe.job.internal.server.ServerStatus;
import com.dangdang.ddframe.job.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.internal.reg.BlockUtils;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 执行作业的服务.
 * 
 * @author zhangliang
 * @author caohao
 */
public class ExecutionService {
    
    private final JobConfiguration jobConfiguration;
    
    private final JobNodeStorage jobNodeStorage;
    
    private final ConfigurationService configService;
    
    private final ServerService serverService;
    
    private final LeaderElectionService leaderElectionService;
    
    public ExecutionService(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration) {
        this.jobConfiguration = jobConfiguration;
        jobNodeStorage = new JobNodeStorage(coordinatorRegistryCenter, jobConfiguration);
        configService = new ConfigurationService(coordinatorRegistryCenter, jobConfiguration);
        serverService = new ServerService(coordinatorRegistryCenter, jobConfiguration);
        leaderElectionService = new LeaderElectionService(coordinatorRegistryCenter, jobConfiguration);
    }
    
    /**
     * 注册作业启动信息.
     * 
     * @param jobExecutionShardingContext 作业运行时分片上下文
     */
    public void registerJobBegin(final JobExecutionMultipleShardingContext jobExecutionShardingContext) {
        if (!jobExecutionShardingContext.getShardingItems().isEmpty() && configService.isMonitorExecution()) {
            serverService.updateServerStatus(ServerStatus.RUNNING);
            for (int each : jobExecutionShardingContext.getShardingItems()) {
                jobNodeStorage.fillEphemeralJobNode(ExecutionNode.getRunningNode(each), "");
                jobNodeStorage.replaceJobNode(ExecutionNode.getLastBeginTimeNode(each), System.currentTimeMillis());
                JobScheduleController jobScheduleController = JobRegistry.getInstance().getJobScheduleController(jobConfiguration.getJobName());
                if (null == jobScheduleController) {
                    continue;
                }
                Date nextFireTime = jobScheduleController.getNextFireTime();
                if (null != nextFireTime) {
                    jobNodeStorage.replaceJobNode(ExecutionNode.getNextFireTimeNode(each), nextFireTime.getTime());
                }
            }
        }
    }
    
    /**
     * 清理作业上次运行时信息.
     * 只会在主节点进行.
     */
    public void cleanPreviousExecutionInfo() {
        if (!jobNodeStorage.isJobNodeExisted(ExecutionNode.ROOT)) {
            return;
        }
        if (leaderElectionService.isLeader()) {
            jobNodeStorage.fillEphemeralJobNode(ExecutionNode.CLEANING, "");
            List<Integer> items = getAllItems();
            for (int each : items) {
                jobNodeStorage.removeJobNodeIfExisted(ExecutionNode.getCompletedNode(each));
            }
            if (jobNodeStorage.isJobNodeExisted(ExecutionNode.NECESSARY)) {
                fixExecutionInfo(items);
            }
            jobNodeStorage.removeJobNodeIfExisted(ExecutionNode.CLEANING);
        }
        while (jobNodeStorage.isJobNodeExisted(ExecutionNode.CLEANING)) {
            BlockUtils.waitingShortTime();
        }
    }
    
    private void fixExecutionInfo(final List<Integer> items) {
        int newShardingTotalCount = configService.getShardingTotalCount();
        int currentShardingTotalCount = items.size();
        if (newShardingTotalCount > currentShardingTotalCount) {
            for (int i = currentShardingTotalCount; i < newShardingTotalCount; i++) {
                jobNodeStorage.createJobNodeIfNeeded(ExecutionNode.ROOT + "/" + i);
            }
        } else if (newShardingTotalCount < currentShardingTotalCount) {
            for (int i = newShardingTotalCount; i < currentShardingTotalCount; i++) {
                jobNodeStorage.removeJobNodeIfExisted(ExecutionNode.ROOT + "/" + i);
            }
        }
        jobNodeStorage.removeJobNodeIfExisted(ExecutionNode.NECESSARY);
    }
    
    /**
     * 注册作业完成信息.
     * 
     * @param jobExecutionShardingContext 作业运行时分片上下文
     */
    public void registerJobCompleted(final JobExecutionMultipleShardingContext jobExecutionShardingContext) {
        if (!configService.isMonitorExecution()) {
            return;
        }
        serverService.updateServerStatus(ServerStatus.READY);
        for (int each : jobExecutionShardingContext.getShardingItems()) {
            jobNodeStorage.createJobNodeIfNeeded(ExecutionNode.getCompletedNode(each));
            jobNodeStorage.removeJobNodeIfExisted(ExecutionNode.getRunningNode(each));
            jobNodeStorage.replaceJobNode(ExecutionNode.getLastCompleteTimeNode(each), System.currentTimeMillis());
        }
    }
    
    /**
     * 设置修复运行时分片信息标记的状态标志位.
     */
    public void setNeedFixExecutionInfoFlag() {
        jobNodeStorage.createJobNodeIfNeeded(ExecutionNode.NECESSARY);
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
            jobNodeStorage.removeJobNodeIfExisted(ExecutionNode.getRunningNode(each));
        }
    }
    
    /**
     * 如果满足条件，设置任务被错过执行的标记.
     * 
     * @param items 需要设置错过执行的任务分片项
     * @return 是否满足misfire条件
     */
    public boolean misfireIfNecessary(final List<Integer> items) {
        if (hasRunningItems(items)) {
            setMisfire(items);
            return true;
        }
        return false;
    }
    
    /**
     * 设置任务被错过执行的标记.
     * 
     * @param items 需要设置错过执行的任务分片项
     */
    public void setMisfire(final List<Integer> items) {
        if (!configService.isMonitorExecution()) {
            return;
        }
        for (int each : items) {
            jobNodeStorage.createJobNodeIfNeeded(ExecutionNode.getMisfireNode(each));
        }
    }
    
    /**
     * 获取标记被错过执行的任务分片项.
     * 
     * @param items 需要获取标记被错过执行的任务分片项
     * @return 标记被错过执行的任务分片项
     */
    public List<Integer> getMisfiredJobItems(final List<Integer> items) {
        List<Integer> result = new ArrayList<>(items.size());
        for (int each : items) {
            if (jobNodeStorage.isJobNodeExisted(ExecutionNode.getMisfireNode(each))) {
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
    public void clearMisfire(final List<Integer> items) {
        for (int each : items) {
            jobNodeStorage.removeJobNodeIfExisted(ExecutionNode.getMisfireNode(each));
        }
    }
    
    /**
     * 删除作业执行时信息.
     */
    public void removeExecutionInfo() {
        jobNodeStorage.removeJobNodeIfExisted(ExecutionNode.ROOT);
    }
    
    /**
     * 判断该分片是否已完成.
     * 
     * @param item 运行中的分片路径
     * @return 该分片是否已完成
     */
    public boolean isCompleted(final int item) {
        return jobNodeStorage.isJobNodeExisted(ExecutionNode.getCompletedNode(item));
    }
    
    /**
     * 判断分片项中是否还有执行中的作业.
     * 
     * @param items 需要判断的分片项列表
     * @return 分片项中是否还有执行中的作业
     */
    public boolean hasRunningItems(final List<Integer> items) {
        if (!configService.isMonitorExecution()) {
            return false;
        }
        for (int each : items) {
            if (jobNodeStorage.isJobNodeExisted(ExecutionNode.getRunningNode(each))) {
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
        return Lists.transform(jobNodeStorage.getJobNodeChildrenKeys(ExecutionNode.ROOT), new Function<String, Integer>() {
            
            @Override
            public Integer apply(final String input) {
                return Integer.parseInt(input);
            }
        });
    }
}
