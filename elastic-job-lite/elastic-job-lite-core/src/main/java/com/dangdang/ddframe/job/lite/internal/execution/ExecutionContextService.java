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

package com.dangdang.ddframe.job.lite.internal.execution;

import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.util.config.ShardingItemParameters;
import com.dangdang.ddframe.job.util.env.LocalHostService;
import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 作业运行时上下文服务.
 * 
 * @author zhangliang
 */
public class ExecutionContextService {
    
    private final JobNodeStorage jobNodeStorage;
    
    private final ConfigurationService configService;
    
    private final LocalHostService localHostService;
    
    public ExecutionContextService(final CoordinatorRegistryCenter regCenter, final String jobName) {
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        configService = new ConfigurationService(regCenter, jobName);
        localHostService = new LocalHostService();
    }
    
    /**
     * 获取当前作业服务器分片上下文.
     * 
     * @param shardingItems 分片项
     * @return 分片上下文
     */
    public ShardingContexts getJobShardingContext(final List<Integer> shardingItems) {
        LiteJobConfiguration liteJobConfig = configService.load(false);
        removeRunningIfMonitorExecution(liteJobConfig.isMonitorExecution(), shardingItems);
        if (shardingItems.isEmpty()) {
            return new ShardingContexts(buildTaskId(liteJobConfig, shardingItems), liteJobConfig.getJobName(), liteJobConfig.getTypeConfig().getCoreConfig().getShardingTotalCount(), 
                    liteJobConfig.getTypeConfig().getCoreConfig().getJobParameter(), Collections.<Integer, String>emptyMap());
        }
        Map<Integer, String> shardingItemParameterMap = new ShardingItemParameters(liteJobConfig.getTypeConfig().getCoreConfig().getShardingItemParameters()).getMap();
        return new ShardingContexts(buildTaskId(liteJobConfig, shardingItems), liteJobConfig.getJobName(), liteJobConfig.getTypeConfig().getCoreConfig().getShardingTotalCount(), 
                liteJobConfig.getTypeConfig().getCoreConfig().getJobParameter(), getAssignedShardingItemParameterMap(shardingItems, shardingItemParameterMap));
    }
    
    private String buildTaskId(final LiteJobConfiguration liteJobConfig, final List<Integer> shardingItems) {
        return Joiner.on("@-@").join(liteJobConfig.getJobName(), Joiner.on(",").join(shardingItems), "READY", localHostService.getIp(), UUID.randomUUID().toString()); 
    }
    
    private void removeRunningIfMonitorExecution(final boolean monitorExecution, final List<Integer> shardingItems) {
        if (!monitorExecution) {
            return;
        }
        List<Integer> runningShardingItems = new ArrayList<>(shardingItems.size());
        for (int each : shardingItems) {
            if (isRunning(each)) {
                runningShardingItems.add(each);
            }
        }
        shardingItems.removeAll(runningShardingItems);
    }
    
    private boolean isRunning(final int shardingItem) {
        return jobNodeStorage.isJobNodeExisted(ExecutionNode.getRunningNode(shardingItem));
    }
    
    private Map<Integer, String> getAssignedShardingItemParameterMap(final List<Integer> shardingItems, final Map<Integer, String> shardingItemParameterMap) {
        Map<Integer, String> result = new HashMap<>(shardingItemParameterMap.size(), 1);
        for (int each : shardingItems) {
            result.put(each, shardingItemParameterMap.get(each));
        }
        return result;
    }
}
