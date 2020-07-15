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
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.dataflow.props.DataflowJobProperties;
import org.apache.shardingsphere.elasticjob.script.props.ScriptJobProperties;

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
    public static JobConfiguration createJobConfigurationContext(final Map<String, String> jobConfigurationMap) {
        int ignoredShardingTotalCount = 1;
        String jobName = jobConfigurationMap.get("jobName");
        String cron = jobConfigurationMap.get("cron");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobName), "jobName can not be empty.");
        JobConfiguration result = JobConfiguration.newBuilder(jobName, ignoredShardingTotalCount).cron(cron)
                .jobExecutorServiceHandlerType(jobConfigurationMap.get("executorServiceHandler")).jobErrorHandlerType(jobConfigurationMap.get("jobExceptionHandler")).build();
        if (jobConfigurationMap.containsKey("streamingProcess")) {
            result.getProps().setProperty(DataflowJobProperties.STREAM_PROCESS_KEY, jobConfigurationMap.get("streamingProcess"));
        } else if (jobConfigurationMap.containsKey("scriptCommandLine")) {
            result.getProps().setProperty(ScriptJobProperties.SCRIPT_KEY, jobConfigurationMap.get("scriptCommandLine"));
        }
        return result;
    }
}
