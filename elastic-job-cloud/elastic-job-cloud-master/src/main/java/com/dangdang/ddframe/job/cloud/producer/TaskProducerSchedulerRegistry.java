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

/**
 * 发布任务作业调度注册表.
 *
 * @author caohao
 */
public class TaskProducerSchedulerRegistry {
    
    private static volatile TaskProducerSchedulerRegistry instance;
    
    private final TaskProducerScheduler schedulerInstance;
    
    private final ConfigurationService configService;
    
    private TaskProducerSchedulerRegistry(final CoordinatorRegistryCenter regCenter) {
        configService = new ConfigurationService(regCenter);
        schedulerInstance = new TaskProducerScheduler(regCenter);
    }
    
    /**
     * 获取实例.
     * 
     * @param regCenter 注册中心对象
     * @return 实例对象
     */
    public static TaskProducerSchedulerRegistry getInstance(final CoordinatorRegistryCenter regCenter) {
        if (null == instance) {
            synchronized (TaskProducerSchedulerRegistry.class) {
                if (null == instance) {
                    instance = new TaskProducerSchedulerRegistry(regCenter);
                }
            }
        }
        return instance;
    }
    
    /**
     * 启动作业调度器.
     */
    public void startup() {
        schedulerInstance.startup(configService.loadAll());
    }
    
    /**
     * 注册作业.
     * 
     * @param jobConfig 作业配置
     */
    public void register(final CloudJobConfiguration jobConfig) {
        schedulerInstance.register(jobConfig);
        Optional<CloudJobConfiguration> jobConfigFromZk = configService.load(jobConfig.getJobName());
        if (!jobConfigFromZk.isPresent()) {
            configService.add(jobConfig);
        } else if (!jobConfigFromZk.get().equals(jobConfig)) {
            configService.update(jobConfig);
        }
    }
    
    /**
     * 注销作业.
     * 
     * @param jobName 作业名称
     */
    public void deregister(final String jobName) {
        Optional<CloudJobConfiguration> jobConfigFromZk = configService.load(jobName);
        if (jobConfigFromZk != null && jobConfigFromZk.isPresent()) {
            schedulerInstance.deregister(jobConfigFromZk.get());
            configService.remove(jobName);
        }
    }
    
    /**
     * 关闭作业调度器.
     */
    public void shutdown() {
        schedulerInstance.shutdown();
    }
}
