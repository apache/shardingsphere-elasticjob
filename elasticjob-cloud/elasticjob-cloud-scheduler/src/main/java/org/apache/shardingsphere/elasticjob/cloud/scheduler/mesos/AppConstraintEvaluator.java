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

import com.google.common.base.Preconditions;
import com.netflix.fenzo.ConstraintEvaluator;
import com.netflix.fenzo.TaskAssignmentResult;
import com.netflix.fenzo.TaskRequest;
import com.netflix.fenzo.TaskTrackerState;
import com.netflix.fenzo.VirtualMachineCurrentState;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.pojo.CloudAppConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.cloud.config.pojo.CloudJobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.MesosStateService.ExecutorStateInfo;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext;
import org.codehaus.jettison.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * App constrain evaluator.
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppConstraintEvaluator implements ConstraintEvaluator {
    
    private static AppConstraintEvaluator instance;
    
    private final Set<String> runningApps = new HashSet<>();
    
    private final FacadeService facadeService;
    
    /**
     * Init.
     * 
     * @param facadeService Mesos facade service
     */
    public static void init(final FacadeService facadeService) {
        instance = new AppConstraintEvaluator(facadeService);
    }
    
    static AppConstraintEvaluator getInstance() {
        return Preconditions.checkNotNull(instance);
    }
    
    void loadAppRunningState() {
        try {
            for (ExecutorStateInfo each : facadeService.loadExecutorInfo()) {
                runningApps.add(each.getId());
            }
        } catch (final JSONException | UniformInterfaceException | ClientHandlerException e) {
            clearAppRunningState();
        }
    }
    
    void clearAppRunningState() {
        runningApps.clear();
    }
    
    @Override
    public String getName() {
        return "App-Fitness-Calculator";
    }
    
    @Override
    public Result evaluate(final TaskRequest taskRequest, final VirtualMachineCurrentState targetVM, final TaskTrackerState taskTrackerState) {
        double assigningCpus = 0.0d;
        double assigningMemoryMB = 0.0d;
        final String slaveId = targetVM.getAllCurrentOffers().iterator().next().getSlaveId().getValue();
        try {
            if (isAppRunningOnSlave(taskRequest.getId(), slaveId)) {
                return new Result(true, "");
            }
            Set<String> calculatedApps = new HashSet<>();
            List<TaskRequest> taskRequests = new ArrayList<>(targetVM.getTasksCurrentlyAssigned().size() + 1);
            taskRequests.add(taskRequest);
            for (TaskAssignmentResult each : targetVM.getTasksCurrentlyAssigned()) {
                taskRequests.add(each.getRequest());
            }
            for (TaskRequest each : taskRequests) {
                assigningCpus += each.getCPUs();
                assigningMemoryMB += each.getMemory();
                if (isAppRunningOnSlave(each.getId(), slaveId)) {
                    continue;
                }
                CloudAppConfigurationPOJO assigningAppConfig = getAppConfiguration(each.getId());
                if (!calculatedApps.add(assigningAppConfig.getAppName())) {
                    continue;
                }
                assigningCpus += assigningAppConfig.getCpuCount();
                assigningMemoryMB += assigningAppConfig.getMemoryMB();
            }
        } catch (final LackConfigException ex) {
            log.warn("Lack config, disable {}", getName(), ex);
            return new Result(true, "");
        }
        if (assigningCpus > targetVM.getCurrAvailableResources().cpuCores()) {
            log.debug("Failure {} {} cpus:{}/{}", taskRequest.getId(), slaveId, assigningCpus, targetVM.getCurrAvailableResources().cpuCores());
            return new Result(false, String.format("cpu:%s/%s", assigningCpus, targetVM.getCurrAvailableResources().cpuCores()));
        }
        if (assigningMemoryMB > targetVM.getCurrAvailableResources().memoryMB()) {
            log.debug("Failure {} {} mem:{}/{}", taskRequest.getId(), slaveId, assigningMemoryMB, targetVM.getCurrAvailableResources().memoryMB());
            return new Result(false, String.format("mem:%s/%s", assigningMemoryMB, targetVM.getCurrAvailableResources().memoryMB()));
        }
        log.debug("Success {} {} cpus:{}/{} mem:{}/{}", taskRequest.getId(), slaveId, assigningCpus, targetVM.getCurrAvailableResources()
                .cpuCores(), assigningMemoryMB, targetVM.getCurrAvailableResources().memoryMB());
        return new Result(true, String.format("cpus:%s/%s mem:%s/%s", assigningCpus, targetVM.getCurrAvailableResources()
                .cpuCores(), assigningMemoryMB, targetVM.getCurrAvailableResources().memoryMB()));
    }
    
    private boolean isAppRunningOnSlave(final String taskId, final String slaveId) throws LackConfigException {
        TaskContext taskContext = TaskContext.from(taskId);
        taskContext.setSlaveId(slaveId);
        return runningApps.contains(taskContext.getExecutorId(getJobConfiguration(taskContext).getAppName()));
    }
    
    private CloudAppConfigurationPOJO getAppConfiguration(final String taskId) throws LackConfigException {
        CloudJobConfiguration cloudJobConfig = getJobConfiguration(TaskContext.from(taskId));
        Optional<CloudAppConfigurationPOJO> appConfigOptional = facadeService.loadAppConfig(cloudJobConfig.getAppName());
        if (!appConfigOptional.isPresent()) {
            throw new LackConfigException("APP", cloudJobConfig.getAppName());
        }
        return appConfigOptional.get();
    }

    private CloudJobConfiguration getJobConfiguration(final TaskContext taskContext) throws LackConfigException {
        Optional<CloudJobConfigurationPOJO> cloudJobConfig = facadeService.load(taskContext.getMetaInfo().getJobName());
        if (!cloudJobConfig.isPresent()) {
            throw new LackConfigException("JOB", taskContext.getMetaInfo().getJobName());
        }
        return cloudJobConfig.get().toCloudJobConfiguration();
    }
    
    private class LackConfigException extends Exception {
        
        LackConfigException(final String scope, final String configName) {
            super(String.format("Lack %s's config %s", scope, configName));
        }
    }
}
