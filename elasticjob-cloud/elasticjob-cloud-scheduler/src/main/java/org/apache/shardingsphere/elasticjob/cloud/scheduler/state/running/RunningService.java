/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.cloud.config.pojo.CloudJobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext.MetaInfo;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * Running service.
 */
@RequiredArgsConstructor
public final class RunningService {
    
    private static final int TASK_INITIAL_SIZE = 1024;
    
    // TODO Using JMX to export
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
     * Start running queue service.
     */
    public void start() {
        clear();
        List<String> jobKeys = regCenter.getChildrenKeys(RunningNode.ROOT);
        for (String each : jobKeys) {
            if (!configurationService.load(each).isPresent()) {
                remove(each);
                continue;
            }
            RUNNING_TASKS.put(each, Sets.newCopyOnWriteArraySet(regCenter.getChildrenKeys(RunningNode.getRunningJobNodePath(each)).stream().map(
                input -> TaskContext.from(regCenter.get(RunningNode.getRunningTaskNodePath(MetaInfo.from(input).toString())))).collect(Collectors.toList())));
        }
    }
    
    /**
     * Add task to running queue.
     * 
     * @param taskContext task running context
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
        Optional<CloudJobConfigurationPOJO> cloudJobConfig = configurationService.load(jobName);
        return cloudJobConfig.isPresent() && CloudJobExecutionType.DAEMON == cloudJobConfig.get().getJobExecutionType();
    }
    
    /**
     * Update task to idle state.
     *
     * @param taskContext task running context
     * @param isIdle is idle
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
        return getRunningTasks(taskContext.getMetaInfo().getJobName()).stream().filter(each -> each.equals(taskContext)).findFirst();
    }
    
    /**
     * Remove job from running queue.
     *
     * @param jobName job name
     */
    public void remove(final String jobName) {
        RUNNING_TASKS.remove(jobName);
        if (!isDaemonOrAbsent(jobName)) {
            return;
        }
        regCenter.remove(RunningNode.getRunningJobNodePath(jobName));
    }
        
    /**
     *  Remove task from running queue.
     *
     * @param taskContext task running context
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
        Optional<CloudJobConfigurationPOJO> cloudJobConfigurationOptional = configurationService.load(jobName);
        return !cloudJobConfigurationOptional.isPresent() || CloudJobExecutionType.DAEMON == cloudJobConfigurationOptional.get().getJobExecutionType();
    }
    
    /**
     * Determine whether the job is running or not.
     *
     * @param jobName job name
     * @return true is running, otherwise not
     */
    public boolean isJobRunning(final String jobName) {
        return !getRunningTasks(jobName).isEmpty();
    }
    
    /**
     * Determine whether the task is running or not.
     *
     * @param metaInfo task meta info
     * @return true is running, otherwise not
     */
    public boolean isTaskRunning(final MetaInfo metaInfo) {
        for (TaskContext each : getRunningTasks(metaInfo.getJobName())) {
            if (each.getMetaInfo().equals(metaInfo)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get running tasks by job name.
     *
     * @param jobName job name
     * @return collection of the running tasks
     */
    public Collection<TaskContext> getRunningTasks(final String jobName) {
        Set<TaskContext> taskContexts = new CopyOnWriteArraySet<>();
        Collection<TaskContext> result = RUNNING_TASKS.putIfAbsent(jobName, taskContexts);
        return null == result ? taskContexts : result;
    }
    
    /**
     * Get all running tasks.
     *
     * @return collection of all the running tasks
     */
    public Map<String, Set<TaskContext>> getAllRunningTasks() {
        Map<String, Set<TaskContext>> result = new HashMap<>(RUNNING_TASKS.size(), 1);
        result.putAll(RUNNING_TASKS);
        return result;
    }
    
    /**
     * Get all running daemon tasks.
     *
     * @return collection of all the running daemon tasks
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
     * Add mapping of task primary key and hostname.
     *
     * @param taskId task primary key
     * @param hostname host name
     */
    public void addMapping(final String taskId, final String hostname) {
        TASK_HOSTNAME_MAPPER.putIfAbsent(taskId, hostname);
    }
    
    /**
     * Retrieve the hostname and then remove this task from the mapping.
     *
     * @param taskId task primary key
     * @return the host name of the removed task
     */
    public String popMapping(final String taskId) {
        return TASK_HOSTNAME_MAPPER.remove(taskId);
    }
    
    /**
     * Clear the running status.
     */
    public void clear() {
        RUNNING_TASKS.clear();
        TASK_HOSTNAME_MAPPER.clear();
    }
}
