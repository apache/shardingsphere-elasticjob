/**
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

package com.dangdang.ddframe.job.internal.listener;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.internal.config.ConfigurationListenerManager;
import com.dangdang.ddframe.job.internal.election.ElectionListenerManager;
import com.dangdang.ddframe.job.internal.execution.ExecutionListenerManager;
import com.dangdang.ddframe.job.internal.failover.FailoverListenerManager;
import com.dangdang.ddframe.job.internal.server.JobOperationListenerManager;
import com.dangdang.ddframe.job.internal.sharding.ShardingListenerManager;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;

/**
 * 作业注册中心的监听器管理者.
 * 
 * @author zhangliang
 */
public class ListenerManager {
    
    private final ElectionListenerManager electionListenerManager;
    
    private final ShardingListenerManager shardingListenerManager;
    
    private final ExecutionListenerManager executionListenerManager;
    
    private final FailoverListenerManager failoverListenerManager;
    
    private final JobOperationListenerManager jobOperationListenerManager;
    
    private final ConfigurationListenerManager configurationListenerManager;
    
    public ListenerManager(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration) {
        electionListenerManager = new ElectionListenerManager(coordinatorRegistryCenter, jobConfiguration);
        shardingListenerManager = new ShardingListenerManager(coordinatorRegistryCenter, jobConfiguration);
        executionListenerManager = new ExecutionListenerManager(coordinatorRegistryCenter, jobConfiguration);
        failoverListenerManager = new FailoverListenerManager(coordinatorRegistryCenter, jobConfiguration);
        jobOperationListenerManager = new JobOperationListenerManager(coordinatorRegistryCenter, jobConfiguration);
        configurationListenerManager = new ConfigurationListenerManager(coordinatorRegistryCenter, jobConfiguration);
    }
    
    /**
     * 开启所有监听器.
     */
    public void startAllListeners() {
        electionListenerManager.start();
        shardingListenerManager.start();
        executionListenerManager.start();
        failoverListenerManager.start();
        jobOperationListenerManager.start();
        configurationListenerManager.start();
    }
}
