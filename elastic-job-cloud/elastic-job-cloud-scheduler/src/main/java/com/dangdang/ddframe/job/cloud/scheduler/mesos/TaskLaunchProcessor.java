/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.scheduler.mesos;

import com.dangdang.ddframe.job.cloud.scheduler.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.context.ExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.context.JobContext;
import com.dangdang.ddframe.job.cloud.scheduler.context.TaskContext;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.util.BlockUtils;
import com.dangdang.ddframe.job.util.config.ShardingItemParameters;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.netflix.fenzo.TaskAssignmentResult;
import com.netflix.fenzo.TaskRequest;
import com.netflix.fenzo.TaskScheduler;
import com.netflix.fenzo.VMAssignmentResult;
import com.netflix.fenzo.VirtualMachineLease;
import lombok.RequiredArgsConstructor;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 任务启动处理器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class TaskLaunchProcessor implements Runnable {
    
    private static volatile boolean shutdown;
    
    private final LeasesQueue leasesQueue;
    
    private final SchedulerDriver schedulerDriver;
    
    private final TaskScheduler taskScheduler;
    
    private final FacadeService facadeService;
    
    /**
     * 线程关闭.
     */
    public static void shutdown() {
        shutdown = true;
    }
    
    @Override
    public void run() {
        while (!shutdown) {
            Collection<JobContext> eligibleJobContexts =  facadeService.getEligibleJobContext();
            Map<String, Integer> jobShardingTotalCountMap = new HashMap<>(eligibleJobContexts.size(), 1);
            List<TaskRequest> pendingTasks = new ArrayList<>(eligibleJobContexts.size() * 10);
            for (JobContext each : eligibleJobContexts) {
                pendingTasks.addAll(getTaskRequests(each));
                if (ExecutionType.FAILOVER != each.getType()) {
                    jobShardingTotalCountMap.put(each.getJobConfig().getJobName(), each.getJobConfig().getTypeConfig().getCoreConfig().getShardingTotalCount());
                }
            }
            Collection<VMAssignmentResult> vmAssignmentResults = taskScheduler.scheduleOnce(pendingTasks, leasesQueue.drainTo()).getResultMap().values();
            Collection<String> integrityViolationJobs = getIntegrityViolationJobs(jobShardingTotalCountMap, vmAssignmentResults);
            for (VMAssignmentResult each: vmAssignmentResults) {
                List<VirtualMachineLease> leasesUsed = each.getLeasesUsed();
                List<Protos.TaskInfo> taskInfoList = new ArrayList<>(each.getTasksAssigned().size() * 10);
                taskInfoList.addAll(getTaskInfoList(integrityViolationJobs, each, leasesUsed.get(0).hostname(), leasesUsed.get(0).getOffer().getSlaveId()));
                schedulerDriver.launchTasks(getOfferIDs(leasesUsed), taskInfoList);
                facadeService.removeLaunchTasksFromQueue(Lists.transform(taskInfoList, new Function<Protos.TaskInfo, TaskContext>() {
                    
                    @Override
                    public TaskContext apply(final Protos.TaskInfo input) {
                        return TaskContext.from(input.getTaskId().getValue());
                    }
                }));
                for (Protos.TaskInfo taskInfo : taskInfoList) {
                    facadeService.addRunning(TaskContext.from(taskInfo.getTaskId().getValue()));
                }
            }
            BlockUtils.waitingShortTime();
        }
    }
    
    private Collection<TaskRequest> getTaskRequests(final JobContext jobContext) {
        Collection<TaskRequest> result = new ArrayList<>(jobContext.getAssignedShardingItems().size());
        CloudJobConfiguration jobConfig = jobContext.getJobConfig();
        for (int each : jobContext.getAssignedShardingItems()) {
            result.add(new JobTaskRequest(new TaskContext(jobConfig.getJobName(), each, jobContext.getType(), "fake-slave"), jobConfig));
        }
        return result;
    }
    
    private Collection<String> getIntegrityViolationJobs(final Map<String, Integer> jobShardingTotalCountMap, final Collection<VMAssignmentResult> vmAssignmentResults) {
        Map<String, Integer> assignedJobShardingTotalCountMap = new HashMap<>(jobShardingTotalCountMap.size(), 1);
        for (VMAssignmentResult vmAssignmentResult: vmAssignmentResults) {
            for (TaskAssignmentResult tasksAssigned: vmAssignmentResult.getTasksAssigned()) {
                String jobName = TaskContext.from(tasksAssigned.getTaskId()).getMetaInfo().getJobName();
                if (assignedJobShardingTotalCountMap.containsKey(jobName)) {
                    assignedJobShardingTotalCountMap.put(jobName, assignedJobShardingTotalCountMap.get(jobName) + 1);
                } else {
                    assignedJobShardingTotalCountMap.put(jobName, 1);
                }
            }
        }
        Collection<String> result = new HashSet<>(assignedJobShardingTotalCountMap.size(), 1);
        for (Map.Entry<String, Integer> entry : assignedJobShardingTotalCountMap.entrySet()) {
            if (jobShardingTotalCountMap.containsKey(entry.getKey()) && !entry.getValue().equals(jobShardingTotalCountMap.get(entry.getKey()))) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
    
    private List<Protos.TaskInfo> getTaskInfoList(final Collection<String> integrityViolationJobs, final VMAssignmentResult vmAssignmentResult, final String hostname, final Protos.SlaveID slaveId) {
        List<Protos.TaskInfo> result = new ArrayList<>(vmAssignmentResult.getTasksAssigned().size());
        for (TaskAssignmentResult each: vmAssignmentResult.getTasksAssigned()) {
            TaskContext taskContext = TaskContext.from(each.getTaskId());
            if (!integrityViolationJobs.contains(taskContext.getMetaInfo().getJobName()) && !facadeService.isRunning(taskContext)) {
                result.add(getTaskInfo(slaveId, each));
                taskScheduler.getTaskAssigner().call(each.getRequest(), hostname);
            }
        }
        return result;
    }
    
    private Protos.TaskInfo getTaskInfo(final Protos.SlaveID slaveID, final TaskAssignmentResult taskAssignmentResult) {
        TaskContext originalTaskContext = TaskContext.from(taskAssignmentResult.getTaskId());
        int shardingItem = originalTaskContext.getMetaInfo().getShardingItem();
        TaskContext taskContext = new TaskContext(originalTaskContext.getMetaInfo().getJobName(), shardingItem, originalTaskContext.getType(), slaveID.getValue());
        CloudJobConfiguration jobConfig = facadeService.load(taskContext.getMetaInfo().getJobName()).get();
        Map<Integer, String> shardingItemParameters = new ShardingItemParameters(jobConfig.getTypeConfig().getCoreConfig().getShardingItemParameters()).getMap();
        Map<Integer, String> assignedShardingItemParameters = new HashMap<>(1, 1);
        assignedShardingItemParameters.put(shardingItem, shardingItemParameters.containsKey(shardingItem) ? shardingItemParameters.get(shardingItem) : "");
        ShardingContexts shardingContexts = new ShardingContexts(
                jobConfig.getJobName(), jobConfig.getTypeConfig().getCoreConfig().getShardingTotalCount(), jobConfig.getTypeConfig().getCoreConfig().getJobParameter(), assignedShardingItemParameters);
        // TODO 更改cache为elastic-job-cloud-scheduler.properties配置
        Protos.CommandInfo.URI uri = Protos.CommandInfo.URI.newBuilder().setValue(jobConfig.getAppURL()).setExtract(true).setCache(false).build();
        Protos.CommandInfo command = Protos.CommandInfo.newBuilder().addUris(uri).setShell(true).setValue(jobConfig.getBootstrapScript()).build();
        Protos.ExecutorInfo executorInfo = 
                Protos.ExecutorInfo.newBuilder().setExecutorId(Protos.ExecutorID.newBuilder().setValue(taskContext.getExecutorId(jobConfig.getAppURL()))).setCommand(command).build();
        return Protos.TaskInfo.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskContext.getId()).build())
                .setName(taskContext.getTaskName())
                .setSlaveId(slaveID)
                .addResources(buildResource("cpus", jobConfig.getCpuCount()))
                .addResources(buildResource("mem", jobConfig.getMemoryMB()))
                .setExecutor(executorInfo)
                .setData(ByteString.copyFrom(new TaskInfoData(shardingContexts, jobConfig).serialize()))
                .build();
    }
    
    private Protos.Resource.Builder buildResource(final String type, final double resourceValue) {
        return Protos.Resource.newBuilder().setName(type).setType(Protos.Value.Type.SCALAR).setScalar(Protos.Value.Scalar.newBuilder().setValue(resourceValue));
    }
    
    private List<Protos.OfferID> getOfferIDs(final List<VirtualMachineLease> leasesUsed) {
        List<Protos.OfferID> offerIDs = new ArrayList<>();
        for (VirtualMachineLease virtualMachineLease: leasesUsed) {
            offerIDs.add(virtualMachineLease.getOffer().getId());
        }
        return offerIDs;
    }
}
