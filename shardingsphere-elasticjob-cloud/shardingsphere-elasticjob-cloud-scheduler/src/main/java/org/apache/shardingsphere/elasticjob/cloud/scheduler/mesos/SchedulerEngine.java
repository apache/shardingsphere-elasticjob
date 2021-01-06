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

import com.netflix.fenzo.TaskScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.ha.FrameworkIDService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.StatisticManager;
import org.apache.shardingsphere.elasticjob.tracing.JobTracingEventBus;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent;

import java.util.List;

/**
 * Scheduler engine.
 */
@RequiredArgsConstructor
@Slf4j
public final class SchedulerEngine implements Scheduler {
    
    private final TaskScheduler taskScheduler;
    
    private final FacadeService facadeService;
    
    private final JobTracingEventBus jobTracingEventBus;
    
    private final FrameworkIDService frameworkIDService;
    
    private final StatisticManager statisticManager;
    
    @Override
    public void registered(final SchedulerDriver schedulerDriver, final Protos.FrameworkID frameworkID, final Protos.MasterInfo masterInfo) {
        log.info("call registered");
        frameworkIDService.save(frameworkID.getValue());
        taskScheduler.expireAllLeases();
        MesosStateService.register(masterInfo.getHostname(), masterInfo.getPort());
    }
    
    @Override
    public void reregistered(final SchedulerDriver schedulerDriver, final Protos.MasterInfo masterInfo) {
        log.info("call reregistered");
        taskScheduler.expireAllLeases();
        MesosStateService.register(masterInfo.getHostname(), masterInfo.getPort());
    }
    
    @Override
    public void resourceOffers(final SchedulerDriver schedulerDriver, final List<Protos.Offer> offers) {
        for (Protos.Offer offer: offers) {
            log.trace("Adding offer {} from host {}", offer.getId(), offer.getHostname());
            LeasesQueue.getInstance().offer(offer);
        }
    }
    
    @Override
    public void offerRescinded(final SchedulerDriver schedulerDriver, final Protos.OfferID offerID) {
        log.trace("call offerRescinded: {}", offerID);
        taskScheduler.expireLease(offerID.getValue());
    }
    
    @Override
    public void statusUpdate(final SchedulerDriver schedulerDriver, final Protos.TaskStatus taskStatus) {
        String taskId = taskStatus.getTaskId().getValue();
        TaskContext taskContext = TaskContext.from(taskId);
        String jobName = taskContext.getMetaInfo().getJobName();
        log.trace("call statusUpdate task state is: {}, task id is: {}", taskStatus.getState(), taskId);
        jobTracingEventBus.post(new JobStatusTraceEvent(jobName, taskContext.getId(), taskContext.getSlaveId(), JobStatusTraceEvent.Source.CLOUD_SCHEDULER, taskContext.getType().toString(), 
                String.valueOf(taskContext.getMetaInfo().getShardingItems()), JobStatusTraceEvent.State.valueOf(taskStatus.getState().name()), taskStatus.getMessage()));
        switch (taskStatus.getState()) {
            case TASK_RUNNING:
                if (!facadeService.load(jobName).isPresent()) {
                    schedulerDriver.killTask(Protos.TaskID.newBuilder().setValue(taskId).build());
                }
                if ("BEGIN".equals(taskStatus.getMessage())) {
                    facadeService.updateDaemonStatus(taskContext, false);
                } else if ("COMPLETE".equals(taskStatus.getMessage())) {
                    facadeService.updateDaemonStatus(taskContext, true);
                    statisticManager.taskRunSuccessfully();
                }
                break;
            case TASK_FINISHED:
                facadeService.removeRunning(taskContext);
                unAssignTask(taskId);
                statisticManager.taskRunSuccessfully();
                break;
            case TASK_KILLED:
                log.warn("task id is: {}, status is: {}, message is: {}, source is: {}", taskId, taskStatus.getState(), taskStatus.getMessage(), taskStatus.getSource());
                facadeService.removeRunning(taskContext);
                facadeService.addDaemonJobToReadyQueue(jobName);
                unAssignTask(taskId);
                break;
            case TASK_LOST:
            case TASK_DROPPED:
            case TASK_GONE:
            case TASK_GONE_BY_OPERATOR:
            case TASK_FAILED:
            case TASK_ERROR:
                log.warn("task id is: {}, status is: {}, message is: {}, source is: {}", taskId, taskStatus.getState(), taskStatus.getMessage(), taskStatus.getSource());
                facadeService.removeRunning(taskContext);
                facadeService.recordFailoverTask(taskContext);
                unAssignTask(taskId);
                statisticManager.taskRunFailed();
                break;
            case TASK_UNKNOWN:
            case TASK_UNREACHABLE:
                log.error("task id is: {}, status is: {}, message is: {}, source is: {}", taskId, taskStatus.getState(), taskStatus.getMessage(), taskStatus.getSource());
                statisticManager.taskRunFailed();
                break;
            default:
                break;
        }
    }
    
    private void unAssignTask(final String taskId) {
        String hostname = facadeService.popMapping(taskId);
        if (null != hostname) {
            taskScheduler.getTaskUnAssigner().call(TaskContext.getIdForUnassignedSlave(taskId), hostname);
        }
    }
    
    @Override
    public void frameworkMessage(final SchedulerDriver schedulerDriver, final Protos.ExecutorID executorID, final Protos.SlaveID slaveID, final byte[] bytes) {
        log.trace("call frameworkMessage slaveID: {}, bytes: {}", slaveID, new String(bytes));
    }
    
    @Override
    public void disconnected(final SchedulerDriver schedulerDriver) {
        log.warn("call disconnected");
        MesosStateService.deregister();
    }
    
    @Override
    public void slaveLost(final SchedulerDriver schedulerDriver, final Protos.SlaveID slaveID) {
        log.warn("call slaveLost slaveID is: {}", slaveID);
        taskScheduler.expireAllLeasesByVMId(slaveID.getValue());
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
