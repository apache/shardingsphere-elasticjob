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

package org.apache.shardingsphere.elasticjob.lite.internal.config;

import org.apache.shardingsphere.elasticjob.lite.config.LiteJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.lite.exception.JobExecutionEnvironmentException;
import org.apache.shardingsphere.elasticjob.lite.internal.config.json.LiteJobConfigurationGsonFactory;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.util.env.TimeService;

/**
 * Configuration service.
 */
public final class ConfigurationService {
    
    private final TimeService timeService;
    
    private final JobNodeStorage jobNodeStorage;
    
    public ConfigurationService(final CoordinatorRegistryCenter regCenter, final String jobName) {
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        timeService = new TimeService();
    }
    
    /**
     * Load job configuration.
     * 
     * @param fromCache load from cache or not
     * @return job configuration
     */
    public LiteJobConfiguration load(final boolean fromCache) {
        String result;
        if (fromCache) {
            result = jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT);
            if (null == result) {
                result = jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT);
            }
        } else {
            result = jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT);
        }
        return LiteJobConfigurationGsonFactory.fromJson(result);
    }
    
    /**
     * Persist job configuration.
     * 
     * @param jobClassName job class name
     * @param liteJobConfig job configuration
     */
    public void persist(final String jobClassName, final LiteJobConfiguration liteJobConfig) {
        checkConflictJob(jobClassName, liteJobConfig);
        if (!jobNodeStorage.isJobNodeExisted(ConfigurationNode.ROOT) || liteJobConfig.isOverwrite()) {
            jobNodeStorage.replaceJobNode(ConfigurationNode.ROOT, LiteJobConfigurationGsonFactory.toJson(liteJobConfig));
            jobNodeStorage.replaceJobRootNode(jobClassName);
        }
    }
    
    private void checkConflictJob(final String newJobClassName, final LiteJobConfiguration liteJobConfig) {
        if (!jobNodeStorage.isJobRootNodeExisted()) {
            return;
        }
        String originalJobClassName = jobNodeStorage.getJobRootNodeData();
        if (null != originalJobClassName && !originalJobClassName.equals(newJobClassName)) {
            throw new JobConfigurationException(
                    "Job conflict with register center. The job '%s' in register center's class is '%s', your job class is '%s'", liteJobConfig.getJobName(), originalJobClassName, newJobClassName);
        }
    }
    
    /**
     * Check max time different seconds tolerable between job server and registry center.
     * 
     * @throws JobExecutionEnvironmentException throe JobExecutionEnvironmentException if exceed max time different seconds
     */
    public void checkMaxTimeDiffSecondsTolerable() throws JobExecutionEnvironmentException {
        int maxTimeDiffSeconds = load(true).getMaxTimeDiffSeconds();
        if (-1 == maxTimeDiffSeconds) {
            return;
        }
        long timeDiff = Math.abs(timeService.getCurrentMillis() - jobNodeStorage.getRegistryCenterTime());
        if (timeDiff > maxTimeDiffSeconds * 1000L) {
            throw new JobExecutionEnvironmentException(
                    "Time different between job server and register center exceed '%s' seconds, max time different is '%s' seconds.", timeDiff / 1000, maxTimeDiffSeconds);
        }
    }
}
