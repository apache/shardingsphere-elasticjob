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

package com.dangdang.ddframe.job.lite.internal.config;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.api.JobCoreConfiguration;
import com.dangdang.ddframe.job.api.internal.config.JobProperties;
import com.dangdang.ddframe.job.api.type.JobType;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJob;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJobConfiguration;
import com.dangdang.ddframe.job.api.type.script.api.ScriptJob;
import com.dangdang.ddframe.job.api.type.script.api.ScriptJobConfiguration;
import com.dangdang.ddframe.job.api.type.simple.api.SimpleJob;
import com.dangdang.ddframe.job.api.type.simple.api.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.api.config.LiteJobConfiguration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * Lite作业配置的Gson.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LiteJobConfigurationGsonFactory {
    
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(LiteJobConfiguration.class, new LiteJobConfigurationGsonTypeAdapter()).create();
    
    /**
     * 获取Gson实例.
     *
     * @return Gson实例
     */
    public static Gson getGson() {
        return GSON;
    }
    
    /**
     * Lite作业配置的Json转换适配器.
     *
     * @author zhangliang
     */
    static final class LiteJobConfigurationGsonTypeAdapter extends TypeAdapter<LiteJobConfiguration> {
    
        @SuppressWarnings("unchecked")
        @Override
        public LiteJobConfiguration read(final JsonReader in) throws IOException {
            String jobName = null;
            String cron = null;
            int shardingTotalCount = 0;
            String shardingItemParameters = null;
            String jobParameter = null;
            boolean failover = false;
            boolean misfire = failover;
            String description = null;
            JobProperties jobProperties = null;
            JobType jobType = null;
            Class<? extends ElasticJob> jobClass = null;
            boolean monitorExecution = false;
            int maxTimeDiffSeconds = 0;
            int monitorPort = 0;
            String jobShardingStrategyClass = null;
            boolean disabled = false;
            boolean overwrite = false;
            DataflowJobConfiguration.DataflowType dataflowType = null;
            boolean streamingProcess = false;
            int concurrentDataProcessThreadCount = 0;
            String scriptCommandLine = null;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
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
                        try {
                            jobClass = (Class<? extends ElasticJob>) Class.forName(in.nextString());
                        } catch (final ClassNotFoundException ex) {
                            throw new RuntimeException(ex);
                        }
                        break;
                    case "monitorExecution":
                        monitorExecution = in.nextBoolean();
                        break;
                    case "maxTimeDiffSeconds":
                        maxTimeDiffSeconds = in.nextInt();
                        break;
                    case "monitorPort":
                        monitorPort = in.nextInt();
                        break;
                    case "jobShardingStrategyClass":
                        jobShardingStrategyClass = in.nextString();
                        break;
                    case "disabled":
                        disabled = in.nextBoolean();
                        break;
                    case "overwrite":
                        overwrite = in.nextBoolean();
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
                        break;
                }
            }
            in.endObject();
            JobCoreConfiguration coreConfig = getJobCoreConfiguration(jobName, cron, shardingTotalCount, shardingItemParameters,
                    jobParameter, failover, misfire, description, jobProperties);
            JobConfiguration jobConfig = getJobConfiguration(coreConfig, jobType, jobClass, dataflowType, streamingProcess, concurrentDataProcessThreadCount, scriptCommandLine);
            return getLiteJobConfiguration(jobConfig, monitorExecution, maxTimeDiffSeconds, monitorPort, jobShardingStrategyClass, disabled, overwrite);
        }
        
        private JobProperties getJobProperties(final JsonReader in) throws IOException {
            JobProperties result = new JobProperties();
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "job_exception_handler":
                        try {
                            result.put(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), Class.forName(in.nextString()));
                        } catch (final ClassNotFoundException ex) {
                            throw new RuntimeException(ex);
                        }
                        break;
                    case "executor_service_handler":
                        try {
                            result.put(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), Class.forName(in.nextString()));
                        } catch (final ClassNotFoundException ex) {
                            throw new RuntimeException(ex);
                        }
                        break;
                    default:
                        break;
                }
            }
            in.endObject();
            return result;
        }
        
        private LiteJobConfiguration getLiteJobConfiguration(final JobConfiguration jobConfig, final boolean monitorExecution, final int maxTimeDiffSeconds, 
                                                             final int monitorPort, final String jobShardingStrategyClass, final boolean disabled, final boolean overwrite) {
            return LiteJobConfiguration.newBuilder(jobConfig).monitorExecution(monitorExecution).maxTimeDiffSeconds(maxTimeDiffSeconds)
                    .monitorPort(monitorPort).jobShardingStrategyClass(jobShardingStrategyClass).disabled(disabled).overwrite(overwrite).build();
        }
        
        private JobCoreConfiguration getJobCoreConfiguration(final String jobName, final String cron, final int shardingTotalCount, final String shardingItemParameters, 
                                                             final String jobParameter, final boolean failover, final boolean misfire, final String description, final JobProperties jobProperties) {
            return JobCoreConfiguration.newBuilder(jobName, cron, shardingTotalCount)
                            .shardingItemParameters(shardingItemParameters).jobParameter(jobParameter).failover(failover).misfire(misfire).description(description)
                            .jobProperties(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), jobProperties.get(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER))
                            .jobProperties(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), jobProperties.get(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER))
                            .build();
        }
        
        @SuppressWarnings("unchecked")
        private JobConfiguration getJobConfiguration(final JobCoreConfiguration coreConfig, final JobType jobType, final Object jobClass, final DataflowJobConfiguration.DataflowType dataflowType, 
                                                     final boolean streamingProcess, final int concurrentDataProcessThreadCount, final String scriptCommandLine) {
            JobConfiguration result;
            switch (jobType) {
                case SIMPLE:
                    result = new SimpleJobConfiguration(coreConfig, (Class<? extends SimpleJob>) jobClass);
                    break;
                case DATAFLOW:
                    result = new DataflowJobConfiguration(coreConfig, (Class<? extends DataflowJob>) jobClass, dataflowType, streamingProcess, concurrentDataProcessThreadCount);
                    break;
                case SCRIPT:
                    result = new ScriptJobConfiguration(coreConfig, scriptCommandLine);
                    break;
                default:
                    throw new UnsupportedOperationException(jobType.name());
            }
            return result;
        }
        
        @Override
        public void write(final JsonWriter out, final LiteJobConfiguration value) throws IOException {
            out.beginObject();
            out.name("jobName").value(value.getJobName());
            out.name("jobClass").value(value.getJobConfig().getJobClass().getCanonicalName());
            out.name("jobType").value(value.getJobConfig().getJobType().name());
            out.name("cron").value(value.getJobConfig().getCoreConfig().getCron());
            out.name("shardingTotalCount").value(value.getJobConfig().getCoreConfig().getShardingTotalCount());
            out.name("shardingItemParameters").value(value.getJobConfig().getCoreConfig().getShardingItemParameters());
            out.name("jobParameter").value(value.getJobConfig().getCoreConfig().getJobParameter());
            out.name("failover").value(value.getJobConfig().getCoreConfig().isFailover());
            out.name("misfire").value(value.getJobConfig().getCoreConfig().isMisfire());
            out.name("description").value(value.getJobConfig().getCoreConfig().getDescription());
            out.name("jobProperties").jsonValue(value.getJobConfig().getCoreConfig().getJobProperties().json());
            out.name("monitorExecution").value(value.isMonitorExecution());
            out.name("maxTimeDiffSeconds").value(value.getMaxTimeDiffSeconds());
            out.name("monitorPort").value(value.getMonitorPort());
            out.name("jobShardingStrategyClass").value(value.getJobShardingStrategyClass());
            out.name("disabled").value(value.isDisabled());
            out.name("overwrite").value(value.isOverwrite());
            if (DataflowJob.class.isAssignableFrom(value.getJobConfig().getJobClass())) {
                DataflowJobConfiguration dataflowJobConfig = (DataflowJobConfiguration) value.getJobConfig();
                out.name("dataflowType").value(dataflowJobConfig.getDataflowType().name());
                out.name("streamingProcess").value(dataflowJobConfig.isStreamingProcess());
                out.name("concurrentDataProcessThreadCount").value(dataflowJobConfig.getConcurrentDataProcessThreadCount());
            } else if (ScriptJob.class == value.getJobConfig().getJobClass()) {
                ScriptJobConfiguration scriptJobConfig = (ScriptJobConfiguration) value.getJobConfig();
                out.name("scriptCommandLine").value(scriptJobConfig.getScriptCommandLine());
            }
            out.endObject();
        }
    }
}
