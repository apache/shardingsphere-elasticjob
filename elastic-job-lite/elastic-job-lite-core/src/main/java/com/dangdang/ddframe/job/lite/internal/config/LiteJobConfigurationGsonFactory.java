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
import com.dangdang.ddframe.job.api.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.api.config.JobTypeConfiguration;
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
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Lite作业配置的Gson工厂.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class LiteJobConfigurationGsonFactory {
    
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(LiteJobConfiguration.class, new LiteJobConfigurationGsonTypeAdapter()).create();
    
    /**
     * 将作业配置转换为JSON字符串.
     * 
     * @param liteJobConfig 作业配置对象
     * @return 作业配置JSON字符串
     */
    public static String toJson(final LiteJobConfiguration liteJobConfig) {
        return GSON.toJson(liteJobConfig);
    }
    
    /**
     * 将作业配置转换为JSON字符串.
     *
     * @param liteJobConfig 作业配置对象
     * @return 作业配置JSON字符串
     */
    // TODO API模块jobSettings使用,未来需调整并删除
    public static String toJsonForObject(final Object liteJobConfig) {
        return GSON.toJson(liteJobConfig);
    }
    
    /**
     * 将JSON字符串转换为作业配置.
     *
     * @param liteJobConfigJson 作业配置JSON字符串
     * @return 作业配置对象
     */
    public static LiteJobConfiguration fromJson(final String liteJobConfigJson) {
        return GSON.fromJson(liteJobConfigJson, LiteJobConfiguration.class);
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
            Class<? extends ElasticJob> jobClass = null;
            boolean monitorExecution = false;
            int maxTimeDiffSeconds = 0;
            int monitorPort = 0;
            String jobShardingStrategyClass = "";
            boolean disabled = false;
            boolean overwrite = false;
            DataflowJobConfiguration.DataflowType dataflowType = null;
            boolean streamingProcess = false;
            int concurrentDataProcessThreadCount = 0;
            String scriptCommandLine = "";
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
                        String jobClassName = in.nextString();
                        try {
                            jobClass = (Class<? extends ElasticJob>) Class.forName(jobClassName);
                        } catch (final ClassNotFoundException ex) {
                            log.warn("Elastic-Job: Job class '{}' is not in classpath, return null job configuration.", jobClassName);
                            return null;
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
            JobTypeConfiguration jobConfig = getJobConfiguration(coreConfig, jobType, jobClass, dataflowType, streamingProcess, concurrentDataProcessThreadCount, scriptCommandLine);
            return getLiteJobConfiguration(jobConfig, monitorExecution, maxTimeDiffSeconds, monitorPort, jobShardingStrategyClass, disabled, overwrite);
        }
        
        private JobProperties getJobProperties(final JsonReader in) throws IOException {
            JobProperties result = new JobProperties();
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "job_exception_handler":
                        result.put(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), getJobPropertiesValue(in, JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER));
                        break;
                    case "executor_service_handler":
                        result.put(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), getJobPropertiesValue(in, JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER));
                        break;
                    default:
                        break;
                }
            }
            in.endObject();
            return result;
        }
        
        private Class<?> getJobPropertiesValue(final JsonReader in, final JobProperties.JobPropertiesEnum jobPropertiesEnum) throws IOException {
            String jobPropertiesClassName = in.nextString();
            try {
                return Class.forName(jobPropertiesClassName);
            } catch (final ClassNotFoundException ex) {
                log.warn("Cannot load class '{}', use default {} class.", jobPropertiesClassName, jobPropertiesEnum.getKey());
                return jobPropertiesEnum.getDefaultValue();
            }
        }
        
        private LiteJobConfiguration getLiteJobConfiguration(final JobTypeConfiguration jobConfig, final boolean monitorExecution, final int maxTimeDiffSeconds,
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
        private JobTypeConfiguration getJobConfiguration(final JobCoreConfiguration coreConfig, final JobType jobType, final Object jobClass, final DataflowJobConfiguration.DataflowType dataflowType,
                                                         final boolean streamingProcess, final int concurrentDataProcessThreadCount, final String scriptCommandLine) {
            JobTypeConfiguration result;
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
            out.name("jobClass").value(value.getTypeConfig().getJobClass().getCanonicalName());
            out.name("jobType").value(value.getTypeConfig().getJobType().name());
            out.name("cron").value(value.getTypeConfig().getCoreConfig().getCron());
            out.name("shardingTotalCount").value(value.getTypeConfig().getCoreConfig().getShardingTotalCount());
            out.name("shardingItemParameters").value(value.getTypeConfig().getCoreConfig().getShardingItemParameters());
            out.name("jobParameter").value(value.getTypeConfig().getCoreConfig().getJobParameter());
            out.name("failover").value(value.getTypeConfig().getCoreConfig().isFailover());
            out.name("misfire").value(value.getTypeConfig().getCoreConfig().isMisfire());
            out.name("description").value(value.getTypeConfig().getCoreConfig().getDescription());
            out.name("jobProperties").jsonValue(value.getTypeConfig().getCoreConfig().getJobProperties().json());
            out.name("monitorExecution").value(value.isMonitorExecution());
            out.name("maxTimeDiffSeconds").value(value.getMaxTimeDiffSeconds());
            out.name("monitorPort").value(value.getMonitorPort());
            out.name("jobShardingStrategyClass").value(value.getJobShardingStrategyClass());
            out.name("disabled").value(value.isDisabled());
            out.name("overwrite").value(value.isOverwrite());
            if (DataflowJob.class.isAssignableFrom(value.getTypeConfig().getJobClass())) {
                DataflowJobConfiguration dataflowJobConfig = (DataflowJobConfiguration) value.getTypeConfig();
                out.name("dataflowType").value(dataflowJobConfig.getDataflowType().name());
                out.name("streamingProcess").value(dataflowJobConfig.isStreamingProcess());
                out.name("concurrentDataProcessThreadCount").value(dataflowJobConfig.getConcurrentDataProcessThreadCount());
            } else if (ScriptJob.class == value.getTypeConfig().getJobClass()) {
                ScriptJobConfiguration scriptJobConfig = (ScriptJobConfiguration) value.getTypeConfig();
                out.name("scriptCommandLine").value(scriptJobConfig.getScriptCommandLine());
            }
            out.endObject();
        }
    }
}
