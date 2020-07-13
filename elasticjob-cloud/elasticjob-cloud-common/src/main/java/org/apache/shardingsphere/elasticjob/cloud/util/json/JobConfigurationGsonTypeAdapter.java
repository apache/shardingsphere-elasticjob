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

package org.apache.shardingsphere.elasticjob.cloud.util.json;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.dataflow.props.DataflowJobProperties;
import org.apache.shardingsphere.elasticjob.script.props.ScriptJobProperties;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Job configuration gson type adapter.
 */
public final class JobConfigurationGsonTypeAdapter extends TypeAdapter<CloudJobConfiguration> {
    
    @Override
    public CloudJobConfiguration read(final JsonReader in) throws IOException {
        String jobName = "";
        String cron = "";
        int shardingTotalCount = 0;
        String shardingItemParameters = "";
        String jobParameter = "";
        boolean failover = false;
        boolean misfire = false;
        String jobExecutorServiceHandlerType = "";
        String jobErrorHandlerType = "";
        String description = "";
        Boolean streamingProcess = null;
        String scriptCommandLine = null;
        Map<String, Object> customizedValueMap = new HashMap<>(32, 1);
        in.beginObject();
        while (in.hasNext()) {
            String jsonName = in.nextName();
            switch (jsonName) {
                case "jobName":
                    jobName = in.nextString();
                    break;
                case "cron":
                    cron = in.nextString();
                    break;
                case "shardingTotalCount":
                    shardingTotalCount = in.nextInt();
                    break;
                case "shardingItemParameters":
                    shardingItemParameters = in.nextString();
                    break;
                case "jobParameter":
                    jobParameter = in.nextString();
                    break;
                case "failover":
                    failover = in.nextBoolean();
                    break;
                case "misfire":
                    misfire = in.nextBoolean();
                    break;
                case "jobExecutorServiceHandlerType":
                    jobExecutorServiceHandlerType = in.nextString();
                    break;
                case "jobErrorHandlerType":
                    jobErrorHandlerType = in.nextString();
                    break;
                case "description":
                    description = in.nextString();
                    break;
                case "streamingProcess":
                    streamingProcess = in.nextBoolean();
                    break;
                case "scriptCommandLine":
                    scriptCommandLine = in.nextString();
                    break;
                default:
                    addToCustomizedValueMap(jsonName, in, customizedValueMap);
                    break;
            }
        }
        in.endObject();
        JobConfiguration jobConfig = getJobConfiguration(jobName, cron, shardingTotalCount, shardingItemParameters,
                jobParameter, failover, misfire, jobExecutorServiceHandlerType, jobErrorHandlerType, description);
        if (null != streamingProcess) {
            jobConfig.getProps().setProperty(DataflowJobProperties.STREAM_PROCESS_KEY, Boolean.toString(streamingProcess));
        }
        if (null != scriptCommandLine) {
            jobConfig.getProps().setProperty(ScriptJobProperties.SCRIPT_KEY, scriptCommandLine);
        }
        return getCloudJobConfiguration(jobConfig, customizedValueMap);
    }
    
    private void addToCustomizedValueMap(final String jsonName, final JsonReader in, final Map<String, Object> customizedValueMap) throws IOException {
        switch (jsonName) {
            case CloudConfigurationConstants.CPU_COUNT:
            case CloudConfigurationConstants.MEMORY_MB:
                customizedValueMap.put(jsonName, in.nextDouble());
                break;
            case CloudConfigurationConstants.APP_NAME:
            case CloudConfigurationConstants.JOB_EXECUTION_TYPE:
                customizedValueMap.put(jsonName, in.nextString());
                break;
            default:
                in.skipValue();
                break;
        }
    }
    
    private JobConfiguration getJobConfiguration(final String jobName, final String cron, final int shardingTotalCount,
                                                 final String shardingItemParameters, final String jobParameter, final boolean failover, final boolean misfire,
                                                 final String jobExecutorServiceHandlerType, final String jobErrorHandlerType, final String description) {
        return JobConfiguration.newBuilder(jobName, shardingTotalCount).cron(cron)
                .shardingItemParameters(shardingItemParameters).jobParameter(jobParameter).failover(failover).misfire(misfire)
                .jobExecutorServiceHandlerType(jobExecutorServiceHandlerType).jobErrorHandlerType(jobErrorHandlerType).description(description)
                .build();
    }
    
    private CloudJobConfiguration getCloudJobConfiguration(final JobConfiguration jobConfig, final Map<String, Object> customizedValueMap) {
        Preconditions.checkNotNull(customizedValueMap.get(CloudConfigurationConstants.APP_NAME), "appName cannot be null.");
        Preconditions.checkNotNull(customizedValueMap.get(CloudConfigurationConstants.CPU_COUNT), "cpuCount cannot be null.");
        Preconditions.checkArgument((double) customizedValueMap.get(CloudConfigurationConstants.CPU_COUNT) >= 0.001, "cpuCount cannot be less than 0.001");
        Preconditions.checkNotNull(customizedValueMap.get(CloudConfigurationConstants.MEMORY_MB), "memoryMB cannot be null.");
        Preconditions.checkArgument((double) customizedValueMap.get(CloudConfigurationConstants.MEMORY_MB) >= 1, "memory cannot be less than 1");
        Preconditions.checkNotNull(customizedValueMap.get(CloudConfigurationConstants.JOB_EXECUTION_TYPE), "jobExecutionType cannot be null.");
        return new CloudJobConfiguration((String) customizedValueMap.get(CloudConfigurationConstants.APP_NAME), jobConfig,
                (double) customizedValueMap.get(CloudConfigurationConstants.CPU_COUNT),
                (double) customizedValueMap.get(CloudConfigurationConstants.MEMORY_MB),
                CloudJobExecutionType.valueOf(customizedValueMap.get(CloudConfigurationConstants.JOB_EXECUTION_TYPE).toString()));
    }
    
    @Override
    public void write(final JsonWriter out, final CloudJobConfiguration value) throws IOException {
        out.beginObject();
        out.name("jobName").value(value.getJobConfig().getJobName());
        out.name("cron").value(value.getJobConfig().getCron());
        out.name("shardingTotalCount").value(value.getJobConfig().getShardingTotalCount());
        out.name("shardingItemParameters").value(value.getJobConfig().getShardingItemParameters());
        out.name("jobParameter").value(value.getJobConfig().getJobParameter());
        out.name("failover").value(value.getJobConfig().isFailover());
        out.name("misfire").value(value.getJobConfig().isMisfire());
        if (!Strings.isNullOrEmpty(value.getJobConfig().getJobExecutorServiceHandlerType())) {
            out.name("executorServiceHandler").value(value.getJobConfig().getJobExecutorServiceHandlerType());
        }
        if (!Strings.isNullOrEmpty(value.getJobConfig().getJobErrorHandlerType())) {
            out.name("jobExceptionHandler").value(value.getJobConfig().getJobErrorHandlerType());
        }
        out.name("description").value(value.getJobConfig().getDescription());
        if (value.getJobConfig().getProps().containsKey(DataflowJobProperties.STREAM_PROCESS_KEY)) {
            out.name("streamingProcess").value(value.getJobConfig().getProps().getProperty(DataflowJobProperties.STREAM_PROCESS_KEY));
        }
        if (value.getJobConfig().getProps().containsKey(ScriptJobProperties.SCRIPT_KEY)) {
            out.name("scriptCommandLine").value(value.getJobConfig().getProps().getProperty(ScriptJobProperties.SCRIPT_KEY));
        }
        writeCustomized(out, value);
        out.endObject();
    }
    
    private void writeCustomized(final JsonWriter out, final CloudJobConfiguration value) throws IOException {
        out.name(CloudConfigurationConstants.APP_NAME).value(value.getAppName());
        out.name(CloudConfigurationConstants.CPU_COUNT).value(value.getCpuCount());
        out.name(CloudConfigurationConstants.MEMORY_MB).value(value.getMemoryMB());
        out.name(CloudConfigurationConstants.JOB_EXECUTION_TYPE).value(value.getJobExecutionType().name());
    }
}
