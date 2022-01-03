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

import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationNode;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.instance.InstanceNode;
import org.apache.shardingsphere.elasticjob.lite.internal.listener.AbstractListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerNode;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodePath;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEventListener;

/**
 * Sharding listener manager.
 */
public final class ShardingListenerManager extends AbstractListenerManager {
    
    private final String jobName;
    
    private final ConfigurationNode configNode;
    
    private final InstanceNode instanceNode;
    
    private final ServerNode serverNode;
    
    private final ShardingService shardingService;
    
    private final JobNodePath jobNodePath;
    
    private final ConfigurationService configService;
    
    public ShardingListenerManager(final CoordinatorRegistryCenter regCenter, final String jobName) {
        super(regCenter, jobName);
        this.jobName = jobName;
        configNode = new ConfigurationNode(jobName);
        instanceNode = new InstanceNode(jobName);
        serverNode = new ServerNode(jobName);
        shardingService = new ShardingService(regCenter, jobName);
        jobNodePath = new JobNodePath(jobName);
        configService = new ConfigurationService(regCenter, jobName);
    }
    
    @Override
    public void start() {
        addDataListener(new ShardingTotalCountChangedJobListener());
        addDataListener(new ListenServersChangedJobListener());
    }
    
    class ShardingTotalCountChangedJobListener implements DataChangedEventListener {
        
        @Override
        public void onChange(final DataChangedEvent event) {
            if (configNode.isConfigPath(event.getKey()) && 0 != JobRegistry.getInstance().getCurrentShardingTotalCount(jobName)) {
                int newShardingTotalCount = YamlEngine.unmarshal(event.getValue(), JobConfigurationPOJO.class).toJobConfiguration().getShardingTotalCount();
                if (newShardingTotalCount != JobRegistry.getInstance().getCurrentShardingTotalCount(jobName)) {
                    shardingService.setReshardingFlag();
                    JobRegistry.getInstance().setCurrentShardingTotalCount(jobName, newShardingTotalCount);
                }
            }
        }
    }
    
    class ListenServersChangedJobListener implements DataChangedEventListener {
        
        @Override
        public void onChange(final DataChangedEvent event) {
            if (!JobRegistry.getInstance().isShutdown(jobName) && (isInstanceChange(event.getType(), event.getKey()) || isServerChange(event.getKey())) && !(isStaticSharding() && hasShardingInfo())) {
                shardingService.setReshardingFlag();
            }
        }
        
        private boolean isStaticSharding() {
            return configService.load(true).isStaticSharding();
        }
        
        private boolean hasShardingInfo() {
            return !JobRegistry.getInstance().getRegCenter(jobName).getChildrenKeys(jobNodePath.getShardingNodePath()).isEmpty();
        }
        
        private boolean isInstanceChange(final Type eventType, final String path) {
            return instanceNode.isInstancePath(path) && Type.UPDATED != eventType;
        }
        
        private boolean isServerChange(final String path) {
            return serverNode.isServerPath(path);
        }
    }
}
