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

package org.apache.shardingsphere.elasticjob.cloud.executor;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.elasticjob.cloud.api.JobType;
import org.apache.shardingsphere.elasticjob.cloud.config.JobCoreConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.JobTypeConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.dataflow.DataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.script.ScriptJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.simple.SimpleJobConfiguration;

import java.util.Map;

/**
 * Job type configuration utility.
 */
public final class JobTypeConfigurationUtil {
    
    /**
     * Create job configuration context.
     * 
     * @param jobConfigurationMap job configuration map
     * @return job type configuration
     */
    public static JobTypeConfiguration createJobConfigurationContext(final Map<String, String> jobConfigurationMap) {
        int ignoredShardingTotalCount = 1;
        String jobClass = jobConfigurationMap.get("jobClass");
        String jobType = jobConfigurationMap.get("jobType");
        String jobName = jobConfigurationMap.get("jobName");
        String cron = jobConfigurationMap.get("cron");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobName), "jobName can not be empty.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobType), "jobType can not be empty.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobClass), "jobClass can not be empty.");
        JobCoreConfiguration jobCoreConfig = JobCoreConfiguration.newBuilder(jobName, cron, ignoredShardingTotalCount)
                .jobExecutorServiceHandlerType(jobConfigurationMap.get("executorServiceHandler")).jobErrorHandlerType(jobConfigurationMap.get("jobExceptionHandler")).build();
        if (JobType.DATAFLOW.name().equals(jobType)) {
            return new DataflowJobConfiguration(jobCoreConfig, jobClass, Boolean.valueOf(jobConfigurationMap.get("streamingProcess")));
        } else if (JobType.SCRIPT.name().equals(jobType)) {
            return new ScriptJobConfiguration(jobCoreConfig, jobConfigurationMap.get("scriptCommandLine"));
        }
        return new SimpleJobConfiguration(jobCoreConfig, jobClass);
    }
}
