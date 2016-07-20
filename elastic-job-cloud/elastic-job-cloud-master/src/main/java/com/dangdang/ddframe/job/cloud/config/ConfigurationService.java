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

import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import com.google.gson.Gson;
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
public class ConfigurationService {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    /**
     * 添加云作业配置.
     * 
     * @param jobConfig 云作业配置对象
     */
    public void add(final CloudJobConfiguration jobConfig) {
        registryCenter.persist(ConfigurationNode.getRootNodePath(jobConfig.getJobName()), new Gson().toJson(jobConfig));
    }
    
    /**
     * 修改云作业配置.
     *
     * @param jobConfig 云作业配置对象
     */
    public void update(final CloudJobConfiguration jobConfig) {
        registryCenter.update(ConfigurationNode.getRootNodePath(jobConfig.getJobName()), new Gson().toJson(jobConfig));
    }
    
    /**
     * 获取所有注册的云作业配置.
     * 
     * @return 注册的云作业配置
     */
    public Collection<CloudJobConfiguration> loadAll() {
        if (!registryCenter.isExisted(ConfigurationNode.ROOT)) {
            return Collections.emptyList();
        }
        List<String> jobNames = registryCenter.getChildrenKeys(ConfigurationNode.ROOT);
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
        return !registryCenter.isExisted(ConfigurationNode.getRootNodePath(jobName)) ? Optional.<CloudJobConfiguration>absent()
                : Optional.of(new Gson().fromJson(registryCenter.get(ConfigurationNode.getRootNodePath(jobName)), CloudJobConfiguration.class));
    }
    
    /**
     * 删除云作业.
     *
     * @param jobName 作业名称
     */
    public void remove(final String jobName) {
        registryCenter.remove(ConfigurationNode.getRootNodePath(jobName));
    }
}
