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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.shardingsphere.elasticjob.api.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.cloud.config.dataflow.DataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.script.ScriptJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobExecutionType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Task info data.
 */
@RequiredArgsConstructor
public final class TaskInfoData {
    
    private final ShardingContexts shardingContexts;
    
    private final CloudJobConfiguration jobConfig;
    
    /**
     * Serialize.
     * 
     * @return byte array
     */
    public byte[] serialize() {
        Map<String, Object> result = new LinkedHashMap<>(2, 1);
        result.put("shardingContext", shardingContexts);
        result.put("jobConfigContext", buildJobConfigurationContext());
        return SerializationUtils.serialize((LinkedHashMap) result);
    }
    
    private Map<String, String> buildJobConfigurationContext() {
        Map<String, String> result = new LinkedHashMap<>(16, 1);
        result.put("jobType", jobConfig.getTypeConfig().getJobType().name());
        result.put("jobName", jobConfig.getJobName());
        result.put("jobClass", jobConfig.getTypeConfig().getJobClass());
        result.put("cron", CloudJobExecutionType.DAEMON == jobConfig.getJobExecutionType() ? jobConfig.getTypeConfig().getCoreConfig().getCron() : "");
        result.put("executorServiceHandler", jobConfig.getTypeConfig().getCoreConfig().getJobExecutorServiceHandlerType());
        result.put("jobExceptionHandler", jobConfig.getTypeConfig().getCoreConfig().getJobErrorHandlerType());
        if (jobConfig.getTypeConfig() instanceof DataflowJobConfiguration) {
            result.put("streamingProcess", Boolean.toString(((DataflowJobConfiguration) jobConfig.getTypeConfig()).isStreamingProcess()));
        } else if (jobConfig.getTypeConfig() instanceof ScriptJobConfiguration) {
            result.put("scriptCommandLine", ((ScriptJobConfiguration) jobConfig.getTypeConfig()).getScriptCommandLine());
        }
        result.put("beanName", jobConfig.getBeanName());
        result.put("applicationContext", jobConfig.getApplicationContext());
        return result;
    }
}
