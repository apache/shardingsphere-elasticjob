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

package com.dangdang.ddframe.job.lite.internal.sharding;

import com.dangdang.ddframe.job.lite.internal.config.ConfigurationNode;
import com.dangdang.ddframe.job.lite.internal.config.LiteJobConfigurationGsonFactory;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractJobListener;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractListenerManager;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;

/**
 * 幂等性监听管理器.
 * 
 * @author zhangliang
 */
public final class MonitorExecutionListenerManager extends AbstractListenerManager {
    
    private final ExecutionService executionService;
    
    private final ConfigurationNode configNode;
    
    public MonitorExecutionListenerManager(final CoordinatorRegistryCenter regCenter, final String jobName) {
        super(regCenter, jobName);
        executionService = new ExecutionService(regCenter, jobName);
        configNode = new ConfigurationNode(jobName);
    }
    
    @Override
    public void start() {
        addDataListener(new MonitorExecutionSettingsChangedJobListener());
    }
    
    class MonitorExecutionSettingsChangedJobListener extends AbstractJobListener {
        
        @Override
        protected void dataChanged(final String path, final Type eventType, final String data) {
            if (configNode.isConfigPath(path) && Type.NODE_UPDATED == eventType && !LiteJobConfigurationGsonFactory.fromJson(data).isMonitorExecution()) {
                executionService.clearAllRunningInfo();
            }
        }
    }
}
