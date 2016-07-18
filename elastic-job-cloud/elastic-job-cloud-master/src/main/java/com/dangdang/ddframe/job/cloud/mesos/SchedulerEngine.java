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

import com.dangdang.ddframe.job.cloud.context.JobContext;
import com.dangdang.ddframe.job.cloud.context.TaskContext;
import com.dangdang.ddframe.job.cloud.mesos.facade.FacadeService;
import com.dangdang.ddframe.job.cloud.mesos.stragety.ExhaustFirstResourceAllocateStrategy;
import com.dangdang.ddframe.job.cloud.mesos.stragety.ResourceAllocateStrategy;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 作业云引擎.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Slf4j
public final class SchedulerEngine implements Scheduler {
    
    private final FacadeService facadeService;
    
    public SchedulerEngine(final CoordinatorRegistryCenter registryCenter) {
        facadeService = new FacadeService(registryCenter);
    }
    
    @Override
    public void registered(final SchedulerDriver schedulerDriver, final Protos.FrameworkID frameworkID, final Protos.MasterInfo masterInfo) {
        log.info("call registered");
        facadeService.start();
    }
    
    @Override
    public void reregistered(final SchedulerDriver schedulerDriver, final Protos.MasterInfo masterInfo) {
        log.info("call reregistered");
        facadeService.start();
    }
    
    @Override
    public void resourceOffers(final SchedulerDriver schedulerDriver, final List<Protos.Offer> offers) {
        log.trace("call resourceOffers: {}", offers);
        ResourceAllocateStrategy resourceAllocateStrategy = new ExhaustFirstResourceAllocateStrategy(getHardwareResource(offers));
        Collection<JobContext> eligibleJobContexts = facadeService.getEligibleJobContext();
        List<Protos.TaskInfo> taskInfoList = resourceAllocateStrategy.allocate(eligibleJobContexts);
        log.trace("call resourceOffers, assignedTaskContext is {}", taskInfoList);
        List<Protos.Offer> declinedOffers = declineUnusedOffers(schedulerDriver, offers, taskInfoList);
        launchTasks(schedulerDriver, offers, declinedOffers, taskInfoList);
        facadeService.removeLaunchTasksFromQueue(Lists.transform(taskInfoList, new Function<Protos.TaskInfo, TaskContext>() {
            
            @Override
            public TaskContext apply(final Protos.TaskInfo input) {
                return TaskContext.from(input.getTaskId().getValue());
            }
        }));
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
        Map<Protos.SlaveID, List<Protos.OfferID>> launchOfferMap = getLaunchOfferMap(offers, declinedOffers);
        for (Map.Entry<Protos.SlaveID, List<Protos.OfferID>> entry : launchOfferMap.entrySet()) {
            schedulerDriver.launchTasks(entry.getValue(), filterTaskInfoBySlaveID(entry.getKey(), tasks));
        }
        for (Protos.TaskInfo each : tasks) {
            facadeService.addRunning(TaskContext.from(each.getTaskId().getValue()));
        }
    }
    
    private Map<Protos.SlaveID, List<Protos.OfferID>> getLaunchOfferMap(final List<Protos.Offer> offers, final List<Protos.Offer> declinedOffers) {
        List<Protos.Offer> launchOffers = new ArrayList<>(offers);
        launchOffers.removeAll(declinedOffers);
        Map<Protos.SlaveID, List<Protos.OfferID>> result = new HashMap<>(launchOffers.size(), 1);
        for (Protos.Offer each : launchOffers) {
            List<Protos.OfferID> offerIdList = result.containsKey(each.getSlaveId()) ? result.get(each.getSlaveId()) : new LinkedList<Protos.OfferID>();
            offerIdList.add(each.getId());
            result.put(each.getSlaveId(), offerIdList);
        }
        return result;
    }
    
    private List<Protos.TaskInfo> filterTaskInfoBySlaveID(final Protos.SlaveID slaveId, final List<Protos.TaskInfo> tasks) {
        List<Protos.TaskInfo> result = new ArrayList<>(tasks.size());
        for (Protos.TaskInfo each : tasks) {
            if (each.getSlaveId().equals(slaveId)) {
                result.add(each);
            }
        }
        return result;
    }
    
    @Override
    public void offerRescinded(final SchedulerDriver schedulerDriver, final Protos.OfferID offerID) {
        log.trace("call offerRescinded: {}", offerID);
    }
    
    @Override
    public void statusUpdate(final SchedulerDriver schedulerDriver, final Protos.TaskStatus taskStatus) {
        String taskId = taskStatus.getTaskId().getValue();
        TaskContext taskContext = TaskContext.from(taskId);
        log.trace("call statusUpdate task state is: {}", taskStatus.getState(), taskContext);
        switch (taskStatus.getState()) {
            case TASK_FINISHED:
            case TASK_KILLED:
                facadeService.removeRunning(taskContext);
                break;
            case TASK_LOST:
                // TODO TASK_FAILED和TASK_ERROR是否要做失效转移
            case TASK_FAILED:
            case TASK_ERROR:
                log.warn("task status is: {}, message is: {}, source is: {}", taskStatus.getState(), taskStatus.getMessage(), taskStatus.getSource());
                facadeService.removeRunning(taskContext);
                facadeService.recordFailoverTask(taskContext);
                break;
            default:
                break;
        }
    }
    
    @Override
    public void frameworkMessage(final SchedulerDriver schedulerDriver, final Protos.ExecutorID executorID, final Protos.SlaveID slaveID, final byte[] bytes) {
        log.trace("call frameworkMessage slaveID: {}, bytes: {}", slaveID, new String(bytes));
    }
    
    @Override
    public void disconnected(final SchedulerDriver schedulerDriver) {
        log.warn("call disconnected");
        facadeService.stop();
    }
    
    @Override
    public void slaveLost(final SchedulerDriver schedulerDriver, final Protos.SlaveID slaveID) {
        log.warn("call slaveLost slaveID is: {}", slaveID);
    }
    
    @Override
    public void executorLost(final SchedulerDriver schedulerDriver, final Protos.ExecutorID executorID, final Protos.SlaveID slaveID, final int i) {
        log.warn("call executorLost slaveID is: {}, executorID is: {}", slaveID, executorID);
    }
    
    @Override
    public void error(final SchedulerDriver schedulerDriver, final String message) {
        log.error("call error, message is: {}", message);
    }
}
