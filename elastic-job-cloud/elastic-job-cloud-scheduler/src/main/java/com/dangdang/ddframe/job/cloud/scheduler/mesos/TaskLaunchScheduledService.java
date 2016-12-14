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

import com.dangdang.ddframe.job.api.JobType;
import com.dangdang.ddframe.job.cloud.scheduler.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.cloud.scheduler.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.JobExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.framework.MesosSchedulerContext;
import com.dangdang.ddframe.job.context.ExecutionType;
import com.dangdang.ddframe.job.context.TaskContext;
import com.dangdang.ddframe.job.event.type.JobStatusTraceEvent;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.util.config.ShardingItemParameters;
import com.dangdang.ddframe.job.util.json.GsonFactory;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.protobuf.ByteString;
import com.netflix.fenzo.TaskAssignmentResult;
import com.netflix.fenzo.VMAssignmentResult;
import com.netflix.fenzo.VirtualMachineLease;
import lombok.RequiredArgsConstructor;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 任务提交调度服务.
 * 
 * @author zhangliang
 * @author gaohongtao
 */
@RequiredArgsConstructor
public class TaskLaunchScheduledService extends AbstractScheduledService {
    
    private static final double EXECUTOR_DEFAULT_CPU_RESOURCE = 0.1d;
    
    private static final double EXECUTOR_DEFAULT_MEMORY_RESOURCE = 32d;
    
    private final MesosSchedulerContext context;
    
    private final SchedulerDriver schedulerDriver;
    
    private final BootstrapEnvironment env = BootstrapEnvironment.getInstance();
    
    @Override
    protected String serviceName() {
        return "task-launch-processor";
    }
    
    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(1000, 100, TimeUnit.MILLISECONDS);
    }
    
    @Override
    protected void runOneIteration() throws Exception {
        LaunchingTasks launchingTasks = new LaunchingTasks(context.getFacadeService().getEligibleJobContext());
        Collection<VMAssignmentResult> vmAssignmentResults = context.getTaskScheduler().scheduleOnce(launchingTasks.getPendingTasks(), context.getLeasesQueue().drainTo()).getResultMap().values();
        for (VMAssignmentResult each: vmAssignmentResults) {
            List<VirtualMachineLease> leasesUsed = each.getLeasesUsed();
            List<Protos.TaskInfo> taskInfoList = new ArrayList<>(each.getTasksAssigned().size() * 10);
            taskInfoList.addAll(getTaskInfoList(launchingTasks.getIntegrityViolationJobs(vmAssignmentResults), each, leasesUsed.get(0).hostname(), leasesUsed.get(0).getOffer().getSlaveId()));
            for (Protos.TaskInfo taskInfo : taskInfoList) {
                TaskContext taskContext = TaskContext.from(taskInfo.getTaskId().getValue());
                context.getFacadeService().addRunning(taskContext);
                context.getJobEventBus().post(createJobStatusTraceEvent(taskContext));
            }
            context.getFacadeService().removeLaunchTasksFromQueue(Lists.transform(taskInfoList, new Function<Protos.TaskInfo, TaskContext>() {
            
                @Override
                public TaskContext apply(final Protos.TaskInfo input) {
                    return TaskContext.from(input.getTaskId().getValue());
                }
            }));
            schedulerDriver.launchTasks(getOfferIDs(leasesUsed), taskInfoList);
        }
    }
    
    
    private List<Protos.TaskInfo> getTaskInfoList(final Collection<String> integrityViolationJobs, final VMAssignmentResult vmAssignmentResult, final String hostname, final Protos.SlaveID slaveId) {
        List<Protos.TaskInfo> result = new ArrayList<>(vmAssignmentResult.getTasksAssigned().size());
        for (TaskAssignmentResult each: vmAssignmentResult.getTasksAssigned()) {
            TaskContext taskContext = TaskContext.from(each.getTaskId());
            if (!integrityViolationJobs.contains(taskContext.getMetaInfo().getJobName()) && !context.getFacadeService().isRunning(taskContext)) {
                Protos.TaskInfo taskInfo = getTaskInfo(slaveId, each);
                if (null != taskInfo) {
                    result.add(taskInfo);
                    context.getFacadeService().addMapping(taskInfo.getTaskId().getValue(), hostname);
                    context.getTaskScheduler().getTaskAssigner().call(each.getRequest(), hostname);
                }
            }
        }
        return result;
    }
    
    private Protos.TaskInfo getTaskInfo(final Protos.SlaveID slaveID, final TaskAssignmentResult taskAssignmentResult) {
        TaskContext taskContext = TaskContext.from(taskAssignmentResult.getTaskId());
        Optional<CloudJobConfiguration> jobConfigOptional = context.getFacadeService().load(taskContext.getMetaInfo().getJobName());
        if (!jobConfigOptional.isPresent()) {
            return null;
        }
        taskContext.setSlaveId(slaveID.getValue());
        CloudJobConfiguration jobConfig = jobConfigOptional.get();
        ShardingContexts shardingContexts = getShardingContexts(taskContext, jobConfig);
        boolean useDefaultExecutor = JobExecutionType.TRANSIENT == jobConfig.getJobExecutionType() && JobType.SCRIPT == jobConfig.getTypeConfig().getJobType();
        Protos.CommandInfo.URI uri = buildURI(jobConfig.getAppURL(), useDefaultExecutor);
        Protos.CommandInfo command = buildCommand(uri, jobConfig.getBootstrapScript(), shardingContexts, useDefaultExecutor);
        return buildTaskInfo(taskContext, jobConfig, shardingContexts, slaveID, command, useDefaultExecutor);
    }
    
    private ShardingContexts getShardingContexts(final TaskContext taskContext, final CloudJobConfiguration jobConfig) {
        Map<Integer, String> shardingItemParameters = new ShardingItemParameters(jobConfig.getTypeConfig().getCoreConfig().getShardingItemParameters()).getMap();
        Map<Integer, String> assignedShardingItemParameters = new HashMap<>(1, 1);
        int shardingItem = taskContext.getMetaInfo().getShardingItems().get(0);
        assignedShardingItemParameters.put(shardingItem, shardingItemParameters.containsKey(shardingItem) ? shardingItemParameters.get(shardingItem) : "");
        return new ShardingContexts(taskContext.getId(), jobConfig.getJobName(), jobConfig.getTypeConfig().getCoreConfig().getShardingTotalCount(),
                jobConfig.getTypeConfig().getCoreConfig().getJobParameter(), assignedShardingItemParameters);
    }
    
    private Protos.CommandInfo.URI buildURI(final String appURL, final boolean useDefaultExecutor) {
        Protos.CommandInfo.URI.Builder result = Protos.CommandInfo.URI.newBuilder().setValue(appURL).setCache(BootstrapEnvironment.getInstance().getFrameworkConfiguration().isAppCacheEnable());
        if (useDefaultExecutor && !SupportedExtractionType.isExtraction(appURL)) {
            result.setExecutable(true);
        } else {
            result.setExtract(true);
        }
        return result.build();
    }
    
    private Protos.CommandInfo buildCommand(final Protos.CommandInfo.URI uri, final String bootstrapScript, final ShardingContexts shardingContexts, final boolean useDefaultExecutor) {
        Protos.CommandInfo.Builder result = Protos.CommandInfo.newBuilder().addUris(uri).setShell(true);
        if (useDefaultExecutor) {
            CommandLine commandLine = CommandLine.parse(bootstrapScript);
            commandLine.addArgument(GsonFactory.getGson().toJson(shardingContexts), false);
            result.setValue(Joiner.on(" ").join(commandLine.getExecutable(), Joiner.on(" ").join(commandLine.getArguments())));
        } else {
            result.setValue(bootstrapScript);
        }
        return result.build();
    }
    
    private Protos.TaskInfo buildTaskInfo(final TaskContext taskContext, final CloudJobConfiguration jobConfig, final ShardingContexts shardingContexts,
                                          final Protos.SlaveID slaveID, final Protos.CommandInfo command, final boolean useDefaultExecutor) {
        Protos.TaskInfo.Builder result = Protos.TaskInfo.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskContext.getId()).build())
                .setName(taskContext.getTaskName()).setSlaveId(slaveID).addResources(buildResource("cpus", jobConfig.getCpuCount())).addResources(buildResource("mem", jobConfig.getMemoryMB()))
                .setData(ByteString.copyFrom(new TaskInfoData(shardingContexts, jobConfig).serialize()));
        if (useDefaultExecutor) {
            return result.setCommand(command).build();
        }
        Protos.ExecutorInfo.Builder executorBuilder = Protos.ExecutorInfo.newBuilder().setExecutorId(Protos.ExecutorID.newBuilder().setValue(taskContext.getExecutorId(jobConfig.getAppURL())))
                .setCommand(command).addResources(buildResource("cpus", EXECUTOR_DEFAULT_CPU_RESOURCE)).addResources(buildResource("mem", EXECUTOR_DEFAULT_MEMORY_RESOURCE));
        if (env.getJobEventRdbConfiguration().isPresent()) {
            executorBuilder.setData(ByteString.copyFrom(SerializationUtils.serialize(env.getJobEventRdbConfigurationMap()))).build();
        }
        return result.setExecutor(executorBuilder.build()).build();
    }
    
    private Protos.Resource buildResource(final String type, final double resourceValue) {
        return Protos.Resource.newBuilder().setName(type).setType(Protos.Value.Type.SCALAR).setScalar(Protos.Value.Scalar.newBuilder().setValue(resourceValue)).build();
    }
    
    private JobStatusTraceEvent createJobStatusTraceEvent(final TaskContext taskContext) {
        TaskContext.MetaInfo metaInfo = taskContext.getMetaInfo();
        JobStatusTraceEvent result = new JobStatusTraceEvent(metaInfo.getJobName(), taskContext.getId(), taskContext.getSlaveId(),
                JobStatusTraceEvent.Source.CLOUD_SCHEDULER, taskContext.getType(), String.valueOf(metaInfo.getShardingItems()), JobStatusTraceEvent.State.TASK_STAGING, "");
        if (ExecutionType.FAILOVER == taskContext.getType()) {
            Optional<String> taskContextOptional = context.getFacadeService().getFailoverTaskId(metaInfo);
            if (taskContextOptional.isPresent()) {
                result.setOriginalTaskId(taskContextOptional.get());
            }
        }
        return result;
    }
    
    private List<Protos.OfferID> getOfferIDs(final List<VirtualMachineLease> leasesUsed) {
        List<Protos.OfferID> result = new ArrayList<>();
        for (VirtualMachineLease virtualMachineLease: leasesUsed) {
            result.add(virtualMachineLease.getOffer().getId());
        }
        return result;
    }
}
