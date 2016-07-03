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

package com.dangdang.ddframe.job.cloud.state.failover;

import com.dangdang.ddframe.job.cloud.state.ElasticJobTask;
import com.dangdang.ddframe.job.cloud.state.running.RunningService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;

import java.util.List;

/**
 * 失效转移任务队列服务.
 *
 * @author zhangliang
 */
public class FailoverService {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    private final RunningService runningService;
    
    public FailoverService(final CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
        runningService = new RunningService(registryCenter);
    }
    
    /**
     * 将任务放入失效转移队列.
     *
     * @param task 任务
     */
    public void enqueue(final ElasticJobTask task) {
        if (!registryCenter.isExisted(FailoverNode.getFailoverNodePath(task.getId()))) {
            registryCenter.persist(FailoverNode.getFailoverNodePath(task.getId()), "");
        }
    }
    
    /**
     * 从失效转移中获取顶端任务名称.
     *
     * @return 出队的任务, 队列为空则不返回数据
     */
    public Optional<ElasticJobTask> dequeue() {
        if (!registryCenter.isExisted(FailoverNode.ROOT)) {
            return Optional.absent();
        }
        List<String> taskIds = registryCenter.getChildrenKeys(FailoverNode.ROOT);
        if (taskIds.isEmpty()) {
            return Optional.absent();
        }
        for (String each : taskIds) {
            ElasticJobTask task = ElasticJobTask.from(each);
            if (!runningService.isJobRunning(task.getJobName())) {
                registryCenter.remove(FailoverNode.getFailoverNodePath(each));
                return Optional.of(task);
            }
        }
        return Optional.absent();
    }
}
