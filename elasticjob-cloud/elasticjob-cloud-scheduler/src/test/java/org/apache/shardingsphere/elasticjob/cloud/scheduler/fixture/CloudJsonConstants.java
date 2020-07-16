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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CloudJsonConstants {

    private static final String JOB_JSON = "{\"appName\":\"test_app\",\"cpuCount\":1.0,\"memoryMB\":128.0,\"jobExecutionType\":\"%s\",\"jobName\":\"%s\","
            + "\"cron\":\"0/30 * * * * ?\",\"shardingTotalCount\":10,\"shardingItemParameters\":\"\",\"jobParameter\":\"\",\"monitorExecution\":false,\"failover\":true,"
            + "\"misfire\":%s,\"maxTimeDiffSeconds\":0,\"reconcileIntervalMinutes\":0,\"description\":\"\",\"props\":{},\"disabled\":false,\"overwrite\":false}";

    private static final String SPRING_JOB_JSON = "{\"jobName\":\"test_spring_job\","
            + "\"cron\":\"0/30 * * * * ?\",\"shardingTotalCount\":10,\"shardingItemParameters\":\"\",\"jobParameter\":\"\",\"failover\":true,\"misfire\":true,\"description\":\"\","
            + "\"appName\":\"test_spring_app\",\"cpuCount\":1.0,\"memoryMB\":128.0,"
            + "\"jobExecutionType\":\"TRANSIENT\"}";
    
    /**
     * Get job in json format.
     * @return job in json format
     */
    public static String getJobJson() {
        return String.format(JOB_JSON, "TRANSIENT", "test_job", true);
    }
    
    /**
     * Get job in json format.
     * @param jobName job name
     * @return job in json format
     */
    public static String getJobJson(final String jobName) {
        return String.format(JOB_JSON, "TRANSIENT", jobName, true);
    }
    
    /**
     * Get job in json format.
     * @param jobExecutionType job execution type
     * @return job in json format
     */
    public static String getJobJson(final CloudJobExecutionType jobExecutionType) {
        return String.format(JOB_JSON, jobExecutionType.name(), "test_job", true);
    }
    
    /**
     * Get job in json format.
     * @param misfire is misfire
     * @return job in json format
     */
    public static String getJobJson(final boolean misfire) {
        return String.format(JOB_JSON, "TRANSIENT", "test_job", misfire);
    }
    
    /**
     * Get sprint job in json format.
     * @return job in json format
     */
    public static String getSpringJobJson() {
        return SPRING_JOB_JSON;
    }
}
