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

package org.apache.shardingsphere.elasticjob.lite.internal.failover;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingNode;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.base.LeaderExecutionCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Failover service.
 */
@Slf4j
public final class FailoverService {
    
    private final String jobName;
    
    private final JobNodeStorage jobNodeStorage;
    
    private final ShardingService shardingService;
    
    private final ConfigurationService configService;
    
    public FailoverService(final CoordinatorRegistryCenter regCenter, final String jobName) {
        this.jobName = jobName;
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        shardingService = new ShardingService(regCenter, jobName);
        configService = new ConfigurationService(regCenter, jobName);
    }
    
    /**
     * set crashed failover flag.
     * 
     * @param item crashed job item
     */
    public void setCrashedFailoverFlag(final int item) {
        if (!isFailoverAssigned(item)) {
            jobNodeStorage.createJobNodeIfNeeded(FailoverNode.getItemsNode(item));
            jobNodeStorage.removeJobNodeIfExisted(ShardingNode.getRunningNode(item));
        }
    }
    
    /**
     * set crashed failover flag directly.
     *
     * @param item crashed item
     */
    public void setCrashedFailoverFlagDirectly(final int item) {
        jobNodeStorage.createJobNodeIfNeeded(FailoverNode.getItemsNode(item));
    }
    
    private boolean isFailoverAssigned(final Integer item) {
        return jobNodeStorage.isJobNodeExisted(FailoverNode.getExecutionFailoverNode(item));
    }
    
    /**
     * Failover if necessary.
     */
    public void failoverIfNecessary() {
        if (needFailover()) {
            jobNodeStorage.executeInLeader(FailoverNode.LATCH, new FailoverLeaderExecutionCallback());
        }
    }
    
    private boolean needFailover() {
        return jobNodeStorage.isJobNodeExisted(FailoverNode.ITEMS_ROOT) && !jobNodeStorage.getJobNodeChildrenKeys(FailoverNode.ITEMS_ROOT).isEmpty()
                && !JobRegistry.getInstance().isJobRunning(jobName);
    }
    
    /**
     * Update sharding items status when failover execution complete.
     * 
     * @param items sharding items of failover execution completed
     */
    public void updateFailoverComplete(final Collection<Integer> items) {
        for (int each : items) {
            jobNodeStorage.removeJobNodeIfExisted(FailoverNode.getExecutionFailoverNode(each));
            jobNodeStorage.removeJobNodeIfExisted(FailoverNode.getExecutingFailoverNode(each));
        }
    }
    
    /**
     * Get failover items.
     *
     * @param jobInstanceId job instance ID
     * @return failover items
     */
    public List<Integer> getFailoverItems(final String jobInstanceId) {
        List<String> items = jobNodeStorage.getJobNodeChildrenKeys(ShardingNode.ROOT);
        List<Integer> result = new ArrayList<>(items.size());
        for (String each : items) {
            int item = Integer.parseInt(each);
            String node = FailoverNode.getExecutionFailoverNode(item);
            if (jobNodeStorage.isJobNodeExisted(node) && jobInstanceId.equals(jobNodeStorage.getJobNodeDataDirectly(node))) {
                result.add(item);
            }
        }
        Collections.sort(result);
        return result;
    }
    
    /**
     * Get failovering items.
     *
     * @param jobInstanceId job instance ID
     * @return failovering items
     */
    public List<Integer> getFailoveringItems(final String jobInstanceId) {
        List<String> items = jobNodeStorage.getJobNodeChildrenKeys(ShardingNode.ROOT);
        List<Integer> result = new ArrayList<>(items.size());
        for (String each : items) {
            int item = Integer.parseInt(each);
            String node = FailoverNode.getExecutingFailoverNode(item);
            if (jobNodeStorage.isJobNodeExisted(node) && jobInstanceId.equals(jobNodeStorage.getJobNodeDataDirectly(node))) {
                result.add(item);
            }
        }
        Collections.sort(result);
        return result;
    }
    
    /**
     * Get failover items which execute on localhost.
     * 
     * @return failover items which execute on localhost
     */
    public List<Integer> getLocalFailoverItems() {
        if (JobRegistry.getInstance().isShutdown(jobName)) {
            return Collections.emptyList();
        }
        return getFailoverItems(JobRegistry.getInstance().getJobInstance(jobName).getJobInstanceId());
    }
    
    /**
     * Get failover items which crashed on localhost.
     * 
     * @return failover items which crashed on localhost
     */
    public List<Integer> getLocalTakeOffItems() {
        List<Integer> shardingItems = shardingService.getLocalShardingItems();
        List<Integer> result = new ArrayList<>(shardingItems.size());
        for (int each : shardingItems) {
            if (jobNodeStorage.isJobNodeExisted(FailoverNode.getExecutionFailoverNode(each))) {
                result.add(each);
            }
        }
        return result;
    }
    
    /**
     * Get all failovering items.
     *
     * @return all failovering items
     */
    public Map<Integer, JobInstance> getAllFailoveringItems() {
        int shardingTotalCount = configService.load(true).getShardingTotalCount();
        Map<Integer, JobInstance> result = new LinkedHashMap<>(shardingTotalCount, 1);
        for (int i = 0; i < shardingTotalCount; i++) {
            String data = jobNodeStorage.getJobNodeData(FailoverNode.getExecutingFailoverNode(i));
            if (!Strings.isNullOrEmpty(data)) {
                result.put(i, new JobInstance(data));
            }
        }
        return result;
    }
    
    /**
     * Clear failovering item.
     *
     * @param item item
     */
    public void clearFailoveringItem(final int item) {
        jobNodeStorage.removeJobNodeIfExisted(FailoverNode.getExecutingFailoverNode(item));
    }
    
    /**
     * Remove failover info.
     */
    public void removeFailoverInfo() {
        for (String each : jobNodeStorage.getJobNodeChildrenKeys(ShardingNode.ROOT)) {
            jobNodeStorage.removeJobNodeIfExisted(FailoverNode.getExecutionFailoverNode(Integer.parseInt(each)));
        }
    }
    
    class FailoverLeaderExecutionCallback implements LeaderExecutionCallback {
        
        @Override
        public void execute() {
            if (JobRegistry.getInstance().isShutdown(jobName) || !needFailover()) {
                return;
            }
            int crashedItem = Integer.parseInt(jobNodeStorage.getJobNodeChildrenKeys(FailoverNode.ITEMS_ROOT).get(0));
            log.debug("Failover job '{}' begin, crashed item '{}'", jobName, crashedItem);
            jobNodeStorage.fillEphemeralJobNode(FailoverNode.getExecutionFailoverNode(crashedItem), JobRegistry.getInstance().getJobInstance(jobName).getJobInstanceId());
            jobNodeStorage.fillJobNode(FailoverNode.getExecutingFailoverNode(crashedItem), JobRegistry.getInstance().getJobInstance(jobName).getJobInstanceId());
            jobNodeStorage.removeJobNodeIfExisted(FailoverNode.getItemsNode(crashedItem));
            // TODO Instead of using triggerJob, use executor for unified scheduling
            JobScheduleController jobScheduleController = JobRegistry.getInstance().getJobScheduleController(jobName);
            if (null != jobScheduleController) {
                jobScheduleController.triggerJob();
            }
        }
    }
}
