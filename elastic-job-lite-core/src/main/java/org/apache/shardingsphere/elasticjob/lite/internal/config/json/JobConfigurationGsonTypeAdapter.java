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

package org.apache.shardingsphere.elasticjob.lite.internal.config.json;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.shardingsphere.elasticjob.lite.api.JobType;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.JobCoreConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.JobTypeConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.dataflow.DataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.script.ScriptJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.simple.SimpleJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.executor.type.impl.DataflowJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.executor.type.impl.ScriptJobExecutor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Job configuration gson type adapter.
 */
public final class JobConfigurationGsonTypeAdapter extends TypeAdapter<JobConfiguration> {
    
    @Override
    public JobConfiguration read(final JsonReader in) throws IOException {
        Map<String, Object> jsonValueMap = new HashMap<>(32, 1);
        in.beginObject();
        while (in.hasNext()) {
            String jsonName = in.nextName();
            Optional<JobConfigurationJsonEnum> jsonEnum = JobConfigurationJsonEnum.find(jsonName);
            if (jsonEnum.isPresent()) {
                Class<?> jsonType = jsonEnum.get().getJsonType();
                if (boolean.class == jsonType) {
                    jsonValueMap.put(jsonName, in.nextBoolean());
                } else if (int.class == jsonType) {
                    jsonValueMap.put(jsonName, in.nextInt());
                } else if (String.class == jsonType) {
                    jsonValueMap.put(jsonName, in.nextString());
                }
            } else {
                in.skipValue();
            }
        }
        in.endObject();
        JobType jobType = JobType.valueOf((String) jsonValueMap.get(JobConfigurationJsonEnum.JOB_TYPE.getJsonName()));
        JobCoreConfiguration coreConfig = createJobCoreConfiguration((String) jsonValueMap.get(JobConfigurationJsonEnum.JOB_NAME.getJsonName()), jobType, 
                (String) jsonValueMap.get(JobConfigurationJsonEnum.CRON.getJsonName()), (int) jsonValueMap.getOrDefault(JobConfigurationJsonEnum.SHARDING_TOTAL_COUNT.getJsonName(), 0), 
                (String) jsonValueMap.get(JobConfigurationJsonEnum.SHARDING_ITEM_PARAMETERS.getJsonName()), (String) jsonValueMap.get(JobConfigurationJsonEnum.JOB_PARAMETER.getJsonName()), 
                (boolean) jsonValueMap.getOrDefault(JobConfigurationJsonEnum.FAILOVER.getJsonName(), false), 
                (boolean) jsonValueMap.getOrDefault(JobConfigurationJsonEnum.MISFIRE.getJsonName(), false), 
                (String) jsonValueMap.get(JobConfigurationJsonEnum.JOB_EXECUTOR_SERVICE_HANDLER_TYPE.getJsonName()), 
                (String) jsonValueMap.get(JobConfigurationJsonEnum.JOB_ERROR_HANDLER_TYPE.getJsonName()), 
                (String) jsonValueMap.get(JobConfigurationJsonEnum.DESCRIPTION.getJsonName()));
        JobTypeConfiguration typeConfig = createJobTypeConfiguration(coreConfig, jobType, 
                (boolean) jsonValueMap.getOrDefault(JobConfigurationJsonEnum.STREAMING_PROCESS.getJsonName(), false), 
                (String) jsonValueMap.getOrDefault(JobConfigurationJsonEnum.SCRIPT_COMMAND_LINE.getJsonName(), ""));
        return createJobRootConfiguration(typeConfig, jsonValueMap);
    }
    
    private JobCoreConfiguration createJobCoreConfiguration(final String jobName, final JobType jobType, final String cron, final int shardingTotalCount,
                                                            final String shardingItemParameters, final String jobParameter, final boolean failover,
                                                            final boolean misfire, final String jobExecutorServiceHandlerType, final String jobErrorHandlerType, final String description) {
        return JobCoreConfiguration.newBuilder(jobName, jobType, cron, shardingTotalCount)
                .shardingItemParameters(shardingItemParameters).jobParameter(jobParameter).failover(failover).misfire(misfire).description(description)
                .jobExecutorServiceHandlerType(jobExecutorServiceHandlerType)
                .jobErrorHandlerType(jobErrorHandlerType)
                .build();
    }
    
    private JobTypeConfiguration createJobTypeConfiguration(final JobCoreConfiguration coreConfig, final JobType jobType, final boolean streamingProcess, final String scriptCommandLine) {
        Preconditions.checkNotNull(jobType, "jobType cannot be null.");
        switch (jobType) {
            case SIMPLE:
                return new SimpleJobConfiguration(coreConfig);
            case DATAFLOW:
                coreConfig.getProps().setProperty(DataflowJobExecutor.STREAM_PROCESS_KEY, Boolean.valueOf(streamingProcess).toString());
                return new DataflowJobConfiguration(coreConfig);
            case SCRIPT:
                coreConfig.getProps().setProperty(ScriptJobExecutor.SCRIPT_KEY, scriptCommandLine);
                return new ScriptJobConfiguration(coreConfig);
            default:
                throw new UnsupportedOperationException(String.valueOf(jobType));
        }
    }
    
    private JobConfiguration createJobRootConfiguration(final JobTypeConfiguration typeConfig, final Map<String, Object> jsonValueMap) {
        JobConfiguration.Builder builder = JobConfiguration.newBuilder(typeConfig);
        if (jsonValueMap.containsKey(JobConfigurationJsonEnum.MONITOR_EXECUTION.getJsonName())) {
            builder.monitorExecution((boolean) jsonValueMap.get(JobConfigurationJsonEnum.MONITOR_EXECUTION.getJsonName()));
        }
        if (jsonValueMap.containsKey(JobConfigurationJsonEnum.MAX_TIME_DIFF_SECONDS.getJsonName())) {
            builder.maxTimeDiffSeconds((int) jsonValueMap.get(JobConfigurationJsonEnum.MAX_TIME_DIFF_SECONDS.getJsonName()));
        }
        if (jsonValueMap.containsKey(JobConfigurationJsonEnum.MONITOR_PORT.getJsonName())) {
            builder.monitorPort((int) jsonValueMap.get(JobConfigurationJsonEnum.MONITOR_PORT.getJsonName()));
        }
        if (jsonValueMap.containsKey(JobConfigurationJsonEnum.JOB_SHARDING_STRATEGY_TYPE.getJsonName())) {
            builder.jobShardingStrategyType((String) jsonValueMap.get(JobConfigurationJsonEnum.JOB_SHARDING_STRATEGY_TYPE.getJsonName()));
        }
        if (jsonValueMap.containsKey(JobConfigurationJsonEnum.RECONCILE_INTERVAL_MINUTES.getJsonName())) {
            builder.reconcileIntervalMinutes((int) jsonValueMap.get(JobConfigurationJsonEnum.RECONCILE_INTERVAL_MINUTES.getJsonName()));
        }
        if (jsonValueMap.containsKey(JobConfigurationJsonEnum.DISABLED.getJsonName())) {
            builder.disabled((boolean) jsonValueMap.get(JobConfigurationJsonEnum.DISABLED.getJsonName()));
        }
        if (jsonValueMap.containsKey(JobConfigurationJsonEnum.OVERWRITE.getJsonName())) {
            builder.overwrite((boolean) jsonValueMap.get(JobConfigurationJsonEnum.OVERWRITE.getJsonName()));
        }
        return builder.build();
    }
    
    @Override
    public void write(final JsonWriter out, final JobConfiguration value) throws IOException {
        out.beginObject();
        out.name(JobConfigurationJsonEnum.JOB_NAME.getJsonName()).value(value.getTypeConfig().getCoreConfig().getJobName());
        out.name(JobConfigurationJsonEnum.JOB_TYPE.getJsonName()).value(value.getTypeConfig().getCoreConfig().getJobType().name());
        out.name(JobConfigurationJsonEnum.CRON.getJsonName()).value(value.getTypeConfig().getCoreConfig().getCron());
        out.name(JobConfigurationJsonEnum.SHARDING_TOTAL_COUNT.getJsonName()).value(value.getTypeConfig().getCoreConfig().getShardingTotalCount());
        out.name(JobConfigurationJsonEnum.SHARDING_ITEM_PARAMETERS.getJsonName()).value(value.getTypeConfig().getCoreConfig().getShardingItemParameters());
        out.name(JobConfigurationJsonEnum.JOB_PARAMETER.getJsonName()).value(value.getTypeConfig().getCoreConfig().getJobParameter());
        out.name(JobConfigurationJsonEnum.FAILOVER.getJsonName()).value(value.getTypeConfig().getCoreConfig().isFailover());
        out.name(JobConfigurationJsonEnum.MISFIRE.getJsonName()).value(value.getTypeConfig().getCoreConfig().isMisfire());
        if (!Strings.isNullOrEmpty(value.getTypeConfig().getCoreConfig().getJobExecutorServiceHandlerType())) {
            out.name(JobConfigurationJsonEnum.JOB_EXECUTOR_SERVICE_HANDLER_TYPE.getJsonName()).value(value.getTypeConfig().getCoreConfig().getJobExecutorServiceHandlerType());
        }
        if (!Strings.isNullOrEmpty(value.getTypeConfig().getCoreConfig().getJobErrorHandlerType())) {
            out.name(JobConfigurationJsonEnum.JOB_ERROR_HANDLER_TYPE.getJsonName()).value(value.getTypeConfig().getCoreConfig().getJobErrorHandlerType());
        }
        out.name(JobConfigurationJsonEnum.DESCRIPTION.getJsonName()).value(value.getTypeConfig().getCoreConfig().getDescription());
        if (JobType.DATAFLOW == value.getTypeConfig().getCoreConfig().getJobType()) {
            out.name(JobConfigurationJsonEnum.STREAMING_PROCESS.getJsonName()).value(
                    Boolean.valueOf(value.getTypeConfig().getCoreConfig().getProps().getOrDefault(DataflowJobExecutor.STREAM_PROCESS_KEY, false).toString()));
        } else if (JobType.SCRIPT == value.getTypeConfig().getCoreConfig().getJobType()) {
            ScriptJobConfiguration scriptJobConfig = (ScriptJobConfiguration) value.getTypeConfig();
            out.name(JobConfigurationJsonEnum.SCRIPT_COMMAND_LINE.getJsonName()).value(scriptJobConfig.getCoreConfig().getProps().getProperty(ScriptJobExecutor.SCRIPT_KEY));
        }
        out.name(JobConfigurationJsonEnum.MONITOR_EXECUTION.getJsonName()).value(value.isMonitorExecution());
        out.name(JobConfigurationJsonEnum.MAX_TIME_DIFF_SECONDS.getJsonName()).value(value.getMaxTimeDiffSeconds());
        out.name(JobConfigurationJsonEnum.MONITOR_PORT.getJsonName()).value(value.getMonitorPort());
        out.name(JobConfigurationJsonEnum.JOB_SHARDING_STRATEGY_TYPE.getJsonName()).value(value.getJobShardingStrategyType());
        out.name(JobConfigurationJsonEnum.RECONCILE_INTERVAL_MINUTES.getJsonName()).value(value.getReconcileIntervalMinutes());
        out.name(JobConfigurationJsonEnum.DISABLED.getJsonName()).value(value.isDisabled());
        out.name(JobConfigurationJsonEnum.OVERWRITE.getJsonName()).value(value.isOverwrite());
        out.endObject();
    }
}
