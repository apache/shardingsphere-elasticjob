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

package com.dangdang.ddframe.job.cloud.scheduler.mesos.strategy;

import org.apache.mesos.Protos;

import com.dangdang.ddframe.job.cloud.scheduler.context.JobContext;

import java.util.Collection;
import java.util.List;

/**
 * 资源分配策略接口.
 *
 * @author zhangliang
 */
public interface ResourceAllocateStrategy {
    
    /**
     * 分配资源.
     * 
     * @param jobContexts 作业运行时上下文集合
     * @return 分配的任务列表
     */
    List<Protos.TaskInfo> allocate(Collection<JobContext> jobContexts);
}
