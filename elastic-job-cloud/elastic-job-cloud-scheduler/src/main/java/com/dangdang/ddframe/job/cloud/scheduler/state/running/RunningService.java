/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.scheduler.state.running;

import com.dangdang.ddframe.job.cloud.scheduler.context.TaskContext;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;

/**
 * 任务运行时服务.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public class RunningService {
    
    private final CoordinatorRegistryCenter regCenter;
    
    /**
     * 将任务运行时上下文放入运行时队列.
     * 
     * @param taskContext 任务运行时上下文
     */
    public void add(final TaskContext taskContext) {
        String runningTaskNodePath = RunningNode.getRunningTaskNodePath(taskContext.getMetaInfo().toString());
        if (!regCenter.isExisted(runningTaskNodePath)) {
            regCenter.persist(runningTaskNodePath, taskContext.getId());
        }
    }
    
    /**
     * 更新常驻作业运行状态.
     * @param taskContext 任务运行时上下文
     */
    public void updateDaemonStatus(final TaskContext taskContext, final boolean isIdle) {
        if (!regCenter.isExisted(RunningNode.getRunningTaskNodePath(taskContext.getMetaInfo().toString()))) {
            return;
        }
        if (isIdle) {
            regCenter.persist(RunningNode.getRunningTaskIdleNodePath(taskContext.getMetaInfo().toString()), "");
        } else {
            regCenter.remove(RunningNode.getRunningTaskIdleNodePath(taskContext.getMetaInfo().toString()));
        }
    }
    
    /**
     * 将任务从运行时队列删除.
     * 
     * @param metaInfo 任务元信息
     */
    public void remove(final TaskContext.MetaInfo metaInfo) {
        regCenter.remove(RunningNode.getRunningTaskNodePath(metaInfo.toString()));
        String jobRootNode = RunningNode.getRunningJobNodePath(metaInfo.getJobName());
        if (regCenter.isExisted(jobRootNode) && regCenter.getChildrenKeys(jobRootNode).isEmpty()) {
            regCenter.remove(jobRootNode);
        }
    }
    
    /**
     * 判断作业是否运行.
     *
     * @param jobName 作业名称
     * @return 作业是否运行
     */
    public boolean isJobRunning(final String jobName) {
        return !regCenter.getChildrenKeys(RunningNode.getRunningJobNodePath(jobName)).isEmpty();
    }
    
    /**
     * 判断任务是否运行.
     *
     * @param metaInfo 任务元信息
     * @return 任务是否运行
     */
    public boolean isTaskRunning(final TaskContext.MetaInfo metaInfo) {
        if (!regCenter.isExisted(RunningNode.getRunningJobNodePath(metaInfo.getJobName()))) {
            return false;
        }
        for (String each : regCenter.getChildrenKeys(RunningNode.getRunningJobNodePath(metaInfo.getJobName()))) {
            if (TaskContext.MetaInfo.from(each).getShardingItem() == metaInfo.getShardingItem()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取运行中的任务集合.
     *
     * @param jobName 作业名称
     * @return 运行中的任务集合
     */
    public Collection<TaskContext> getRunningTasks(final String jobName) {
        if (!regCenter.isExisted(RunningNode.getRunningJobNodePath(jobName))) {
            return Collections.emptyList();
        }
        return Lists.transform(regCenter.getChildrenKeys(RunningNode.getRunningJobNodePath(jobName)), new Function<String, TaskContext>() {
            
            @Override
            public TaskContext apply(final String input) {
                return TaskContext.from(regCenter.get(RunningNode.getRunningTaskNodePath(input)));
            }
        });
    }
    
    /**
     * 清理所有运行时状态.
     */
    public void clear() {
        regCenter.remove(RunningNode.ROOT);
    }
}
