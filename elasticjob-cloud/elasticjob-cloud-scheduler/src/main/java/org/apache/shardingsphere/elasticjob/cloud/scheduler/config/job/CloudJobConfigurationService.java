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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.cloud.reg.base.CoordinatorRegistryCenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Cloud job configuration service.
 */
@RequiredArgsConstructor
public final class CloudJobConfigurationService {
    
    private final CoordinatorRegistryCenter regCenter;
    
    /**
     * Add cloud job configuration.
     * 
     * @param jobConfig cloud job configuration
     */
    public void add(final CloudJobConfiguration jobConfig) {
        regCenter.persist(CloudJobConfigurationNode.getRootNodePath(jobConfig.getJobName()), CloudJobConfigurationGsonFactory.toJson(jobConfig));
    }
    
    /**
     * Update cloud job configuration.
     *
     * @param jobConfig cloud job configuration
     */
    public void update(final CloudJobConfiguration jobConfig) {
        regCenter.update(CloudJobConfigurationNode.getRootNodePath(jobConfig.getJobName()), CloudJobConfigurationGsonFactory.toJson(jobConfig));
    }
    
    /**
     * Load all registered cloud job configurations.
     *
     * @return collection of the registered cloud job configuration
     */
    public Collection<CloudJobConfiguration> loadAll() {
        if (!regCenter.isExisted(CloudJobConfigurationNode.ROOT)) {
            return Collections.emptyList();
        }
        List<String> jobNames = regCenter.getChildrenKeys(CloudJobConfigurationNode.ROOT);
        Collection<CloudJobConfiguration> result = new ArrayList<>(jobNames.size());
        for (String each : jobNames) {
            load(each).ifPresent(result::add);
        }
        return result;
    }
    
    /**
     * Load cloud job configuration by job name.
     *
     * @param jobName job name
     * @return cloud job configuration
     */
    public Optional<CloudJobConfiguration> load(final String jobName) {
        return Optional.ofNullable(CloudJobConfigurationGsonFactory.fromJson(regCenter.get(CloudJobConfigurationNode.getRootNodePath(jobName))));
    }
    
    /**
     * Remove cloud job configuration.
     *
     * @param jobName job name
     */
    public void remove(final String jobName) {
        regCenter.remove(CloudJobConfigurationNode.getRootNodePath(jobName));
    }
}
