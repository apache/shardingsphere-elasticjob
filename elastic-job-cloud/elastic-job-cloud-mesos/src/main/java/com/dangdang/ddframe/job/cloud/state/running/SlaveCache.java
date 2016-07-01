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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 记录每个执行机有哪些任务正在运行.
 *
 * @author zhangliang
 */
final class SlaveCache {
    
    private final static ConcurrentHashMap<String, List<ElasticJobTask>> runningTasks = new ConcurrentHashMap<>(128);
    
    private static SlaveCache instance;
    
    private SlaveCache(final CoordinatorRegistryCenter registryCenter) {
        init(registryCenter);
    }
    
    private void init(final CoordinatorRegistryCenter registryCenter) {
        List<String> runningJobNames = registryCenter.getChildrenKeys(RunningTaskNode.ROOT);
        for (String runningJobName : runningJobNames) {
            List<String> runningTaskIds = registryCenter.getChildrenKeys(RunningTaskNode.getRunningJobNodePath(runningJobName));
            for (String runningTaskId : runningTaskIds) {
                String slaveId = registryCenter.get(RunningTaskNode.getRunningTaskNodePath(runningTaskId));
                runningTasks.putIfAbsent(slaveId, new CopyOnWriteArrayList<ElasticJobTask>());
                runningTasks.get(slaveId).add(ElasticJobTask.from(runningTaskId));
            }
        }
    }
    
    static SlaveCache getInstance(final CoordinatorRegistryCenter registryCenter) {
        if (null == instance) {
            synchronized (SlaveCache.class) {
                if (null == instance) {
                    instance = new SlaveCache(registryCenter);
                }
            }
        }
        return instance;
    }
    
    void add(final String slaveId, final ElasticJobTask task) {
        runningTasks.putIfAbsent(slaveId, new CopyOnWriteArrayList<ElasticJobTask>());
        runningTasks.get(slaveId).add(task);
    }
    
    void remove(final String slaveId, final ElasticJobTask task) {
        runningTasks.putIfAbsent(slaveId, new CopyOnWriteArrayList<ElasticJobTask>());
        runningTasks.get(slaveId).remove(task);
    }
    
    List<ElasticJobTask> load(final String slaveId) {
        return runningTasks.get(slaveId);
    }
}
