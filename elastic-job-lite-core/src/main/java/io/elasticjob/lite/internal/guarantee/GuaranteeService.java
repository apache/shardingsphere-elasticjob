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

package io.elasticjob.lite.internal.guarantee;

import io.elasticjob.lite.internal.config.ConfigurationService;
import io.elasticjob.lite.internal.storage.JobNodeStorage;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 保证分布式任务全部开始和结束状态的服务.
 * 
 * @author zhangliang
 */
public final class GuaranteeService {
    
    private final JobNodeStorage jobNodeStorage;
    
    private final ConfigurationService configService;
    
    public GuaranteeService(final CoordinatorRegistryCenter regCenter, final String jobName) {
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        configService = new ConfigurationService(regCenter, jobName);
    }
    
    /**
     * Register the start status of job with the sharding count.
     *
     * @param itemCount the count of items
     * @param shardingTotalCount the total count of sharding
     * @return the num of items which has been registered
     */
    public int registerStart(final int itemCount, final int shardingTotalCount) {
        int each = 0;
        while (each++ < shardingTotalCount + 3) {
            Pair<String, Integer> dataAndVerionPair = jobNodeStorage.getNodeDataAndVersion(GuaranteeNode.STARTED_ROOT);
            if (null == dataAndVerionPair) {
                jobNodeStorage.createJobNodeIfNeeded(GuaranteeNode.STARTED_ROOT);
                continue;
            }
            String oldValue = StringUtils.isBlank(dataAndVerionPair.getLeft()) ? "0" : dataAndVerionPair.getLeft().trim();
            String setValue = String.valueOf(Integer.parseInt(oldValue) + itemCount);
            if (jobNodeStorage.setNodeDataAndVersion(GuaranteeNode.STARTED_ROOT, setValue, dataAndVerionPair.getRight())) {
                return Integer.parseInt(setValue);
            }
        }
        return 0;
    }
    
    /**
     * Determines if all task have been started.
     *
     * @param registeredItemCount the count of item which has been registered
     * @return Whether all tasks have been started
     */
    public boolean isAllStarted(final int registeredItemCount) {
        return jobNodeStorage.isJobNodeExisted(GuaranteeNode.STARTED_ROOT)
                && configService.load(false).getTypeConfig().getCoreConfig().getShardingTotalCount() == registeredItemCount;
    }
    
    /**
     * 清理所有任务启动信息.
     */
    public void clearAllStartedInfo() {
        jobNodeStorage.removeJobNodeIfExisted(GuaranteeNode.STARTED_ROOT);
    }
    
    /**
     * Register the complete status of job with the sharding count.
     *
     * @param itemCount the count of items
     * @param shardingTotalCount the total count of sharding
     * @return the num of items which has been registered
     */
    public int registerComplete(final int itemCount, final int shardingTotalCount) {
        int each = 0;
        while (each++ < shardingTotalCount + 3) {
            Pair<String, Integer> dataAndVerionPair = jobNodeStorage.getNodeDataAndVersion(GuaranteeNode.COMPLETED_ROOT);
            if (null == dataAndVerionPair) {
                jobNodeStorage.createJobNodeIfNeeded(GuaranteeNode.COMPLETED_ROOT);
                continue;
            }
            String oldValue = StringUtils.isBlank(dataAndVerionPair.getLeft()) ? "0" : dataAndVerionPair.getLeft().trim();
            String setValue = String.valueOf(Integer.parseInt(oldValue) + itemCount);
            if (jobNodeStorage.setNodeDataAndVersion(GuaranteeNode.COMPLETED_ROOT, setValue, dataAndVerionPair.getRight())) {
                return Integer.parseInt(setValue);
            }
        }
        return 0;
    }
    
    /**
     * Determines if all task have been completed.
     *
     * @param registeredItemCount the count of item which has been registered
     * @return Whether all tasks have been started
     */
    public boolean isAllCompleted(final int registeredItemCount) {
        return jobNodeStorage.isJobNodeExisted(GuaranteeNode.COMPLETED_ROOT)
                && configService.load(false).getTypeConfig().getCoreConfig().getShardingTotalCount() == registeredItemCount;
    }
    
    /**
     * 清理所有任务启动信息.
     */
    public void clearAllCompletedInfo() {
        jobNodeStorage.removeJobNodeIfExisted(GuaranteeNode.COMPLETED_ROOT);
    }
}
