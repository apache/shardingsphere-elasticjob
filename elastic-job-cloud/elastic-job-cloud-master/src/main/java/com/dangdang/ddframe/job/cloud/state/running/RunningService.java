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

package com.dangdang.ddframe.job.cloud.state.running;

import com.dangdang.ddframe.job.cloud.context.TaskContext;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import lombok.RequiredArgsConstructor;

/**
 * 任务运行时服务.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public class RunningService {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    /**
     * 将任务运行时上下文放入运行时队列.
     * 
     * @param taskContext 任务运行时上下文
     */
    public void add(final TaskContext taskContext) {
        String runningTaskNodePath = RunningNode.getRunningTaskNodePath(taskContext.getMetaInfo());
        if (!registryCenter.isExisted(runningTaskNodePath)) {
            registryCenter.persist(runningTaskNodePath, taskContext.getId());
        }
    }
    
    /**
     * 将任务运行时上下文从队列删除.
     * 
     * @param taskContext 任务运行时上下文
     */
    public void remove(final TaskContext taskContext) {
        registryCenter.remove(RunningNode.getRunningTaskNodePath(taskContext.getMetaInfo()));
        String jobRootNode = RunningNode.getRunningJobNodePath(taskContext.getJobName());
        if (registryCenter.isExisted(jobRootNode) && registryCenter.getChildrenKeys(jobRootNode).isEmpty()) {
            registryCenter.remove(jobRootNode);
        }
    }
    
    /**
     * 判断作业是否运行.
     *
     * @param jobName 作业名称
     * @return 作业是否运行
     */
    public boolean isJobRunning(final String jobName) {
        return !registryCenter.getChildrenKeys(RunningNode.getRunningJobNodePath(jobName)).isEmpty();
    }
    
    /**
     * 判断任务是否运行.
     *
     * @param taskContext 任务运行时上下文
     * @return 任务是否运行
     */
    public boolean isTaskRunning(final TaskContext taskContext) {
        if (!registryCenter.isExisted(RunningNode.getRunningJobNodePath(taskContext.getJobName()))) {
            return false;
        }
        for (String each : registryCenter.getChildrenKeys(RunningNode.getRunningJobNodePath(taskContext.getJobName()))) {
            if (TaskContext.fromMetaInfo(each).getShardingItem() == taskContext.getShardingItem()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 清理所有运行时状态.
     */
    public void clear() {
        registryCenter.remove(RunningNode.ROOT);
    }
}
