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

import com.dangdang.ddframe.job.cloud.JobContext;
import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.state.ElasticJobTask;
import com.dangdang.ddframe.job.cloud.state.running.RunningService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;

/**
 * 失效转移队列服务.
 *
 * @author zhangliang
 */
public class FailoverService {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    private final ConfigurationService configService;
    
    private final RunningService runningService;
    
    public FailoverService(final CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
        configService = new ConfigurationService(registryCenter);
        runningService = new RunningService(registryCenter);
    }
    
    /**
     * 将任务放入失效转移队列.
     *
     * @param task 任务
     */
    public void enqueue(final ElasticJobTask task) {
        if (!registryCenter.isExisted(FailoverNode.getFailoverTaskNodePath(task.getId()))) {
            registryCenter.persist(FailoverNode.getFailoverTaskNodePath(task.getId()), "");
        }
    }
    
    /**
     * 从失效转移中获取顶端作业.
     *
     * @return 出队的作业
     */
    public Optional<JobContext> dequeue() {
        if (!registryCenter.isExisted(FailoverNode.ROOT)) {
            return Optional.absent();
        }
        List<String> jobNames = registryCenter.getChildrenKeys(FailoverNode.ROOT);
        for (String each : jobNames) {
            List<String> taskIds = registryCenter.getChildrenKeys(FailoverNode.getFailoverJobNodePath(each));
            if (taskIds.isEmpty()) {
                registryCenter.remove(FailoverNode.getFailoverJobNodePath(each));
                continue;
            }
            Optional<CloudJobConfiguration> jobConfig = configService.load(each);
            if (!jobConfig.isPresent()) {
                registryCenter.remove(FailoverNode.getFailoverJobNodePath(each));
                continue;
            }
            List<Integer> assignedShardingItems = new ArrayList<>(taskIds.size());
            for (String taskId : taskIds) {
                ElasticJobTask task = ElasticJobTask.from(taskId);
                if (!runningService.isTaskRunning(task)) {
                    assignedShardingItems.add(task.getShardingItem());
                }
                // TODO 需考虑,是否发现已运行的任务就删除此失效转移
                registryCenter.remove(FailoverNode.getFailoverTaskNodePath(taskId));
            }
            return Optional.of(new JobContext(jobConfig.get(), assignedShardingItems));
        }
        return Optional.absent();
    }
}
