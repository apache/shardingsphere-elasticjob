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

package com.dangdang.ddframe.job.lite.internal.listener;

import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationListenerManager;
import com.dangdang.ddframe.job.lite.internal.election.LeaderListenerManager;
import com.dangdang.ddframe.job.lite.internal.failover.FailoverListenerManager;
import com.dangdang.ddframe.job.lite.internal.guarantee.GuaranteeListenerManager;
import com.dangdang.ddframe.job.lite.internal.instance.InstanceShutdownListenerManager;
import com.dangdang.ddframe.job.lite.internal.instance.InstanceTriggerListenerManager;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingListenerManager;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;

import java.util.List;

/**
 * 作业注册中心的监听器管理者.
 * 
 * @author zhangliang
 */
public class ListenerManager {
    
    private final JobNodeStorage jobNodeStorage;
    
    private final LeaderListenerManager leaderListenerManager;
    
    private final ShardingListenerManager shardingListenerManager;
    
    private final FailoverListenerManager failoverListenerManager;
    
    private final InstanceShutdownListenerManager instanceShutdownListenerManager;
    
    private final InstanceTriggerListenerManager instanceTriggerListenerManager;
    
    private final ConfigurationListenerManager configurationListenerManager;

    private final GuaranteeListenerManager guaranteeListenerManager;
    
    private final RegistryCenterConnectionStateListener regCenterConnectionStateListener;
    
    public ListenerManager(final CoordinatorRegistryCenter regCenter, final String jobName, final List<ElasticJobListener> elasticJobListeners) {
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        leaderListenerManager = new LeaderListenerManager(regCenter, jobName);
        shardingListenerManager = new ShardingListenerManager(regCenter, jobName);
        failoverListenerManager = new FailoverListenerManager(regCenter, jobName);
        instanceShutdownListenerManager = new InstanceShutdownListenerManager(regCenter, jobName);
        instanceTriggerListenerManager = new InstanceTriggerListenerManager(regCenter, jobName);
        configurationListenerManager = new ConfigurationListenerManager(regCenter, jobName);
        guaranteeListenerManager = new GuaranteeListenerManager(regCenter, jobName, elasticJobListeners);
        regCenterConnectionStateListener = new RegistryCenterConnectionStateListener(regCenter, jobName);
    }
    
    /**
     * 开启所有监听器.
     */
    public void startAllListeners() {
        leaderListenerManager.start();
        shardingListenerManager.start();
        failoverListenerManager.start();
        instanceShutdownListenerManager.start();
        instanceTriggerListenerManager.start();
        configurationListenerManager.start();
        guaranteeListenerManager.start();
        jobNodeStorage.addConnectionStateListener(regCenterConnectionStateListener);
    }
    
    /**
     * 设置当前分片总数.
     * 
     * @param currentShardingTotalCount 当前分片总数
     */
    public void setCurrentShardingTotalCount(final int currentShardingTotalCount) {
        shardingListenerManager.setCurrentShardingTotalCount(currentShardingTotalCount);
    }
}
