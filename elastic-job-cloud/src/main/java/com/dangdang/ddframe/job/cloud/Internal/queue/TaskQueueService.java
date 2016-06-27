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

package com.dangdang.ddframe.job.cloud.Internal.queue;

import com.dangdang.ddframe.job.cloud.Internal.running.RunningService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;

import java.util.List;

/**
 * 任务队列服务.
 *
 * @author zhangliang
 */
public class TaskQueueService {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    private final RunningService runningService;
    
    public TaskQueueService(final CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
        runningService = new RunningService(registryCenter);
    }
    
    /**
     * 将作业放入待执行队列.
     * 
     * @param jobName 作业名称
     */
    public void enqueue(final String jobName) {
        registryCenter.persistSequential(QueueNode.getQueueNodePath(jobName));
    }
    
    /**
     * 从待执行队列中获取顶端作业名称.
     * 
     * @return 出队的作业名称, 队列为空则不返回数据
     */
    public Optional<String> dequeue() {
        if (!registryCenter.isExisted(QueueNode.ROOT)) {
            return Optional.absent();
        }
        List<String> jobNames = registryCenter.getChildrenKeys(QueueNode.ROOT);
        if (jobNames.isEmpty()) {
            return Optional.absent();
        }
        for (String each : jobNames) {
            String jobName = getLogicNodeForSequential(each);
            if (!runningService.isRunning(jobName)) {
                registryCenter.remove(QueueNode.getQueueNodePath(each));
                return Optional.of(jobName);
            }
        }
        return Optional.absent();
    }
    
    private String getLogicNodeForSequential(final String sequentialNode) {
        return sequentialNode.substring(0, sequentialNode.length() - 10);
    }
}
