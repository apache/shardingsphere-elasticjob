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

package com.dangdang.ddframe.job.mesos.facade;

import com.dangdang.ddframe.job.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.config.ConfigurationService;
import com.dangdang.ddframe.job.context.JobContext;
import com.dangdang.ddframe.job.context.TaskContext;
import com.dangdang.ddframe.job.producer.TaskProducerSchedulerRegistry;
import com.dangdang.ddframe.job.state.failover.FailoverService;
import com.dangdang.ddframe.job.state.misfired.MisfiredService;
import com.dangdang.ddframe.job.state.ready.ReadyService;
import com.dangdang.ddframe.job.state.running.RunningService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

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
     * @return 作业上下文集合
     */
    public Collection<JobContext> getEligibleJobContext() {
        Collection<JobContext> failoverJobContexts = failoverService.getAllEligibleJobContexts();
        Collection<JobContext> misfiredJobContexts = misfiredService.getAllEligibleJobContexts(failoverJobContexts);
        Collection<JobContext> ineligibleJobContexts = new ArrayList<>(failoverJobContexts.size() + misfiredJobContexts.size());
        ineligibleJobContexts.addAll(failoverJobContexts);
        ineligibleJobContexts.addAll(misfiredJobContexts);
        Collection<JobContext> readyJobContexts = readyService.getAllEligibleJobContexts(ineligibleJobContexts);
        Collection<JobContext> result = new ArrayList<>(failoverJobContexts.size() + misfiredJobContexts.size() + readyJobContexts.size());
        result.addAll(failoverJobContexts);
        result.addAll(misfiredJobContexts);
        result.addAll(readyJobContexts);
        return result;
    }
    
    /**
     * 从队列中删除已运行的作业.
     * 
     * @param taskContexts 任务上下文集合
     */
    public void removeLaunchTasksFromQueue(final Collection<TaskContext> taskContexts) {
        Collection<TaskContext> failoverTaskContexts = new ArrayList<>(taskContexts.size());
        Collection<String> misfiredJobNames = new HashSet<>(taskContexts.size(), 1);
        Collection<String> readyJobNames = new HashSet<>(taskContexts.size(), 1);
        for (TaskContext each : taskContexts) {
            switch (each.getType()) {
                case FAILOVER:
                    failoverTaskContexts.add(each);
                    break;
                case MISFIRED:
                    misfiredJobNames.add(each.getJobName());
                    break;
                case READY:
                    readyJobNames.add(each.getJobName());
                    break;
                default:
                    break;
            }
        }
        failoverService.remove(failoverTaskContexts);
        misfiredService.remove(misfiredJobNames);
        readyService.remove(readyJobNames);
    }
    
    /**
     * 将任务运行时上下文放入运行时队列.
     *
     * @param taskContext 任务运行时上下文
     */
    public void addRunning(final TaskContext taskContext) {
        runningService.add(taskContext);
    }
    
    /**
     * 将任务运行时上下文从队列删除.
     *
     * @param taskContext 任务运行时上下文
     */
    public void removeRunning(final TaskContext taskContext) {
        runningService.remove(taskContext);
    }
    
    /**
     * 记录失效转移队列.
     * 
     * @param taskContext 任务上下文
     */
    public void recordFailoverTask(final TaskContext taskContext) {
        Optional<CloudJobConfiguration> jobConfig = configService.load(taskContext.getJobName());
        if (jobConfig.isPresent() && jobConfig.get().isFailover()) {
            failoverService.add(taskContext);
        }
        runningService.remove(taskContext);
    }
    
    /**
     * 框架停止.
     */
    public void stop() {
        // TODO 停止作业调度
        runningService.clear();
    }
}
