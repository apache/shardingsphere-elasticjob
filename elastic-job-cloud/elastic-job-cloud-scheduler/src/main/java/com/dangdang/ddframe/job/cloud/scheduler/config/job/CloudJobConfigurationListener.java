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

package com.dangdang.ddframe.job.cloud.scheduler.config.job;

import com.dangdang.ddframe.job.cloud.scheduler.producer.ProducerManager;
import com.dangdang.ddframe.job.cloud.scheduler.state.ready.ReadyService;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

import java.util.Collections;
import java.util.concurrent.Executors;

/**
 * 云作业配置变更监听.
 *
 * @author zhangliang
 * @author caohao
 */
public class CloudJobConfigurationListener implements TreeCacheListener {
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final ProducerManager producerManager;
    
    private final ReadyService readyService;
    
    public CloudJobConfigurationListener(final CoordinatorRegistryCenter regCenter, final ProducerManager producerManager) {
        this.regCenter = regCenter;
        readyService = new ReadyService(regCenter);
        this.producerManager = producerManager;
    }
    
    @Override
    public void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws Exception {
        String path = null == event.getData() ? "" : event.getData().getPath();
        if (isJobConfigNode(event, path, Type.NODE_ADDED)) {
            CloudJobConfiguration jobConfig = getJobConfig(event);
            if (null != jobConfig) {
                producerManager.schedule(jobConfig);
            }
        } else if (isJobConfigNode(event, path, Type.NODE_UPDATED)) {
            CloudJobConfiguration jobConfig = getJobConfig(event);
            if (null == jobConfig) {
                return;
            }
            if (CloudJobExecutionType.DAEMON == jobConfig.getJobExecutionType()) {
                readyService.remove(Collections.singletonList(jobConfig.getJobName()));
            }
            if (!jobConfig.getTypeConfig().getCoreConfig().isMisfire()) {
                readyService.setMisfireDisabled(jobConfig.getJobName());
            }
            producerManager.reschedule(jobConfig);
        } else if (isJobConfigNode(event, path, Type.NODE_REMOVED)) {
            String jobName = path.substring(CloudJobConfigurationNode.ROOT.length() + 1, path.length());
            producerManager.unschedule(jobName);
        }
    }
    
    private boolean isJobConfigNode(final TreeCacheEvent event, final String path, final Type type) {
        return type == event.getType() && path.startsWith(CloudJobConfigurationNode.ROOT) && path.length() > CloudJobConfigurationNode.ROOT.length();
    }
    
    private CloudJobConfiguration getJobConfig(final TreeCacheEvent event) {
        try {
            return CloudJobConfigurationGsonFactory.fromJson(new String(event.getData().getData()));
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            return null;
        }
    }
    
    /**
     * 启动云作业配置变更监听服务.
     */
    public void start() {
        getCache().getListenable().addListener(this, Executors.newSingleThreadExecutor());
    }
    
    /**
     * 停止云作业配置变更监听服务.
     */
    public void stop() {
        getCache().getListenable().removeListener(this);
    }
    
    private TreeCache getCache() {
        TreeCache result = (TreeCache) regCenter.getRawCache(CloudJobConfigurationNode.ROOT);
        if (null != result) {
            return result;
        }
        regCenter.addCacheData(CloudJobConfigurationNode.ROOT);
        return (TreeCache) regCenter.getRawCache(CloudJobConfigurationNode.ROOT);
    }
}
