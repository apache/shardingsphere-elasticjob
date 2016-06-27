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

package com.dangdang.ddframe.job.cloud.Internal.running;

import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;

/**
 * 记录运行时状态的服务.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class RunningService {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    /**
     * 记录作业开始运行的状态.
     * 
     * @param jobName 作业名称
     * @param taskId 任务主键
     */
    public void startRunning(final String jobName, final String taskId) {
        String runningJobNodePath = RunningNode.getRunningJobNodePath(jobName);
        if (!registryCenter.isExisted(runningJobNodePath)) {
            registryCenter.persist(runningJobNodePath, "");
        }
        String runningTaskNodePath = RunningNode.getRunningTaskNodePath(taskId);
        if (!registryCenter.isExisted(runningTaskNodePath)) {
            registryCenter.persist(runningTaskNodePath, jobName);
        }
    }
    
    /**
     * 记录作业结束运行的状态.
     * 
     * @param jobName 作业名称
     * @param taskId 任务主键
     */
    public void completeRunning(final String jobName, final String taskId) {
        String runningJobNodePath = RunningNode.getRunningJobNodePath(jobName);
        if (registryCenter.isExisted(runningJobNodePath)) {
            registryCenter.remove(runningJobNodePath);
        }
        String runningTaskNodePath = RunningNode.getRunningTaskNodePath(taskId);
        if (registryCenter.isExisted(runningTaskNodePath)) {
            registryCenter.remove(runningTaskNodePath);
        }
    }
    
    /**
     * 判断作业是否运行.
     * 
     * @param jobName 作业名称
     * @return 作业是否运行
     */
    public boolean isRunning(final String jobName) {
        return registryCenter.isExisted(RunningNode.getRunningJobNodePath(jobName));
    }
    
    /**
     * 根据任务主键获取运行中的作业名称.
     * 
     * @param taskId 任务主键
     * @return 运行中的作业名称
     */
    public String getRunningJobName(final String taskId) {
        String runningTaskNode = RunningNode.getRunningTaskNodePath(taskId);
        Preconditions.checkState(registryCenter.isExisted(runningTaskNode));
        return registryCenter.get(runningTaskNode);
    }
}
