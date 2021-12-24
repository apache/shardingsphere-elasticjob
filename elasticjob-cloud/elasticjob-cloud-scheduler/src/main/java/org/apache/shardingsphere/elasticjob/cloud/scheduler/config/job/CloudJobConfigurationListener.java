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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job;

import lombok.extern.slf4j.Slf4j;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.config.pojo.CloudJobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.producer.ProducerManager;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.ready.ReadyService;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.util.Collections;
import java.util.concurrent.Executors;

/**
 * Cloud job configuration change listener.
 */
@Slf4j
public final class CloudJobConfigurationListener implements CuratorCacheListener {
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final ProducerManager producerManager;
    
    private final ReadyService readyService;
    
    public CloudJobConfigurationListener(final CoordinatorRegistryCenter regCenter, final ProducerManager producerManager) {
        this.regCenter = regCenter;
        readyService = new ReadyService(regCenter);
        this.producerManager = producerManager;
    }
    
    @Override
    public void event(final Type type, final ChildData oldData, final ChildData data) {
        String path = Type.NODE_DELETED == type ? oldData.getPath() : data.getPath();
        if (Type.NODE_CREATED == type && isJobConfigNode(path)) {
            CloudJobConfigurationPOJO cloudJobConfig = getCloudJobConfiguration(data);
            if (null != cloudJobConfig) {
                producerManager.schedule(cloudJobConfig);
            }
        } else if (Type.NODE_CHANGED == type && isJobConfigNode(path)) {
            CloudJobConfigurationPOJO cloudJobConfig = getCloudJobConfiguration(data);
            if (null == cloudJobConfig) {
                return;
            }
            if (CloudJobExecutionType.DAEMON == cloudJobConfig.getJobExecutionType()) {
                readyService.remove(Collections.singletonList(cloudJobConfig.getJobName()));
            }
            if (!cloudJobConfig.isMisfire()) {
                readyService.setMisfireDisabled(cloudJobConfig.getJobName());
            }
            producerManager.reschedule(cloudJobConfig.getJobName());
        } else if (Type.NODE_DELETED == type && isJobConfigNode(path)) {
            String jobName = path.substring(CloudJobConfigurationNode.ROOT.length() + 1);
            producerManager.unschedule(jobName);
        }
    }
    
    private boolean isJobConfigNode(final String path) {
        return path.startsWith(CloudJobConfigurationNode.ROOT) && path.length() > CloudJobConfigurationNode.ROOT.length();
    }

    private CloudJobConfigurationPOJO getCloudJobConfiguration(final ChildData data) {
        try {
            return YamlEngine.unmarshal(new String(data.getData()), CloudJobConfigurationPOJO.class);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            log.warn("Wrong Cloud Job Configuration with:", ex);
            // CHECKSTYLE:ON
            return null;
        }
    }
    
    /**
     * Start the listener service of the cloud job service.
     */
    public void start() {
        getCache().listenable().addListener(this, Executors.newSingleThreadExecutor());
    }
    
    /**
     * Stop the listener service of the cloud job service.
     */
    public void stop() {
        getCache().listenable().removeListener(this);
    }
    
    private CuratorCache getCache() {
        CuratorCache result = (CuratorCache) regCenter.getRawCache(CloudJobConfigurationNode.ROOT);
        if (null != result) {
            return result;
        }
        regCenter.addCacheData(CloudJobConfigurationNode.ROOT);
        return (CuratorCache) regCenter.getRawCache(CloudJobConfigurationNode.ROOT);
    }
}
