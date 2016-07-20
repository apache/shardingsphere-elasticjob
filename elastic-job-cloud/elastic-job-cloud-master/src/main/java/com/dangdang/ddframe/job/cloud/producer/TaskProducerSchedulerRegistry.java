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

package com.dangdang.ddframe.job.cloud.producer;

import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.config.ConfigurationService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 发布任务作业调度注册表.
 *
 * @author zhangliang
 */
public class TaskProducerSchedulerRegistry {
    
    private static volatile TaskProducerSchedulerRegistry instance;
    
    private final ConcurrentHashMap<String, TaskProducerScheduler> taskProducerSchedulerMap = new ConcurrentHashMap<>(65535);
    
    private final CoordinatorRegistryCenter registryCenter;
    
    private final ConfigurationService configService;
    
    private TaskProducerSchedulerRegistry(final CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
        configService = new ConfigurationService(registryCenter);
    }
    
    /**
     * 获取实例.
     * 
     * @param registryCenter 注册中心对象
     * @return 实例对象
     */
    public static TaskProducerSchedulerRegistry getInstance(final CoordinatorRegistryCenter registryCenter) {
        if (null == instance) {
            synchronized (TaskProducerSchedulerRegistry.class) {
                if (null == instance) {
                    instance = new TaskProducerSchedulerRegistry(registryCenter);
                }
            }
        }
        return instance;
    }
    
    /**
     * 将注册中心中的云作业配置注册调度.
     */
    public void registerFromRegistryCenter() {
        for (CloudJobConfiguration each : configService.loadAll()) {
            register(each);
        }
    }
    
    /**
     * 注册作业.
     * 
     * @param jobConfig 作业配置
     */
    public void register(final CloudJobConfiguration jobConfig) {
        String jobName = jobConfig.getJobName();
        if (taskProducerSchedulerMap.containsKey(jobName)) {
            taskProducerSchedulerMap.get(jobName).shutdown();
            taskProducerSchedulerMap.remove(jobName);
        }
        Optional<CloudJobConfiguration> jobConfigFromZk = configService.load(jobName);
        if (!jobConfigFromZk.isPresent()) {
            configService.add(jobConfig);
        } else if (!jobConfigFromZk.get().equals(jobConfig)) {
            configService.update(jobConfig);
        }
        TaskProducerScheduler taskProducerScheduler = new TaskProducerScheduler(jobConfig, registryCenter);
        taskProducerScheduler.startup();
        taskProducerSchedulerMap.put(jobName, taskProducerScheduler);
    }
    
    /**
     * 注销作业.
     * 
     * @param jobName 作业名称
     */
    public void deregister(final String jobName) {
        if (taskProducerSchedulerMap.containsKey(jobName)) {
            taskProducerSchedulerMap.get(jobName).shutdown();
            taskProducerSchedulerMap.remove(jobName);
        }
        configService.remove(jobName);
    }
}
