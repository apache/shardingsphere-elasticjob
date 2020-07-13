/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Cloud app configuration service.
 */
@RequiredArgsConstructor
public final class CloudAppConfigurationService {
    
    private final CoordinatorRegistryCenter regCenter;
    
    /**
     * Add cloud app configuration.
     *
     * @param appConfig cloud app configuration
     */
    public void add(final CloudAppConfiguration appConfig) {
        regCenter.persist(CloudAppConfigurationNode.getRootNodePath(appConfig.getAppName()), CloudAppConfigurationGsonFactory.toJson(appConfig));
    }
    
    /**
     * Update cloud app configuration.
     *
     * @param appConfig cloud app configuration
     */
    public void update(final CloudAppConfiguration appConfig) {
        regCenter.update(CloudAppConfigurationNode.getRootNodePath(appConfig.getAppName()), CloudAppConfigurationGsonFactory.toJson(appConfig));
    }
    
    /**
     * Load app configuration by app name.
     *
     * @param appName application name
     * @return cloud app configuration
     */
    public Optional<CloudAppConfiguration> load(final String appName) {
        return Optional.ofNullable(CloudAppConfigurationGsonFactory.fromJson(regCenter.get(CloudAppConfigurationNode.getRootNodePath(appName))));
    }
    
    /**
     * Load all registered cloud app configurations.
     *
     * @return collection of the registered cloud app configuration
     */
    public Collection<CloudAppConfiguration> loadAll() {
        if (!regCenter.isExisted(CloudAppConfigurationNode.ROOT)) {
            return Collections.emptyList();
        }
        List<String> appNames = regCenter.getChildrenKeys(CloudAppConfigurationNode.ROOT);
        Collection<CloudAppConfiguration> result = new ArrayList<>(appNames.size());
        for (String each : appNames) {
            Optional<CloudAppConfiguration> config = load(each);
            config.ifPresent(result::add);
        }
        return result;
    }
    
    /**
     * Remove cloud app configuration by app name.
     *
     * @param appName to be removed application name
     */
    public void remove(final String appName) {
        regCenter.remove(CloudAppConfigurationNode.getRootNodePath(appName));
    }
}
