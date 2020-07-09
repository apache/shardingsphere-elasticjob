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
public final class LiteYamlConstants {
    
    private static final String JOB_YAML = "jobName: test_job\n"
            + "cron: 0/1 * * * * ?\n"
            + "shardingTotalCount: 3\n"
            + "jobParameter: 'param'\n"
            + "failover: %s\n"
            + "monitorExecution: %s\n"
            + "misfire: false\n"
            + "maxTimeDiffSeconds: %s\n"
            + "reconcileIntervalMinutes: 15\n"
            + "description: 'desc'\n"
            + "disabled: true\n"
            + "overwrite: true";
    
    private static final boolean DEFAULT_FAILOVER = true;
    
    private static final boolean DEFAULT_MONITOR_EXECUTION = true;
    
    private static final int DEFAULT_MAX_TIME_DIFF_SECONDS = 1000;
    
    /**
     * Get the config of simple job in YAML format.
     *
     * @return the string of job config
     */
    public static String getJobYaml() {
        return String.format(JOB_YAML, DEFAULT_FAILOVER, DEFAULT_MONITOR_EXECUTION, DEFAULT_MAX_TIME_DIFF_SECONDS);
    }
    
    /**
     * Get the config of simple job in YAML format.
     *
     * @param maxTimeDiffSeconds max different time in seconds
     * @return the string of job config
     */
    public static String getJobYaml(final int maxTimeDiffSeconds) {
        return String.format(JOB_YAML, DEFAULT_FAILOVER, DEFAULT_MONITOR_EXECUTION, maxTimeDiffSeconds);
    }
    
    /**
     * Get the config of simple job in YAML format.
     *
     * @param failover Whether to enable failover
     * @return the string of job config
     */
    public static String getJobYamlWithFailover(final boolean failover) {
        return String.format(JOB_YAML, failover, DEFAULT_MONITOR_EXECUTION, DEFAULT_MAX_TIME_DIFF_SECONDS);
    }
    
    /**
     * Get the config of simple job in YAML format.
     *
     * @param monitorExecution Whether to enable monitor execution
     * @return the string of job config
     */
    public static String getJobYamlWithMonitorExecution(final boolean monitorExecution) {
        return String.format(JOB_YAML, DEFAULT_FAILOVER, monitorExecution, DEFAULT_MAX_TIME_DIFF_SECONDS);
    }
}
