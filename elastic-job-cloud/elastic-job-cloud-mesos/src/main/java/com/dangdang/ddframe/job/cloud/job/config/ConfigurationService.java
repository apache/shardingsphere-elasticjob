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

package com.dangdang.ddframe.job.cloud.job.config;

import com.dangdang.ddframe.job.cloud.job.JobNode;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 作业配置服务.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ConfigurationService {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    /**
     * 添加云作业配置.
     * 
     * @param cloudJobConfig 云作业配置对象
     */
    public void add(final CloudJobConfiguration cloudJobConfig) {
        registryCenter.persist(ConfigurationNode.getShardingTotalCountNodePath(cloudJobConfig.getJobName()), Integer.toString(cloudJobConfig.getShardingTotalCount()));
        registryCenter.persist(ConfigurationNode.getCpuCountNodePath(cloudJobConfig.getJobName()), Double.toString(cloudJobConfig.getCpuCount()));
        registryCenter.persist(ConfigurationNode.getMemoryMBNodePath(cloudJobConfig.getJobName()), Double.toString(cloudJobConfig.getMemoryMB()));
        registryCenter.persist(ConfigurationNode.getDockerImageNameNodePath(cloudJobConfig.getJobName()), cloudJobConfig.getDockerImageName());
        registryCenter.persist(ConfigurationNode.getAppURLNodePath(cloudJobConfig.getJobName()), cloudJobConfig.getAppURL());
        registryCenter.persist(ConfigurationNode.getCronNodePath(cloudJobConfig.getJobName()), cloudJobConfig.getCron());
    }
    
    /**
     * 修改云作业配置.
     *
     * @param cloudJobConfig 云作业配置对象
     */
    public void update(final CloudJobConfiguration cloudJobConfig) {
        updateIfNeed(ConfigurationNode.getShardingTotalCountNodePath(cloudJobConfig.getJobName()), Integer.toString(cloudJobConfig.getShardingTotalCount()));
        updateIfNeed(ConfigurationNode.getShardingTotalCountNodePath(cloudJobConfig.getJobName()), Integer.toString(cloudJobConfig.getShardingTotalCount()));
        updateIfNeed(ConfigurationNode.getCpuCountNodePath(cloudJobConfig.getJobName()), Double.toString(cloudJobConfig.getCpuCount()));
        updateIfNeed(ConfigurationNode.getMemoryMBNodePath(cloudJobConfig.getJobName()), Double.toString(cloudJobConfig.getMemoryMB()));
        updateIfNeed(ConfigurationNode.getDockerImageNameNodePath(cloudJobConfig.getJobName()), cloudJobConfig.getDockerImageName());
        updateIfNeed(ConfigurationNode.getAppURLNodePath(cloudJobConfig.getJobName()), cloudJobConfig.getAppURL());
        updateIfNeed(ConfigurationNode.getCronNodePath(cloudJobConfig.getJobName()), cloudJobConfig.getCron());
    }
    
    private void updateIfNeed(final String znode, final String newValue) {
        if (null != newValue && !newValue.equals(registryCenter.get(znode))) {
            registryCenter.update(znode, newValue);
        }
    }
    
    /**
     * 获取所有注册的云作业配置.
     * 
     * @return 注册的云作业配置
     */
    public Collection<CloudJobConfiguration> loadAll() {
        if (!registryCenter.isExisted(JobNode.ROOT)) {
            return Collections.emptyList();
        }
        List<String> jobNames = registryCenter.getChildrenKeys(JobNode.ROOT);
        Collection<CloudJobConfiguration> result = new ArrayList<>(jobNames.size());
        for (String each : jobNames) {
            Optional<CloudJobConfiguration> config = load(each);
            if (config.isPresent()) {
                result.add(config.get());
            }
        }
        return result;
    }
    
    /**
     * 根据作业名称获取云作业配置.
     * 
     * @param jobName 作业名称
     * @return 云作业配置
     */
    public Optional<CloudJobConfiguration> load(final String jobName) {
        return !registryCenter.isExisted(ConfigurationNode.getRootNodePath(jobName)) ? Optional.<CloudJobConfiguration>absent() : Optional.of(new CloudJobConfiguration(
                jobName, 
                registryCenter.get(ConfigurationNode.getCronNodePath(jobName)), 
                Integer.parseInt(registryCenter.get(ConfigurationNode.getShardingTotalCountNodePath(jobName))), 
                Double.parseDouble(registryCenter.get(ConfigurationNode.getCpuCountNodePath(jobName))),
                Double.parseDouble(registryCenter.get(ConfigurationNode.getMemoryMBNodePath(jobName))), 
                registryCenter.get(ConfigurationNode.getDockerImageNameNodePath(jobName)),
                registryCenter.get(ConfigurationNode.getAppURLNodePath(jobName))));
    }
    
    /**
     * 删除云作业.
     *
     * @param jobName 作业名称
     */
    public void remove(final String jobName) {
        registryCenter.remove(JobNode.getJobRootNodePath(jobName));
    }
}
