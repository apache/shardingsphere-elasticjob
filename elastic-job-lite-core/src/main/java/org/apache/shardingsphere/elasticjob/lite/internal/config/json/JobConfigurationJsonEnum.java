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

package org.apache.shardingsphere.elasticjob.lite.internal.config.json;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * Job configuration json enum.
 */
@RequiredArgsConstructor
@Getter
public enum JobConfigurationJsonEnum {
    
    JOB_NAME("jobName", String.class),
    
    JOB_TYPE("jobType", String.class),
    
    CRON("cron", String.class),
    
    SHARDING_TOTAL_COUNT("shardingTotalCount", int.class),
    
    SHARDING_ITEM_PARAMETERS("shardingItemParameters", String.class),
    
    JOB_PARAMETER("jobParameter", String.class),
    
    FAILOVER("failover", boolean.class),
    
    MISFIRE("misfire", boolean.class),
    
    JOB_EXECUTOR_SERVICE_HANDLER_TYPE("jobExecutorServiceHandlerType", String.class),
    
    JOB_ERROR_HANDLER_TYPE("jobErrorHandlerType", String.class),
    
    MONITOR_EXECUTION("monitorExecution", boolean.class),
    
    MAX_TIME_DIFF_SECONDS("maxTimeDiffSeconds", int.class),
    
    MONITOR_PORT("monitorPort", int.class),
    
    JOB_SHARDING_STRATEGY_TYPE("jobShardingStrategyType", String.class),
    
    RECONCILE_INTERVAL_MINUTES("reconcileIntervalMinutes", int.class),
    
    DESCRIPTION("description", String.class),
    
    STREAMING_PROCESS("streamingProcess", boolean.class),
    
    SCRIPT_COMMAND_LINE("scriptCommandLine", String.class),
    
    DISABLED("disabled", boolean.class),
    
    OVERWRITE("overwrite", boolean.class);
    
    private final String jsonName;
    
    private final Class<?> jsonType;
    
    /**
     * Find job configuration json enumeration.
     * 
     * @param jsonName json name
     * @return job configuration json enumeration
     */
    public static Optional<JobConfigurationJsonEnum> find(final String jsonName) {
        return Arrays.stream(JobConfigurationJsonEnum.values()).filter(each -> each.getJsonName().equals(jsonName)).findFirst();
    }
}
