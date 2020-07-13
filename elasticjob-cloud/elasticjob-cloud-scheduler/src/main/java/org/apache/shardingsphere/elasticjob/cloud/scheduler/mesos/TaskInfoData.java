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
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.config.script.ScriptJobConfiguration;
import org.apache.shardingsphere.elasticjob.dataflow.props.DataflowJobProperties;
import org.apache.shardingsphere.elasticjob.script.props.ScriptJobProperties;

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
        result.put("jobName", jobConfig.getJobName());
        result.put("cron", CloudJobExecutionType.DAEMON == jobConfig.getJobExecutionType() ? jobConfig.getTypeConfig().getCoreConfig().getCron() : "");
        result.put("executorServiceHandler", jobConfig.getTypeConfig().getCoreConfig().getJobExecutorServiceHandlerType());
        result.put("jobExceptionHandler", jobConfig.getTypeConfig().getCoreConfig().getJobErrorHandlerType());
        if (jobConfig.getTypeConfig().getCoreConfig().getProps().containsKey(DataflowJobProperties.STREAM_PROCESS_KEY)) {
            result.put("streamingProcess", jobConfig.getTypeConfig().getCoreConfig().getProps().getProperty(DataflowJobProperties.STREAM_PROCESS_KEY));
        } else if (jobConfig.getTypeConfig() instanceof ScriptJobConfiguration) {
            result.put("scriptCommandLine", jobConfig.getTypeConfig().getCoreConfig().getProps().getProperty(ScriptJobProperties.SCRIPT_KEY));
        }
        result.put("beanName", jobConfig.getBeanName());
        result.put("applicationContext", jobConfig.getApplicationContext());
        return result;
    }
}
