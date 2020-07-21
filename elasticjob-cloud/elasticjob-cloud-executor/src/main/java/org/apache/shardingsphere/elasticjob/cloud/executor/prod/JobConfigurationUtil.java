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

package org.apache.shardingsphere.elasticjob.cloud.executor.prod;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;

import java.util.Map;

/**
 * Job configuration utility.
 */
public final class JobConfigurationUtil {
    
    /**
     * Create job configuration context.
     * 
     * @param jobConfigurationMap job configuration map
     * @return job configuration
     */
    public static JobConfiguration createJobConfiguration(final Map<String, String> jobConfigurationMap) {
        int ignoredShardingTotalCount = 1;
        String jobName = jobConfigurationMap.remove("jobName");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobName), "jobName can not be empty.");
        JobConfiguration result = JobConfiguration.newBuilder(jobName, ignoredShardingTotalCount).cron(jobConfigurationMap.remove("cron"))
                .jobExecutorServiceHandlerType(jobConfigurationMap.remove("jobExecutorServiceHandlerType")).jobErrorHandlerType(jobConfigurationMap.remove("jobErrorHandlerType")).build();
        jobConfigurationMap.forEach((key, value) -> result.getProps().setProperty(key, value));
        return result;
    }
}
