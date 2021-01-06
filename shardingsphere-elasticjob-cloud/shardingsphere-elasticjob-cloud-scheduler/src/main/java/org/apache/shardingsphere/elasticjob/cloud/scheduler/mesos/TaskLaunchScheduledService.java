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
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.config.pojo.CloudJobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.pojo.CloudAppConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.BootstrapEnvironment;
import org.apache.shardingsphere.elasticjob.infra.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.infra.context.ShardingItemParameters;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext.MetaInfo;
import org.apache.shardingsphere.elasticjob.infra.json.GsonFactory;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.script.props.ScriptJobProperties;
import org.apache.shardingsphere.elasticjob.tracing.JobTracingEventBus;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent.Source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Task launch schedule service.
 */
@RequiredArgsConstructor
@Slf4j
public final class TaskLaunchScheduledService extends AbstractScheduledService {
    
    private final SchedulerDriver schedulerDriver;
    
    private final TaskScheduler taskScheduler;
    
    private final FacadeService facadeService;
    
    private final JobTracingEventBus jobTracingEventBus;
    
    private final BootstrapEnvironment env = BootstrapEnvironment.getINSTANCE();
    
    @Override
    protected String serviceName() {
        return "task-launch-processor";
    }
    
    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(2, 10, TimeUnit.SECONDS);
    }
    
    @Override
    protected void startUp() {
        log.info("Elastic Job: Start {}", serviceName());
        AppConstraintEvaluator.init(facadeService);
    }
    
    @Override
    protected void shutDown() {
        log.info("Elastic Job: Stop {}", serviceName());
    }
    
    @Override
    protected void runOneIteration() {
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
                jobTracingEventBus.post(createJobStatusTraceEvent(each));
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
        Optional<CloudJobConfigurationPOJO> cloudJobConfig = facadeService.load(taskContext.getMetaInfo().getJobName());
        if (!cloudJobConfig.isPresent()) {
            return null;
        }
        Optional<CloudAppConfigurationPOJO> appConfig = facadeService.loadAppConfig(cloudJobConfig.get().getAppName());
        if (!appConfig.isPresent()) {
            return null;
        }
        taskContext.setSlaveId(offer.getSlaveId().getValue());
        ShardingContexts shardingContexts = getShardingContexts(taskContext, appConfig.get(), cloudJobConfig.get().toCloudJobConfiguration());
        boolean isCommandExecutor = CloudJobExecutionType.TRANSIENT == cloudJobConfig.get().getJobExecutionType()
                && cloudJobConfig.get().getProps().contains(ScriptJobProperties.SCRIPT_KEY);
        String script = appConfig.get().getBootstrapScript();
        if (isCommandExecutor) {
            script = cloudJobConfig.get().getProps().getProperty(ScriptJobProperties.SCRIPT_KEY);
        }
        Protos.CommandInfo.URI uri = buildURI(appConfig.get(), isCommandExecutor);
        Protos.CommandInfo command = buildCommand(uri, script, shardingContexts, isCommandExecutor);
        if (isCommandExecutor) {
            return buildCommandExecutorTaskInfo(taskContext, cloudJobConfig.get().toCloudJobConfiguration(), shardingContexts, offer, command);
        } else {
            return buildCustomizedExecutorTaskInfo(taskContext, appConfig.get(), cloudJobConfig.get().toCloudJobConfiguration(), shardingContexts, offer, command);
        }
    }
    
    private ShardingContexts getShardingContexts(final TaskContext taskContext, final CloudAppConfigurationPOJO appConfig, final CloudJobConfiguration cloudJobConfig) {
        Map<Integer, String> shardingItemParameters = new ShardingItemParameters(cloudJobConfig.getJobConfig().getShardingItemParameters()).getMap();
        Map<Integer, String> assignedShardingItemParameters = new HashMap<>(1, 1);
        int shardingItem = taskContext.getMetaInfo().getShardingItems().get(0);
        assignedShardingItemParameters.put(shardingItem, shardingItemParameters.getOrDefault(shardingItem, ""));
        return new ShardingContexts(taskContext.getId(), cloudJobConfig.getJobConfig().getJobName(), cloudJobConfig.getJobConfig().getShardingTotalCount(),
                cloudJobConfig.getJobConfig().getJobParameter(), assignedShardingItemParameters, appConfig.getEventTraceSamplingCount());
    }
    
    private Protos.TaskInfo buildCommandExecutorTaskInfo(final TaskContext taskContext, final CloudJobConfiguration cloudJobConfig, final ShardingContexts shardingContexts,
                                                         final Protos.Offer offer, final Protos.CommandInfo command) {
        Protos.TaskInfo.Builder result = Protos.TaskInfo.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskContext.getId()).build())
                .setName(taskContext.getTaskName()).setSlaveId(offer.getSlaveId())
                .addResources(buildResource("cpus", cloudJobConfig.getCpuCount(), offer.getResourcesList()))
                .addResources(buildResource("mem", cloudJobConfig.getMemoryMB(), offer.getResourcesList()))
                .setData(ByteString.copyFrom(new TaskInfoData(shardingContexts, cloudJobConfig).serialize()));
        return result.setCommand(command).build();
    }
    
    private Protos.TaskInfo buildCustomizedExecutorTaskInfo(final TaskContext taskContext, final CloudAppConfigurationPOJO appConfig, final CloudJobConfiguration cloudJobConfig,
                                                            final ShardingContexts shardingContexts, final Protos.Offer offer, final Protos.CommandInfo command) {
        Protos.TaskInfo.Builder result = Protos.TaskInfo.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskContext.getId()).build())
                .setName(taskContext.getTaskName()).setSlaveId(offer.getSlaveId())
                .addResources(buildResource("cpus", cloudJobConfig.getCpuCount(), offer.getResourcesList()))
                .addResources(buildResource("mem", cloudJobConfig.getMemoryMB(), offer.getResourcesList()))
                .setData(ByteString.copyFrom(new TaskInfoData(shardingContexts, cloudJobConfig).serialize()));
        Protos.ExecutorInfo.Builder executorBuilder = Protos.ExecutorInfo.newBuilder().setExecutorId(Protos.ExecutorID.newBuilder()
                .setValue(taskContext.getExecutorId(cloudJobConfig.getAppName()))).setCommand(command)
                .addResources(buildResource("cpus", appConfig.getCpuCount(), offer.getResourcesList()))
                .addResources(buildResource("mem", appConfig.getMemoryMB(), offer.getResourcesList()));
        if (env.getTracingConfiguration().isPresent()) {
            executorBuilder.setData(ByteString.copyFrom(SerializationUtils.serialize(env.getJobEventRdbConfigurationMap()))).build();
        }
        return result.setExecutor(executorBuilder.build()).build();
    }
    
    private Protos.CommandInfo.URI buildURI(final CloudAppConfigurationPOJO appConfig, final boolean isCommandExecutor) {
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
            result.setValue(String.join("-", commandLine.getExecutable(), getArguments(commandLine)));
        } else {
            result.setValue(script);
        }
        return result.build();
    }
    
    private String getArguments(final CommandLine commandLine) {
        return String.join(" ", commandLine.getArguments());
    }
    
    private Protos.Resource buildResource(final String type, final double resourceValue, final List<Protos.Resource> resources) {
        return Protos.Resource.newBuilder().mergeFrom(
                resources.stream().filter(input -> input.getName().equals(type)).findFirst().get()).setScalar(Protos.Value.Scalar.newBuilder().setValue(resourceValue)).build();
    }
    
    private JobStatusTraceEvent createJobStatusTraceEvent(final TaskContext taskContext) {
        MetaInfo metaInfo = taskContext.getMetaInfo();
        JobStatusTraceEvent result = new JobStatusTraceEvent(metaInfo.getJobName(), taskContext.getId(), taskContext.getSlaveId(),
                Source.CLOUD_SCHEDULER, taskContext.getType().toString(), String.valueOf(metaInfo.getShardingItems()), JobStatusTraceEvent.State.TASK_STAGING, "");
        if (ExecutionType.FAILOVER == taskContext.getType()) {
            Optional<String> taskContextOptional = facadeService.getFailoverTaskId(metaInfo);
            taskContextOptional.ifPresent(result::setOriginalTaskId);
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
