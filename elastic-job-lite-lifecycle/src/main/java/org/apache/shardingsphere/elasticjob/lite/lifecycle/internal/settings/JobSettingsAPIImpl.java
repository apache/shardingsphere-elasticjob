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
import org.apache.shardingsphere.elasticjob.lite.api.JobType;
import org.apache.shardingsphere.elasticjob.lite.config.LiteJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.dataflow.DataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.script.ScriptJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.executor.handler.JobProperties;
import org.apache.shardingsphere.elasticjob.lite.internal.config.LiteJobConfigurationGsonFactory;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodePath;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobSettingsAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.JobSettings;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.lite.config.dataflow.DataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.executor.handler.JobProperties;
import org.apache.shardingsphere.elasticjob.lite.internal.config.LiteJobConfigurationGsonFactory;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodePath;

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
        LiteJobConfiguration liteJobConfig = LiteJobConfigurationGsonFactory.fromJson(regCenter.get(jobNodePath.getConfigNodePath()));
        String jobType = liteJobConfig.getTypeConfig().getJobType().name();
        buildSimpleJobSettings(jobName, result, liteJobConfig);
        if (JobType.DATAFLOW.name().equals(jobType)) {
            buildDataflowJobSettings(result, (DataflowJobConfiguration) liteJobConfig.getTypeConfig());
        }
        if (JobType.SCRIPT.name().equals(jobType)) {
            buildScriptJobSettings(result, (ScriptJobConfiguration) liteJobConfig.getTypeConfig());
        }
        return result;
    }
    
    private void buildSimpleJobSettings(final String jobName, final JobSettings result, final LiteJobConfiguration liteJobConfig) {
        result.setJobName(jobName);
        result.setJobType(liteJobConfig.getTypeConfig().getJobType().name());
        result.setJobClass(liteJobConfig.getTypeConfig().getJobClass());
        result.setShardingTotalCount(liteJobConfig.getTypeConfig().getCoreConfig().getShardingTotalCount());
        result.setCron(liteJobConfig.getTypeConfig().getCoreConfig().getCron());
        result.setShardingItemParameters(liteJobConfig.getTypeConfig().getCoreConfig().getShardingItemParameters());
        result.setJobParameter(liteJobConfig.getTypeConfig().getCoreConfig().getJobParameter());
        result.setMonitorExecution(liteJobConfig.isMonitorExecution());
        result.setMaxTimeDiffSeconds(liteJobConfig.getMaxTimeDiffSeconds());
        result.setMonitorPort(liteJobConfig.getMonitorPort());
        result.setFailover(liteJobConfig.getTypeConfig().getCoreConfig().isFailover());
        result.setMisfire(liteJobConfig.getTypeConfig().getCoreConfig().isMisfire());
        result.setJobShardingStrategyClass(liteJobConfig.getJobShardingStrategyClass());
        result.setDescription(liteJobConfig.getTypeConfig().getCoreConfig().getDescription());
        result.setReconcileIntervalMinutes(liteJobConfig.getReconcileIntervalMinutes());
        result.getJobProperties().put(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(),
                liteJobConfig.getTypeConfig().getCoreConfig().getJobProperties().get(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER));
        result.getJobProperties().put(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), 
                liteJobConfig.getTypeConfig().getCoreConfig().getJobProperties().get(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER));
    }
    
    private void buildDataflowJobSettings(final JobSettings result, final DataflowJobConfiguration config) {
        result.setStreamingProcess(config.isStreamingProcess());
    }
    
    private void buildScriptJobSettings(final JobSettings result, final ScriptJobConfiguration config) {
        result.setScriptCommandLine(config.getScriptCommandLine());
    }
    
    @Override
    public void updateJobSettings(final JobSettings jobSettings) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobSettings.getJobName()), "jobName can not be empty.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobSettings.getCron()), "cron can not be empty.");
        Preconditions.checkArgument(jobSettings.getShardingTotalCount() > 0, "shardingTotalCount should larger than zero.");
        JobNodePath jobNodePath = new JobNodePath(jobSettings.getJobName());
        regCenter.update(jobNodePath.getConfigNodePath(), LiteJobConfigurationGsonFactory.toJsonForObject(jobSettings));
    }
    
    @Override
    public void removeJobSettings(final String jobName) {
        regCenter.remove("/" + jobName);
    }
}
