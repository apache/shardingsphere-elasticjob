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
public final class LiteJsonConstants {
    
    private static final String JOB_JSON = "{\"jobName\":\"test_job\",\"jobType\":\"SIMPLE\",\"cron\":\"0/1 * * * * ?\","
            + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"param\",\"failover\":%s,\"misfire\":false,\"description\":\"desc\","
            + "\"monitorExecution\":%s,\"maxTimeDiffSeconds\":%s,"
            + "\"monitorPort\":8888,\"jobShardingStrategyType\":\"testClass\",\"disabled\":true,\"overwrite\":true, \"reconcileIntervalMinutes\": 15}";
    
    private static final boolean DEFAULT_FAILOVER = true;
    
    private static final boolean DEFAULT_MONITOR_EXECUTION = true;
    
    private static final int DEFAULT_MAX_TIME_DIFF_SECONDS = 1000;
    
    /**
     * Get the config of simple job in json format.
     *
     * @return the string of job config
     */
    public static String getJobJson() {
        return String.format(JOB_JSON, DEFAULT_FAILOVER, DEFAULT_MONITOR_EXECUTION, DEFAULT_MAX_TIME_DIFF_SECONDS);
    }
    
    /**
     * Get the config of simple job in json format.
     *
     * @param maxTimeDiffSeconds max different time in seconds
     * @return the string of job config
     */
    public static String getJobJson(final int maxTimeDiffSeconds) {
        return String.format(JOB_JSON, DEFAULT_FAILOVER, DEFAULT_MONITOR_EXECUTION, maxTimeDiffSeconds);
    }
    
    /**
     * Get the config of simple job in json format.
     *
     * @param failover Whether to enable failover
     * @return the string of job config
     */
    public static String getJobJsonWithFailover(final boolean failover) {
        return String.format(JOB_JSON, failover, DEFAULT_MONITOR_EXECUTION, DEFAULT_MAX_TIME_DIFF_SECONDS);
    }
    
    /**
     * Get the config of simple job in json format.
     *
     * @param monitorExecution Whether to enable monitor execution
     * @return the string of job config
     */
    public static String getJobJsonWithMonitorExecution(final boolean monitorExecution) {
        return String.format(JOB_JSON, DEFAULT_FAILOVER, monitorExecution, DEFAULT_MAX_TIME_DIFF_SECONDS);
    }
}
