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

package org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.settings;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.lite.internal.config.yaml.YamlJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodePath;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobConfigurationAPI;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.util.yaml.YamlEngine;

/**
 * Job Configuration API implementation class.
 */
@RequiredArgsConstructor
public final class JobConfigurationAPIImpl implements JobConfigurationAPI {
    
    private final CoordinatorRegistryCenter regCenter;
    
    @Override
    public YamlJobConfiguration getJobConfiguration(final String jobName) {
        return YamlEngine.unmarshal(regCenter.get(new JobNodePath(jobName).getConfigNodePath()), YamlJobConfiguration.class);
    }
    
    @Override
    public void updateJobConfiguration(final YamlJobConfiguration yamlJobConfiguration) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(yamlJobConfiguration.getJobName()), "jobName can not be empty.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(yamlJobConfiguration.getCron()), "cron can not be empty.");
        Preconditions.checkArgument(yamlJobConfiguration.getShardingTotalCount() > 0, "shardingTotalCount should larger than zero.");
        JobNodePath jobNodePath = new JobNodePath(yamlJobConfiguration.getJobName());
        regCenter.update(jobNodePath.getConfigNodePath(), YamlEngine.marshal(yamlJobConfiguration));
    }
    
    @Override
    public void removeJobConfiguration(final String jobName) {
        regCenter.remove("/" + jobName);
    }
}
