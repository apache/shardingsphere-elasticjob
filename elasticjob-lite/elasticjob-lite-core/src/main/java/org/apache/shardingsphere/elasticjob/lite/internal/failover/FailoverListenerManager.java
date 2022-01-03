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

import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationNode;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.instance.InstanceNode;
import org.apache.shardingsphere.elasticjob.lite.internal.listener.AbstractListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEventListener;

import java.util.List;

/**
 * Failover listener manager.
 */
public final class FailoverListenerManager extends AbstractListenerManager {
    
    private final String jobName;
    
    private final ConfigurationService configService;
    
    private final ShardingService shardingService;
    
    private final FailoverService failoverService;
    
    private final ConfigurationNode configNode;
    
    private final InstanceNode instanceNode;
    
    public FailoverListenerManager(final CoordinatorRegistryCenter regCenter, final String jobName) {
        super(regCenter, jobName);
        this.jobName = jobName;
        configService = new ConfigurationService(regCenter, jobName);
        shardingService = new ShardingService(regCenter, jobName);
        failoverService = new FailoverService(regCenter, jobName);
        configNode = new ConfigurationNode(jobName);
        instanceNode = new InstanceNode(jobName);
    }
    
    @Override
    public void start() {
        addDataListener(new JobCrashedJobListener());
        addDataListener(new FailoverSettingsChangedJobListener());
    }
    
    private boolean isFailoverEnabled() {
        return configService.load(true).isFailover();
    }
    
    class JobCrashedJobListener implements DataChangedEventListener {
        
        @Override
        public void onChange(final DataChangedEvent event) {
            if (!JobRegistry.getInstance().isShutdown(jobName) && isFailoverEnabled() && Type.DELETED == event.getType() && instanceNode.isInstancePath(event.getKey())) {
                String jobInstanceId = event.getKey().substring(instanceNode.getInstanceFullPath().length() + 1);
                if (jobInstanceId.equals(JobRegistry.getInstance().getJobInstance(jobName).getJobInstanceId())) {
                    return;
                }
                List<Integer> failoverItems = failoverService.getFailoveringItems(jobInstanceId);
                if (!failoverItems.isEmpty()) {
                    for (int each : failoverItems) {
                        failoverService.setCrashedFailoverFlagDirectly(each);
                        failoverService.failoverIfNecessary();
                    }
                } else {
                    for (int each : shardingService.getCrashedShardingItems(jobInstanceId)) {
                        failoverService.setCrashedFailoverFlag(each);
                        failoverService.failoverIfNecessary();
                    }
                }
            }
        }
    }
    
    class FailoverSettingsChangedJobListener implements DataChangedEventListener {
        
        @Override
        public void onChange(final DataChangedEvent event) {
            if (configNode.isConfigPath(event.getKey()) && Type.UPDATED == event.getType() && !YamlEngine.unmarshal(event.getValue(), JobConfigurationPOJO.class).toJobConfiguration().isFailover()) {
                failoverService.removeFailoverInfo();
            }
        }
    }
}
