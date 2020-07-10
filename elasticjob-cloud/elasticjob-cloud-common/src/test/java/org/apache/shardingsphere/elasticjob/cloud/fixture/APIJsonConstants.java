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

package org.apache.shardingsphere.elasticjob.cloud.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.cloud.executor.handler.impl.DefaultExecutorServiceHandler;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class APIJsonConstants {
    
    private static final String JOB_PROPS_JSON = "{\"job_exception_handler\":\"%s\",\"executor_service_handler\":\"" + DefaultExecutorServiceHandler.class.getCanonicalName() + "\"}";
    
    // CHECKSTYLE:OFF
    private static final String SIMPLE_JOB_JSON =  "{\"jobName\":\"test_job\",\"jobClass\":\"org.apache.shardingsphere.elasticjob.cloud.fixture.job.TestSimpleJob\",\"jobType\":\"SIMPLE\","
            + "\"cron\":\"0/1 * * * * ?\",\"shardingTotalCount\":3,\"shardingItemParameters\":\"0\\u003dA,1\\u003dB,2\\u003dC\",\"jobParameter\":\"param\",\"failover\":true,\"misfire\":false,"
            + "\"description\":\"desc\",\"jobProperties\":%s}";
    // CHECKSTYLE:ON
    
    private static final String DATAFLOW_JOB_JSON = "{\"jobName\":\"test_job\",\"jobClass\":\"org.apache.shardingsphere.elasticjob.cloud.fixture.job.TestDataflowJob\",\"jobType\":\"DATAFLOW\","
            + "\"cron\":\"0/1 * * * * ?\",\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"\",\"failover\":false,\"misfire\":true,\"description\":\"\","
            + "\"jobProperties\":%s,\"streamingProcess\":true}";
    
    private static final String SCRIPT_JOB_JSON = "{\"jobName\":\"test_job\",\"jobClass\":\"org.apache.shardingsphere.elasticjob.cloud.api.script.ScriptJob\",\"jobType\":\"SCRIPT\","
            + "\"cron\":\"0/1 * * * * ?\",\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"\",\"failover\":false,\"misfire\":true,\"description\":\"\","
            + "\"jobProperties\":%s,\"scriptCommandLine\":\"test.sh\"}";

    /**
     * Get job properties in json format.
     * @param jobExceptionHandler the job exception handler
     * @return the json string
     */
    public static String getJobPropertiesJson(final String jobExceptionHandler) {
        return String.format(JOB_PROPS_JSON, jobExceptionHandler);
    }

    /**
     * Get simple job in json format.
     * @param jobExceptionHandler the job exception handler
     * @return the json string
     */
    public static String getSimpleJobJson(final String jobExceptionHandler) {
        return String.format(SIMPLE_JOB_JSON, getJobPropertiesJson(jobExceptionHandler));
    }

    /**
     * Get dataflow job in json format.
     * @param jobExceptionHandler the job exception handler
     * @return the json string
     */
    public static String getDataflowJobJson(final String jobExceptionHandler) {
        return String.format(DATAFLOW_JOB_JSON, getJobPropertiesJson(jobExceptionHandler));
    }

    /**
     * Get script job in json format.
     * @param jobExceptionHandler the job exception handler
     * @return the json string
     */
    public static String getScriptJobJson(final String jobExceptionHandler) {
        return String.format(SCRIPT_JOB_JSON, getJobPropertiesJson(jobExceptionHandler));
    }
}
