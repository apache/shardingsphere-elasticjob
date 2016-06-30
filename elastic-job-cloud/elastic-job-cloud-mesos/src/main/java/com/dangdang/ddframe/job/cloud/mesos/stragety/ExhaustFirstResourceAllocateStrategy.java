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

import com.dangdang.ddframe.job.cloud.job.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.mesos.MesosUtil;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.mesos.Protos;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 同一机器资源优先策略.
 *
 * @author zhangliang
 */
public final class ExhaustFirstResourceAllocateStrategy implements ResourceAllocateStrategy {
    
    private final List<Protos.TaskInfo> tasks;
    
    private final List<MachineResource> machineResources;
    
    public ExhaustFirstResourceAllocateStrategy(final List<Protos.Offer> offers) {
        tasks = new LinkedList<>();
        machineResources = new ArrayList<>(offers.size());
        machineResources.addAll(Lists.transform(offers, new Function<Protos.Offer, MachineResource>() {
            
            @Override
            public MachineResource apply(final Protos.Offer input) {
                return new MachineResource(input);
            }
        }));
    }
    
    @Override
    public boolean allocate(final CloudJobConfiguration jobConfig) {
        List<Protos.TaskInfo> tasks = new ArrayList<>(jobConfig.getShardingTotalCount());
        int startShardingItem = 0;
        int shardingTotalCount = jobConfig.getShardingTotalCount();
        int assignedShardingCount = 0;
        for (MachineResource each : machineResources) {
            if (0 == shardingTotalCount) {
                break;
            }
            assignedShardingCount += each.calculateShardingCount(shardingTotalCount, jobConfig.getCpuCount(), jobConfig.getMemoryMB());
            shardingTotalCount -= assignedShardingCount;
            for (int i = startShardingItem; i < assignedShardingCount; i++) {
                each.reserveResources(jobConfig.getCpuCount(), jobConfig.getMemoryMB());
                tasks.add(MesosUtil.createTaskInfo(each.getOffer(), jobConfig, i));
            }
            startShardingItem = assignedShardingCount;
        }
        if (tasks.size() != jobConfig.getShardingTotalCount()) {
            return false;
        }
        this.tasks.addAll(tasks);
        for (MachineResource each : machineResources) {
            each.commitReservedResources();
        }
        return true;
    }
    
    @Override
    public List<Protos.TaskInfo> getTasks() {
        return tasks;
    }
}
