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

package com.dangdang.ddframe.job.internal.settings;

import com.dangdang.ddframe.job.api.JobSettingsAPI;
import com.dangdang.ddframe.job.domain.JobSettings;
import com.dangdang.ddframe.job.internal.storage.JobNodePath;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;

/**
 * 作业配置的实现类.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class JobSettingsAPIImpl implements JobSettingsAPI {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    @Override
    public JobSettings getJobSettings(final String jobName) {
        JobSettings result = new JobSettings();
        JobNodePath jobNodePath = new JobNodePath(jobName);
        result.setJobName(jobName);
        result.setJobClass(registryCenter.get(jobNodePath.getConfigNodePath("jobClass")));
        result.setShardingTotalCount(Integer.parseInt(registryCenter.get(jobNodePath.getConfigNodePath("shardingTotalCount"))));
        result.setCron(registryCenter.get(jobNodePath.getConfigNodePath("cron")));
        result.setShardingItemParameters(registryCenter.get(jobNodePath.getConfigNodePath("shardingItemParameters")));
        result.setJobParameter(registryCenter.get(jobNodePath.getConfigNodePath("jobParameter")));
        result.setMonitorExecution(Boolean.valueOf(registryCenter.get(jobNodePath.getConfigNodePath("monitorExecution"))));
        result.setProcessCountIntervalSeconds(Integer.parseInt(registryCenter.get(jobNodePath.getConfigNodePath("processCountIntervalSeconds"))));
        result.setConcurrentDataProcessThreadCount(Integer.parseInt(registryCenter.get(jobNodePath.getConfigNodePath("concurrentDataProcessThreadCount"))));
        result.setFetchDataCount(Integer.parseInt(registryCenter.get(jobNodePath.getConfigNodePath("fetchDataCount"))));
        result.setMaxTimeDiffSeconds(Integer.parseInt(registryCenter.get(jobNodePath.getConfigNodePath("maxTimeDiffSeconds"))));
        String monitorPort = registryCenter.get(jobNodePath.getConfigNodePath("monitorPort"));
        if (!Strings.isNullOrEmpty(monitorPort)) {
            result.setMonitorPort(Integer.parseInt(monitorPort));
        }
        result.setFailover(Boolean.valueOf(registryCenter.get(jobNodePath.getConfigNodePath("failover"))));
        result.setMisfire(Boolean.valueOf(registryCenter.get(jobNodePath.getConfigNodePath("misfire"))));
        result.setJobShardingStrategyClass(registryCenter.get(jobNodePath.getConfigNodePath("jobShardingStrategyClass")));
        result.setDescription(registryCenter.get(jobNodePath.getConfigNodePath("description")));
        return result;
    }
    
    @Override
    public void updateJobSettings(final JobSettings jobSettings) {
        JobNodePath jobNodePath = new JobNodePath(jobSettings.getJobName());
        updateIfChanged(jobNodePath.getConfigNodePath("shardingTotalCount"), jobSettings.getShardingTotalCount());
        updateIfChanged(jobNodePath.getConfigNodePath("cron"), jobSettings.getCron());
        updateIfChanged(jobNodePath.getConfigNodePath("shardingItemParameters"), jobSettings.getShardingItemParameters());
        updateIfChanged(jobNodePath.getConfigNodePath("jobParameter"), jobSettings.getJobParameter());
        updateIfChanged(jobNodePath.getConfigNodePath("monitorExecution"), jobSettings.isMonitorExecution());
        updateIfChanged(jobNodePath.getConfigNodePath("processCountIntervalSeconds"), jobSettings.getProcessCountIntervalSeconds());
        updateIfChanged(jobNodePath.getConfigNodePath("concurrentDataProcessThreadCount"), jobSettings.getConcurrentDataProcessThreadCount());
        updateIfChanged(jobNodePath.getConfigNodePath("fetchDataCount"), jobSettings.getFetchDataCount());
        updateIfChanged(jobNodePath.getConfigNodePath("maxTimeDiffSeconds"), jobSettings.getMaxTimeDiffSeconds());
        updateIfChanged(jobNodePath.getConfigNodePath("monitorPort"), jobSettings.getMonitorPort());
        updateIfChanged(jobNodePath.getConfigNodePath("failover"), jobSettings.isFailover());
        updateIfChanged(jobNodePath.getConfigNodePath("misfire"), jobSettings.isMisfire());
        updateIfChanged(jobNodePath.getConfigNodePath("jobShardingStrategyClass"), jobSettings.getJobShardingStrategyClass());
        updateIfChanged(jobNodePath.getConfigNodePath("description"), jobSettings.getDescription());
    }
    
    private void updateIfChanged(final String nodePath, final Object value) {
        if (null == value || value.toString().equals(registryCenter.get(nodePath))) {
            return;
        }
        registryCenter.update(nodePath, value.toString());
    }
}
