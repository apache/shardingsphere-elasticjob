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

package com.dangdang.ddframe.job.cloud.scheduler.fixture;

import com.dangdang.ddframe.job.executor.handler.impl.DefaultExecutorServiceHandler;
import com.dangdang.ddframe.job.executor.handler.impl.DefaultJobExceptionHandler;
import com.dangdang.ddframe.job.cloud.scheduler.config.JobExecutionType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CloudJsonConstants {
    
    private static final String JOB_PROPS_JSON = "{\"job_exception_handler\":\"" + DefaultJobExceptionHandler.class.getCanonicalName() + "\","
            + "\"executor_service_handler\":\"" + DefaultExecutorServiceHandler.class.getCanonicalName() + "\"}";
    
    private static final String JOB_JSON = "{\"jobName\":\"%s\",\"jobClass\":\"com.dangdang.ddframe.job.cloud.scheduler.state.fixture.TestSimpleJob\",\"jobType\":\"SIMPLE\","
            + "\"cron\":\"0/30 * * * * ?\",\"shardingTotalCount\":10,\"shardingItemParameters\":\"\",\"jobParameter\":\"\",\"failover\":true,\"misfire\":true,\"description\":\"\","
            + "\"jobProperties\":" + JOB_PROPS_JSON + ",\"jobEventConfigs\":{\"log\":{}}" + ",\"cpuCount\":1.0,\"memoryMB\":128.0," 
            + "\"appURL\":\"http://localhost/app.jar\",\"bootstrapScript\":\"bin/start.sh\",\"jobExecutionType\":\"%s\"}";
    
    private static final String SPRING_JOB_JSON = "{\"jobName\":\"%s\",\"jobClass\":\"com.dangdang.ddframe.job.cloud.scheduler.state.fixture.TestSimpleJob\",\"jobType\":\"SIMPLE\","
            + "\"cron\":\"0/30 * * * * ?\",\"shardingTotalCount\":10,\"shardingItemParameters\":\"\",\"jobParameter\":\"\",\"failover\":true,\"misfire\":true,\"description\":\"\","
            + "\"jobProperties\":" + JOB_PROPS_JSON + ",\"jobEventConfigs\":{\"log\":{}}" + ",\"cpuCount\":1.0,\"memoryMB\":128.0,"
            + "\"appURL\":\"http://localhost/app.jar\",\"bootstrapScript\":\"bin/start.sh\",\"jobExecutionType\":\"%s\",\"beanName\":\"springSimpleJob\","
            + "\"applicationContext\":\"applicationContext.xml\"}";
    
    public static String getJobJson() {
        return String.format(JOB_JSON, "test_job", "TRANSIENT");
    }
    
    public static String getJobJson(final String jobName) {
        return String.format(JOB_JSON, jobName, "TRANSIENT");
    }
    
    public static String getJobJson(final JobExecutionType jobExecutionType) {
        return String.format(JOB_JSON, "test_job", jobExecutionType.name());
    }
    
    public static String getSpringJobJson() {
        return String.format(SPRING_JOB_JSON, "test_spring_job", "TRANSIENT");
    }
}
