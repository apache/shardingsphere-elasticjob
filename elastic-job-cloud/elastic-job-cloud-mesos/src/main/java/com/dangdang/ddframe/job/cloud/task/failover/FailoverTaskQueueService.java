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

package com.dangdang.ddframe.job.cloud.task.failover;

import com.dangdang.ddframe.job.cloud.job.state.StateService;
import com.dangdang.ddframe.job.cloud.task.ElasticJobTask;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;

import java.util.List;

/**
 * 失效转移任务队列服务.
 *
 * @author zhangliang
 */
public class FailoverTaskQueueService {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    private final StateService stateService;
    
    public FailoverTaskQueueService(final CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
        stateService = new StateService(registryCenter);
    }
    
    /**
     * 将任务放入失效转移队列.
     *
     * @param taskId 任务主键
     */
    public void enqueue(final String taskId) {
        if (!registryCenter.isExisted(FailoverTaskQueueNode.getFailoverNodePath(taskId))) {
            registryCenter.persist(FailoverTaskQueueNode.getFailoverNodePath(taskId), "");
        }
    }
    
    /**
     * 从待执行队列中获取顶端作业名称.
     *
     * @return 出队的任务, 队列为空则不返回数据
     */
    public Optional<ElasticJobTask> dequeue() {
        if (!registryCenter.isExisted(FailoverTaskQueueNode.ROOT)) {
            return Optional.absent();
        }
        List<String> taskIds = registryCenter.getChildrenKeys(FailoverTaskQueueNode.ROOT);
        if (taskIds.isEmpty()) {
            return Optional.absent();
        }
        for (String each : taskIds) {
            ElasticJobTask task = ElasticJobTask.from(each);
            if (!stateService.isRunning(task.getJobName())) {
                registryCenter.remove(FailoverTaskQueueNode.getFailoverNodePath(each));
                return Optional.of(task);
            }
        }
        return Optional.absent();
    }
}
