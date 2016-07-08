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

import com.dangdang.ddframe.job.cloud.TaskContext;
import com.dangdang.ddframe.job.cloud.mesos.facade.AssignedTaskContext;
import com.dangdang.ddframe.job.cloud.mesos.facade.EligibleJobContext;
import com.dangdang.ddframe.job.cloud.mesos.facade.FacadeService;
import com.dangdang.ddframe.job.cloud.mesos.stragety.ExhaustFirstResourceAllocateStrategy;
import com.dangdang.ddframe.job.cloud.mesos.stragety.ResourceAllocateStrategy;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;

import java.util.ArrayList;
import java.util.List;

/**
 * 作业云引擎.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SchedulerEngine implements Scheduler {
    
    private final FacadeService facadeService;
    
    public SchedulerEngine(final CoordinatorRegistryCenter registryCenter) {
        facadeService = new FacadeService(registryCenter);
    }
    
    @Override
    public void registered(final SchedulerDriver schedulerDriver, final Protos.FrameworkID frameworkID, final Protos.MasterInfo masterInfo) {
        facadeService.start();
    }
    
    @Override
    public void reregistered(final SchedulerDriver schedulerDriver, final Protos.MasterInfo masterInfo) {
        facadeService.start();
    }
    
    @Override
    public void resourceOffers(final SchedulerDriver schedulerDriver, final List<Protos.Offer> offers) {
        ResourceAllocateStrategy resourceAllocateStrategy = new ExhaustFirstResourceAllocateStrategy(getHardwareResource(offers));
        EligibleJobContext eligibleJobContext = facadeService.getEligibleJobContext();
        AssignedTaskContext assignedTaskContext = eligibleJobContext.allocate(resourceAllocateStrategy);
        List<Protos.TaskInfo> taskInfoList = assignedTaskContext.getTaskInfoList();
        List<Protos.Offer> declinedOffers = declineUnusedOffers(schedulerDriver, offers, taskInfoList);
        launchTasks(schedulerDriver, offers, declinedOffers, taskInfoList);
        facadeService.removeLaunchTasksFromQueue(assignedTaskContext);
    }
    
    private List<HardwareResource> getHardwareResource(final List<Protos.Offer> offers) {
        return Lists.transform(offers, new Function<Protos.Offer, HardwareResource>() {
            
            @Override
            public HardwareResource apply(final Protos.Offer input) {
                return new HardwareResource(input);
            }
        });
    }
    
    private List<Protos.Offer> declineUnusedOffers(final SchedulerDriver schedulerDriver, final List<Protos.Offer> offers, final List<Protos.TaskInfo> tasks) {
        List<Protos.Offer> result = new ArrayList<>(offers.size());
        for (Protos.Offer each : offers) {
            if (!isUsed(each, tasks)) {
                schedulerDriver.declineOffer(each.getId());
                result.add(each);
            }
        }
        return result;
    }
    
    private boolean isUsed(final Protos.Offer offer, final List<Protos.TaskInfo> tasks) {
        for (Protos.TaskInfo each : tasks) {
            if (offer.getSlaveId().getValue().equals(each.getSlaveId().getValue())) {
                return true;
            }
        }
        return false;
    }
    
    private void launchTasks(final SchedulerDriver schedulerDriver, final List<Protos.Offer> offers, final List<Protos.Offer> declinedOffers, final List<Protos.TaskInfo> tasks) {
        List<Protos.Offer> launchOffers = new ArrayList<>(offers);
        launchOffers.removeAll(declinedOffers);
        schedulerDriver.launchTasks(Lists.transform(launchOffers, new Function<Protos.Offer, Protos.OfferID>() {
            
            @Override
            public Protos.OfferID apply(final Protos.Offer input) {
                return input.getId();
            }
        }), tasks);
        // TODO 状态回调调整好, 这里的代码应删除
        for (Protos.TaskInfo each : tasks) {
            facadeService.addRunning(each.getSlaveId().getValue(), TaskContext.from(each.getTaskId().getValue()));
        }
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
                facadeService.addRunning(taskStatus.getSlaveId().getValue(), taskContext);
                break;
            case TASK_FINISHED:
            // TODO TASK_FAILED, TASK_LOST走failover
            case TASK_FAILED:
            case TASK_KILLED:
            case TASK_LOST:
                facadeService.removeRunning(taskStatus.getSlaveId().getValue(), taskContext);
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
        facadeService.stop();
    }
    
    
    @Override
    public void slaveLost(final SchedulerDriver schedulerDriver, final Protos.SlaveID slaveID) {
        facadeService.recordFailoverTasks(slaveID.getValue());
    }
    
    @Override
    public void executorLost(final SchedulerDriver schedulerDriver, final Protos.ExecutorID executorID, final Protos.SlaveID slaveID, final int i) {
        facadeService.recordFailoverTask(slaveID.getValue(), TaskContext.from(executorID.getValue()));
    }
    
    @Override
    public void error(final SchedulerDriver schedulerDriver, final String s) {
    }
}
