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

package com.dangdang.ddframe.job.cloud.task.running;

import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 任务运行时服务.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public class RunningTaskService {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    /**
     * 将任务主键放入运行时队列.
     * 
     * @param taskId 任务主键
     * @return 是否加入运行时队列
     */
    public boolean add(final String slaveId, final String taskId) {
        String runningTaskNodePath = RunningTaskNode.getRunningTaskNodePath(getSlaveIdWithoutSequence(slaveId), taskId);
        if (registryCenter.isExisted(runningTaskNodePath)) {
            return false;
        }
        registryCenter.persist(runningTaskNodePath, "");
        return true;
    }
    
    /**
     * 将任务主键从运行时队列删除.
     */
    public void remove(final String slaveId, final String taskId) {
        registryCenter.remove(RunningTaskNode.getRunningTaskNodePath(getSlaveIdWithoutSequence(slaveId), taskId));
    }
    
    /**
     * 通过执行机主键获取运行时任务.
     * 
     * @param slaveId 执行机主键
     * @return 运行时任务
     */
    public List<String> load(final String slaveId) {
        String runningSlaveNodePath = RunningTaskNode.getRunningSlaveNodePath(getSlaveIdWithoutSequence(slaveId));
        return registryCenter.isExisted(runningSlaveNodePath) ? registryCenter.getChildrenKeys(runningSlaveNodePath) : Collections.<String>emptyList();
    }
    
    private String getSlaveIdWithoutSequence(final String slaveId) {
        return slaveId.substring(0, slaveId.lastIndexOf("-"));
    }
}
