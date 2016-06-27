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

import com.dangdang.ddframe.job.api.JobAPIFactory;
import com.dangdang.ddframe.job.cloud.Internal.queue.TaskQueueService;
import com.dangdang.ddframe.job.cloud.Internal.running.RunningService;
import com.dangdang.ddframe.job.cloud.Internal.task.CloudTask;
import com.dangdang.ddframe.job.cloud.Internal.task.CloudTaskService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Elastic-Job-Cloud的Mesos调度器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ElasticJobCloudScheduler implements Scheduler {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    @Override
    public void registered(final SchedulerDriver schedulerDriver, final Protos.FrameworkID frameworkID, final Protos.MasterInfo masterInfo) {
    }
    
    @Override
    public void reregistered(final SchedulerDriver schedulerDriver, final Protos.MasterInfo masterInfo) {
    }
    
    @Override
    public void resourceOffers(final SchedulerDriver schedulerDriver, final List<Protos.Offer> offers) {
        CloudTaskService cloudTaskService = new CloudTaskService(registryCenter);
        TaskQueueService taskQueueService = new TaskQueueService(registryCenter);
        RunningService runningService = new RunningService(registryCenter);
        List<Protos.TaskInfo> tasks = new ArrayList<>(offers.size());
        List<Protos.OfferID> offerIdList = new ArrayList<>(offers.size());
        for (Protos.Offer each : offers) {
            Optional<String> jobName = taskQueueService.dequeue();
            if (!jobName.isPresent()) {
                schedulerDriver.declineOffer(each.getId());
                return;
            }
            Protos.TaskID taskId = Protos.TaskID.newBuilder().setValue(Joiner.on("-").join(jobName.get(), UUID.randomUUID().toString())).build();
            CloudTask cloudTask = cloudTaskService.getTask(jobName.get());
            Protos.TaskInfo task = Protos.TaskInfo.newBuilder()
                    .setName(Joiner.on("-").join(jobName.get(), taskId.getValue()))
                    .setTaskId(taskId)
                    .setSlaveId(each.getSlaveId())
                    .addResources(Protos.Resource.newBuilder()
                            .setName("cpus")
                            .setType(Protos.Value.Type.SCALAR)
                            .setScalar(Protos.Value.Scalar.newBuilder().setValue(cloudTask.getCpuCount())))
                    .addResources(Protos.Resource.newBuilder()
                            .setName("mem")
                            .setType(Protos.Value.Type.SCALAR)
                            .setScalar(Protos.Value.Scalar.newBuilder().setValue(cloudTask.getMemoryMB())))
                    .setCommand(Protos.CommandInfo.newBuilder().setValue("/Users/zhangliang/docker-sample/elastic-job-example/bin/start.sh > /Users/zhangliang/docker-sample/elastic-job-example/logs/log1.log"))
                    .build();
            tasks.add(task);
            offerIdList.add(each.getId());
            runningService.startRunning(jobName.get(), taskId.getValue());
        }
        schedulerDriver.launchTasks(offerIdList, tasks);
    }
    
    @Override
    public void offerRescinded(final SchedulerDriver schedulerDriver, final Protos.OfferID offerID) {
    }
    
    @Override
    public void statusUpdate(final SchedulerDriver schedulerDriver, final Protos.TaskStatus taskStatus) {
        RunningService runningService = new RunningService(registryCenter);
        String taskId = taskStatus.getTaskId().getValue();
        String jobName = runningService.getRunningJobName(taskId);
        switch (taskStatus.getState()) {
            case TASK_FINISHED:
            case TASK_FAILED:
            case TASK_KILLED:
            case TASK_LOST:
                runningService.completeRunning(jobName, taskId);
                break;
            case TASK_RUNNING:
                CloudTask cloudTask = new CloudTaskService(registryCenter).getTask(jobName);
                JobAPIFactory.createJobOperateAPI(cloudTask.getConnectString(), cloudTask.getNamespace(), cloudTask.getDigest()).trigger(Optional.of(jobName), Optional.<String>absent());
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
