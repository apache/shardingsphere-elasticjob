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

package org.apache.shardingsphere.elasticjob.lite.lifecycle.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.lite.executor.handler.impl.DefaultExecutorServiceHandler;
import org.apache.shardingsphere.elasticjob.lite.executor.handler.impl.DefaultJobExceptionHandler;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LifecycleJsonConstants {
    
    private static final String JOB_PROPS_JSON = "{\"job_exception_handler\":\"" + DefaultJobExceptionHandler.class.getCanonicalName() + "\","
            + "\"executor_service_handler\":\"" + DefaultExecutorServiceHandler.class.getCanonicalName() + "\"}";
    
    private static final String SIMPLE_JOB_JSON = "{\"jobName\":\"%s\","
            + "\"jobType\":\"SIMPLE\",\"cron\":\"0/1 * * * * ?\","
            + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"param\",\"failover\":true,\"misfire\":false,\"description\":\"%s\","
            + "\"jobProperties\":" + JOB_PROPS_JSON + ",\"monitorExecution\":false,\"maxTimeDiffSeconds\":1000,\"monitorPort\":8888,\"jobShardingStrategyClass\":\"testClass\","
            + "\"disabled\":true,\"overwrite\":true}";
    
    private static final String DATAFLOW_JOB_JSON = "{\"jobName\":\"test_job\",\"jobType\":\"DATAFLOW\","
            + "\"cron\":\"0/1 * * * * ?\",\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"param\",\"failover\":false,\"misfire\":true,\"description\":\"\","
            + "\"jobProperties\":" + JOB_PROPS_JSON + ",\"monitorExecution\":true,\"maxTimeDiffSeconds\":-1,\"monitorPort\":8888,\"jobShardingStrategyClass\":\"\",\"disabled\":false,"
            + "\"overwrite\":false,\"streamingProcess\":true}";
    
    private static final String SCRIPT_JOB_JSON = "{\"jobName\":\"%s\","
            + "\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
            + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"param\",\"failover\":false,\"misfire\":true,\"description\":\"\","
            + "\"jobProperties\":" + JOB_PROPS_JSON + ",\"monitorExecution\":true,\"maxTimeDiffSeconds\":-1,\"monitorPort\":8888,\"jobShardingStrategyClass\":\"\","
            + "\"disabled\":false,\"overwrite\":false,\"scriptCommandLine\":\"test.sh\"}";
    
    /**
     * Get the config of simple job in json format.
     *
     * @param jobName name of the job
     * @param desc description of the job
     * @return the string of job config
     */
    public static String getSimpleJobJson(final String jobName, final String desc) {
        return String.format(SIMPLE_JOB_JSON, jobName, desc);
    }

    /**
     * Get the config of dataflow job in json format.
     *
     * @return the string of job config
     */
    public static String getDataflowJobJson() {
        return DATAFLOW_JOB_JSON;
    }

    /**
     * Get the config of script job in json format.
     *
     * @return the string of job config
     */
    public static String getScriptJobJson() {
        return SCRIPT_JOB_JSON;
    }
}
