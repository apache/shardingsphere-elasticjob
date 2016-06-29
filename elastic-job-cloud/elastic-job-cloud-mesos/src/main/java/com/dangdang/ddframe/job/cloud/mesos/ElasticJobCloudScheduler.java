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

import com.dangdang.ddframe.job.cloud.Internal.config.CloudConfigurationService;
import com.dangdang.ddframe.job.cloud.Internal.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.Internal.queue.TaskQueueService;
import com.dangdang.ddframe.job.cloud.Internal.schedule.CloudTaskSchedulerRegistry;
import com.dangdang.ddframe.job.cloud.Internal.state.StateService;
import com.dangdang.ddframe.job.cloud.Internal.task.CloudJobTask;
import com.dangdang.ddframe.job.cloud.Internal.task.CloudJobTaskService;
import com.dangdang.ddframe.job.cloud.mesos.stragety.ExhaustFirstResourceAllocateStrategy;
import com.dangdang.ddframe.job.cloud.mesos.stragety.ResourceAllocateStrategy;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;

import java.util.List;

/**
 * 作业云的Mesos调度器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ElasticJobCloudScheduler implements Scheduler {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    private final CloudConfigurationService configService;
    
    private final CloudJobTaskService taskService;
    
    private final TaskQueueService taskQueueService;
    
    private final StateService stateService;
    
    public ElasticJobCloudScheduler(final CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
        configService = new CloudConfigurationService(registryCenter);
        taskService = new CloudJobTaskService();
        taskQueueService = new TaskQueueService(registryCenter);
        stateService = new StateService(registryCenter);
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
        Optional<String> jobName = taskQueueService.dequeue();
        if (!jobName.isPresent()) {
            declineOffers(schedulerDriver, offers);
            return;
        }
        Optional<CloudJobConfiguration> cloudJobConfig = configService.load(jobName.get());
        if (!cloudJobConfig.isPresent()) {
            declineOffers(schedulerDriver, offers);
            return;
        }
        // TODO 出队时如果mesos framework死机,则已出队但未运行的作业将丢失
        ResourceAllocateStrategy resourceAllocateStrategy = new ExhaustFirstResourceAllocateStrategy();
        List<Protos.TaskInfo> tasks = resourceAllocateStrategy.allocate(offers, cloudJobConfig.get());
        if (tasks.size() < cloudJobConfig.get().getShardingTotalCount()) {
            declineOffers(schedulerDriver, offers);
            return;
        }
        // TODO 未使用的slaveid 机器调用declineOffer方法放回
        schedulerDriver.launchTasks(Lists.transform(offers, new Function<Protos.Offer, Protos.OfferID>() {
            
            @Override
            public Protos.OfferID apply(final Protos.Offer input) {
                return input.getId();
            }
        }), tasks);
    }
    
    private void declineOffers(final SchedulerDriver schedulerDriver, final List<Protos.Offer> offers) {
        for (Protos.Offer each : offers) {
            schedulerDriver.declineOffer(each.getId());
        }
    }
    
    @Override
    public void offerRescinded(final SchedulerDriver schedulerDriver, final Protos.OfferID offerID) {
    }
    
    @Override
    public void statusUpdate(final SchedulerDriver schedulerDriver, final Protos.TaskStatus taskStatus) {
        String taskId = taskStatus.getTaskId().getValue();
        CloudJobTask cloudJobTask = taskService.getJobTask(taskId);
        switch (taskStatus.getState()) {
            case TASK_STARTING:
                stateService.startRunning(cloudJobTask.getJobName(), cloudJobTask.getShardingItem());
            case TASK_FINISHED:
            case TASK_FAILED:
            case TASK_KILLED:
            case TASK_LOST:
                stateService.completeRunning(cloudJobTask.getJobName(), cloudJobTask.getShardingItem());
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
    }
    
    @Override
    public void executorLost(final SchedulerDriver schedulerDriver, final Protos.ExecutorID executorID, final Protos.SlaveID slaveID, final int i) {
    }
    
    @Override
    public void error(final SchedulerDriver schedulerDriver, final String s) {
    }
}
