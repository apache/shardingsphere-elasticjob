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

package com.dangdang.ddframe.job.cloud.mesos;

import com.dangdang.ddframe.job.cloud.JobContext;
import com.dangdang.ddframe.job.cloud.TaskContext;
import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.mesos.stragety.ExhaustFirstResourceAllocateStrategy;
import com.dangdang.ddframe.job.cloud.mesos.stragety.ResourceAllocateStrategy;
import com.dangdang.ddframe.job.cloud.schedule.CloudTaskSchedulerRegistry;
import com.dangdang.ddframe.job.cloud.state.failover.FailoverService;
import com.dangdang.ddframe.job.cloud.state.misfired.MisfiredService;
import com.dangdang.ddframe.job.cloud.state.ready.ReadyService;
import com.dangdang.ddframe.job.cloud.state.running.RunningService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 作业云引擎.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SchedulerEngine implements Scheduler {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    private final ConfigurationService configService;
    
    private final ReadyService readyService;
    
    private final RunningService runningService;
    
    private final FailoverService failoverService;
    
    private final MisfiredService misfiredService;
    
    public SchedulerEngine(final CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
        configService = new ConfigurationService(registryCenter);
        readyService = new ReadyService(registryCenter);
        runningService = new RunningService(registryCenter);
        failoverService = new FailoverService(registryCenter);
        misfiredService = new MisfiredService(registryCenter);
    }
    
    @Override
    public void registered(final SchedulerDriver schedulerDriver, final Protos.FrameworkID frameworkID, final Protos.MasterInfo masterInfo) {
        runningService.clear();
        CloudTaskSchedulerRegistry.getInstance(registryCenter).registerFromRegistryCenter();
    }
    
    @Override
    public void reregistered(final SchedulerDriver schedulerDriver, final Protos.MasterInfo masterInfo) {
        runningService.clear();
        CloudTaskSchedulerRegistry.getInstance(registryCenter).registerFromRegistryCenter();
    }
    
    @Override
    public void resourceOffers(final SchedulerDriver schedulerDriver, final List<Protos.Offer> offers) {
        ResourceAllocateStrategy resourceAllocateStrategy = new ExhaustFirstResourceAllocateStrategy(getHardwareResource(offers));
        Collection<JobContext> failoverJobContexts = failoverService.getAllEligibleJobContexts();
        Map<String, JobContext> misfiredJobContexts = misfiredService.getAllEligibleJobContexts(failoverJobContexts);
        Collection<JobContext> ineligibleJobContexts = new ArrayList<>(failoverJobContexts.size() + misfiredJobContexts.size());
        ineligibleJobContexts.addAll(failoverJobContexts);
        ineligibleJobContexts.addAll(misfiredJobContexts.values());
        Map<String, JobContext> readyJobContexts = readyService.getAllEligibleJobContexts(ineligibleJobContexts);
        List<Protos.TaskInfo> failoverTaskInfoList = resourceAllocateStrategy.allocate(failoverJobContexts);
        Map<String, List<Protos.TaskInfo>> misfiredTaskInfoMap = resourceAllocateStrategy.allocate(misfiredJobContexts);
        Map<String, List<Protos.TaskInfo>> readyTaskInfoMap = resourceAllocateStrategy.allocate(readyJobContexts);
        List<Protos.TaskInfo> taskInfoList = getTaskInfoList(failoverTaskInfoList, misfiredTaskInfoMap, readyTaskInfoMap);
        declineUnusedOffers(schedulerDriver, offers, taskInfoList);
        launchTasks(schedulerDriver, offers, taskInfoList);
        removeLaunchTasksFromQueue(failoverTaskInfoList, misfiredTaskInfoMap, readyTaskInfoMap);
    }
    
    private List<HardwareResource> getHardwareResource(final List<Protos.Offer> offers) {
        return Lists.transform(offers, new Function<Protos.Offer, HardwareResource>() {
            
            @Override
            public HardwareResource apply(final Protos.Offer input) {
                return new HardwareResource(input);
            }
        });
    }
    
    private List<Protos.TaskInfo> getTaskInfoList(final List<Protos.TaskInfo> failoverTaskInfoList, 
                                                  final Map<String, List<Protos.TaskInfo>> misfiredTaskInfoMap, final Map<String, List<Protos.TaskInfo>> readyTaskInfoMap) {
        List<Protos.TaskInfo> result = new ArrayList<>(failoverTaskInfoList.size() + misfiredTaskInfoMap.size() + readyTaskInfoMap.size());
        result.addAll(failoverTaskInfoList);
        for (List<Protos.TaskInfo> each : misfiredTaskInfoMap.values()) {
            result.addAll(each);
        }
        for (List<Protos.TaskInfo> each : readyTaskInfoMap.values()) {
            result.addAll(each);
        }
        return result;
    }
    
    private void declineUnusedOffers(final SchedulerDriver schedulerDriver, final List<Protos.Offer> offers, final List<Protos.TaskInfo> tasks) {
        for (Protos.Offer each : offers) {
            if (!isUsed(each, tasks)) {
                schedulerDriver.declineOffer(each.getId());
            }
        }
    }
    
    private boolean isUsed(final Protos.Offer offer, final List<Protos.TaskInfo> tasks) {
        for (Protos.TaskInfo each : tasks) {
            if (offer.getSlaveId().getValue().equals(each.getSlaveId().getValue())) {
                return true;
            }
        }
        return false;
    }
    
    private void launchTasks(final SchedulerDriver schedulerDriver, final List<Protos.Offer> offers, final List<Protos.TaskInfo> tasks) {
        schedulerDriver.launchTasks(Lists.transform(offers, new Function<Protos.Offer, Protos.OfferID>() {
            
            @Override
            public Protos.OfferID apply(final Protos.Offer input) {
                return input.getId();
            }
        }), tasks);
        // TODO 状态回调调整好, 这里的代码应删除
        for (Protos.TaskInfo each : tasks) {
            runningService.add(each.getSlaveId().getValue(), TaskContext.from(each.getTaskId().getValue()));
        }
    }
    
    private void removeLaunchTasksFromQueue(final List<Protos.TaskInfo> failoverTaskInfoList, 
                                            final Map<String, List<Protos.TaskInfo>> misfiredTaskInfoMap, final Map<String, List<Protos.TaskInfo>> readyTaskInfoMap) {
        failoverService.remove(Lists.transform(failoverTaskInfoList, new Function<Protos.TaskInfo, TaskContext>() {
            
            @Override
            public TaskContext apply(final Protos.TaskInfo input) {
                return TaskContext.from(input.getTaskId().getValue());
            }
        }));
        misfiredService.remove(misfiredTaskInfoMap.keySet());
        readyService.remove(readyTaskInfoMap.keySet());
    }
    
    @Override
    public void offerRescinded(final SchedulerDriver schedulerDriver, final Protos.OfferID offerID) {
    }
    
    @Override
    // TODO 状态返回不正确,不能正确记录状态,导致不能failover, 目前先放在resourceOffers实现
    public void statusUpdate(final SchedulerDriver schedulerDriver, final Protos.TaskStatus taskStatus) {
        String taskId = taskStatus.getTaskId().getValue();
        TaskContext taskContext = TaskContext.from(taskId);
        switch (taskStatus.getState()) {
            case TASK_STARTING:
                runningService.add(taskStatus.getSlaveId().getValue(), taskContext);
                break;
            case TASK_FINISHED:
            case TASK_FAILED:
            case TASK_KILLED:
            case TASK_LOST:
                runningService.remove(taskStatus.getSlaveId().getValue(), taskContext);
                break;
            default:
                break;
        }
    }
    
    @Override
    public void frameworkMessage(final SchedulerDriver schedulerDriver, final Protos.ExecutorID executorID, final Protos.SlaveID slaveID, final byte[] bytes) {
    }
    
    @Override
    public void disconnected(final SchedulerDriver schedulerDriver) {
        // TODO 停止作业调度
        runningService.clear();
    }
    
    @Override
    public void slaveLost(final SchedulerDriver schedulerDriver, final Protos.SlaveID slaveID) {
        List<TaskContext> runningTaskContexts = runningService.load(slaveID.getValue());
        for (TaskContext each : runningTaskContexts) {
            Optional<CloudJobConfiguration> jobConfig = configService.load(each.getJobName());
            if (jobConfig.isPresent() && jobConfig.get().isFailover()) {
                failoverService.add(each);
            }
            runningService.remove(slaveID.getValue(), each);
        }
    }
    
    @Override
    public void executorLost(final SchedulerDriver schedulerDriver, final Protos.ExecutorID executorID, final Protos.SlaveID slaveID, final int i) {
        TaskContext taskContext = TaskContext.from(executorID.getValue());
        Optional<CloudJobConfiguration> jobConfig = configService.load(taskContext.getJobName());
        if (jobConfig.isPresent() && jobConfig.get().isFailover()) {
            failoverService.add(taskContext);
        }
    }
    
    @Override
    public void error(final SchedulerDriver schedulerDriver, final String s) {
    }
}
