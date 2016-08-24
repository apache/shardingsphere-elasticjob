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

package com.dangdang.ddframe.job.cloud.config;

import com.dangdang.ddframe.job.cloud.context.TaskContext;
import com.dangdang.ddframe.job.cloud.state.ready.ReadyService;
import com.dangdang.ddframe.job.cloud.state.running.RunningService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;

/**
 * 云作业配置变更监听.
 *
 * @author zhangliang
 */
public final class CloudJobConfigurationListener implements TreeCacheListener {
    
    private final ReadyService readyService;
    
    private final RunningService runningService;
    
    private final SchedulerDriver schedulerDriver;
    
    public CloudJobConfigurationListener(final CoordinatorRegistryCenter regCenter, final SchedulerDriver schedulerDriver) {
        readyService = new ReadyService(regCenter);
        runningService = new RunningService(regCenter);
        this.schedulerDriver = schedulerDriver;
    }
    
    @Override
    public void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws Exception {
        String path = null == event.getData() ? "" : event.getData().getPath();
        if (TreeCacheEvent.Type.NODE_UPDATED == event.getType() && path.startsWith(ConfigurationNode.ROOT) 
                && JobExecutionType.DAEMON == CloudJobConfigurationGsonFactory.fromJson(new String(event.getData().getData())).getJobExecutionType()) {
            String jobName = path.substring(ConfigurationNode.ROOT.length() + 1, path.length());
            // TODO 目前是修改了配置作业都停止,并由调度重启,以后应改成缩容kill相关,并且只有改了cron才重启
            for (TaskContext each : runningService.getRunningTasks(jobName)) {
                schedulerDriver.killTask(Protos.TaskID.newBuilder().setValue(each.getId()).build());
                runningService.remove(each.getMetaInfo());
            }
            readyService.addDaemon(jobName);
        }
    }
}
