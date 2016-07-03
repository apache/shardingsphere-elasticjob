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

import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.mesos.stragety.ExhaustFirstResourceAllocateStrategy;
import com.dangdang.ddframe.job.cloud.mesos.stragety.ResourceAllocateStrategy;
import com.dangdang.ddframe.job.cloud.schedule.CloudTaskSchedulerRegistry;
import com.dangdang.ddframe.job.cloud.state.ElasticJobTask;
import com.dangdang.ddframe.job.cloud.state.failover.FailoverService;
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
import java.util.Collections;
import java.util.List;

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
    
    public SchedulerEngine(final CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
        configService = new ConfigurationService(registryCenter);
        readyService = new ReadyService(registryCenter);
        runningService = new RunningService(registryCenter);
        failoverService = new FailoverService(registryCenter);
    }
    
    @Override
    public void registered(final SchedulerDriver schedulerDriver, final Protos.FrameworkID frameworkID, final Protos.MasterInfo masterInfo) {
        CloudTaskSchedulerRegistry.getInstance(registryCenter).registerFromRegistryCenter();
    }
    
    @Override
    public void reregistered(final SchedulerDriver schedulerDriver, final Protos.MasterInfo masterInfo) {
    }
    
    @Override
    public void resourceOffers(final SchedulerDriver schedulerDriver, final List<Protos.Offer> offers) {
        List<HardwareResource> hardwareResources = getHardwareResource(offers);
        ResourceAllocateStrategy resourceAllocateStrategy = new ExhaustFirstResourceAllocateStrategy(hardwareResources);
        offerFailoverJobs(resourceAllocateStrategy);
        offerReadyJobs(resourceAllocateStrategy);
        List<Protos.TaskInfo> taskInfoList = resourceAllocateStrategy.getOfferedTaskInfoList();
        removeRunningTasks(taskInfoList);
        declineUnusedOffers(schedulerDriver, offers, taskInfoList);
        launchTasks(schedulerDriver, offers, taskInfoList);
    }
    
    private List<HardwareResource> getHardwareResource(final List<Protos.Offer> offers) {
        return Lists.transform(offers, new Function<Protos.Offer, HardwareResource>() {
            
            @Override
            public HardwareResource apply(final Protos.Offer input) {
                return new HardwareResource(input);
            }
        });
    }
    
    private void offerFailoverJobs(final ResourceAllocateStrategy resourceAllocateStrategy) {
        Optional<ElasticJobTask> task = failoverService.dequeue();
        while (task.isPresent()) {
            Optional<CloudJobConfiguration> jobConfig = configService.load(task.get().getJobName());
            if (!jobConfig.isPresent()) {
                task = failoverService.dequeue();
                continue;
            }
            if (!resourceAllocateStrategy.allocate(jobConfig.get(), Collections.singletonList(task.get().getShardingItem()))) {
                for (Protos.TaskInfo each : resourceAllocateStrategy.getDeclinedTaskInfoList()) {
                    failoverService.enqueue(ElasticJobTask.from(each.getTaskId().getValue()));
                }
                break;
            }
            task = failoverService.dequeue();
        }
    }
    
    private void offerReadyJobs(final ResourceAllocateStrategy resourceAllocateStrategy) {
        Optional<String> jobName = readyService.dequeue();
        while (jobName.isPresent()) {
            Optional<CloudJobConfiguration> jobConfig = configService.load(jobName.get());
            if (!jobConfig.isPresent()) {
                jobName = readyService.dequeue();
                continue;
            }
            if (!resourceAllocateStrategy.allocate(jobConfig.get())) {
                if (!resourceAllocateStrategy.getDeclinedTaskInfoList().isEmpty()) {
                    readyService.enqueue(ElasticJobTask.from(resourceAllocateStrategy.getDeclinedTaskInfoList().get(0).getTaskId().getValue()).getJobName());
                }
                break;
            }
            jobName = readyService.dequeue();
        }
    }
    
    private void removeRunningTasks(final List<Protos.TaskInfo> tasks) {
        List<Protos.TaskInfo> runningTasks = new ArrayList<>(tasks.size());
        for (Protos.TaskInfo each : tasks) {
            if (runningService.isTaskRunning(ElasticJobTask.from(each.getTaskId().getValue()))) {
                runningTasks.add(each);
            }
        }
        tasks.removeAll(runningTasks);
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
            runningService.add(each.getSlaveId().getValue(), ElasticJobTask.from(each.getTaskId().getValue()));
        }
    }
    
    @Override
    public void offerRescinded(final SchedulerDriver schedulerDriver, final Protos.OfferID offerID) {
    }
    
    @Override
    // TODO 状态返回不正确,不能正确记录状态,导致不能failover, 目前先放在resourceOffers实现
    public void statusUpdate(final SchedulerDriver schedulerDriver, final Protos.TaskStatus taskStatus) {
        String taskId = taskStatus.getTaskId().getValue();
        ElasticJobTask elasticJobTask = ElasticJobTask.from(taskId);
        switch (taskStatus.getState()) {
            case TASK_STARTING:
                runningService.add(taskStatus.getSlaveId().getValue(), elasticJobTask);
                break;
            case TASK_FINISHED:
            case TASK_FAILED:
            case TASK_KILLED:
            case TASK_LOST:
                runningService.remove(taskStatus.getSlaveId().getValue(), elasticJobTask);
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
    }
    
    @Override
    public void slaveLost(final SchedulerDriver schedulerDriver, final Protos.SlaveID slaveID) {
        List<ElasticJobTask> runningTasks = runningService.load(slaveID.getValue());
        for (ElasticJobTask each : runningTasks) {
            Optional<CloudJobConfiguration> jobConfig = configService.load(each.getJobName());
            if (jobConfig.isPresent() && jobConfig.get().isFailover()) {
                failoverService.enqueue(each);
            }
            runningService.remove(slaveID.getValue(), each);
        }
    }
    
    @Override
    public void executorLost(final SchedulerDriver schedulerDriver, final Protos.ExecutorID executorID, final Protos.SlaveID slaveID, final int i) {
        ElasticJobTask task = ElasticJobTask.from(executorID.getValue());
        Optional<CloudJobConfiguration> jobConfig = configService.load(task.getJobName());
        if (jobConfig.isPresent() && jobConfig.get().isFailover()) {
            failoverService.enqueue(task);
        }
    }
    
    @Override
    public void error(final SchedulerDriver schedulerDriver, final String s) {
    }
}
