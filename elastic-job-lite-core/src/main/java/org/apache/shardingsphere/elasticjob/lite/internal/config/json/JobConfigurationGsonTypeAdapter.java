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

import com.google.common.base.Strings;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.shardingsphere.elasticjob.lite.api.JobType;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration.Builder;
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
        return createJobConfiguration(jsonValueMap);
    }
    
    private JobConfiguration createJobConfiguration(final Map<String, Object> jsonValueMap) {
        JobType jobType = JobType.valueOf((String) jsonValueMap.get(JobConfigurationJsonEnum.JOB_TYPE.getJsonName()));
        Builder builder = JobConfiguration.newBuilder((String) jsonValueMap.get(JobConfigurationJsonEnum.JOB_NAME.getJsonName()), jobType,
                (String) jsonValueMap.get(JobConfigurationJsonEnum.CRON.getJsonName()), (int) jsonValueMap.getOrDefault(JobConfigurationJsonEnum.SHARDING_TOTAL_COUNT.getJsonName(), 0));
        if (jsonValueMap.containsKey(JobConfigurationJsonEnum.SHARDING_ITEM_PARAMETERS.getJsonName())) {
            builder.shardingItemParameters(jsonValueMap.get(JobConfigurationJsonEnum.SHARDING_ITEM_PARAMETERS.getJsonName()).toString());
        }
        if (jsonValueMap.containsKey(JobConfigurationJsonEnum.JOB_PARAMETER.getJsonName())) {
            builder.jobParameter(jsonValueMap.get(JobConfigurationJsonEnum.JOB_PARAMETER.getJsonName()).toString());
        }
        if (jsonValueMap.containsKey(JobConfigurationJsonEnum.MONITOR_EXECUTION.getJsonName())) {
            builder.monitorExecution((boolean) jsonValueMap.get(JobConfigurationJsonEnum.MONITOR_EXECUTION.getJsonName()));
        }
        if (jsonValueMap.containsKey(JobConfigurationJsonEnum.FAILOVER.getJsonName())) {
            builder.failover((boolean) jsonValueMap.get(JobConfigurationJsonEnum.FAILOVER.getJsonName()));
        }
        if (jsonValueMap.containsKey(JobConfigurationJsonEnum.MISFIRE.getJsonName())) {
            builder.misfire((boolean) jsonValueMap.get(JobConfigurationJsonEnum.MISFIRE.getJsonName()));
        }
        if (jsonValueMap.containsKey(JobConfigurationJsonEnum.MAX_TIME_DIFF_SECONDS.getJsonName())) {
            builder.maxTimeDiffSeconds((int) jsonValueMap.get(JobConfigurationJsonEnum.MAX_TIME_DIFF_SECONDS.getJsonName()));
        }
        if (jsonValueMap.containsKey(JobConfigurationJsonEnum.RECONCILE_INTERVAL_MINUTES.getJsonName())) {
            builder.reconcileIntervalMinutes((int) jsonValueMap.get(JobConfigurationJsonEnum.RECONCILE_INTERVAL_MINUTES.getJsonName()));
        }
        if (jsonValueMap.containsKey(JobConfigurationJsonEnum.MONITOR_PORT.getJsonName())) {
            builder.monitorPort((int) jsonValueMap.get(JobConfigurationJsonEnum.MONITOR_PORT.getJsonName()));
        }
        if (jsonValueMap.containsKey(JobConfigurationJsonEnum.JOB_SHARDING_STRATEGY_TYPE.getJsonName())) {
            builder.jobShardingStrategyType((String) jsonValueMap.get(JobConfigurationJsonEnum.JOB_SHARDING_STRATEGY_TYPE.getJsonName()));
        }
        if (jsonValueMap.containsKey(JobConfigurationJsonEnum.JOB_EXECUTOR_SERVICE_HANDLER_TYPE.getJsonName())) {
            builder.jobExecutorServiceHandlerType((String) jsonValueMap.get(JobConfigurationJsonEnum.JOB_EXECUTOR_SERVICE_HANDLER_TYPE.getJsonName()));
        }
        if (jsonValueMap.containsKey(JobConfigurationJsonEnum.JOB_ERROR_HANDLER_TYPE.getJsonName())) {
            builder.jobErrorHandlerType((String) jsonValueMap.get(JobConfigurationJsonEnum.JOB_ERROR_HANDLER_TYPE.getJsonName()));
        }
        if (jsonValueMap.containsKey(JobConfigurationJsonEnum.DESCRIPTION.getJsonName())) {
            builder.description((String) jsonValueMap.get(JobConfigurationJsonEnum.DESCRIPTION.getJsonName()));
        }
        if (JobType.DATAFLOW == jobType) {
            builder.setProperty(DataflowJobExecutor.STREAM_PROCESS_KEY, jsonValueMap.getOrDefault(JobConfigurationJsonEnum.STREAMING_PROCESS.getJsonName(), false).toString());
        } else if (JobType.SCRIPT == jobType) {
            builder.setProperty(ScriptJobExecutor.SCRIPT_KEY, jsonValueMap.getOrDefault(JobConfigurationJsonEnum.SCRIPT_COMMAND_LINE.getJsonName(), "").toString());
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
        out.name(JobConfigurationJsonEnum.JOB_NAME.getJsonName()).value(value.getJobName());
        out.name(JobConfigurationJsonEnum.JOB_TYPE.getJsonName()).value(value.getJobType().name());
        out.name(JobConfigurationJsonEnum.CRON.getJsonName()).value(value.getCron());
        out.name(JobConfigurationJsonEnum.SHARDING_TOTAL_COUNT.getJsonName()).value(value.getShardingTotalCount());
        out.name(JobConfigurationJsonEnum.SHARDING_ITEM_PARAMETERS.getJsonName()).value(value.getShardingItemParameters());
        out.name(JobConfigurationJsonEnum.JOB_PARAMETER.getJsonName()).value(value.getJobParameter());
        out.name(JobConfigurationJsonEnum.FAILOVER.getJsonName()).value(value.isFailover());
        out.name(JobConfigurationJsonEnum.MISFIRE.getJsonName()).value(value.isMisfire());
        if (!Strings.isNullOrEmpty(value.getJobExecutorServiceHandlerType())) {
            out.name(JobConfigurationJsonEnum.JOB_EXECUTOR_SERVICE_HANDLER_TYPE.getJsonName()).value(value.getJobExecutorServiceHandlerType());
        }
        if (!Strings.isNullOrEmpty(value.getJobErrorHandlerType())) {
            out.name(JobConfigurationJsonEnum.JOB_ERROR_HANDLER_TYPE.getJsonName()).value(value.getJobErrorHandlerType());
        }
        out.name(JobConfigurationJsonEnum.DESCRIPTION.getJsonName()).value(value.getDescription());
        if (JobType.DATAFLOW == value.getJobType()) {
            out.name(JobConfigurationJsonEnum.STREAMING_PROCESS.getJsonName()).value(
                    Boolean.valueOf(value.getProps().getOrDefault(DataflowJobExecutor.STREAM_PROCESS_KEY, false).toString()));
        } else if (JobType.SCRIPT == value.getJobType()) {
            out.name(JobConfigurationJsonEnum.SCRIPT_COMMAND_LINE.getJsonName()).value(value.getProps().getProperty(ScriptJobExecutor.SCRIPT_KEY));
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
