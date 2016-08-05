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

package com.dangdang.ddframe.job.cloud.fixture;

import com.dangdang.ddframe.job.api.executor.handler.impl.DefaultExecutorServiceHandler;
import com.dangdang.ddframe.job.api.executor.handler.impl.DefaultJobExceptionHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CloudJsonConstants {
    
    private static final String JOB_PROPS_JSON = "{\"job_exception_handler\":\"" + DefaultJobExceptionHandler.class.getCanonicalName() + "\","
            + "\"executor_service_handler\":\"" + DefaultExecutorServiceHandler.class.getCanonicalName() + "\"}";
    
    private static final String JOB_JSON = "{\"jobName\":\"%s\",\"jobClass\":\"com.dangdang.ddframe.job.cloud.state.fixture.TestSimpleJob\",\"jobType\":\"SIMPLE\","
            + "\"cron\":\"0/30 * * * * ?\",\"shardingTotalCount\":10,\"shardingItemParameters\":\"\",\"jobParameter\":\"\",\"failover\":true,\"misfire\":true,\"description\":\"\","
            + "\"jobProperties\":" + JOB_PROPS_JSON + ",\"cpuCount\":1.0,\"memoryMB\":128.0,\"dockerImageName\":\"dockerImage\",\"appURL\":\"http://localhost/app.jar\","
            + "\"bootstrapScript\":\"bin/start.sh\"}";
    
    public static String getJobJson() {
        return String.format(JOB_JSON, "test_job");
    }
    
    public static String getJobJson(final String jobName) {
        return String.format(JOB_JSON, jobName);
    }
}
