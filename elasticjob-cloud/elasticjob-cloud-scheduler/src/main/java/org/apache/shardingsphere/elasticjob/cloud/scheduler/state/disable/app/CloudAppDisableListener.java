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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.app;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.shardingsphere.elasticjob.cloud.config.pojo.CloudJobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.producer.ProducerManager;
import org.apache.shardingsphere.elasticjob.infra.listener.CuratorCacheListener;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.util.Objects;
import java.util.concurrent.Executors;

/**
 * Cloud app disable listener.
 */
public final class CloudAppDisableListener implements TreeCacheListener, CuratorCacheListener {

    private final CoordinatorRegistryCenter regCenter;

    private final ProducerManager producerManager;

    private final CloudJobConfigurationService jobConfigService;

    public CloudAppDisableListener(final CoordinatorRegistryCenter regCenter, final ProducerManager producerManager) {
        this.regCenter = regCenter;
        this.producerManager = producerManager;
        jobConfigService = new CloudJobConfigurationService(regCenter);
    }

    @Override
    public void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws Exception {
        switch (event.getType()) {
            case NODE_ADDED:
                event(Type.NODE_CREATED, null, event.getData());
                break;
            case NODE_REMOVED:
                event(Type.NODE_DELETED, event.getData(), null);
                break;
            case NODE_UPDATED:
                event(Type.NODE_CHANGED, null, event.getData());
                break;
            default:
                break;
        }
    }

    /**
     * Cloud app disable event.
     * @param type the event type
     * @param oldData  the oldData
     * @param data the data
     */
    public void event(final Type type, final ChildData oldData, final ChildData data) {
        String path = Type.NODE_DELETED == type ? oldData.getPath() : data.getPath();
        if (Type.NODE_CREATED == type && isAppDisableNode(path)) {
            String appName = path.substring(DisableAppNode.ROOT.length() + 1);
            if (Objects.nonNull(appName)) {
                disableApp(appName);
            }
        } else if (Type.NODE_DELETED == type && isAppDisableNode(path)) {
            String appName = path.substring(DisableAppNode.ROOT.length() + 1);
            if (Objects.nonNull(appName)) {
                enableApp(appName);
            }
        }
    }
    
    private boolean isAppDisableNode(final String path) {
        return path.startsWith(DisableAppNode.ROOT) && path.length() > DisableAppNode.ROOT.length();
    }
    
    /**
     * Start the listener service of the cloud job service.
     */
    public void start() {
        getCache().getListenable().addListener(this, Executors.newSingleThreadExecutor());
    }

    /**
     * Stop the listener service of the cloud job service.
     */
    public void stop() {
        getCache().getListenable().removeListener(this);
    }

    private TreeCache getCache() {
        TreeCache result = (TreeCache) regCenter.getRawCache(DisableAppNode.ROOT);
        if (null != result) {
            return result;
        }
        regCenter.addCacheData(DisableAppNode.ROOT);
        return (TreeCache) regCenter.getRawCache(DisableAppNode.ROOT);
    }

    private void disableApp(final String appName) {
        for (CloudJobConfigurationPOJO each : jobConfigService.loadAll()) {
            if (appName.equals(each.getAppName())) {
                producerManager.unschedule(each.getJobName());
            }
        }
    }
    
    private void enableApp(final String appName) {
        for (CloudJobConfigurationPOJO each : jobConfigService.loadAll()) {
            if (appName.equals(each.getAppName())) {
                producerManager.reschedule(each.getJobName());
            }
        }
    }
}
