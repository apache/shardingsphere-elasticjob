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

package org.apache.shardingsphere.elasticjob.kernel.internal.sharding;

import com.google.common.base.Strings;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.kernel.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.kernel.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Execution service.
 */
public final class ExecutionService {
    
    private final String jobName;
    
    private final JobNodeStorage jobNodeStorage;
    
    private final ConfigurationService configService;
    
    public ExecutionService(final CoordinatorRegistryCenter regCenter, final String jobName) {
        this.jobName = jobName;
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        configService = new ConfigurationService(regCenter, jobName);
    }
    
    /**
     * Register job begin.
     * 
     * @param shardingContexts sharding contexts
     */
    public void registerJobBegin(final ShardingContexts shardingContexts) {
        JobRegistry.getInstance().setJobRunning(jobName, true);
        JobConfiguration jobConfig = configService.load(true);
        if (!jobConfig.isMonitorExecution()) {
            return;
        }
        String jobInstanceId = JobRegistry.getInstance().getJobInstance(jobName).getJobInstanceId();
        for (int each : shardingContexts.getShardingItemParameters().keySet()) {
            if (jobConfig.isFailover()) {
                jobNodeStorage.fillJobNode(ShardingNode.getRunningNode(each), jobInstanceId);
            } else {
                jobNodeStorage.fillEphemeralJobNode(ShardingNode.getRunningNode(each), jobInstanceId);
            }
        }
    }
    
    /**
     * Register job completed.
     * 
     * @param shardingContexts sharding contexts
     */
    public void registerJobCompleted(final ShardingContexts shardingContexts) {
        JobRegistry.getInstance().setJobRunning(jobName, false);
        if (!configService.load(true).isMonitorExecution()) {
            return;
        }
        for (int each : shardingContexts.getShardingItemParameters().keySet()) {
            jobNodeStorage.removeJobNodeIfExisted(ShardingNode.getRunningNode(each));
        }
    }
    
    /**
     * Clear all running info.
     */
    public void clearAllRunningInfo() {
        clearRunningInfo(getAllItems());
    }
    
    /**
     * Clear running info.
     * 
     * @param items sharding items which need to be cleared
     */
    public void clearRunningInfo(final List<Integer> items) {
        for (int each : items) {
            jobNodeStorage.removeJobNodeIfExisted(ShardingNode.getRunningNode(each));
        }
    }
    
    /**
     * Judge has running items or not.
     *
     * @param items sharding items need to be judged
     * @return has running items or not
     */
    public boolean hasRunningItems(final Collection<Integer> items) {
        JobConfiguration jobConfig = configService.load(true);
        if (!jobConfig.isMonitorExecution()) {
            return false;
        }
        for (int each : items) {
            if (jobNodeStorage.isJobNodeExisted(ShardingNode.getRunningNode(each))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Judge has running items or not.
     *
     * @return has running items or not
     */
    public boolean hasRunningItems() {
        return hasRunningItems(getAllItems());
    }
    
    private List<Integer> getAllItems() {
        int shardingTotalCount = configService.load(true).getShardingTotalCount();
        List<Integer> result = new ArrayList<>(shardingTotalCount);
        for (int i = 0; i < shardingTotalCount; i++) {
            result.add(i);
        }
        return result;
    }
    
    /**
     * Get all running items with instance.
     *
     * @return running items with instance.
     */
    public Map<Integer, JobInstance> getAllRunningItems() {
        int shardingTotalCount = configService.load(true).getShardingTotalCount();
        Map<Integer, JobInstance> result = new LinkedHashMap<>(shardingTotalCount, 1);
        for (int i = 0; i < shardingTotalCount; i++) {
            String data = jobNodeStorage.getJobNodeData(ShardingNode.getRunningNode(i));
            if (!Strings.isNullOrEmpty(data)) {
                result.put(i, new JobInstance(data));
            }
        }
        return result;
    }
    
    /**
     * Set misfire flag if sharding items still running.
     * 
     * @param items sharding items need to be set misfire flag
     * @return is misfired for this schedule time or not
     */
    public boolean misfireIfHasRunningItems(final Collection<Integer> items) {
        if (!hasRunningItems(items)) {
            return false;
        }
        setMisfire(items);
        return true;
    }
    
    /**
     * Set misfire flag if sharding items still running.
     *
     * @param items sharding items need to be set misfire flag
     */
    public void setMisfire(final Collection<Integer> items) {
        for (int each : items) {
            jobNodeStorage.createJobNodeIfNeeded(ShardingNode.getMisfireNode(each));
        }
    }
    
    /**
     * Get misfired job sharding items.
     * 
     * @param items sharding items need to be judged
     * @return misfired job sharding items
     */
    public List<Integer> getMisfiredJobItems(final Collection<Integer> items) {
        List<Integer> result = new ArrayList<>(items.size());
        for (int each : items) {
            if (jobNodeStorage.isJobNodeExisted(ShardingNode.getMisfireNode(each))) {
                result.add(each);
            }
        }
        return result;
    }
    
    /**
     * Clear misfire flag.
     * 
     * @param items sharding items need to be cleared
     */
    public void clearMisfire(final Collection<Integer> items) {
        for (int each : items) {
            jobNodeStorage.removeJobNodeIfExisted(ShardingNode.getMisfireNode(each));
        }
    }
    
    /**
     * Get disabled sharding items.
     *
     * @param items sharding items need to be got
     * @return disabled sharding items
     */
    public List<Integer> getDisabledItems(final List<Integer> items) {
        List<Integer> result = new ArrayList<>(items.size());
        for (int each : items) {
            if (jobNodeStorage.isJobNodeExisted(ShardingNode.getDisabledNode(each))) {
                result.add(each);
            }
        }
        return result;
    }
}
