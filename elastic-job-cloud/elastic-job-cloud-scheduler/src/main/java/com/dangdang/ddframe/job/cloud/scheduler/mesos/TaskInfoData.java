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

package com.dangdang.ddframe.job.cloud.scheduler.mesos;

import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobExecutionType;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.executor.handler.JobProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.SerializationUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 随任务传递的数据.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class TaskInfoData {
    
    private final ShardingContexts shardingContexts;
    
    private final CloudJobConfiguration jobConfig;
    
    /**
     * 序列化.
     * 
     * @return 序列化后的字节数组
     */
    public byte[] serialize() {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>(2, 1);
        result.put("shardingContext", shardingContexts);
        result.put("jobConfigContext", buildJobConfigurationContext());
        return SerializationUtils.serialize(result);
    }
    
    private Map<String, String> buildJobConfigurationContext() {
        Map<String, String> result = new LinkedHashMap<>(16, 1);
        result.put("jobType", jobConfig.getTypeConfig().getJobType().name());
        result.put("jobName", jobConfig.getJobName());
        result.put("jobClass", jobConfig.getTypeConfig().getJobClass());
        result.put("cron", CloudJobExecutionType.DAEMON == jobConfig.getJobExecutionType() ? jobConfig.getTypeConfig().getCoreConfig().getCron() : "");
        result.put("jobExceptionHandler", jobConfig.getTypeConfig().getCoreConfig().getJobProperties().get(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER));
        result.put("executorServiceHandler", jobConfig.getTypeConfig().getCoreConfig().getJobProperties().get(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER));
        if (jobConfig.getTypeConfig() instanceof DataflowJobConfiguration) {
            result.put("streamingProcess", Boolean.toString(((DataflowJobConfiguration) jobConfig.getTypeConfig()).isStreamingProcess()));
        } else if (jobConfig.getTypeConfig() instanceof ScriptJobConfiguration) {
            result.put("scriptCommandLine", ((ScriptJobConfiguration) jobConfig.getTypeConfig()).getScriptCommandLine());
        }
        result.put("beanName", jobConfig.getBeanName());
        result.put("applicationContext", jobConfig.getApplicationContext());
        return result;
    }
}
