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
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Streams;
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
        Map<String, Object> customizedValueMap = new HashMap<String, Object>(32, 1);
        in.beginObject();
        while (in.hasNext()) {
            String jsonName = in.nextName();
            if ("jobName".equals(jsonName)) {
                jobName = in.nextString();
            } else if ("cron".equals(jsonName)) {
                cron = in.nextString();
            } else if ("shardingTotalCount".equals(jsonName)) {
                shardingTotalCount = in.nextInt();
            } else if ("shardingItemParameters".equals(jsonName)) {
                shardingItemParameters = in.nextString();
            } else if ("jobParameter".equals(jsonName)) {
                jobParameter = in.nextString();
            } else if ("failover".equals(jsonName)) {
                failover = in.nextBoolean();
            } else if ("misfire".equals(jsonName)) {
                misfire = in.nextBoolean();
            } else if ("description".equals(jsonName)) {
                description = in.nextString();
            } else if ("jobProperties".equals(jsonName)) {
                jobProperties = getJobProperties(in);
            } else if ("jobType".equals(jsonName)) {
                jobType = JobType.valueOf(in.nextString());
            } else if ("jobClass".equals(jsonName)) {
                jobClass = in.nextString();
            } else if ("streamingProcess".equals(jsonName)) {
                streamingProcess = in.nextBoolean();
            } else if ("scriptCommandLine".equals(jsonName)) {
                scriptCommandLine = in.nextString();
            } else {
                addToCustomizedValueMap(jsonName, in, customizedValueMap);
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
            String name = in.nextName();
            if ("job_exception_handler".equals(name)) {
                result.put(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), in.nextString());
            } else if ("executor_service_handler".equals(name)) {
                result.put(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), in.nextString());
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
        Preconditions.checkNotNull(jobType, "jobType cannot be null.");
        switch (jobType) {
            case SIMPLE:
                Preconditions.checkArgument(!Strings.isNullOrEmpty(jobClass), "jobClass cannot be empty.");
                return new SimpleJobConfiguration(coreConfig, jobClass);
            case DATAFLOW:
                Preconditions.checkArgument(!Strings.isNullOrEmpty(jobClass), "jobClass cannot be empty.");
                return new DataflowJobConfiguration(coreConfig, jobClass, streamingProcess);
            case SCRIPT:
                return new ScriptJobConfiguration(coreConfig, scriptCommandLine);
            default:
                throw new UnsupportedOperationException(String.valueOf(jobType));
        }
    }
    
    protected abstract T getJobRootConfiguration(final JobTypeConfiguration typeConfig, final Map<String, Object> customizedValueMap);
    
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
        out.name("jobProperties");
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(value.getTypeConfig().getCoreConfig().getJobProperties().json());
        Streams.write(element, out);
        if (value.getTypeConfig().getJobType() == JobType.DATAFLOW) {
            DataflowJobConfiguration dataflowJobConfig = (DataflowJobConfiguration) value.getTypeConfig();
            out.name("streamingProcess").value(dataflowJobConfig.isStreamingProcess());
        } else if (value.getTypeConfig().getJobType() == JobType.SCRIPT) {
            ScriptJobConfiguration scriptJobConfig = (ScriptJobConfiguration) value.getTypeConfig();
            out.name("scriptCommandLine").value(scriptJobConfig.getScriptCommandLine());
        }
        writeCustomized(out, value);
        out.endObject();
    }
    
    protected abstract void writeCustomized(final JsonWriter out, final T value) throws IOException;
}
