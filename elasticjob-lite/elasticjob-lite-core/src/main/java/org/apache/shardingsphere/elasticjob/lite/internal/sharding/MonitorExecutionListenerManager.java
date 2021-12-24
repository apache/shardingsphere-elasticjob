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

package org.apache.shardingsphere.elasticjob.lite.internal.sharding;

import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationNode;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.internal.listener.AbstractJobListener;
import org.apache.shardingsphere.elasticjob.lite.internal.listener.AbstractListenerManager;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

/**
 * Monitor execution listener manager.
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
            if (configNode.isConfigPath(path) && Type.NODE_CHANGED == eventType && !YamlEngine.unmarshal(data, JobConfigurationPOJO.class).toJobConfiguration().isMonitorExecution()) {
                executionService.clearAllRunningInfo();
            }
        }
    }
}
