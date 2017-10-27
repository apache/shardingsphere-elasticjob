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

package io.elasticjob.cloud.scheduler.mesos;

import io.elasticjob.api.JobType;
import io.elasticjob.cloud.scheduler.env.BootstrapEnvironment;
import io.elasticjob.cloud.scheduler.config.job.CloudJobConfiguration;
import io.elasticjob.cloud.scheduler.config.job.CloudJobExecutionType;
import io.elasticjob.cloud.scheduler.config.app.CloudAppConfiguration;
import io.elasticjob.config.script.ScriptJobConfiguration;
import io.elasticjob.context.ExecutionType;
import io.elasticjob.context.TaskContext;
import io.elasticjob.event.JobEventBus;
import io.elasticjob.event.type.JobStatusTraceEvent;
import io.elasticjob.event.type.JobStatusTraceEvent.Source;
import io.elasticjob.executor.ShardingContexts;
import io.elasticjob.util.config.ShardingItemParameters;
import io.elasticjob.util.json.GsonFactory;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.protobuf.ByteString;
import com.netflix.fenzo.TaskAssignmentResult;
import com.netflix.fenzo.TaskRequest;
import com.netflix.fenzo.TaskScheduler;
import com.netflix.fenzo.VMAssignmentResult;
import com.netflix.fenzo.VirtualMachineLease;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.OfferID;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.SchedulerDriver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * 任务提交调度服务.
 * 
 * @author zhangliang
 * @author gaohongtao
 */
@RequiredArgsConstructor
@Slf4j
public final class TaskLaunchScheduledService extends AbstractScheduledService {
    
    private final SchedulerDriver schedulerDriver;
    
    private final TaskScheduler taskScheduler;
    
    private final FacadeService facadeService;
    
    private final JobEventBus jobEventBus;
    
    private final BootstrapEnvironment env = BootstrapEnvironment.getInstance();
    
    @Override
    protected String serviceName() {
        return "task-launch-processor";
    }
    
    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(2, 10, TimeUnit.SECONDS);
    }
    
    @Override
    protected void startUp() throws Exception {
        log.info("Elastic Job: Start {}", serviceName());
        AppConstraintEvaluator.init(facadeService);
    }
    
    @Override
    protected void shutDown() throws Exception {
        log.info("Elastic Job: Stop {}", serviceName());
    }
    
    @Override
    protected void runOneIteration() throws Exception {
        try {
            LaunchingTasks launchingTasks = new LaunchingTasks(facadeService.getEligibleJobContext());
            List<TaskRequest> taskRequests = launchingTasks.getPendingTasks();
            if (!taskRequests.isEmpty()) {
                AppConstraintEvaluator.getInstance().loadAppRunningState();
            }
            Collection<VMAssignmentResult> vmAssignmentResults = taskScheduler.scheduleOnce(taskRequests, LeasesQueue.getInstance().drainTo()).getResultMap().values();
            List<TaskContext> taskContextsList = new LinkedList<>();
            Map<List<Protos.OfferID>, List<Protos.TaskInfo>> offerIdTaskInfoMap = new HashMap<>();
            for (VMAssignmentResult each: vmAssignmentResults) {
                List<VirtualMachineLease> leasesUsed = each.getLeasesUsed();
                List<Protos.TaskInfo> taskInfoList = new ArrayList<>(each.getTasksAssigned().size() * 10);
                taskInfoList.addAll(getTaskInfoList(launchingTasks.getIntegrityViolationJobs(vmAssignmentResults), each, leasesUsed.get(0).hostname(), leasesUsed.get(0).getOffer()));
                for (Protos.TaskInfo taskInfo : taskInfoList) {
                    taskContextsList.add(TaskContext.from(taskInfo.getTaskId().getValue()));
                }
                offerIdTaskInfoMap.put(getOfferIDs(leasesUsed), taskInfoList);
            }
            for (TaskContext each : taskContextsList) {
                facadeService.addRunning(each);
                jobEventBus.post(createJobStatusTraceEvent(each));
            }
            facadeService.removeLaunchTasksFromQueue(taskContextsList);
            for (Entry<List<OfferID>, List<TaskInfo>> each : offerIdTaskInfoMap.entrySet()) {
                schedulerDriver.launchTasks(each.getKey(), each.getValue());
            }
            //CHECKSTYLE:OFF
        } catch (Throwable throwable) {
            //CHECKSTYLE:ON
            log.error("Launch task error", throwable);
        } finally {
            AppConstraintEvaluator.getInstance().clearAppRunningState();
        }
    }
    
    private List<Protos.TaskInfo> getTaskInfoList(final Collection<String> integrityViolationJobs, final VMAssignmentResult vmAssignmentResult, final String hostname, final Protos.Offer offer) {
        List<Protos.TaskInfo> result = new ArrayList<>(vmAssignmentResult.getTasksAssigned().size());
        for (TaskAssignmentResult each: vmAssignmentResult.getTasksAssigned()) {
            TaskContext taskContext = TaskContext.from(each.getTaskId());
            String jobName = taskContext.getMetaInfo().getJobName();
            if (!integrityViolationJobs.contains(jobName) && !facadeService.isRunning(taskContext) && !facadeService.isJobDisabled(jobName)) {
                Protos.TaskInfo taskInfo = getTaskInfo(offer, each);
                if (null != taskInfo) {
                    result.add(taskInfo);
                    facadeService.addMapping(taskInfo.getTaskId().getValue(), hostname);
                    taskScheduler.getTaskAssigner().call(each.getRequest(), hostname);
                }
            }
        }
        return result;
    }
    
    private Protos.TaskInfo getTaskInfo(final Protos.Offer offer, final TaskAssignmentResult taskAssignmentResult) {
        TaskContext taskContext = TaskContext.from(taskAssignmentResult.getTaskId());
        Optional<CloudJobConfiguration> jobConfigOptional = facadeService.load(taskContext.getMetaInfo().getJobName());
        if (!jobConfigOptional.isPresent()) {
            return null;
        }
        CloudJobConfiguration jobConfig = jobConfigOptional.get();
        Optional<CloudAppConfiguration> appConfigOptional = facadeService.loadAppConfig(jobConfig.getAppName());
        if (!appConfigOptional.isPresent()) {
            return null;
        }
        CloudAppConfiguration appConfig = appConfigOptional.get();
        taskContext.setSlaveId(offer.getSlaveId().getValue());
        ShardingContexts shardingContexts = getShardingContexts(taskContext, appConfig, jobConfig);
        boolean isCommandExecutor = CloudJobExecutionType.TRANSIENT == jobConfig.getJobExecutionType() && JobType.SCRIPT == jobConfig.getTypeConfig().getJobType();
        String script = appConfig.getBootstrapScript();
        if (isCommandExecutor) {
            script = ((ScriptJobConfiguration) jobConfig.getTypeConfig()).getScriptCommandLine();
        }
        Protos.CommandInfo.URI uri = buildURI(appConfig, isCommandExecutor);
        Protos.CommandInfo command = buildCommand(uri, script, shardingContexts, isCommandExecutor);
        if (isCommandExecutor) {
            return buildCommandExecutorTaskInfo(taskContext, jobConfig, shardingContexts, offer, command);
        } else {
            return buildCustomizedExecutorTaskInfo(taskContext, appConfig, jobConfig, shardingContexts, offer, command);
        }
    }
    
    private ShardingContexts getShardingContexts(final TaskContext taskContext, final CloudAppConfiguration appConfig, final CloudJobConfiguration jobConfig) {
        Map<Integer, String> shardingItemParameters = new ShardingItemParameters(jobConfig.getTypeConfig().getCoreConfig().getShardingItemParameters()).getMap();
        Map<Integer, String> assignedShardingItemParameters = new HashMap<>(1, 1);
        int shardingItem = taskContext.getMetaInfo().getShardingItems().get(0);
        assignedShardingItemParameters.put(shardingItem, shardingItemParameters.containsKey(shardingItem) ? shardingItemParameters.get(shardingItem) : "");
        return new ShardingContexts(taskContext.getId(), jobConfig.getJobName(), jobConfig.getTypeConfig().getCoreConfig().getShardingTotalCount(),
                jobConfig.getTypeConfig().getCoreConfig().getJobParameter(), assignedShardingItemParameters, appConfig.getEventTraceSamplingCount());
    }
    
    private Protos.TaskInfo buildCommandExecutorTaskInfo(final TaskContext taskContext, final CloudJobConfiguration jobConfig, final ShardingContexts shardingContexts,
                                                         final Protos.Offer offer, final Protos.CommandInfo command) {
        Protos.TaskInfo.Builder result = Protos.TaskInfo.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskContext.getId()).build())
                .setName(taskContext.getTaskName()).setSlaveId(offer.getSlaveId())
                .addResources(buildResource("cpus", jobConfig.getCpuCount(), offer.getResourcesList()))
                .addResources(buildResource("mem", jobConfig.getMemoryMB(), offer.getResourcesList()))
                .setData(ByteString.copyFrom(new TaskInfoData(shardingContexts, jobConfig).serialize()));
        return result.setCommand(command).build();
    }
    
    private Protos.TaskInfo buildCustomizedExecutorTaskInfo(final TaskContext taskContext, final CloudAppConfiguration appConfig, final CloudJobConfiguration jobConfig, 
                                                            final ShardingContexts shardingContexts, final Protos.Offer offer, final Protos.CommandInfo command) {
        Protos.TaskInfo.Builder result = Protos.TaskInfo.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskContext.getId()).build())
                .setName(taskContext.getTaskName()).setSlaveId(offer.getSlaveId())
                .addResources(buildResource("cpus", jobConfig.getCpuCount(), offer.getResourcesList()))
                .addResources(buildResource("mem", jobConfig.getMemoryMB(), offer.getResourcesList()))
                .setData(ByteString.copyFrom(new TaskInfoData(shardingContexts, jobConfig).serialize()));
        Protos.ExecutorInfo.Builder executorBuilder = Protos.ExecutorInfo.newBuilder().setExecutorId(Protos.ExecutorID.newBuilder()
                .setValue(taskContext.getExecutorId(jobConfig.getAppName()))).setCommand(command)
                .addResources(buildResource("cpus", appConfig.getCpuCount(), offer.getResourcesList()))
                .addResources(buildResource("mem", appConfig.getMemoryMB(), offer.getResourcesList()));
        if (env.getJobEventRdbConfiguration().isPresent()) {
            executorBuilder.setData(ByteString.copyFrom(SerializationUtils.serialize(env.getJobEventRdbConfigurationMap()))).build();
        }
        return result.setExecutor(executorBuilder.build()).build();
    }
    
    private Protos.CommandInfo.URI buildURI(final CloudAppConfiguration appConfig, final boolean isCommandExecutor) {
        Protos.CommandInfo.URI.Builder result = Protos.CommandInfo.URI.newBuilder().setValue(appConfig.getAppURL()).setCache(appConfig.isAppCacheEnable());
        if (isCommandExecutor && !SupportedExtractionType.isExtraction(appConfig.getAppURL())) {
            result.setExecutable(true);
        } else {
            result.setExtract(true);
        }
        return result.build();
    }
    
    private Protos.CommandInfo buildCommand(final Protos.CommandInfo.URI uri, final String script, final ShardingContexts shardingContexts, final boolean isCommandExecutor) {
        Protos.CommandInfo.Builder result = Protos.CommandInfo.newBuilder().addUris(uri).setShell(true);
        if (isCommandExecutor) {
            CommandLine commandLine = CommandLine.parse(script);
            commandLine.addArgument(GsonFactory.getGson().toJson(shardingContexts), false);
            result.setValue(Joiner.on(" ").join(commandLine.getExecutable(), Joiner.on(" ").join(commandLine.getArguments())));
        } else {
            result.setValue(script);
        }
        return result.build();
    }
    
    private Protos.Resource buildResource(final String type, final double resourceValue, final List<Protos.Resource> resources) {
        return Protos.Resource.newBuilder().mergeFrom(Iterables.find(resources, new Predicate<Protos.Resource>() {
            @Override
            public boolean apply(final Protos.Resource input) {
                return input.getName().equals(type);
            }
        })).setScalar(Protos.Value.Scalar.newBuilder().setValue(resourceValue)).build();
    }
    
    private JobStatusTraceEvent createJobStatusTraceEvent(final TaskContext taskContext) {
        TaskContext.MetaInfo metaInfo = taskContext.getMetaInfo();
        JobStatusTraceEvent result = new JobStatusTraceEvent(metaInfo.getJobName(), taskContext.getId(), taskContext.getSlaveId(),
                Source.CLOUD_SCHEDULER, taskContext.getType(), String.valueOf(metaInfo.getShardingItems()), JobStatusTraceEvent.State.TASK_STAGING, "");
        if (ExecutionType.FAILOVER == taskContext.getType()) {
            Optional<String> taskContextOptional = facadeService.getFailoverTaskId(metaInfo);
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
