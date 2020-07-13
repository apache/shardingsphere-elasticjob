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

package org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.settings;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.lite.api.JobType;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.dag.JobDagConfig;
import org.apache.shardingsphere.elasticjob.lite.executor.type.impl.DataflowJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.executor.type.impl.ScriptJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.internal.config.json.JobConfigurationGsonFactory;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodePath;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobSettingsAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.JobSettings;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;

/**
 * Job settings API implementation class.
 */
@RequiredArgsConstructor
public final class JobSettingsAPIImpl implements JobSettingsAPI {
    
    private final CoordinatorRegistryCenter regCenter;
    
    @Override
    public JobSettings getJobSettings(final String jobName) {
        JobSettings result = new JobSettings();
        JobNodePath jobNodePath = new JobNodePath(jobName);
        JobConfiguration jobConfig = JobConfigurationGsonFactory.fromJson(regCenter.get(jobNodePath.getConfigNodePath()));
        String jobType = jobConfig.getJobType().name();
        buildSimpleJobSettings(jobName, result, jobConfig);
        if (JobType.DATAFLOW.name().equals(jobType)) {
            buildDataflowJobSettings(result, jobConfig);
        }
        if (JobType.SCRIPT.name().equals(jobType)) {
            buildScriptJobSettings(result, jobConfig);
        }
        return result;
    }
    
    private void buildSimpleJobSettings(final String jobName, final JobSettings jobSettings, final JobConfiguration jobConfig) {
        jobSettings.setJobName(jobName);
        jobSettings.setJobType(jobConfig.getJobType().name());
        jobSettings.setShardingTotalCount(jobConfig.getShardingTotalCount());
        jobSettings.setCron(jobConfig.getCron());
        jobSettings.setShardingItemParameters(jobConfig.getShardingItemParameters());
        jobSettings.setJobParameter(jobConfig.getJobParameter());
        jobSettings.setMonitorExecution(jobConfig.isMonitorExecution());
        jobSettings.setMaxTimeDiffSeconds(jobConfig.getMaxTimeDiffSeconds());
        jobSettings.setMonitorPort(jobConfig.getMonitorPort());
        jobSettings.setFailover(jobConfig.isFailover());
        jobSettings.setMisfire(jobConfig.isMisfire());
        jobSettings.setJobShardingStrategyType(jobConfig.getJobShardingStrategyType());
        jobSettings.setJobExecutorServiceHandlerType(jobConfig.getJobExecutorServiceHandlerType());
        jobSettings.setJobErrorHandlerType(jobConfig.getJobErrorHandlerType());
        jobSettings.setReconcileIntervalMinutes(jobConfig.getReconcileIntervalMinutes());
        jobSettings.setDescription(jobConfig.getDescription());
        // for dag settings
        JobDagConfig jobDagConfig = jobConfig.getJobDagConfig();
        if (jobDagConfig != null) {
            jobSettings.setDagGroup(jobDagConfig.getDagGroup());
            jobSettings.setDagDependencies(jobDagConfig.getDagDependencies());
            jobSettings.setDagRunAlone(jobDagConfig.isDagRunAlone());
            jobSettings.setDagSkipWhenFail(jobDagConfig.isDagSkipWhenFail());
            jobSettings.setRetryTimes(jobDagConfig.getRetryTimes());
            jobSettings.setRetryInterval(jobDagConfig.getRetryInterval());
        }
    }
    
    private void buildDataflowJobSettings(final JobSettings jobSettings, final JobConfiguration config) {
        jobSettings.setStreamingProcess(Boolean.parseBoolean(config.getProps().getOrDefault(DataflowJobExecutor.STREAM_PROCESS_KEY, false).toString()));
    }
    
    private void buildScriptJobSettings(final JobSettings jobSettings, final JobConfiguration config) {
        jobSettings.setScriptCommandLine(config.getProps().getProperty(ScriptJobExecutor.SCRIPT_KEY));
    }
    
    @Override
    public void updateJobSettings(final JobSettings jobSettings) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobSettings.getJobName()), "jobName can not be empty.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobSettings.getCron()), "cron can not be empty.");
        Preconditions.checkArgument(jobSettings.getShardingTotalCount() > 0, "shardingTotalCount should larger than zero.");
        JobNodePath jobNodePath = new JobNodePath(jobSettings.getJobName());
        regCenter.update(jobNodePath.getConfigNodePath(), JobConfigurationGsonFactory.toJsonForObject(jobSettings));
    }
    
    @Override
    public void removeJobSettings(final String jobName) {
        regCenter.remove("/" + jobName);
    }
}
