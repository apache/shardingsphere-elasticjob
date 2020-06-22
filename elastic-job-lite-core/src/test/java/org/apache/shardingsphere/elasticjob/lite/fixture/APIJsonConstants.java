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

package org.apache.shardingsphere.elasticjob.lite.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class APIJsonConstants {
    
    private static final String SIMPLE_JOB_JSON = "{\"jobName\":\"test_job\",\"jobType\":\"SIMPLE\","
            + "\"cron\":\"0/1 * * * * ?\",\"shardingTotalCount\":3,\"shardingItemParameters\":\"0\\u003dA,1\\u003dB,2\\u003dC\",\"jobParameter\":\"param\",\"failover\":true,\"misfire\":false,"
            + "\"jobErrorHandlerType\":\"%s\","
            + "\"description\":\"desc\"}";
    
    private static final String DATAFLOW_JOB_JSON = "{\"jobName\":\"test_job\",\"jobType\":\"DATAFLOW\","
            + "\"cron\":\"0/1 * * * * ?\",\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"\",\"failover\":false,\"misfire\":true,"
            + "\"jobErrorHandlerType\":\"%s\","
            + "\"description\":\"\",\"streamingProcess\":true}";
    
    private static final String SCRIPT_JOB_JSON = "{\"jobName\":\"test_job\",\"jobType\":\"SCRIPT\","
            + "\"cron\":\"0/1 * * * * ?\","
            + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"\",\"failover\":false,\"misfire\":true,"
            + "\"jobErrorHandlerType\":\"%s\","
            + "\"description\":\"\","
            + "\"scriptCommandLine\":\"test.sh\"}";
    
    /**
     * Get configuration of simple job in json format.
     *
     * @param jobErrorHandlerType job error handler type
     * @return configuration of simple job in json format
     */
    public static String getSimpleJobJson(final String jobErrorHandlerType) {
        return String.format(SIMPLE_JOB_JSON, jobErrorHandlerType);
    }
    
    /**
     * Get configuration of dataflow job in json format.
     *
     * @param jobErrorHandlerType job error handler type
     * @return configuration of dataflow job in json format
     */
    public static String getDataflowJobJson(final String jobErrorHandlerType) {
        return String.format(DATAFLOW_JOB_JSON, jobErrorHandlerType);
    }
    
    /**
     * Get configuration of script job in json format.
     *
     * @param jobErrorHandlerType job error handler type
     * @return configuration of script job in json format
     */
    public static String getScriptJobJson(final String jobErrorHandlerType) {
        return String.format(SCRIPT_JOB_JSON, jobErrorHandlerType);
    }
}
