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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.state.ready;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.cloud.config.pojo.CloudJobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.context.JobContext;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.BootstrapEnvironment;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running.RunningService;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Ready service.
 */
@Slf4j
public final class ReadyService {
    
    private final BootstrapEnvironment env = BootstrapEnvironment.getINSTANCE();
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final CloudJobConfigurationService configService;
    
    private final RunningService runningService;
    
    public ReadyService(final CoordinatorRegistryCenter regCenter) {
        this.regCenter = regCenter;
        configService = new CloudJobConfigurationService(regCenter);
        runningService = new RunningService(regCenter);
    }
    
    /**
     * Add transient job to ready queue.
     * 
     * @param jobName job name
     */
    public void addTransient(final String jobName) {
        if (regCenter.getNumChildren(ReadyNode.ROOT) > env.getFrameworkConfiguration().getJobStateQueueSize()) {
            log.warn("Cannot add transient job, caused by read state queue size is larger than {}.", env.getFrameworkConfiguration().getJobStateQueueSize());
            return;
        }
        Optional<CloudJobConfigurationPOJO> cloudJobConfig = configService.load(jobName);
        if (!cloudJobConfig.isPresent() || CloudJobExecutionType.TRANSIENT != cloudJobConfig.get().getJobExecutionType()) {
            return;
        }
        String readyJobNode = ReadyNode.getReadyJobNodePath(jobName);
        String times = regCenter.getDirectly(readyJobNode);
        if (cloudJobConfig.get().isMisfire()) {
            regCenter.persist(readyJobNode, Integer.toString(null == times ? 1 : Integer.parseInt(times) + 1));
        } else {
            regCenter.persist(ReadyNode.getReadyJobNodePath(jobName), "1");
        }
    }
    
    /**
     * Add daemon job to ready queue.
     *
     * @param jobName job name
     */
    public void addDaemon(final String jobName) {
        if (regCenter.getNumChildren(ReadyNode.ROOT) > env.getFrameworkConfiguration().getJobStateQueueSize()) {
            log.warn("Cannot add daemon job, caused by read state queue size is larger than {}.", env.getFrameworkConfiguration().getJobStateQueueSize());
            return;
        }
        Optional<CloudJobConfigurationPOJO> cloudJobConfig = configService.load(jobName);
        if (!cloudJobConfig.isPresent() || CloudJobExecutionType.DAEMON != cloudJobConfig.get().getJobExecutionType() || runningService.isJobRunning(jobName)) {
            return;
        }
        regCenter.persist(ReadyNode.getReadyJobNodePath(jobName), "1");
    }
    
    /**
     * Set misfire disabled.
     * 
     * @param jobName job name
     */
    public void setMisfireDisabled(final String jobName) {
        Optional<CloudJobConfigurationPOJO> cloudJobConfig = configService.load(jobName);
        if (cloudJobConfig.isPresent() && null != regCenter.getDirectly(ReadyNode.getReadyJobNodePath(jobName))) {
            regCenter.persist(ReadyNode.getReadyJobNodePath(jobName), "1");
        }
    }
    
    /**
     * Get all the eligible job contexts from ready queue.
     *
     * @param ineligibleJobContexts ineligible job contexts
     * @return collection of eligible contexts
     */
    public Collection<JobContext> getAllEligibleJobContexts(final Collection<JobContext> ineligibleJobContexts) {
        if (!regCenter.isExisted(ReadyNode.ROOT)) {
            return Collections.emptyList();
        }
        Collection<String> ineligibleJobNames = ineligibleJobContexts.stream().map(input -> input.getCloudJobConfig().getJobConfig().getJobName()).collect(Collectors.toList());
        List<String> jobNames = regCenter.getChildrenKeys(ReadyNode.ROOT);
        List<JobContext> result = new ArrayList<>(jobNames.size());
        for (String each : jobNames) {
            if (ineligibleJobNames.contains(each)) {
                continue;
            }
            Optional<CloudJobConfigurationPOJO> jobConfig = configService.load(each);
            if (!jobConfig.isPresent()) {
                regCenter.remove(ReadyNode.getReadyJobNodePath(each));
                continue;
            }
            if (!runningService.isJobRunning(each)) {
                result.add(JobContext.from(jobConfig.get().toCloudJobConfiguration(), ExecutionType.READY));
            }
        }
        return result;
    }
    
    /**
     * Remove jobs from ready queue.
     *
     * @param jobNames collection of jobs to be removed
     */
    public void remove(final Collection<String> jobNames) {
        for (String each : jobNames) {
            String readyJobNode = ReadyNode.getReadyJobNodePath(each);
            String timesStr = regCenter.getDirectly(readyJobNode);
            int times = null == timesStr ? 0 : Integer.parseInt(timesStr);
            if (times <= 1) {
                regCenter.remove(readyJobNode);
            } else {
                regCenter.persist(readyJobNode, Integer.toString(times - 1));
            }
        }
    }
    
    /**
     * Get all ready tasks.
     * 
     * @return all ready tasks
     */
    public Map<String, Integer> getAllReadyTasks() {
        if (!regCenter.isExisted(ReadyNode.ROOT)) {
            return Collections.emptyMap();
        }
        List<String> jobNames = regCenter.getChildrenKeys(ReadyNode.ROOT);
        Map<String, Integer> result = new HashMap<>(jobNames.size(), 1);
        for (String each : jobNames) {
            String times = regCenter.get(ReadyNode.getReadyJobNodePath(each));
            if (!Strings.isNullOrEmpty(times)) {
                result.put(each, Integer.parseInt(times));
            }
        }
        return result;
    }
}
