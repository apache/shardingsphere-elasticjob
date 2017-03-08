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

package com.dangdang.ddframe.job.cloud.scheduler.mesos;

import com.dangdang.ddframe.job.cloud.scheduler.context.JobContext;
import com.dangdang.ddframe.job.context.ExecutionType;
import com.dangdang.ddframe.job.context.TaskContext;
import com.netflix.fenzo.TaskAssignmentResult;
import com.netflix.fenzo.TaskRequest;
import com.netflix.fenzo.VMAssignmentResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 分配任务行为包.
 *
 * @author zhangliang
 */
@Slf4j
public final class LaunchingTasks {
    
    private final Map<String, JobContext> eligibleJobContextsMap;
    
    public LaunchingTasks(final Collection<JobContext> eligibleJobContexts) {
        eligibleJobContextsMap = new HashMap<>(eligibleJobContexts.size(), 1);
        for (JobContext each : eligibleJobContexts) {
            eligibleJobContextsMap.put(each.getJobConfig().getJobName(), each);
        }
    }
    
    List<TaskRequest> getPendingTasks() {
        List<TaskRequest> result = new ArrayList<>(eligibleJobContextsMap.size() * 10);
        for (JobContext each : eligibleJobContextsMap.values()) {
            result.addAll(createTaskRequests(each));
        }
        return result;
    }
    
    private Collection<TaskRequest> createTaskRequests(final JobContext jobContext) {
        Collection<TaskRequest> result = new ArrayList<>(jobContext.getAssignedShardingItems().size());
        for (int each : jobContext.getAssignedShardingItems()) {
            result.add(new JobTaskRequest(new TaskContext(jobContext.getJobConfig().getJobName(), Arrays.asList(each), jobContext.getType()), jobContext.getJobConfig()));
        }
        return result;
    }
    
    Collection<String> getIntegrityViolationJobs(final Collection<VMAssignmentResult> vmAssignmentResults) {
        Map<String, Integer> assignedJobShardingTotalCountMap = getAssignedJobShardingTotalCountMap(vmAssignmentResults);
        Collection<String> result = new HashSet<>(assignedJobShardingTotalCountMap.size(), 1);
        for (Map.Entry<String, Integer> entry : assignedJobShardingTotalCountMap.entrySet()) {
            JobContext jobContext = eligibleJobContextsMap.get(entry.getKey());
            if (ExecutionType.FAILOVER != jobContext.getType() && !entry.getValue().equals(jobContext.getJobConfig().getTypeConfig().getCoreConfig().getShardingTotalCount())) {
                log.warn("Job {} is not assigned at this time, because resources not enough to run all sharding instances.", entry.getKey());
                result.add(entry.getKey());
            }
        }
        return result;
    }
    
    private Map<String, Integer> getAssignedJobShardingTotalCountMap(final Collection<VMAssignmentResult> vmAssignmentResults) {
        Map<String, Integer> result = new HashMap<>(eligibleJobContextsMap.size(), 1);
        for (VMAssignmentResult vmAssignmentResult: vmAssignmentResults) {
            for (TaskAssignmentResult tasksAssigned: vmAssignmentResult.getTasksAssigned()) {
                String jobName = TaskContext.from(tasksAssigned.getTaskId()).getMetaInfo().getJobName();
                if (result.containsKey(jobName)) {
                    result.put(jobName, result.get(jobName) + 1);
                } else {
                    result.put(jobName, 1);
                }
            }
        }
        return result;
    }
}
