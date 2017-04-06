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

import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobExecutionType;
import com.dangdang.ddframe.job.context.TaskContext;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 任务运行时服务.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class RunningService {
    
    private static final int TASK_INITIAL_SIZE = 1024;
    
    // TODO 使用JMX导出
    @Getter
    private static final ConcurrentHashMap<String, Set<TaskContext>> RUNNING_TASKS = new ConcurrentHashMap<>(TASK_INITIAL_SIZE);
    
    private static final ConcurrentHashMap<String, String> TASK_HOSTNAME_MAPPER = new ConcurrentHashMap<>(TASK_INITIAL_SIZE);
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final CloudJobConfigurationService configurationService;
    
    public RunningService(final CoordinatorRegistryCenter regCenter) {
        this.regCenter = regCenter;
        this.configurationService = new CloudJobConfigurationService(regCenter);
    }
    
    /**
     * 启动任务运行队列.
     */
    public void start() {
        clear();
        List<String> jobKeys = regCenter.getChildrenKeys(RunningNode.ROOT);
        for (String each : jobKeys) {
            if (!configurationService.load(each).isPresent()) {
                remove(each);
                continue;
            }
            RUNNING_TASKS.put(each, Sets.newCopyOnWriteArraySet(Lists.transform(regCenter.getChildrenKeys(RunningNode.getRunningJobNodePath(each)), new Function<String, TaskContext>() {
                
                @Override
                public TaskContext apply(final String input) {
                    return TaskContext.from(regCenter.get(RunningNode.getRunningTaskNodePath(TaskContext.MetaInfo.from(input).toString())));
                }
            })));
        }
    }
    
    /**
     * 将任务运行时上下文放入运行时队列.
     * 
     * @param taskContext 任务运行时上下文
     */
    public void add(final TaskContext taskContext) {
        if (!configurationService.load(taskContext.getMetaInfo().getJobName()).isPresent()) {
            return;
        }
        getRunningTasks(taskContext.getMetaInfo().getJobName()).add(taskContext);
        if (!isDaemon(taskContext.getMetaInfo().getJobName())) {
            return;
        }
        String runningTaskNodePath = RunningNode.getRunningTaskNodePath(taskContext.getMetaInfo().toString());
        if (!regCenter.isExisted(runningTaskNodePath)) {
            regCenter.persist(runningTaskNodePath, taskContext.getId());
        }
    }
    
    private boolean isDaemon(final String jobName) {
        Optional<CloudJobConfiguration> cloudJobConfigurationOptional = configurationService.load(jobName);
        return cloudJobConfigurationOptional.isPresent() && CloudJobExecutionType.DAEMON == cloudJobConfigurationOptional.get().getJobExecutionType();
    }
    
    /**
     * 更新作业闲置状态.
     * @param taskContext 任务运行时上下文
     * @param isIdle 是否闲置
     */
    public void updateIdle(final TaskContext taskContext, final boolean isIdle) {
        synchronized (RUNNING_TASKS) {
            Optional<TaskContext> taskContextOptional = findTask(taskContext);
            if (taskContextOptional.isPresent()) {
                taskContextOptional.get().setIdle(isIdle);
            } else {
                add(taskContext);
            }
        }
    }
    
    private Optional<TaskContext> findTask(final TaskContext taskContext) {
        return Iterators.tryFind(getRunningTasks(taskContext.getMetaInfo().getJobName()).iterator(), new Predicate<TaskContext>() {
            @Override
            public boolean apply(final TaskContext input) {
                return input.equals(taskContext);
            }
        });
    }
    
    /**
     * 将作业从运行时队列删除.
     *
     * @param jobName 作业名称
     */
    public void remove(final String jobName) {
        RUNNING_TASKS.remove(jobName);
        if (!isDaemonOrAbsent(jobName)) {
            return;
        }
        regCenter.remove(RunningNode.getRunningJobNodePath(jobName));
    }
        
    /**
     * 将任务从运行时队列删除.
     * 
     * @param taskContext 任务运行时上下文
     */
    public void remove(final TaskContext taskContext) {
        getRunningTasks(taskContext.getMetaInfo().getJobName()).remove(taskContext);
        if (!isDaemonOrAbsent(taskContext.getMetaInfo().getJobName())) {
            return;
        }
        regCenter.remove(RunningNode.getRunningTaskNodePath(taskContext.getMetaInfo().toString()));
        String jobRootNode = RunningNode.getRunningJobNodePath(taskContext.getMetaInfo().getJobName());
        if (regCenter.isExisted(jobRootNode) && regCenter.getChildrenKeys(jobRootNode).isEmpty()) {
            regCenter.remove(jobRootNode);
        }
    }
    
    private boolean isDaemonOrAbsent(final String jobName) {
        Optional<CloudJobConfiguration> cloudJobConfigurationOptional = configurationService.load(jobName);
        return !cloudJobConfigurationOptional.isPresent() || CloudJobExecutionType.DAEMON == cloudJobConfigurationOptional.get().getJobExecutionType();
    }
    
    /**
     * 判断作业是否运行.
     *
     * @param jobName 作业名称
     * @return 作业是否运行
     */
    public boolean isJobRunning(final String jobName) {
        return !getRunningTasks(jobName).isEmpty();
    }
    
    /**
     * 判断任务是否运行.
     *
     * @param metaInfo 任务元信息
     * @return 任务是否运行
     */
    public boolean isTaskRunning(final TaskContext.MetaInfo metaInfo) {
        for (TaskContext each : getRunningTasks(metaInfo.getJobName())) {
            if (each.getMetaInfo().equals(metaInfo)) {
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
        Set<TaskContext> taskContexts = new CopyOnWriteArraySet<>();
        Collection<TaskContext> result = RUNNING_TASKS.putIfAbsent(jobName, taskContexts);
        return null == result ? taskContexts : result;
    }
    
    /**
     * 获取运行中的全部任务.
     *
     * @return 运行中的全部任务
     */
    public Map<String, Set<TaskContext>> getAllRunningTasks() {
        Map<String, Set<TaskContext>> result = new HashMap<>(RUNNING_TASKS.size(), 1);
        result.putAll(RUNNING_TASKS);
        return result;
    }
    
    /**
     * 获取所有的运行中的常驻作业.
     * 
     * @return 运行中常驻作业集合
     */
    public Set<TaskContext> getAllRunningDaemonTasks() {
        List<String> jobKeys = regCenter.getChildrenKeys(RunningNode.ROOT);
        for (String each : jobKeys) {
            if (!RUNNING_TASKS.containsKey(each)) {
                remove(each);
            }
        }
        Set<TaskContext> result = Sets.newHashSet();
        for (Map.Entry<String, Set<TaskContext>> each : RUNNING_TASKS.entrySet()) {
            if (isDaemonOrAbsent(each.getKey())) {
                result.addAll(each.getValue());
            }
        }
        return result;
    }
    
    /**
     * 添加任务主键和主机名称的映射.
     *
     * @param taskId 任务主键
     * @param hostname 主机名称
     */
    public void addMapping(final String taskId, final String hostname) {
        TASK_HOSTNAME_MAPPER.putIfAbsent(taskId, hostname);
    }
    
    /**
     * 根据任务主键获取主机名称并清除该任务.
     *
     * @param taskId 任务主键
     * @return 删除任务的主机名称
     */
    public String popMapping(final String taskId) {
        return TASK_HOSTNAME_MAPPER.remove(taskId);
    }
    
    /**
     * 清理所有运行时状态.
     */
    public void clear() {
        RUNNING_TASKS.clear();
        TASK_HOSTNAME_MAPPER.clear();
    }
}
