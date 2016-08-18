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

package com.dangdang.ddframe.job.cloud.mesos.strategy;

import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.config.JobExecutionType;
import com.dangdang.ddframe.job.cloud.context.JobContext;
import com.dangdang.ddframe.job.cloud.context.TaskContext;
import com.dangdang.ddframe.job.cloud.mesos.HardwareResource;
import com.dangdang.ddframe.job.cloud.mesos.facade.FacadeService;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.Protos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 同一机器资源优先策略.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Slf4j
public final class ExhaustFirstResourceAllocateStrategy implements ResourceAllocateStrategy {
    
    private final List<HardwareResource> hardwareResources;
    
    private final FacadeService facadeService;
    
    @Override
    public List<Protos.TaskInfo> allocate(final Collection<JobContext> jobContexts) {
        List<Protos.TaskInfo> result = new LinkedList<>();
        for (JobContext each : jobContexts) {
            if (JobExecutionType.TRANSIENT == each.getJobConfig().getJobExecutionType()) {
                result.addAll(allocateTransient(each));
            } else if (JobExecutionType.DAEMON == each.getJobConfig().getJobExecutionType()) {
                result.addAll(allocateDaemon(each));
            }
        }
        return result;
    }
    
    private List<Protos.TaskInfo> allocateTransient(final JobContext jobContext) {
        return getTaskInfoList(jobContext, jobContext.getAssignedShardingItems());
    }
    
    private List<Protos.TaskInfo> allocateDaemon(final JobContext jobContext) {
        return getTaskInfoList(jobContext, getToBeAssignedShardingItems(jobContext));
    }
    
    private List<Protos.TaskInfo> getTaskInfoList(final JobContext jobContext, final List<Integer> assignedShardingItems) {
        CloudJobConfiguration jobConfig = jobContext.getJobConfig();
        int assignedShardingItemsCount = assignedShardingItems.size();
        int startShardingItemIndex = 0;
        int assignedShardingCount = 0;
        List<Protos.TaskInfo> result = new ArrayList<>(assignedShardingItemsCount);
        for (HardwareResource each : hardwareResources) {
            if (0 == assignedShardingItemsCount) {
                break;
            }
            assignedShardingCount += each.calculateShardingCount(assignedShardingItemsCount, jobConfig.getCpuCount(), jobConfig.getMemoryMB());
            assignedShardingItemsCount -= assignedShardingCount;
            for (int i = startShardingItemIndex; i < assignedShardingCount; i++) {
                each.reserveResources(jobConfig.getCpuCount(), jobConfig.getMemoryMB());
                result.add(each.createTaskInfo(jobContext, assignedShardingItems.get(i)));
            }
            startShardingItemIndex = assignedShardingCount;
        }
        if (result.size() != assignedShardingItems.size()) {
            if (!result.isEmpty()) {
                log.warn("Resources not enough, job `{}` is not allocated.", jobContext.getJobConfig().getJobName());
            }
            return Collections.emptyList();
        }
        for (HardwareResource each : hardwareResources) {
            each.commitReservedResources();
        }
        return result;
    }
    
    private List<Integer> getToBeAssignedShardingItems(final JobContext jobContext) {
        int shardingTotalCount = jobContext.getJobConfig().getTypeConfig().getCoreConfig().getShardingTotalCount();
        List<Integer> result = new ArrayList<>(shardingTotalCount);
        Collection<Integer> runningShardingItems = Collections2.transform(facadeService.getRunningTasks(jobContext.getJobConfig().getJobName()), new Function<TaskContext, Integer>() {
            
            @Override
            public Integer apply(final TaskContext input) {
                return input.getMetaInfo().getShardingItem();
            }
        });
        for (int i = 0; i < shardingTotalCount; i++) {
            if (!runningShardingItems.contains(i)) {
                result.add(i);
            }
        }
        return result;
    }
}
