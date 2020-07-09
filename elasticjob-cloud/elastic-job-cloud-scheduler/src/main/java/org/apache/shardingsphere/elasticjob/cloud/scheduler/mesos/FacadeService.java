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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos;

import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.CloudAppConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.app.DisableAppService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.job.DisableJobService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running.RunningService;
import org.apache.shardingsphere.elasticjob.cloud.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.CloudAppConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.context.JobContext;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.failover.FailoverService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.failover.FailoverTaskInfo;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.ready.ReadyService;
import org.apache.shardingsphere.elasticjob.cloud.context.TaskContext;
import org.apache.shardingsphere.elasticjob.cloud.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mesos facade service.
 */
@Slf4j
public final class FacadeService {
    
    private final CloudAppConfigurationService appConfigService;
    
    private final CloudJobConfigurationService jobConfigService;
    
    private final ReadyService readyService;
    
    private final RunningService runningService;
    
    private final FailoverService failoverService;
    
    private final DisableAppService disableAppService;
    
    private final DisableJobService disableJobService;
    
    private final MesosStateService mesosStateService;
    
    public FacadeService(final CoordinatorRegistryCenter regCenter) {
        appConfigService = new CloudAppConfigurationService(regCenter);
        jobConfigService = new CloudJobConfigurationService(regCenter);
        readyService = new ReadyService(regCenter);
        runningService = new RunningService(regCenter);
        failoverService = new FailoverService(regCenter);
        disableAppService = new DisableAppService(regCenter);
        disableJobService = new DisableJobService(regCenter);
        mesosStateService = new MesosStateService(regCenter);
    }
    
    /**
     * Start facade service.
     */
    public void start() {
        log.info("Elastic Job: Start facade service");
        runningService.start();
    }
    
    /**
     * Get eligible job.
     * 
     * @return collection of eligible job context
     */
    public Collection<JobContext> getEligibleJobContext() {
        Collection<JobContext> failoverJobContexts = failoverService.getAllEligibleJobContexts();
        Collection<JobContext> readyJobContexts = readyService.getAllEligibleJobContexts(failoverJobContexts);
        Collection<JobContext> result = new ArrayList<>(failoverJobContexts.size() + readyJobContexts.size());
        result.addAll(failoverJobContexts);
        result.addAll(readyJobContexts);
        return result;
    }
    
    /**
     * Remove launched task from queue.
     * 
     * @param taskContexts task running contexts
     */
    public void removeLaunchTasksFromQueue(final List<TaskContext> taskContexts) {
        List<TaskContext> failoverTaskContexts = new ArrayList<>(taskContexts.size());
        Collection<String> readyJobNames = new HashSet<>(taskContexts.size(), 1);
        for (TaskContext each : taskContexts) {
            switch (each.getType()) {
                case FAILOVER:
                    failoverTaskContexts.add(each);
                    break;
                case READY:
                    readyJobNames.add(each.getMetaInfo().getJobName());
                    break;
                default:
                    break;
            }
        }
        failoverService.remove(Lists.transform(failoverTaskContexts, new Function<TaskContext, TaskContext.MetaInfo>() {
            
            @Override
            public TaskContext.MetaInfo apply(final TaskContext input) {
                return input.getMetaInfo();
            }
        }));
        readyService.remove(readyJobNames);
    }
    
    /**
     * Add task to running queue.
     *
     * @param taskContext task running context
     */
    public void addRunning(final TaskContext taskContext) {
        runningService.add(taskContext);
    }
    
    /**
     * Update daemon task status.
     * 
     * @param taskContext task running context
     * @param isIdle set to idle or not
     */
    public void updateDaemonStatus(final TaskContext taskContext, final boolean isIdle) {
        runningService.updateIdle(taskContext, isIdle);
    }
    
    /**
     * Remove task from running queue.
     *
     * @param taskContext task running context
     */
    public void removeRunning(final TaskContext taskContext) {
        runningService.remove(taskContext);
    }
    
    /**
     * Record task to failover queue.
     * 
     * @param taskContext task running context
     */
    public void recordFailoverTask(final TaskContext taskContext) {
        Optional<CloudJobConfiguration> jobConfigOptional = jobConfigService.load(taskContext.getMetaInfo().getJobName());
        if (!jobConfigOptional.isPresent()) {
            return;
        }
        if (isDisable(jobConfigOptional.get())) {
            return;
        }
        CloudJobConfiguration jobConfig = jobConfigOptional.get();
        if (jobConfig.getTypeConfig().getCoreConfig().isFailover() || CloudJobExecutionType.DAEMON == jobConfig.getJobExecutionType()) {
            failoverService.add(taskContext);
        }
    }
    
    private boolean isDisable(final CloudJobConfiguration jobConfiguration) {
        return disableAppService.isDisabled(jobConfiguration.getAppName()) || disableJobService.isDisabled(jobConfiguration.getJobName());
    }
    
    /**
     * Add transient job to ready queue.
     *
     * @param jobName job name
     */
    public void addTransient(final String jobName) {
        readyService.addTransient(jobName);
    }
    
    /**
     * Load cloud job config.
     *
     * @param jobName job name
     * @return cloud job config
     */
    public Optional<CloudJobConfiguration> load(final String jobName) {
        return jobConfigService.load(jobName);
    }
    
    /**
     * Load app config by app name.
     *
     * @param appName app name
     * @return cloud app config
     */
    public Optional<CloudAppConfiguration> loadAppConfig(final String appName) {
        return appConfigService.load(appName);
    }
    
    /**
     * Get failover task id by task meta info.
     *
     * @param metaInfo task meta info
     * @return failover task id
     */
    public Optional<String> getFailoverTaskId(final TaskContext.MetaInfo metaInfo) {
        return failoverService.getTaskId(metaInfo);
    }
    
    /**
     * Add daemon job to ready queue.
     *
     * @param jobName job name
     */
    public void addDaemonJobToReadyQueue(final String jobName) {
        Optional<CloudJobConfiguration> jobConfigOptional = jobConfigService.load(jobName);
        if (!jobConfigOptional.isPresent()) {
            return;
        }
        if (isDisable(jobConfigOptional.get())) {
            return;
        }
        readyService.addDaemon(jobName);
    }
    
    /**
     * Determine whether the task is running or not.
     *
     * @param taskContext task running context
     * @return true is running, otherwise not
     */
    public boolean isRunning(final TaskContext taskContext) {
        return ExecutionType.FAILOVER != taskContext.getType() && !runningService.getRunningTasks(taskContext.getMetaInfo().getJobName()).isEmpty()
                || ExecutionType.FAILOVER == taskContext.getType() && runningService.isTaskRunning(taskContext.getMetaInfo());
    }
    
    /**
     * Add mapping of the task primary key and host name.
     *
     * @param taskId task primary key
     * @param hostname host name
     */
    public void addMapping(final String taskId, final String hostname) {
        runningService.addMapping(taskId, hostname);
    }
    
    /**
     * Retrieve hostname and then remove task.
     *
     * @param taskId task primary key
     * @return hostname of the removed task
     */
    public String popMapping(final String taskId) {
        return runningService.popMapping(taskId);
    }
    
    /**
     * Get all ready tasks.
     *
     * @return ready tasks
     */
    public Map<String, Integer> getAllReadyTasks() {
        return readyService.getAllReadyTasks();
    }
    
    /**
     * Get all running tasks.
     *
     * @return running tasks
     */
    public Map<String, Set<TaskContext>> getAllRunningTasks() {
        return runningService.getAllRunningTasks();
    }
    
    /**
     * Get all failover tasks.
     *
     * @return failover tasks
     */
    public Map<String, Collection<FailoverTaskInfo>> getAllFailoverTasks() {
        return failoverService.getAllFailoverTasks();
    }
    
    /**
     * Determine whether the job is disable or not.
     * 
     * @param jobName job name
     * @return true is disabled, otherwise not
     */
    public boolean isJobDisabled(final String jobName) {
        Optional<CloudJobConfiguration> jobConfiguration = jobConfigService.load(jobName);
        return !jobConfiguration.isPresent() || disableAppService.isDisabled(jobConfiguration.get().getAppName()) || disableJobService.isDisabled(jobName);
    }
    
    /**
     * Enable job.
     *
     * @param jobName job name
     */
    public void enableJob(final String jobName) {
        disableJobService.remove(jobName);
    }
    
    /**
     * Disable job.
     *
     * @param jobName job name
     */
    public void disableJob(final String jobName) {
        disableJobService.add(jobName);
    }
    
    /**
     * Get all running executor info.
     * 
     * @return collection of executor info
     * @throws JSONException json exception
     */
    public Collection<MesosStateService.ExecutorStateInfo> loadExecutorInfo() throws JSONException {
        return mesosStateService.executors();
    }
    
    /**
     * Stop facade service.
     */
    public void stop() {
        log.info("Elastic Job: Stop facade service");
        // TODO stop scheduler
        runningService.clear();
    }
}
