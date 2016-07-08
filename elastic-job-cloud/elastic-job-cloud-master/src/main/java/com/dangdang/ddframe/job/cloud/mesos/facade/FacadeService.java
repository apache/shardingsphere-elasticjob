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

package com.dangdang.ddframe.job.cloud.mesos.facade;

import com.dangdang.ddframe.job.cloud.JobContext;
import com.dangdang.ddframe.job.cloud.TaskContext;
import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.producer.TaskProducerSchedulerRegistry;
import com.dangdang.ddframe.job.cloud.state.failover.FailoverService;
import com.dangdang.ddframe.job.cloud.state.misfired.MisfiredService;
import com.dangdang.ddframe.job.cloud.state.ready.ReadyService;
import com.dangdang.ddframe.job.cloud.state.running.RunningService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 为Mesos提供的门面服务.
 *
 * @author zhangliang
 */
public class FacadeService {
    
    private final ConfigurationService configService;
    
    private final ReadyService readyService;
    
    private final RunningService runningService;
    
    private final FailoverService failoverService;
    
    private final MisfiredService misfiredService;
    
    private final TaskProducerSchedulerRegistry taskProducerSchedulerRegistry;
    
    public FacadeService(final CoordinatorRegistryCenter registryCenter) {
        configService = new ConfigurationService(registryCenter);
        readyService = new ReadyService(registryCenter);
        runningService = new RunningService(registryCenter);
        failoverService = new FailoverService(registryCenter);
        misfiredService = new MisfiredService(registryCenter);
        taskProducerSchedulerRegistry = TaskProducerSchedulerRegistry.getInstance(registryCenter);
    }
    
    /**
     * 框架启动.
     */
    public void start() {
        runningService.clear();
        taskProducerSchedulerRegistry.registerFromRegistryCenter();
    }
    
    /**
     * 获取有资格运行的作业.
     * 
     * @return 有资格运行的作业集合上下文
     */
    public EligibleJobContext getEligibleJobContext() {
        Collection<JobContext> failoverJobContexts = failoverService.getAllEligibleJobContexts();
        Map<String, JobContext> misfiredJobContexts = misfiredService.getAllEligibleJobContexts(failoverJobContexts);
        Collection<JobContext> ineligibleJobContexts = new ArrayList<>(failoverJobContexts.size() + misfiredJobContexts.size());
        ineligibleJobContexts.addAll(failoverJobContexts);
        ineligibleJobContexts.addAll(misfiredJobContexts.values());
        Map<String, JobContext> readyJobContexts = readyService.getAllEligibleJobContexts(ineligibleJobContexts);
        return new EligibleJobContext(failoverJobContexts, misfiredJobContexts, readyJobContexts);
    }
    
    /**
     * 从队列中删除已运行的作业.
     * 
     * @param assignedTaskContext 分配完成的任务集合上下文
     */
    public void removeLaunchTasksFromQueue(final AssignedTaskContext assignedTaskContext) {
        failoverService.remove(assignedTaskContext.getFailoverTaskContexts());
        misfiredService.remove(assignedTaskContext.getMisfiredJobNames());
        readyService.remove(assignedTaskContext.getReadyJobNames());
    }
    
    /**
     * 将任务运行时上下文放入运行时队列.
     *
     * @param slaveId 执行机主键
     * @param taskContext 任务运行时上下文
     */
    public void addRunning(final String slaveId, final TaskContext taskContext) {
        runningService.add(slaveId, taskContext);
    }
    
    /**
     * 将任务运行时上下文从队列删除.
     *
     * @param slaveId 执行机主键
     * @param taskContext 任务运行时上下文
     */
    public void removeRunning(final String slaveId, final TaskContext taskContext) {
        runningService.remove(slaveId, taskContext);
    }
    
    /**
     * 记录失效转移队列.
     *
     * @param slaveId 执行机主键
     */
    public void recordFailoverTasks(final String slaveId) {
        List<TaskContext> runningTaskContexts = runningService.load(slaveId);
        for (TaskContext each : runningTaskContexts) {
            recordFailoverTask(slaveId, each);
        }
    }
    
    /**
     * 记录失效转移队列.
     * 
     * @param slaveId 执行机主键
     * @param taskContext 任务上下文
     */
    public void recordFailoverTask(final String slaveId, final TaskContext taskContext) {
        Optional<CloudJobConfiguration> jobConfig = configService.load(taskContext.getJobName());
        if (jobConfig.isPresent() && jobConfig.get().isFailover()) {
            failoverService.add(taskContext);
        }
        runningService.remove(slaveId, taskContext);
    }
    
    /**
     * 框架停止.
     */
    public void stop() {
        // TODO 停止作业调度
        runningService.clear();
    }
}
