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

import com.dangdang.ddframe.job.cloud.TaskContext;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;

import java.util.List;

/**
 * 任务运行时服务.
 *
 * @author zhangliang
 */
public class RunningService {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    private final SlaveCache slaveCache;
    
    public RunningService(final CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
        slaveCache = SlaveCache.getInstance(registryCenter);
    }
    
    /**
     * 通过执行机主键获取任务运行时上下文.
     *
     * @param slaveId 执行机主键
     * @return 任务运行时上下文集合
     */
    public List<TaskContext> load(final String slaveId) {
        return slaveCache.load(slaveId);
    }
    
    /**
     * 将任务运行时上下文放入运行时队列.
     * 
     * @param slaveId 执行机主键
     * @param taskContext 任务运行时上下文
     */
    public void add(final String slaveId, final TaskContext taskContext) {
        String runningTaskNodePath = RunningNode.getRunningTaskNodePath(taskContext.getId());
        if (!registryCenter.isExisted(runningTaskNodePath)) {
            registryCenter.persist(runningTaskNodePath, slaveId);
            slaveCache.add(slaveId, taskContext);
        }
    }
    
    /**
     * 将任务运行时上下文从队列删除.
     * 
     * @param slaveId 执行机主键
     * @param taskContext 任务运行时上下文
     */
    public void remove(final String slaveId, final TaskContext taskContext) {
        slaveCache.remove(slaveId, taskContext);
        registryCenter.remove(RunningNode.getRunningTaskNodePath(taskContext.getId()));
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
        return registryCenter.isExisted(RunningNode.getRunningTaskNodePath(taskContext.getId()));
    }
    
    /**
     * 清理所有运行时状态.
     */
    public void clear() {
        registryCenter.remove(RunningNode.ROOT);
    }
}
