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
import com.dangdang.ddframe.job.cloud.context.JobContext;
import com.dangdang.ddframe.job.cloud.mesos.HardwareResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.Protos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 同一机器资源优先策略.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Slf4j
public final class ExhaustFirstResourceAllocateStrategy implements ResourceAllocateStrategy {
    
    private final List<HardwareResource> hardwareResources;
    
    @Override
    public List<Protos.TaskInfo> allocate(final Collection<JobContext> jobContexts) {
        List<Protos.TaskInfo> result = new LinkedList<>();
        for (JobContext each : jobContexts) {
            result.addAll(allocate(each));
        }
        return result;
    }
    
    @Override
    public Map<String, List<Protos.TaskInfo>> allocate(final Map<String, JobContext> jobContextMap) {
        Map<String, List<Protos.TaskInfo>> result = new HashMap<>(jobContextMap.size());
        for (Map.Entry<String, JobContext> entry : jobContextMap.entrySet()) {
            List<Protos.TaskInfo> taskInfoList = allocate(entry.getValue());
            if (!taskInfoList.isEmpty()) {
                result.put(entry.getKey(), taskInfoList);
            }
        }
        return result;
    }
    
    private List<Protos.TaskInfo> allocate(final JobContext jobContext) {
        CloudJobConfiguration jobConfig = jobContext.getJobConfig();
        List<Integer> assignedShardingItems = jobContext.getAssignedShardingItems();
        int shardingTotalCount = assignedShardingItems.size();
        int startShardingItemIndex = 0;
        int assignedShardingCount = 0;
        List<Protos.TaskInfo> result = new ArrayList<>(shardingTotalCount);
        for (HardwareResource each : hardwareResources) {
            if (0 == shardingTotalCount) {
                break;
            }
            assignedShardingCount += each.calculateShardingCount(shardingTotalCount, jobConfig.getCpuCount(), jobConfig.getMemoryMB());
            shardingTotalCount -= assignedShardingCount;
            for (int i = startShardingItemIndex; i < assignedShardingCount; i++) {
                each.reserveResources(jobConfig.getCpuCount(), jobConfig.getMemoryMB());
                result.add(each.createTaskInfo(jobContext, assignedShardingItems.get(i)));
            }
            startShardingItemIndex = assignedShardingCount;
        }
        if (result.size() != jobConfig.getShardingTotalCount()) {
            if (!result.isEmpty()) {
                log.warn("Resources not enough, job `{}` is not allocated. ", jobContext.getJobConfig().getJobName());
            }
            return Collections.emptyList();
        }
        for (HardwareResource each : hardwareResources) {
            each.commitReservedResources();
        }
        return result;
    }
}
