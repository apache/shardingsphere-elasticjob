/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.Internal.schedule;

import com.dangdang.ddframe.job.cloud.Internal.task.CloudTask;
import com.dangdang.ddframe.job.cloud.Internal.task.CloudTaskService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 云任务调度注册器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CloudTaskSchedulerRegistry {
    
    private static volatile CloudTaskSchedulerRegistry instance;
    
    private final ConcurrentHashMap<String, CloudTaskScheduler> cloudTaskSchedulerMap = new ConcurrentHashMap<>(65535);
    
    private final CoordinatorRegistryCenter registryCenter;
    
    /**
     * 获取实例.
     * 
     * @param registryCenter 注册中心对象
     * @return 实例对象
     */
    public static CloudTaskSchedulerRegistry getInstance(final CoordinatorRegistryCenter registryCenter) {
        if (null == instance) {
            synchronized (CloudTaskSchedulerRegistry.class) {
                if (null == instance) {
                    instance = new CloudTaskSchedulerRegistry(registryCenter);
                }
            }
        }
        return instance;
    }
    
    /**
     * 注册任务.
     * 
     * @param task 云任务
     */
    public void register(final CloudTask task) {
        String jobName = task.getJobName();
        if (cloudTaskSchedulerMap.containsKey(jobName)) {
            cloudTaskSchedulerMap.get(jobName).shutdown();
            cloudTaskSchedulerMap.remove(jobName);
        }
        CloudTaskService cloudTaskService = new CloudTaskService(registryCenter);
        cloudTaskService.addTask(task);
        CloudTaskScheduler cloudTaskScheduler = new CloudTaskScheduler(task, registryCenter);
        cloudTaskScheduler.startup();
        cloudTaskSchedulerMap.put(jobName, cloudTaskScheduler);
    }
    
    /**
     * 注销任务.
     * 
     * @param jobName 作业名称
     */
    public void unregister(final String jobName) {
        cloudTaskSchedulerMap.get(jobName).shutdown();
        cloudTaskSchedulerMap.remove(jobName);
    }
}
