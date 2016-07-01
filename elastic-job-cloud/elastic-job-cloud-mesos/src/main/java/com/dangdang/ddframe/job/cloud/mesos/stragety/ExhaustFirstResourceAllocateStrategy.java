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

package com.dangdang.ddframe.job.cloud.mesos.stragety;

import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.mesos.MesosUtil;
import lombok.RequiredArgsConstructor;
import org.apache.mesos.Protos;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 同一机器资源优先策略.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ExhaustFirstResourceAllocateStrategy implements ResourceAllocateStrategy {
    
    private final List<Protos.TaskInfo> offeredTaskInfoList = new LinkedList<>();
    
    private final List<Protos.TaskInfo> declinedTaskInfoList = new LinkedList<>();
    
    private final List<MachineResource> machineResources;
    
    @Override
    public boolean allocate(final CloudJobConfiguration jobConfig) {
        List<Integer> shardingItems = new ArrayList<>(jobConfig.getShardingTotalCount());
        for (int i = 0; i < jobConfig.getShardingTotalCount(); i++) {
            shardingItems.add(i);
        }
        return allocate(jobConfig, shardingItems);
    }
    
    @Override
    public boolean allocate(final CloudJobConfiguration jobConfig, final List<Integer> shardingItems) {
        declinedTaskInfoList.clear();
        List<Protos.TaskInfo> taskInfoList = new ArrayList<>(shardingItems.size());
        int startShardingItemIndex = 0;
        int shardingTotalCount = shardingItems.size();
        int assignedShardingCount = 0;
        for (MachineResource each : machineResources) {
            if (0 == shardingTotalCount) {
                break;
            }
            assignedShardingCount += each.calculateShardingCount(shardingTotalCount, jobConfig.getCpuCount(), jobConfig.getMemoryMB());
            shardingTotalCount -= assignedShardingCount;
            for (int i = startShardingItemIndex; i < assignedShardingCount; i++) {
                each.reserveResources(jobConfig.getCpuCount(), jobConfig.getMemoryMB());
                taskInfoList.add(MesosUtil.createTaskInfo(each.getOffer(), jobConfig, shardingItems.get(i)));
            }
            startShardingItemIndex = assignedShardingCount;
        }
        if (taskInfoList.size() != jobConfig.getShardingTotalCount()) {
            declinedTaskInfoList.addAll(taskInfoList);
            return false;
        }
        this.offeredTaskInfoList.addAll(taskInfoList);
        for (MachineResource each : machineResources) {
            each.commitReservedResources();
        }
        return true;
    }
    
    @Override
    public List<Protos.TaskInfo> getOfferedTaskInfoList() {
        return offeredTaskInfoList;
    }
    
    @Override
    public List<Protos.TaskInfo> getDeclinedTaskInfoList() {
        return declinedTaskInfoList;
    }
}
