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
        CloudJobConfiguration cloudJobConfig = configService.load(jobName.get());
        List<Protos.TaskInfo> tasks = new ArrayList<>(cloudJobConfig.getShardingTotalCount());
        int startShardingItem = 0;
        int assignedShares = 0;
        for (Protos.Offer each : offers) {
            int availableCpuShare = getValue(each.getResourcesList(), "cpus").divide(new BigDecimal(cloudJobConfig.getCpuCount()), BigDecimal.ROUND_DOWN).intValue();
            int availableMemoriesShare = getValue(each.getResourcesList(), "mem").divide(new BigDecimal(cloudJobConfig.getMemoryMB()), BigDecimal.ROUND_DOWN).intValue();
            assignedShares += availableCpuShare < availableMemoriesShare ? availableCpuShare : availableMemoriesShare;
            for (int i = startShardingItem; i < assignedShares; i++) {
                Protos.TaskID taskId = Protos.TaskID.newBuilder().setValue(taskService.generateTaskId(jobName.get(), i)).build();
                Protos.TaskInfo task = Protos.TaskInfo.newBuilder()
                        .setName(taskId.getValue())
                        .setTaskId(taskId)
                        .setSlaveId(each.getSlaveId())
                        .addResources(getResources("cpus", cloudJobConfig.getCpuCount()))
                        .addResources(getResources("mem", cloudJobConfig.getMemoryMB()))
                        .setCommand(Protos.CommandInfo.newBuilder().setValue("/Users/zhangliang/docker-sample/elastic-job-example/bin/start.sh " + taskId.getValue() + " > /Users/zhangliang/docker-sample/elastic-job-example/logs/log" + taskService.getJobTask(taskId.getValue()).getShardingItem() + ".log"))
                        .build();
                tasks.add(task);
                if (tasks.size() == cloudJobConfig.getShardingTotalCount()) {
                    break;
                }
            }
            if (tasks.size() == cloudJobConfig.getShardingTotalCount()) {
                break;
            }
            startShardingItem = assignedShares;
        }
        if (tasks.size() < cloudJobConfig.getShardingTotalCount()) {
            declineOffers(schedulerDriver, offers);
            return;
        }
        // TODO 未使用的slaveid 机器调用declineOffer方法放回
        stateService.sharding(jobName.get());
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
    
    private BigDecimal getValue(final List<Protos.Resource> resources, final String type) {
        for (Protos.Resource each : resources) {
            if (type.equals(each.getName())) {
                return new BigDecimal(each.getScalar().getValue());
            }
        }
        return BigDecimal.ZERO;
    }
    
    private Protos.Resource.Builder getResources(final String type, final double value) {
        return Protos.Resource.newBuilder().setName(type).setType(Protos.Value.Type.SCALAR).setScalar(Protos.Value.Scalar.newBuilder().setValue(value));
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
