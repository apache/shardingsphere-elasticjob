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

package com.dangdang.ddframe.job.cloud.scheduler.config;

import com.dangdang.ddframe.job.cloud.scheduler.producer.ProducerManager;
import com.dangdang.ddframe.job.cloud.scheduler.state.ready.ReadyService;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.mesos.SchedulerDriver;

import java.util.Collections;

/**
 * 云作业配置变更监听.
 *
 * @author zhangliang
 * @author caohao
 */
public final class CloudJobConfigurationListener implements TreeCacheListener {
    
    private final ProducerManager producerManager;
    
    private final ReadyService readyService;
    
    public CloudJobConfigurationListener(final SchedulerDriver schedulerDriver, final ProducerManager producerManager, final CoordinatorRegistryCenter regCenter) {
        this.producerManager = producerManager;
        readyService = new ReadyService(regCenter);
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
            if (JobExecutionType.DAEMON == jobConfig.getJobExecutionType()) {
                readyService.remove(Collections.singletonList(jobConfig.getJobName()));
            }
            if (!jobConfig.getTypeConfig().getCoreConfig().isMisfire()) {
                readyService.setMisfireDisabled(jobConfig.getJobName());
            }
            producerManager.reschedule(jobConfig);
        } else if (isJobConfigNode(event, path, Type.NODE_REMOVED)) {
            String jobName = path.substring(ConfigurationNode.ROOT.length() + 1, path.length());
            producerManager.unschedule(jobName);
        }
    }
    
    private boolean isJobConfigNode(final TreeCacheEvent event, final String path, final Type type) {
        return type == event.getType() && path.startsWith(ConfigurationNode.ROOT) && path.length() > ConfigurationNode.ROOT.length();
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
}
