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

package com.dangdang.ddframe.job.cloud.Internal.config;

import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 云作业配置服务.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class CloudConfigurationService {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    /**
     * 添加云作业配置.
     * 
     * @param cloudJobConfig 云作业配置对象
     */
    // TODO 应为API
    public void add(final CloudJobConfiguration cloudJobConfig) {
        registryCenter.persist(CloudConfigurationNode.getShardingTotalCountNodePath(cloudJobConfig.getJobName()), Integer.toString(cloudJobConfig.getShardingTotalCount()));
        registryCenter.persist(CloudConfigurationNode.getCpuCountNodePath(cloudJobConfig.getJobName()), Double.toString(cloudJobConfig.getCpuCount()));
        registryCenter.persist(CloudConfigurationNode.getMemoryMBNodePath(cloudJobConfig.getJobName()), Double.toString(cloudJobConfig.getMemoryMB()));
        registryCenter.persist(CloudConfigurationNode.getDockerImageNameNodePath(cloudJobConfig.getJobName()), cloudJobConfig.getDockerImageName());
        registryCenter.persist(CloudConfigurationNode.getAppURLNodePath(cloudJobConfig.getJobName()), cloudJobConfig.getAppURL());
        registryCenter.persist(CloudConfigurationNode.getCronNodePath(cloudJobConfig.getJobName()), cloudJobConfig.getCron());
    }
    
    /**
     * 获取所有注册的云作业配置.
     * 
     * @return 注册的云作业配置
     */
    public Collection<CloudJobConfiguration> loadAll() {
        if (!registryCenter.isExisted("/jobs")) {
            return Collections.emptyList();
        }
        List<String> jobNames = registryCenter.getChildrenKeys("/jobs");
        Collection<CloudJobConfiguration> result = new ArrayList<>(jobNames.size());
        for (String each : jobNames) {
            result.add(load(each));
        }
        return result;
    }
    
    /**
     * 根据作业名称获取云作业配置.
     * 
     * @param jobName 作业名称
     * @return 云作业配置
     */
    public CloudJobConfiguration load(final String jobName) {
        Preconditions.checkState(registryCenter.isExisted(CloudConfigurationNode.getRootNodePath(jobName)));
        return new CloudJobConfiguration(
                jobName, 
                registryCenter.get(CloudConfigurationNode.getCronNodePath(jobName)), 
                Integer.parseInt(registryCenter.get(CloudConfigurationNode.getShardingTotalCountNodePath(jobName))), 
                Double.parseDouble(registryCenter.get(CloudConfigurationNode.getCpuCountNodePath(jobName))),
                Double.parseDouble(registryCenter.get(CloudConfigurationNode.getMemoryMBNodePath(jobName))), 
                registryCenter.get(CloudConfigurationNode.getDockerImageNameNodePath(jobName)),
                registryCenter.get(CloudConfigurationNode.getAppURLNodePath(jobName)));
    }
}
