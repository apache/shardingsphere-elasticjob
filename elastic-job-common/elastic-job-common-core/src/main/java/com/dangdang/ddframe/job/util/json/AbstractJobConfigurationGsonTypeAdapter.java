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

package com.dangdang.ddframe.job.util.json;

import com.dangdang.ddframe.job.api.JobType;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobRootConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.executor.handler.JobProperties;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 作业配置的Json转换适配器.
 *
 * @param <T> 作业配置对象泛型
 *
 * @author zhangliang
 * @author caohao
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
                case JobCoreConfigurationConstants.JOB_NAME:
                    jobName = in.nextString();
                    break;
                case JobCoreConfigurationConstants.JOB_CRON:
                    cron = in.nextString();
                    break;
                case JobCoreConfigurationConstants.SHARDING_TOTAL_COUNT:
                    shardingTotalCount = in.nextInt();
                    break;
                case JobCoreConfigurationConstants.SHARDING_ITEM_PARAMETERS:
                    shardingItemParameters = in.nextString();
                    break;
                case JobCoreConfigurationConstants.JOB_PARAMETER:
                    jobParameter = in.nextString();
                    break;
                case JobCoreConfigurationConstants.FAIL_OVER:
                    failover = in.nextBoolean();
                    break;
                case JobCoreConfigurationConstants.MIS_FIRE:
                    misfire = in.nextBoolean();
                    break;
                case JobCoreConfigurationConstants.DESCRIPTION:
                    description = in.nextString();
                    break;
                case JobCoreConfigurationConstants.JOB_PROPERTIES:
                    jobProperties = getJobProperties(in);
                    break;
                case JobCoreConfigurationConstants.JOB_TYPE:
                    jobType = JobType.valueOf(in.nextString());
                    break;
                case JobCoreConfigurationConstants.JOB_CLASS:
                    jobClass = in.nextString();
                    break;
                case JobCoreConfigurationConstants.STREAMING_PROCESS:
                    streamingProcess = in.nextBoolean();
                    break;
                case JobCoreConfigurationConstants.SCRIPT_COMMAND_LINE:
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
        JobTypeConfiguration typeConfig = getJobTypeConfiguration(coreConfig, jobType, jobClass, streamingProcess, scriptCommandLine);
        return getJobRootConfiguration(typeConfig, customizedValueMap);
    }
    
    private JobProperties getJobProperties(final JsonReader in) throws IOException {
        JobProperties result = new JobProperties();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case JobCoreConfigurationConstants.JOB_EXCEPTION_HANDLER:
                    result.put(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), in.nextString());
                    break;
                case JobCoreConfigurationConstants.EXECUTOR_SERVICE_HANDLER:
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
    
    private JobCoreConfiguration getJobCoreConfiguration(final String jobName, final String cron, final int shardingTotalCount,
                                                         final String shardingItemParameters, final String jobParameter, final boolean failover,
                                                         final boolean misfire, final String description,
                                                         final JobProperties jobProperties) {
        return JobCoreConfiguration.newBuilder(jobName, cron, shardingTotalCount)
                .shardingItemParameters(shardingItemParameters).jobParameter(jobParameter).failover(failover).misfire(misfire).description(description)
                .jobProperties(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), jobProperties.get(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER))
                .jobProperties(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), jobProperties.get(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER))
                .build();
    }
    
    private JobTypeConfiguration getJobTypeConfiguration(
            final JobCoreConfiguration coreConfig, final JobType jobType, final String jobClass, final boolean streamingProcess, final String scriptCommandLine) {
        JobTypeConfiguration result;
        Preconditions.checkNotNull(jobType, "jobType cannot be null.");
        switch (jobType) {
            case SIMPLE:
                Preconditions.checkArgument(!Strings.isNullOrEmpty(jobClass), "jobClass cannot be empty.");
                result = new SimpleJobConfiguration(coreConfig, jobClass);
                break;
            case DATAFLOW:
                Preconditions.checkArgument(!Strings.isNullOrEmpty(jobClass), "jobClass cannot be empty.");
                result = new DataflowJobConfiguration(coreConfig, jobClass, streamingProcess);
                break;
            case SCRIPT:
                result = new ScriptJobConfiguration(coreConfig, scriptCommandLine);
                break;
            default:
                throw new UnsupportedOperationException(jobType.name());
        }
        return result;
    }
    
    protected abstract T getJobRootConfiguration(final JobTypeConfiguration typeConfig, final Map<String, Object> customizedValueMap);
    
    @Override
    public void write(final JsonWriter out, final T value) throws IOException {
        out.beginObject();
        out.name(JobCoreConfigurationConstants.JOB_NAME).value(value.getTypeConfig().getCoreConfig().getJobName());
        out.name(JobCoreConfigurationConstants.JOB_CLASS).value(value.getTypeConfig().getJobClass());
        out.name(JobCoreConfigurationConstants.JOB_TYPE).value(value.getTypeConfig().getJobType().name());
        out.name(JobCoreConfigurationConstants.JOB_CRON).value(value.getTypeConfig().getCoreConfig().getCron());
        out.name(JobCoreConfigurationConstants.SHARDING_TOTAL_COUNT).value(value.getTypeConfig().getCoreConfig().getShardingTotalCount());
        out.name(JobCoreConfigurationConstants.SHARDING_ITEM_PARAMETERS).value(value.getTypeConfig().getCoreConfig().getShardingItemParameters());
        out.name(JobCoreConfigurationConstants.JOB_PARAMETER).value(value.getTypeConfig().getCoreConfig().getJobParameter());
        out.name(JobCoreConfigurationConstants.FAIL_OVER).value(value.getTypeConfig().getCoreConfig().isFailover());
        out.name(JobCoreConfigurationConstants.MIS_FIRE).value(value.getTypeConfig().getCoreConfig().isMisfire());
        out.name(JobCoreConfigurationConstants.DESCRIPTION).value(value.getTypeConfig().getCoreConfig().getDescription());
        out.name(JobCoreConfigurationConstants.JOB_PROPERTIES).jsonValue(value.getTypeConfig().getCoreConfig().getJobProperties().json());
        if (value.getTypeConfig().getJobType() == JobType.DATAFLOW) {
            DataflowJobConfiguration dataflowJobConfig = (DataflowJobConfiguration) value.getTypeConfig();
            out.name(JobCoreConfigurationConstants.STREAMING_PROCESS).value(dataflowJobConfig.isStreamingProcess());
        } else if (value.getTypeConfig().getJobType() == JobType.SCRIPT) {
            ScriptJobConfiguration scriptJobConfig = (ScriptJobConfiguration) value.getTypeConfig();
            out.name(JobCoreConfigurationConstants.SCRIPT_COMMAND_LINE).value(scriptJobConfig.getScriptCommandLine());
        }
        writeCustomized(out, value);
        out.endObject();
    }
    
    protected abstract void writeCustomized(final JsonWriter out, final T value) throws IOException;
}
