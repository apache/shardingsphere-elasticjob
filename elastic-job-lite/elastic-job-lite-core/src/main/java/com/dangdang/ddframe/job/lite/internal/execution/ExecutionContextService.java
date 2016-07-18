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

package com.dangdang.ddframe.job.lite.internal.execution;

import com.dangdang.ddframe.job.lite.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.lite.api.config.JobConfiguration;
import com.dangdang.ddframe.job.lite.api.config.impl.JobType;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.lite.internal.offset.OffsetService;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 作业运行时上下文服务.
 * 
 * @author zhangliang
 */
public class ExecutionContextService {
    
    private final JobConfiguration jobConfiguration;
    
    private final JobNodeStorage jobNodeStorage;
    
    private final ConfigurationService configService;
    
    private final OffsetService offsetService;
    
    public ExecutionContextService(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration) {
        this.jobConfiguration = jobConfiguration;
        jobNodeStorage = new JobNodeStorage(coordinatorRegistryCenter, jobConfiguration);
        configService = new ConfigurationService(coordinatorRegistryCenter, jobConfiguration);
        offsetService = new OffsetService(coordinatorRegistryCenter, jobConfiguration);
    }
    
    /**
     * 获取当前作业服务器运行时分片上下文.
     * 
     * @param shardingItems 分片项
     * @return 当前作业服务器运行时分片上下文
     */
    public JobExecutionMultipleShardingContext getJobExecutionShardingContext(final List<Integer> shardingItems) {
        JobExecutionMultipleShardingContext result = new JobExecutionMultipleShardingContext();
        result.setJobName(jobConfiguration.getJobName());
        result.setShardingTotalCount(configService.getShardingTotalCount());
        result.setShardingItems(shardingItems);
        boolean isMonitorExecution = configService.isMonitorExecution();
        if (isMonitorExecution) {
            removeRunningItems(shardingItems);
        }
        result.setJobParameter(configService.getJobParameter());
        result.setMonitorExecution(isMonitorExecution);
        if (JobType.DATA_FLOW.equals(configService.getJobType())) {
            result.setFetchDataCount(configService.getFetchDataCount());    
        }
        if (result.getShardingItems().isEmpty()) {
            return result;
        }
        Map<Integer, String> shardingItemParameters = configService.getShardingItemParameters();
        for (int each : result.getShardingItems()) {
            if (shardingItemParameters.containsKey(each)) {
                result.getShardingItemParameters().put(each, shardingItemParameters.get(each));
            }
        }
        result.setOffsets(offsetService.getOffsets(result.getShardingItems()));
        return result;
    }
    
    private void removeRunningItems(final List<Integer> items) {
        List<Integer> toBeRemovedItems = new ArrayList<>(items.size());
        for (int each : items) {
            if (isRunningItem(each)) {
                toBeRemovedItems.add(each);
            }
        }
        items.removeAll(toBeRemovedItems);
    }
    
    private boolean isRunningItem(final int item) {
        return jobNodeStorage.isJobNodeExisted(ExecutionNode.getRunningNode(item));
    }
}
