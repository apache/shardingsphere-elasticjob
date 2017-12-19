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

package io.elasticjob.lite.fixture;

import io.elasticjob.lite.executor.handler.impl.DefaultExecutorServiceHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class APIJsonConstants {
    
    private static final String JOB_PROPS_JSON = "{\"job_exception_handler\":\"%s\",\"executor_service_handler\":\"" + DefaultExecutorServiceHandler.class.getCanonicalName() + "\"}";
    
    // CHECKSTYLE:OFF
    private static final String SIMPLE_JOB_JSON =  "{\"jobName\":\"test_job\",\"jobClass\":\"io.elasticjob.lite.fixture.job.TestSimpleJob\",\"jobType\":\"SIMPLE\","
            + "\"cron\":\"0/1 * * * * ?\",\"shardingTotalCount\":3,\"shardingItemParameters\":\"0\\u003dA,1\\u003dB,2\\u003dC\",\"jobParameter\":\"param\",\"failover\":true,\"misfire\":false,"
            + "\"description\":\"desc\",\"jobProperties\":%s}";
    // CHECKSTYLE:ON
    
    private static final String DATAFLOW_JOB_JSON = "{\"jobName\":\"test_job\",\"jobClass\":\"io.elasticjob.lite.fixture.job.TestDataflowJob\",\"jobType\":\"DATAFLOW\","
            + "\"cron\":\"0/1 * * * * ?\",\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"\",\"failover\":false,\"misfire\":true,\"description\":\"\","
            + "\"jobProperties\":%s,\"streamingProcess\":true}";
    
    private static final String SCRIPT_JOB_JSON = "{\"jobName\":\"test_job\",\"jobClass\":\"io.elasticjob.lite.api.script.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
            + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"\",\"failover\":false,\"misfire\":true,\"description\":\"\","
            + "\"jobProperties\":%s,\"scriptCommandLine\":\"test.sh\"}";
    
    public static String getJobPropertiesJson(final String jobExceptionHandler) {
        return String.format(JOB_PROPS_JSON, jobExceptionHandler);
    }
    
    public static String getSimpleJobJson(final String jobExceptionHandler) {
        return String.format(SIMPLE_JOB_JSON, getJobPropertiesJson(jobExceptionHandler));
    }
    
    public static String getDataflowJobJson(final String jobExceptionHandler) {
        return String.format(DATAFLOW_JOB_JSON, getJobPropertiesJson(jobExceptionHandler));
    }
    
    public static String getScriptJobJson(final String jobExceptionHandler) {
        return String.format(SCRIPT_JOB_JSON, getJobPropertiesJson(jobExceptionHandler));
    }
}
