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
import org.apache.shardingsphere.elasticjob.lite.config.JobCoreConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.JobTypeConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.LiteJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.dataflow.DataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.script.ScriptJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.simple.SimpleJobConfiguration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Job configuration gson type adapter.
 */
public final class LiteJobConfigurationGsonTypeAdapter extends TypeAdapter<LiteJobConfiguration> {
    
    @Override
    public LiteJobConfiguration read(final JsonReader in) throws IOException {
        String jobName = "";
        String cron = "";
        int shardingTotalCount = 0;
        String shardingItemParameters = "";
        String jobParameter = "";
        boolean failover = false;
        boolean misfire = failover;
        String jobExecutorServiceHandlerType = "";
        String jobErrorHandlerType = "";
        String description = "";
        JobType jobType = null;
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
                case "jobExecutorServiceHandlerType":
                    jobExecutorServiceHandlerType = in.nextString();
                    break;
                case "jobErrorHandlerType":
                    jobErrorHandlerType = in.nextString();
                    break;
                case "description":
                    description = in.nextString();
                    break;
                case "jobType":
                    jobType = JobType.valueOf(in.nextString());
                    break;
                case "streamingProcess":
                    streamingProcess = in.nextBoolean();
                    break;
                case "scriptCommandLine":
                    scriptCommandLine = in.nextString();
                    break;
                case LiteJobConfigurationConstants.MONITOR_EXECUTION:
                    customizedValueMap.put(jsonName, in.nextBoolean());
                    break;
                case LiteJobConfigurationConstants.MAX_TIME_DIFF_SECONDS:
                    customizedValueMap.put(jsonName, in.nextInt());
                    break;
                case LiteJobConfigurationConstants.MONITOR_PORT:
                    customizedValueMap.put(jsonName, in.nextInt());
                    break;
                case LiteJobConfigurationConstants.JOB_SHARDING_STRATEGY_TYPE:
                    customizedValueMap.put(jsonName, in.nextString());
                    break;
                case LiteJobConfigurationConstants.RECONCILE_INTERVAL_MINUTES:
                    customizedValueMap.put(jsonName, in.nextInt());
                    break;
                case LiteJobConfigurationConstants.DISABLED:
                    customizedValueMap.put(jsonName, in.nextBoolean());
                    break;
                case LiteJobConfigurationConstants.OVERWRITE:
                    customizedValueMap.put(jsonName, in.nextBoolean());
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();
        JobCoreConfiguration coreConfig = getJobCoreConfiguration(
                jobName, cron, shardingTotalCount, shardingItemParameters, jobParameter, failover, misfire, jobExecutorServiceHandlerType, jobErrorHandlerType, description);
        JobTypeConfiguration typeConfig = getJobTypeConfiguration(coreConfig, jobType, streamingProcess, scriptCommandLine);
        return getJobRootConfiguration(typeConfig, customizedValueMap);
    }
    
    private JobCoreConfiguration getJobCoreConfiguration(final String jobName, final String cron, final int shardingTotalCount,
                                                         final String shardingItemParameters, final String jobParameter, final boolean failover,
                                                         final boolean misfire, final String jobExecutorServiceHandlerType, final String jobErrorHandlerType, final String description) {
        return JobCoreConfiguration.newBuilder(jobName, cron, shardingTotalCount)
                .shardingItemParameters(shardingItemParameters).jobParameter(jobParameter).failover(failover).misfire(misfire).description(description)
                .jobExecutorServiceHandlerType(jobExecutorServiceHandlerType)
                .jobErrorHandlerType(jobErrorHandlerType)
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
    
    private LiteJobConfiguration getJobRootConfiguration(final JobTypeConfiguration typeConfig, final Map<String, Object> customizedValueMap) {
        LiteJobConfiguration.Builder builder = LiteJobConfiguration.newBuilder(typeConfig);
        if (customizedValueMap.containsKey(LiteJobConfigurationConstants.MONITOR_EXECUTION)) {
            builder.monitorExecution((boolean) customizedValueMap.get(LiteJobConfigurationConstants.MONITOR_EXECUTION));
        }
        if (customizedValueMap.containsKey(LiteJobConfigurationConstants.MAX_TIME_DIFF_SECONDS)) {
            builder.maxTimeDiffSeconds((int) customizedValueMap.get(LiteJobConfigurationConstants.MAX_TIME_DIFF_SECONDS));
        }
        if (customizedValueMap.containsKey(LiteJobConfigurationConstants.MONITOR_PORT)) {
            builder.monitorPort((int) customizedValueMap.get(LiteJobConfigurationConstants.MONITOR_PORT));
        }
        if (customizedValueMap.containsKey(LiteJobConfigurationConstants.JOB_SHARDING_STRATEGY_TYPE)) {
            builder.jobShardingStrategyType((String) customizedValueMap.get(LiteJobConfigurationConstants.JOB_SHARDING_STRATEGY_TYPE));
        }
        if (customizedValueMap.containsKey(LiteJobConfigurationConstants.RECONCILE_INTERVAL_MINUTES)) {
            builder.reconcileIntervalMinutes((int) customizedValueMap.get(LiteJobConfigurationConstants.RECONCILE_INTERVAL_MINUTES));
        }
        if (customizedValueMap.containsKey(LiteJobConfigurationConstants.DISABLED)) {
            builder.disabled((boolean) customizedValueMap.get(LiteJobConfigurationConstants.DISABLED));
        }
        if (customizedValueMap.containsKey(LiteJobConfigurationConstants.OVERWRITE)) {
            builder.overwrite((boolean) customizedValueMap.get(LiteJobConfigurationConstants.OVERWRITE));
        }
        return builder.build();
    }
    
    @Override
    public void write(final JsonWriter out, final LiteJobConfiguration value) throws IOException {
        out.beginObject();
        out.name("jobName").value(value.getTypeConfig().getCoreConfig().getJobName());
        out.name("jobType").value(value.getTypeConfig().getJobType().name());
        out.name("cron").value(value.getTypeConfig().getCoreConfig().getCron());
        out.name("shardingTotalCount").value(value.getTypeConfig().getCoreConfig().getShardingTotalCount());
        out.name("shardingItemParameters").value(value.getTypeConfig().getCoreConfig().getShardingItemParameters());
        out.name("jobParameter").value(value.getTypeConfig().getCoreConfig().getJobParameter());
        out.name("failover").value(value.getTypeConfig().getCoreConfig().isFailover());
        out.name("misfire").value(value.getTypeConfig().getCoreConfig().isMisfire());
        if (!Strings.isNullOrEmpty(value.getTypeConfig().getCoreConfig().getJobExecutorServiceHandlerType())) {
            out.name("jobExecutorServiceHandlerType").value(value.getTypeConfig().getCoreConfig().getJobExecutorServiceHandlerType());
        }
        if (!Strings.isNullOrEmpty(value.getTypeConfig().getCoreConfig().getJobErrorHandlerType())) {
            out.name("jobErrorHandlerType").value(value.getTypeConfig().getCoreConfig().getJobErrorHandlerType());
        }
        out.name("description").value(value.getTypeConfig().getCoreConfig().getDescription());
        if (value.getTypeConfig().getJobType() == JobType.DATAFLOW) {
            out.name("streamingProcess").value(((DataflowJobConfiguration) value.getTypeConfig()).isStreamingProcess());
        } else if (value.getTypeConfig().getJobType() == JobType.SCRIPT) {
            ScriptJobConfiguration scriptJobConfig = (ScriptJobConfiguration) value.getTypeConfig();
            out.name("scriptCommandLine").value(scriptJobConfig.getScriptCommandLine());
        }
        out.name(LiteJobConfigurationConstants.MONITOR_EXECUTION).value(value.isMonitorExecution());
        out.name(LiteJobConfigurationConstants.MAX_TIME_DIFF_SECONDS).value(value.getMaxTimeDiffSeconds());
        out.name(LiteJobConfigurationConstants.MONITOR_PORT).value(value.getMonitorPort());
        out.name(LiteJobConfigurationConstants.JOB_SHARDING_STRATEGY_TYPE).value(value.getJobShardingStrategyType());
        out.name(LiteJobConfigurationConstants.RECONCILE_INTERVAL_MINUTES).value(value.getReconcileIntervalMinutes());
        out.name(LiteJobConfigurationConstants.DISABLED).value(value.isDisabled());
        out.name(LiteJobConfigurationConstants.OVERWRITE).value(value.isOverwrite());
        out.endObject();
    }
    
}
