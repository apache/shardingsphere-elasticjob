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

package org.apache.shardingsphere.elasticjob.lite.util.json;

import com.google.common.base.Preconditions;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.shardingsphere.elasticjob.lite.api.JobType;
import org.apache.shardingsphere.elasticjob.lite.config.JobCoreConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.JobRootConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.JobTypeConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.dataflow.DataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.script.ScriptJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.simple.SimpleJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.executor.handler.JobProperties;
import org.apache.shardingsphere.elasticjob.lite.executor.handler.JobProperties.JobPropertiesEnum;
import org.apache.shardingsphere.elasticjob.lite.internal.config.provided.InstanceProvidedDataflowJobConfiguration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Job configuration gson type adapter.
 *
 * @param <T> type of job root configuration
 */
public abstract class AbstractJobConfigurationGsonTypeAdapter<T extends JobRootConfiguration> extends TypeAdapter<T> {
    
    @Override
    public T read(final JsonReader in) throws IOException {
        String jobName = "";
        String cron = "";
        int shardingTotalCount = 0;
        String shardingItemParameters = "";
        String jobParameter = "";
        boolean failover = false;
        boolean misfire = failover;
        String description = "";
        JobProperties jobProperties = new JobProperties();
        JobType jobType = null;
        String jobClass = "";
        boolean streamingProcess = false;
        String scriptCommandLine = "";
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
                case "description":
                    description = in.nextString();
                    break;
                case "jobProperties":
                    jobProperties = getJobProperties(in);
                    break;
                case "jobType":
                    jobType = JobType.valueOf(in.nextString());
                    break;
                case "jobClass":
                    jobClass = in.nextString();
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
        JobCoreConfiguration coreConfig = getJobCoreConfiguration(jobName, cron, shardingTotalCount, shardingItemParameters, jobParameter, failover, misfire, description, jobProperties);
        JobTypeConfiguration typeConfig = getJobTypeConfiguration(coreConfig, jobType, streamingProcess, scriptCommandLine);
        return getJobRootConfiguration(typeConfig, customizedValueMap);
    }
    
    private JobProperties getJobProperties(final JsonReader in) throws IOException {
        JobProperties result = new JobProperties();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "job_exception_handler":
                    result.put(JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), in.nextString());
                    break;
                case "executor_service_handler":
                    result.put(JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), in.nextString());
                    break;
                default:
                    break;
            }
        }
        in.endObject();
        return result;
    }
    
    protected abstract void addToCustomizedValueMap(String jsonName, JsonReader in, Map<String, Object> customizedValueMap) throws IOException;
    
    private JobCoreConfiguration getJobCoreConfiguration(final String jobName, final String cron, final int shardingTotalCount,
                                                         final String shardingItemParameters, final String jobParameter, final boolean failover,
                                                         final boolean misfire, final String description,
                                                         final JobProperties jobProperties) {
        return JobCoreConfiguration.newBuilder(jobName, cron, shardingTotalCount)
                .shardingItemParameters(shardingItemParameters).jobParameter(jobParameter).failover(failover).misfire(misfire).description(description)
                .jobProperties(JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), jobProperties.get(JobPropertiesEnum.JOB_EXCEPTION_HANDLER))
                .jobProperties(JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), jobProperties.get(JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER))
                .build();
    }
    
    private JobTypeConfiguration getJobTypeConfiguration(final JobCoreConfiguration coreConfig, final JobType jobType, final boolean streamingProcess, final String scriptCommandLine) {
        Preconditions.checkNotNull(jobType, "jobType cannot be null.");
        switch (jobType) {
            case SIMPLE:
                return new SimpleJobConfiguration(coreConfig);
            case DATAFLOW:
                return new DataflowJobConfiguration(coreConfig, streamingProcess);
            case SCRIPT:
                return new ScriptJobConfiguration(coreConfig, scriptCommandLine);
            default:
                throw new UnsupportedOperationException(String.valueOf(jobType));
        }
    }
    
    protected abstract T getJobRootConfiguration(JobTypeConfiguration typeConfig, Map<String, Object> customizedValueMap);
    
    @Override
    public void write(final JsonWriter out, final T value) throws IOException {
        out.beginObject();
        out.name("jobName").value(value.getTypeConfig().getCoreConfig().getJobName());
        out.name("jobType").value(value.getTypeConfig().getJobType().name());
        out.name("cron").value(value.getTypeConfig().getCoreConfig().getCron());
        out.name("shardingTotalCount").value(value.getTypeConfig().getCoreConfig().getShardingTotalCount());
        out.name("shardingItemParameters").value(value.getTypeConfig().getCoreConfig().getShardingItemParameters());
        out.name("jobParameter").value(value.getTypeConfig().getCoreConfig().getJobParameter());
        out.name("failover").value(value.getTypeConfig().getCoreConfig().isFailover());
        out.name("misfire").value(value.getTypeConfig().getCoreConfig().isMisfire());
        out.name("description").value(value.getTypeConfig().getCoreConfig().getDescription());
        out.name("jobProperties").jsonValue(value.getTypeConfig().getCoreConfig().getJobProperties().json());
        if (value.getTypeConfig().getJobType() == JobType.DATAFLOW) {
            boolean streamingProcess;
            if (value.getTypeConfig() instanceof InstanceProvidedDataflowJobConfiguration) {
                streamingProcess = ((InstanceProvidedDataflowJobConfiguration) value.getTypeConfig()).isStreamingProcess();
            } else {
                streamingProcess = ((DataflowJobConfiguration) value.getTypeConfig()).isStreamingProcess();
            }
            out.name("streamingProcess").value(streamingProcess);
        } else if (value.getTypeConfig().getJobType() == JobType.SCRIPT) {
            ScriptJobConfiguration scriptJobConfig = (ScriptJobConfiguration) value.getTypeConfig();
            out.name("scriptCommandLine").value(scriptJobConfig.getScriptCommandLine());
        }
        writeCustomized(out, value);
        out.endObject();
    }
    
    protected abstract void writeCustomized(JsonWriter out, T value) throws IOException;
}
