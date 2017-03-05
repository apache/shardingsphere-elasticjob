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

package com.dangdang.ddframe.job.cloud.scheduler.config.app;

import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 云作业App配置服务.
 *
 * @author caohao
 */
@RequiredArgsConstructor
public class CloudAppConfigurationService {
    
    private final CoordinatorRegistryCenter regCenter;
    
    /**
     * 添加云作业APP配置.
     *
     * @param appConfig 云作业App配置对象
     */
    public void add(final CloudAppConfiguration appConfig) {
        regCenter.persist(CloudAppConfigurationNode.getRootNodePath(appConfig.getAppName()), CloudAppConfigurationGsonFactory.toJson(appConfig));
    }
    
    /**
     * 修改云作业APP配置.
     *
     * @param appConfig 云作业App配置对象
     */
    public void update(final CloudAppConfiguration appConfig) {
        regCenter.update(CloudAppConfigurationNode.getRootNodePath(appConfig.getAppName()), CloudAppConfigurationGsonFactory.toJson(appConfig));
    }
    
    /**
     * 根据云作业App名称获取App配置.
     *
     * @param appName 云作业App名称
     * @return 云作业App配置
     */
    public Optional<CloudAppConfiguration> load(final String appName) {
        return Optional.fromNullable(CloudAppConfigurationGsonFactory.fromJson(regCenter.get(CloudAppConfigurationNode.getRootNodePath(appName))));
    }
    
    /**
     * 获取所有注册的云作业App配置.
     *
     * @return 注册的云作业App配置
     */
    public Collection<CloudAppConfiguration> loadAll() {
        if (!regCenter.isExisted(CloudAppConfigurationNode.ROOT)) {
            return Collections.emptyList();
        }
        List<String> appNames = regCenter.getChildrenKeys(CloudAppConfigurationNode.ROOT);
        Collection<CloudAppConfiguration> result = new ArrayList<>(appNames.size());
        for (String each : appNames) {
            Optional<CloudAppConfiguration> config = load(each);
            if (config.isPresent()) {
                result.add(config.get());
            }
        }
        return result;
    }
    
    
    /**
     * 删除云作业App配置.
     *
     * @param appName 云作业App名称
     */
    public void remove(final String appName) {
        regCenter.remove(CloudAppConfigurationNode.getRootNodePath(appName));
    }
}
