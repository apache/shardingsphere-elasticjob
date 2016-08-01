/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.api.internal.config;

import com.dangdang.ddframe.job.api.config.JobConfiguration;
import com.dangdang.ddframe.job.api.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.api.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.api.type.JobType;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJobConfiguration;
import com.dangdang.ddframe.job.api.type.script.api.ScriptJobConfiguration;
import com.dangdang.ddframe.job.api.type.simple.api.SimpleJobConfiguration;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 作业配置的Json转换适配器.
 * 
 * @param <T> 作业配置对象泛型
 *     
 * @author zhangliang
 */
@Slf4j
public abstract class AbstractJobConfigurationGsonTypeAdapter<T extends JobConfiguration> extends TypeAdapter<T> {
    
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
        DataflowJobConfiguration.DataflowType dataflowType = null;
        boolean streamingProcess = false;
        int concurrentDataProcessThreadCount = 0;
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
                case "dataflowType":
                    dataflowType = DataflowJobConfiguration.DataflowType.valueOf(in.nextString());
                    break;
                case "streamingProcess":
                    streamingProcess = in.nextBoolean();
                    break;
                case "concurrentDataProcessThreadCount":
                    concurrentDataProcessThreadCount = in.nextInt();
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
        JobCoreConfiguration coreConfig = getJobCoreConfiguration(jobName, cron, shardingTotalCount, shardingItemParameters,
                jobParameter, failover, misfire, description, jobProperties);
        JobTypeConfiguration typeConfig = getJobConfiguration(coreConfig, jobType, jobClass, dataflowType, streamingProcess, concurrentDataProcessThreadCount, scriptCommandLine);
        return getJobConfiguration(typeConfig, customizedValueMap);
    }
    
    private JobProperties getJobProperties(final JsonReader in) throws IOException {
        JobProperties result = new JobProperties();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "job_exception_handler":
                    result.put(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), in.nextString());
                    break;
                case "executor_service_handler":
                    result.put(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), in.nextString());
                    break;
                default:
                    break;
            }
        }
        in.endObject();
        return result;
    }
    
    protected abstract void addToCustomizedValueMap(final String jsonName, final JsonReader in, final Map<String, Object> customizedValueMap) throws IOException;
    
    private JobCoreConfiguration getJobCoreConfiguration(final String jobName, final String cron, final int shardingTotalCount, final String shardingItemParameters,
                                                         final String jobParameter, final boolean failover, final boolean misfire, final String description, final JobProperties jobProperties) {
        return JobCoreConfiguration.newBuilder(jobName, cron, shardingTotalCount)
                .shardingItemParameters(shardingItemParameters).jobParameter(jobParameter).failover(failover).misfire(misfire).description(description)
                .jobProperties(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), jobProperties.get(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER))
                .jobProperties(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), jobProperties.get(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER))
                .build();
    }
    
    private JobTypeConfiguration getJobConfiguration(final JobCoreConfiguration coreConfig, final JobType jobType, final String jobClass, final DataflowJobConfiguration.DataflowType dataflowType,
                                                     final boolean streamingProcess, final int concurrentDataProcessThreadCount, final String scriptCommandLine) {
        JobTypeConfiguration result;
        switch (jobType) {
            case SIMPLE:
                result = new SimpleJobConfiguration(coreConfig, jobClass);
                break;
            case DATAFLOW:
                result = new DataflowJobConfiguration(coreConfig, jobClass, dataflowType, streamingProcess, concurrentDataProcessThreadCount);
                break;
            case SCRIPT:
                result = new ScriptJobConfiguration(coreConfig, scriptCommandLine);
                break;
            default:
                throw new UnsupportedOperationException(jobType.name());
        }
        return result;
    }
    
    protected abstract T getJobConfiguration(final JobTypeConfiguration typeConfig, final Map<String, Object> customizedValueMap);
    
    @Override
    public void write(final JsonWriter out, final T value) throws IOException {
        out.beginObject();
        out.name("jobName").value(value.getTypeConfig().getCoreConfig().getJobName());
        out.name("jobClass").value(value.getTypeConfig().getJobClass());
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
            DataflowJobConfiguration dataflowJobConfig = (DataflowJobConfiguration) value.getTypeConfig();
            out.name("dataflowType").value(dataflowJobConfig.getDataflowType().name());
            out.name("streamingProcess").value(dataflowJobConfig.isStreamingProcess());
            out.name("concurrentDataProcessThreadCount").value(dataflowJobConfig.getConcurrentDataProcessThreadCount());
        } else if (value.getTypeConfig().getJobType() == JobType.SCRIPT) {
            ScriptJobConfiguration scriptJobConfig = (ScriptJobConfiguration) value.getTypeConfig();
            out.name("scriptCommandLine").value(scriptJobConfig.getScriptCommandLine());
        }
        writeCustomized(out, value);
        out.endObject();
    }
    
    protected abstract void writeCustomized(final JsonWriter out, final T value) throws IOException;
}
