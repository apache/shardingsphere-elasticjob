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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app;

import com.google.gson.JsonParseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.mesos.Protos;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.MesosStateService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.producer.ProducerManager;
import org.apache.shardingsphere.elasticjob.infra.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.util.Collection;
import java.util.concurrent.Executors;

/**
 * Cloud app configuration change listener.
 */
@Slf4j
public final class CloudAppConfigurationListener implements CuratorCacheListener {
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final ProducerManager producerManager;
    
    private MesosStateService mesosStateService;
    
    public CloudAppConfigurationListener(final CoordinatorRegistryCenter regCenter, final ProducerManager producerManager) {
        this.regCenter = regCenter;
        this.producerManager = producerManager;
        mesosStateService = new MesosStateService(regCenter);
    }
    
    @Override
    public void event(final Type type, final ChildData oldData, final ChildData data) {
        String path = Type.NODE_DELETED == type ? oldData.getPath() : data.getPath();
        if (Type.NODE_DELETED == type && isJobAppConfigNode(path)) {
            String appName = path.substring(CloudAppConfigurationNode.ROOT.length() + 1);
            stopExecutors(appName);
        }
    }
    
    private boolean isJobAppConfigNode(final String path) {
        return path.startsWith(CloudAppConfigurationNode.ROOT) && path.length() > CloudAppConfigurationNode.ROOT.length();
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
        CuratorCache result = (CuratorCache) regCenter.getRawCache(CloudAppConfigurationNode.ROOT);
        if (null != result) {
            return result;
        }
        regCenter.addCacheData(CloudAppConfigurationNode.ROOT);
        return (CuratorCache) regCenter.getRawCache(CloudAppConfigurationNode.ROOT);
    }
    
    private void stopExecutors(final String appName) {
        try {
            Collection<MesosStateService.ExecutorStateInfo> executorBriefInfo = mesosStateService.executors(appName);
            for (MesosStateService.ExecutorStateInfo each : executorBriefInfo) {
                producerManager.sendFrameworkMessage(Protos.ExecutorID.newBuilder().setValue(each.getId()).build(),
                        Protos.SlaveID.newBuilder().setValue(each.getSlaveId()).build(), "STOP".getBytes());
            }
        } catch (final JsonParseException ex) {
            throw new JobSystemException(ex);
        }
    }
}
