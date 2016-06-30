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

import com.dangdang.ddframe.job.cloud.task.ElasticJobTask;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
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
     * 将任务放入运行时队列.
     * 
     * @param task 任务对象
     * @return 是否成功加入运行时队列
     */
    public boolean add(final String slaveId, final ElasticJobTask task) {
        String runningTaskNodePath = RunningTaskNode.getRunningTaskNodePath(getSlaveIdWithoutSequence(slaveId), task.getId());
        if (registryCenter.isExisted(runningTaskNodePath)) {
            return false;
        }
        registryCenter.persist(runningTaskNodePath, "");
        return true;
    }
    
    /**
     * 将任务主键从运行时队列删除.
     * 
     * @param slaveId 执行机主键
     * @param task 任务对象
     */
    public void remove(final String slaveId, final ElasticJobTask task) {
        registryCenter.remove(RunningTaskNode.getRunningTaskNodePath(getSlaveIdWithoutSequence(slaveId), task.getId()));
    }
    
    /**
     * 通过执行机主键获取运行时任务.
     * 
     * @param slaveId 执行机主键
     * @return 运行时任务集合
     */
    public List<ElasticJobTask> load(final String slaveId) {
        String runningSlaveNodePath = RunningTaskNode.getRunningSlaveNodePath(getSlaveIdWithoutSequence(slaveId));
        return registryCenter.isExisted(runningSlaveNodePath) ? Lists.transform(registryCenter.getChildrenKeys(runningSlaveNodePath), new Function<String, ElasticJobTask>() {
            
            @Override
            public ElasticJobTask apply(final String input) {
                return ElasticJobTask.from(input);
            }
        }) : Collections.<ElasticJobTask>emptyList();
    }
    
    private String getSlaveIdWithoutSequence(final String slaveId) {
        return slaveId.substring(0, slaveId.lastIndexOf("-"));
    }
}
