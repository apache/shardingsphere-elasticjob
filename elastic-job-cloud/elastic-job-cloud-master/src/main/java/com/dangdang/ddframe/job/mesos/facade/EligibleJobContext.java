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

package com.dangdang.ddframe.job.mesos.facade;

import com.dangdang.ddframe.job.context.JobContext;
import com.dangdang.ddframe.job.context.TaskContext;
import com.dangdang.ddframe.job.mesos.stragety.ResourceAllocateStrategy;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.mesos.Protos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 有资格运行的作业集合上下文.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@ToString
public final class EligibleJobContext {
    
    private final Collection<JobContext> failoverJobContexts;
    
    private final Collection<JobContext> misfiredJobContexts;
    
    private final Collection<JobContext> readyJobContexts;
    
    /**
     * 分配资源.
     * 
     * @param resourceAllocateStrategy 资源分配策略
     * @return 分配完成的任务集合上下文
     */
    public AssignedTaskContext allocate(final ResourceAllocateStrategy resourceAllocateStrategy) {
        List<Protos.TaskInfo> failoverTaskInfoList = resourceAllocateStrategy.allocate(failoverJobContexts);
        List<Protos.TaskInfo> misfiredTaskInfoList = resourceAllocateStrategy.allocate(misfiredJobContexts);
        List<Protos.TaskInfo> readyTaskInfoList = resourceAllocateStrategy.allocate(readyJobContexts);
        return new AssignedTaskContext(getTaskInfoList(failoverTaskInfoList, misfiredTaskInfoList, readyTaskInfoList), 
                getFailoverTaskContext(failoverTaskInfoList), getJobNames(misfiredTaskInfoList), getJobNames(readyTaskInfoList));
    }
    
    private List<Protos.TaskInfo> getTaskInfoList(final List<Protos.TaskInfo> failoverTaskInfoList,
                                                  final List<Protos.TaskInfo> misfiredTaskInfoList, final List<Protos.TaskInfo> readyTaskInfoList) {
        List<Protos.TaskInfo> result = new ArrayList<>(failoverTaskInfoList.size() + misfiredTaskInfoList.size() + readyTaskInfoList.size());
        result.addAll(failoverTaskInfoList);
        result.addAll(misfiredTaskInfoList);
        result.addAll(readyTaskInfoList);
        return result;
    }
    
    private List<TaskContext> getFailoverTaskContext(final List<Protos.TaskInfo> failoverTaskInfoList) {
        return Lists.transform(failoverTaskInfoList, new Function<Protos.TaskInfo, TaskContext>() {
            
            @Override
            public TaskContext apply(final Protos.TaskInfo input) {
                return TaskContext.from(input.getTaskId().getValue());
            }
        });
    }
    
    private List<String> getJobNames(final List<Protos.TaskInfo> taskInfoList) {
        return Lists.transform(taskInfoList, new Function<Protos.TaskInfo, String>() {
            
            @Override
            public String apply(final Protos.TaskInfo input) {
                return TaskContext.from(input.getTaskId().getValue()).getJobName();
            }
        });
    }
}
