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

import com.dangdang.ddframe.job.cloud.job.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.job.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.job.state.StateService;
import com.dangdang.ddframe.job.cloud.mesos.stragety.ExhaustFirstResourceAllocateStrategy;
import com.dangdang.ddframe.job.cloud.mesos.stragety.ResourceAllocateStrategy;
import com.dangdang.ddframe.job.cloud.schedule.CloudTaskSchedulerRegistry;
import com.dangdang.ddframe.job.cloud.task.ElasticJobTask;
import com.dangdang.ddframe.job.cloud.task.failover.FailoverTaskQueueService;
import com.dangdang.ddframe.job.cloud.task.ready.ReadyJobQueueService;
import com.dangdang.ddframe.job.cloud.task.running.RunningTaskService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 作业云的Mesos调度器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ElasticJobCloudScheduler implements Scheduler {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    private final ConfigurationService configService;
    
    private final ReadyJobQueueService readyJobQueueService;
    
    private final StateService stateService;
    
    private final RunningTaskService taskRunningService;
    
    private final FailoverTaskQueueService failoverTaskQueueService;
    
    public ElasticJobCloudScheduler(final CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
        configService = new ConfigurationService(registryCenter);
        readyJobQueueService = new ReadyJobQueueService(registryCenter);
        stateService = new StateService(registryCenter);
        taskRunningService = new RunningTaskService(registryCenter);
        failoverTaskQueueService = new FailoverTaskQueueService(registryCenter);
    }
    
    @Override
    public void registered(final SchedulerDriver schedulerDriver, final Protos.FrameworkID frameworkID, final Protos.MasterInfo masterInfo) {
        CloudTaskSchedulerRegistry.getInstance(registryCenter).registerFromRegistryCenter();
    }
    
    @Override
    public void reregistered(final SchedulerDriver schedulerDriver, final Protos.MasterInfo masterInfo) {
    }
    
    private boolean failover(final SchedulerDriver schedulerDriver, final List<Protos.Offer> offers) {
        // TODO 批量failover
        Optional<ElasticJobTask> elasticJobTask = failoverTaskQueueService.dequeue();
        if (!elasticJobTask.isPresent()) {
            return false;
        }
        Optional<CloudJobConfiguration> cloudJobConfig = configService.load(elasticJobTask.get().getJobName());
        if (!cloudJobConfig.isPresent()) {
            return false;
        }
        for (Protos.Offer each : offers) {
            BigDecimal cpuCount = MesosUtil.getValue(each.getResourcesList(), "cpus");
            BigDecimal memories = MesosUtil.getValue(each.getResourcesList(), "mem");
            if (cpuCount.doubleValue() >= cloudJobConfig.get().getCpuCount() && memories.doubleValue() >= cloudJobConfig.get().getMemoryMB()) {
                Protos.TaskInfo task = MesosUtil.createTaskInfo(each, cloudJobConfig.get(), elasticJobTask.get().getShardingItem());
                schedulerDriver.launchTasks(Lists.transform(offers, new Function<Protos.Offer, Protos.OfferID>() {
                    
                    @Override
                    public Protos.OfferID apply(final Protos.Offer input) {
                        return input.getId();
                    }
                }), Collections.singleton(task));
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void resourceOffers(final SchedulerDriver schedulerDriver, final List<Protos.Offer> offers) {
        if (failover(schedulerDriver, offers)) {
            return;
        }
        Optional<String> jobName = readyJobQueueService.dequeue();
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
        // TODO 目前是每个实例处理一片, 要改成每个实例能处理n片,按照资源决定每个服务分配的分片
        ResourceAllocateStrategy resourceAllocateStrategy = new ExhaustFirstResourceAllocateStrategy();
        List<Protos.TaskInfo> tasks = resourceAllocateStrategy.allocate(offers, cloudJobConfig.get());
        if (tasks.size() < cloudJobConfig.get().getShardingTotalCount()) {
            declineOffers(schedulerDriver, offers);
            return;
        }
        removeRunningTasks(tasks);
        declineUnusedOffers(schedulerDriver, offers, tasks);
        launchTasks(schedulerDriver, offers, tasks);
    }
    
    private void declineOffers(final SchedulerDriver schedulerDriver, final List<Protos.Offer> offers) {
        for (Protos.Offer each : offers) {
            schedulerDriver.declineOffer(each.getId());
        }
    }
    
    private void removeRunningTasks(final List<Protos.TaskInfo> tasks) {
        List<Protos.TaskInfo> runningTasks = new ArrayList<>(tasks.size());
        for (Protos.TaskInfo each : tasks) {
            if (!taskRunningService.add(each.getSlaveId().getValue(), ElasticJobTask.from(each.getTaskId().getValue()))) {
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
    }
    
    @Override
    public void offerRescinded(final SchedulerDriver schedulerDriver, final Protos.OfferID offerID) {
    }
    
    @Override
    public void statusUpdate(final SchedulerDriver schedulerDriver, final Protos.TaskStatus taskStatus) {
        String taskId = taskStatus.getTaskId().getValue();
        ElasticJobTask elasticJobTask = ElasticJobTask.from(taskId);
        switch (taskStatus.getState()) {
            case TASK_STARTING:
                stateService.startRunning(elasticJobTask.getJobName(), elasticJobTask.getShardingItem());
                break;
            case TASK_FINISHED:
            case TASK_FAILED:
            case TASK_KILLED:
            case TASK_LOST:
                stateService.completeRunning(elasticJobTask.getJobName(), elasticJobTask.getShardingItem());
                taskRunningService.remove(taskStatus.getSlaveId().getValue(), elasticJobTask);
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
        List<ElasticJobTask> runningTasks = taskRunningService.load(slaveID.getValue());
        for (ElasticJobTask each : runningTasks) {
            Optional<CloudJobConfiguration> jobConfig = configService.load(each.getJobName());
            if (jobConfig.isPresent() && jobConfig.get().isFailover()) {
                failoverTaskQueueService.enqueue(each);
            }
            taskRunningService.remove(slaveID.getValue(), each);
        }
    }
    
    @Override
    public void executorLost(final SchedulerDriver schedulerDriver, final Protos.ExecutorID executorID, final Protos.SlaveID slaveID, final int i) {
        failoverTaskQueueService.enqueue(ElasticJobTask.from(executorID.getValue()));
    }
    
    @Override
    public void error(final SchedulerDriver schedulerDriver, final String s) {
    }
}
