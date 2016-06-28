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

package com.dangdang.ddframe.job.cloud.Internal.state;

import com.dangdang.ddframe.job.cloud.Internal.config.CloudConfigurationService;
import com.dangdang.ddframe.job.cloud.Internal.config.CloudJobConfiguration;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;

/**
 * 云作业状态服务.
 *
 * @author zhangliang
 */
public final class StateService {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    private final CloudConfigurationService cloudConfigurationService;
    
    public StateService(final CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
        cloudConfigurationService = new CloudConfigurationService(registryCenter);
    }
    
    /**
     * 分片.
     * 
     * @param jobName 作业名称.
     */
    public void sharding(final String jobName) {
        CloudJobConfiguration cloudJobConfig = cloudConfigurationService.load(jobName);
        for (int i = 0; i < cloudJobConfig.getShardingTotalCount(); i++) {
            registryCenter.persist(StateNode.getShardingItemNodePath(jobName, i), "");
        }
    }
    
    /**
     * 记录作业开始运行的状态.
     * 
     * @param jobName 作业名称
     * @param shardingItem 分片项
     */
    public void startRunning(final String jobName, final int shardingItem) {
        registryCenter.persistEphemeral(StateNode.getRunningNodePath(jobName, shardingItem), "");
    }
    
    /**
     * 记录作业结束运行的状态.
     * 
     * @param jobName 作业名称
     * @param shardingItem 分片项
     */
    public void completeRunning(final String jobName, final int shardingItem) {
        registryCenter.remove(StateNode.getRunningNodePath(jobName, shardingItem));
    }
    
    /**
     * 判断作业是否运行.
     * 
     * @param jobName 作业名称
     * @return 作业是否运行
     */
    public boolean isRunning(final String jobName) {
        CloudJobConfiguration cloudJobConfig = cloudConfigurationService.load(jobName);
        for (int i = 0; i < cloudJobConfig.getShardingTotalCount(); i++) {
            if (registryCenter.isExisted(StateNode.getRunningNodePath(jobName, i))) {
                return true;
            }
        }
        return false;
    }
}
