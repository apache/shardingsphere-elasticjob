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

import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.context.ExecutionType;
import com.dangdang.ddframe.job.cloud.context.JobContext;
import com.dangdang.ddframe.job.cloud.context.TaskContext;
import com.dangdang.ddframe.job.cloud.state.running.RunningService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     * @param taskContext 任务运行时上下文
     */
    public void add(final TaskContext taskContext) {
        if (!registryCenter.isExisted(FailoverNode.getFailoverTaskNodePath(taskContext.getId())) && !runningService.isTaskRunning(taskContext)) {
            registryCenter.persist(FailoverNode.getFailoverTaskNodePath(taskContext.getId()), "");
        }
    }
    
    /**
     * 从失效转移队列中获取所有有资格执行的作业上下文.
     *
     * @return 有资格执行的作业上下文集合
     */
    public Collection<JobContext> getAllEligibleJobContexts() {
        if (!registryCenter.isExisted(FailoverNode.ROOT)) {
            return Collections.emptyList();
        }
        List<String> jobNames = registryCenter.getChildrenKeys(FailoverNode.ROOT);
        Collection<JobContext> result = new ArrayList<>(jobNames.size());
        Set<HashCode> assignedTasks = new HashSet<>(jobNames.size() * 10, 1);
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
            List<Integer> assignedShardingItems = getAssignedShardingItems(each, taskIds, assignedTasks);
            if (!assignedShardingItems.isEmpty()) {
                result.add(new JobContext(jobConfig.get(), assignedShardingItems, ExecutionType.FAILOVER));
            }
        }
        return result;
    }
    
    private List<Integer> getAssignedShardingItems(final String jobName, final List<String> taskIds, final Set<HashCode> assignedTasks) {
        List<Integer> result = new ArrayList<>(taskIds.size());
        for (String each : taskIds) {
            TaskContext taskContext = TaskContext.from(each);
            if (assignedTasks.add(Hashing.md5().newHasher().putString(jobName, Charsets.UTF_8).putInt(taskContext.getShardingItem()).hash()) && !runningService.isTaskRunning(taskContext)) {
                result.add(taskContext.getShardingItem());
            }
        }
        return result;
    }
    
    /**
     * 从失效转移队列中删除相关任务.
     * 
     * @param taskContexts 待删除的任务
     */
    public void remove(final Collection<TaskContext> taskContexts) {
        for (TaskContext each : taskContexts) {
            registryCenter.remove(FailoverNode.getFailoverTaskNodePath(each.getId()));
        }
    }
}
