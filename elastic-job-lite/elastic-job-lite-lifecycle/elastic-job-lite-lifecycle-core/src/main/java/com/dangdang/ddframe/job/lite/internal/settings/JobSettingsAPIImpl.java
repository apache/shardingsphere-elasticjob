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

package com.dangdang.ddframe.job.lite.internal.settings;

import com.dangdang.ddframe.job.api.type.JobType;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJobConfiguration;
import com.dangdang.ddframe.job.api.type.script.api.ScriptJobConfiguration;
import com.dangdang.ddframe.job.lite.api.JobSettingsAPI;
import com.dangdang.ddframe.job.lite.api.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.domain.JobSettings;
import com.dangdang.ddframe.job.lite.internal.config.LiteJobConfigurationGsonFactory;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodePath;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import lombok.RequiredArgsConstructor;

/**
 * 作业配置的实现类.
 *
 * @author zhangliang
 * @author caohao
 */
@RequiredArgsConstructor
public final class JobSettingsAPIImpl implements JobSettingsAPI {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    @Override
    public JobSettings getJobSettings(final String jobName) {
        JobSettings result = new JobSettings();
        JobNodePath jobNodePath = new JobNodePath(jobName);
        LiteJobConfiguration liteJobConfig = LiteJobConfigurationGsonFactory.getGson().fromJson(registryCenter.get(jobNodePath.getConfigNodePath()), LiteJobConfiguration.class);
        String jobType = liteJobConfig.getJobConfig().getJobType().name();
        buildSimpleJobSettings(jobName, result, liteJobConfig);
        if (JobType.DATAFLOW.name().equals(jobType)) {
            buildDataflowJobSettings(result, (DataflowJobConfiguration) liteJobConfig.getJobConfig());
        }
        if (JobType.SCRIPT.name().equals(jobType)) {
            buildScriptJobSettings(result, (ScriptJobConfiguration) liteJobConfig.getJobConfig());
        }
        return result;
    }
    
    private void buildSimpleJobSettings(final String jobName, final JobSettings result, final LiteJobConfiguration liteJobConfig) {
        result.setJobName(jobName);
        result.setJobType(liteJobConfig.getJobConfig().getJobType().name());
        result.setJobClass(liteJobConfig.getJobConfig().getJobClass().getCanonicalName());
        result.setShardingTotalCount(liteJobConfig.getJobConfig().getCoreConfig().getShardingTotalCount());
        result.setCron(liteJobConfig.getJobConfig().getCoreConfig().getCron());
        result.setShardingItemParameters(liteJobConfig.getJobConfig().getCoreConfig().getShardingItemParameters());
        result.setJobParameter(liteJobConfig.getJobConfig().getCoreConfig().getJobParameter());
        result.setMonitorExecution(liteJobConfig.isMonitorExecution());
        result.setMaxTimeDiffSeconds(liteJobConfig.getMaxTimeDiffSeconds());
        result.setMonitorPort(liteJobConfig.getMonitorPort());
        result.setFailover(liteJobConfig.getJobConfig().getCoreConfig().isFailover());
        result.setMisfire(liteJobConfig.getJobConfig().getCoreConfig().isMisfire());
        result.setJobShardingStrategyClass(liteJobConfig.getJobShardingStrategyClass());
        result.setDescription(liteJobConfig.getJobConfig().getCoreConfig().getDescription());
    } 
    
    private void buildDataflowJobSettings(final JobSettings result, final DataflowJobConfiguration config) {
        result.setConcurrentDataProcessThreadCount(config.getConcurrentDataProcessThreadCount());
        result.setStreamingProcess(config.isStreamingProcess());
    }
    
    private void buildScriptJobSettings(final JobSettings result, final ScriptJobConfiguration config) {
        result.setScriptCommandLine(config.getScriptCommandLine());
    }
    
    // TODO JobProperties未更新
    @Override
    public void updateJobSettings(final JobSettings jobSettings) {
        JobNodePath jobNodePath = new JobNodePath(jobSettings.getJobName());
        registryCenter.update(jobNodePath.getConfigNodePath(), LiteJobConfigurationGsonFactory.getGson().toJson(jobSettings));
    }
}
