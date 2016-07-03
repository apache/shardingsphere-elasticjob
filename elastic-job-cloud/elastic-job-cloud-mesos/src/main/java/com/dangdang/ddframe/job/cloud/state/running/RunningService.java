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

import com.dangdang.ddframe.job.cloud.state.ElasticJobTask;
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
     * 通过执行机主键获取运行时任务.
     *
     * @param slaveId 执行机主键
     * @return 运行时任务集合
     */
    public List<ElasticJobTask> load(final String slaveId) {
        return slaveCache.load(slaveId);
    }
    
    /**
     * 将任务放入运行时队列.
     * 
     * @param slaveId 执行机主键
     * @param task 任务对象
     * @return 是否成功加入运行时队列
     */
    public boolean add(final String slaveId, final ElasticJobTask task) {
        String runningTaskNodePath = RunningNode.getRunningTaskNodePath(task.getId());
        if (registryCenter.isExisted(runningTaskNodePath)) {
            return false;
        }
        registryCenter.persist(runningTaskNodePath, slaveId);
        slaveCache.add(slaveId, task);
        return true;
    }
    
    /**
     * 将任务主键从运行时队列删除.
     * 
     * @param slaveId 执行机主键
     * @param task 任务对象
     */
    public void remove(final String slaveId, final ElasticJobTask task) {
        slaveCache.remove(slaveId, task);
        registryCenter.remove(RunningNode.getRunningTaskNodePath(task.getId()));
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
     * 判断作业是否运行.
     *
     * @param task 任务对象
     * @return 任务是否运行
     */
    public boolean isTaskRunning(final ElasticJobTask task) {
        return !registryCenter.getChildrenKeys(RunningNode.getRunningTaskNodePath(task.getId())).isEmpty();
    }
}
