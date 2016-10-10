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

import com.dangdang.ddframe.job.cloud.scheduler.context.TaskContext;
import com.dangdang.ddframe.job.cloud.scheduler.lifecycle.LifecycleService;
import com.dangdang.ddframe.job.cloud.scheduler.state.ready.ReadyService;
import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningService;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.mesos.SchedulerDriver;

/**
 * 云作业配置变更监听.
 *
 * @author zhangliang
 * @author caohao
 */
public final class CloudJobConfigurationListener implements TreeCacheListener {
    
    private final ReadyService readyService;
    
    private final RunningService runningService;
    
    private final LifecycleService lifecycleService;
    
    public CloudJobConfigurationListener(final CoordinatorRegistryCenter regCenter, final SchedulerDriver schedulerDriver) {
        readyService = new ReadyService(regCenter);
        runningService = new RunningService(regCenter);
        lifecycleService = new LifecycleService(schedulerDriver, regCenter);
    }
    
    @Override
    public void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws Exception {
        String path = null == event.getData() ? "" : event.getData().getPath();
        if (isDaemonJobConfigNodeUpdated(event, path)) {
            String jobName = path.substring(ConfigurationNode.ROOT.length() + 1, path.length());
            // TODO 目前是修改了配置作业都停止,并由调度重启,以后应改成缩容kill相关,并且只有改了cron才重启
            lifecycleService.killJob(jobName);
            for (TaskContext each : runningService.getRunningTasks(jobName)) {
                runningService.remove(each.getMetaInfo());
            }
            readyService.addDaemon(jobName);
        }
        if (isJobConfigNodeRemoved(event, path)) {
            String jobName = path.substring(ConfigurationNode.ROOT.length() + 1, path.length());
            lifecycleService.killJob(jobName);
            for (TaskContext each : runningService.getRunningTasks(jobName)) {
                runningService.remove(each.getMetaInfo());
            }
            readyService.remove(Lists.newArrayList(jobName));
        }
    }
    
    private boolean isDaemonJobConfigNodeUpdated(final TreeCacheEvent event, final String path) {
        return Type.NODE_UPDATED == event.getType() && path.startsWith(ConfigurationNode.ROOT) 
                && JobExecutionType.DAEMON == CloudJobConfigurationGsonFactory.fromJson(new String(event.getData().getData())).getJobExecutionType();
    }
    
    private boolean isJobConfigNodeRemoved(final TreeCacheEvent event, final String path) {
        return Type.NODE_REMOVED == event.getType() && path.startsWith(ConfigurationNode.ROOT) && path.length() > ConfigurationNode.ROOT.length();
    }
}
