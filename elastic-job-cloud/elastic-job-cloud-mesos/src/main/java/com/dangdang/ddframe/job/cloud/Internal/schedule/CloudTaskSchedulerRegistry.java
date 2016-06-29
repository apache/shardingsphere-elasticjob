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

import com.dangdang.ddframe.job.cloud.Internal.config.CloudConfigurationService;
import com.dangdang.ddframe.job.cloud.Internal.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.Internal.state.StateService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 云任务调度注册器.
 *
 * @author zhangliang
 */
public final class CloudTaskSchedulerRegistry {
    
    private static volatile CloudTaskSchedulerRegistry instance;
    
    private final ConcurrentHashMap<String, CloudTaskScheduler> cloudTaskSchedulerMap = new ConcurrentHashMap<>(65535);
    
    private final CoordinatorRegistryCenter registryCenter;
    
    private final CloudConfigurationService configService;
    
    private final StateService stateService;
    
    private CloudTaskSchedulerRegistry(final CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
        configService = new CloudConfigurationService(registryCenter);
        stateService = new StateService(registryCenter);
    }
    
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
     * 将注册中心中的云作业配置注册调度.
     */
    public void registerFromRegistryCenter() {
        for (CloudJobConfiguration each : configService.loadAll()) {
            register(each);
        }
    }
    
    /**
     * 注册调度.
     * 
     * @param cloudJobConfig 云任务配置
     */
    public void register(final CloudJobConfiguration cloudJobConfig) {
        String jobName = cloudJobConfig.getJobName();
        if (cloudTaskSchedulerMap.containsKey(jobName)) {
            cloudTaskSchedulerMap.get(jobName).shutdown();
            cloudTaskSchedulerMap.remove(jobName);
        }
        configService.add(cloudJobConfig);
        stateService.sharding(jobName);
        CloudTaskScheduler cloudTaskScheduler = new CloudTaskScheduler(cloudJobConfig, registryCenter);
        cloudTaskScheduler.startup();
        cloudTaskSchedulerMap.put(jobName, cloudTaskScheduler);
    }
    
    /**
     * 重新注册调度.
     *
     * @param cloudJobConfig 云任务配置
     */
    public void reregister(final CloudJobConfiguration cloudJobConfig) {
        String jobName = cloudJobConfig.getJobName();
        Preconditions.checkState(cloudTaskSchedulerMap.containsKey(jobName));
        Optional<CloudJobConfiguration> originalCloudJobConfig = configService.load(jobName);
        Preconditions.checkState(originalCloudJobConfig.isPresent());
        configService.update(cloudJobConfig);
        boolean isCronChanged = !originalCloudJobConfig.get().getCron().equals(cloudJobConfig.getCron());
        boolean isShardingTotalCountChanged = originalCloudJobConfig.get().getShardingTotalCount() != cloudJobConfig.getShardingTotalCount();
        if (isCronChanged) {
            cloudTaskSchedulerMap.get(jobName).shutdown();
            cloudTaskSchedulerMap.remove(jobName);
        }
        if (isShardingTotalCountChanged) {
            stateService.reSharding(jobName);
        }
        if (isCronChanged) {
            CloudTaskScheduler cloudTaskScheduler = new CloudTaskScheduler(cloudJobConfig, registryCenter);
            cloudTaskScheduler.startup();
            cloudTaskSchedulerMap.put(jobName, cloudTaskScheduler);
        }
    }
    
    /**
     * 注销任务.
     * 
     * @param jobName 作业名称
     */
    public void unregister(final String jobName) {
        cloudTaskSchedulerMap.get(jobName).shutdown();
        cloudTaskSchedulerMap.remove(jobName);
        configService.remove(jobName);
    }
}
