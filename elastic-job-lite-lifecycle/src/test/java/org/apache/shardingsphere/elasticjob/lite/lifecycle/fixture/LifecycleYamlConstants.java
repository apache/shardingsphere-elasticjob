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

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LifecycleYamlConstants {
    
    private static final String SIMPLE_JOB_YAML = "jobName: %s\n"
            + "cron: 0/1 * * * * ?\n"
            + "shardingTotalCount: 3\n"
            + "jobParameter: param\n"
            + "monitorExecution: false\n"
            + "failover: true\n"
            + "misfire: false\n"
            + "maxTimeDiffSeconds: 100\n"
            + "jobShardingStrategyType: AVG_ALLOCATION\n"
            + "description: %s\n"
            + "disabled: false\n"
            + "overwrite: false\n";
    
    private static final String DATAFLOW_JOB_YAML = "cron: 0/1 * * * * ?\n"
            + "description: ''\n"
            + "disabled: false\n"
            + "failover: false\n"
            + "jobName: test_job\n"
            + "jobParameter: param\n"
            + "maxTimeDiffSeconds: -1\n"
            + "misfire: true\n"
            + "monitorExecution: true\n"
            + "overwrite: false\n"
            + "props:\n"
            + "  streaming.process: 'true'\n"
            + "reconcileIntervalMinutes: 10\n"
            + "shardingTotalCount: 3\n";
    
    private static final String SCRIPT_JOB_YAML = "jobName: test_job\n"
            + "cron: 0/1 * * * * ?\n"
            + "shardingTotalCount: 3\n"
            + "jobParameter: param\n"
            + "monitorExecution: true\n"
            + "failover: false\n"
            + "misfire: true\n"
            + "maxTimeDiffSeconds: -1\n"
            + "reconcileIntervalMinutes: 10\n"
            + "description: ''\n"
            + "props:\n"
            + "  script.command.line: echo\n"
            + "disabled: false\n"
            + "overwrite: false\n";
    
    /**
     * Get the config of simple job in YAML format.
     *
     * @param jobName name of the job
     * @param desc description of the job
     * @return the string of job config
     */
    public static String getSimpleJobYaml(final String jobName, final String desc) {
        return String.format(SIMPLE_JOB_YAML, jobName, desc);
    }
    
    /**
     * Get the config of dataflow job in YAML format.
     *
     * @return the string of job config
     */
    public static String getDataflowJobYaml() {
        return DATAFLOW_JOB_YAML;
    }
    
    /**
     * Get the config of script job in YAML format.
     *
     * @return the string of job config
     */
    public static String getScriptJobYaml() {
        return SCRIPT_JOB_YAML;
    }
}
