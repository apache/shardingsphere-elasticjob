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

package io.elasticjob.lite.lifecycle.fixture;

import io.elasticjob.lite.executor.handler.impl.DefaultExecutorServiceHandler;
import io.elasticjob.lite.executor.handler.impl.DefaultJobExceptionHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LifecycleJsonConstants {
    
    private static final String JOB_PROPS_JSON = "{\"job_exception_handler\":\"" + DefaultJobExceptionHandler.class.getCanonicalName() + "\","
            + "\"executor_service_handler\":\"" + DefaultExecutorServiceHandler.class.getCanonicalName() + "\"}";
    
    private static final String SIMPLE_JOB_JSON =  "{\"jobName\":\"%s\",\"jobClass\":\"io.elasticjob.lite.fixture.TestSimpleJob\",\"jobType\":\"SIMPLE\",\"cron\":\"0/1 * * * * ?\","
            + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"param\",\"failover\":true,\"misfire\":false,\"description\":\"%s\","
            + "\"jobProperties\":" + JOB_PROPS_JSON + ",\"monitorExecution\":false,\"maxTimeDiffSeconds\":1000,\"monitorPort\":8888,\"jobShardingStrategyClass\":\"testClass\","
            + "\"disabled\":true,\"overwrite\":true}";
    
    private static final String DATAFLOW_JOB_JSON =  "{\"jobName\":\"test_job\",\"jobClass\":\"io.elasticjob.lite.fixture.TestDataflowJob\",\"jobType\":\"DATAFLOW\","
            + "\"cron\":\"0/1 * * * * ?\",\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"param\",\"failover\":false,\"misfire\":true,\"description\":\"\","
            + "\"jobProperties\":" + JOB_PROPS_JSON + ",\"monitorExecution\":true,\"maxTimeDiffSeconds\":-1,\"monitorPort\":8888,\"jobShardingStrategyClass\":\"\",\"disabled\":false,"
            + "\"overwrite\":false,\"streamingProcess\":true}";
    
    private static final String SCRIPT_JOB_JSON =  "{\"jobName\":\"%s\",\"jobClass\":\"io.elasticjob.lite.api.script.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
            + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"param\",\"failover\":false,\"misfire\":true,\"description\":\"\","
            + "\"jobProperties\":" + JOB_PROPS_JSON + ",\"monitorExecution\":true,\"maxTimeDiffSeconds\":-1,\"monitorPort\":8888,\"jobShardingStrategyClass\":\"\","
            + "\"disabled\":false,\"overwrite\":false,\"scriptCommandLine\":\"test.sh\"}";
    
    public static String getSimpleJobJson(final String jobName, final String desc) {
        return String.format(SIMPLE_JOB_JSON, jobName, desc);
    }
    
    public static String getDataflowJobJson() {
        return DATAFLOW_JOB_JSON;
    }
    
    public static String getScriptJobJson() {
        return SCRIPT_JOB_JSON;
    }
}
