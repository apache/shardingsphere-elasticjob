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

package com.dangdang.ddframe.job.lite.fixture;

import com.dangdang.ddframe.job.executor.handler.impl.DefaultExecutorServiceHandler;
import com.dangdang.ddframe.job.executor.handler.impl.DefaultJobExceptionHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LiteJsonConstants {
    
    private static final String JOB_PROPS_JSON = "{\"job_exception_handler\":\"" + DefaultJobExceptionHandler.class.getCanonicalName() + "\","
            + "\"executor_service_handler\":\"" + DefaultExecutorServiceHandler.class.getCanonicalName() + "\"}";
    
    private static final String JOB_JSON = "{\"jobName\":\"test_job\",\"jobClass\":\"%s\",\"jobType\":\"SIMPLE\",\"cron\":\"0/1 * * * * ?\","
            + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"param\",\"failover\":true,\"misfire\":false,\"description\":\"desc\","
            + "\"jobProperties\":" + JOB_PROPS_JSON + ",\"monitorExecution\":%s,\"maxTimeDiffSeconds\":%s,"
            + "\"monitorPort\":8888,\"jobShardingStrategyClass\":\"testClass\",\"disabled\":true,\"overwrite\":true, \"reconcileIntervalMinutes\": %s}";
    
    private static final String DEFAULT_JOB_CLASS = "com.dangdang.ddframe.job.lite.fixture.TestSimpleJob";
    
    private static final boolean DEFAULT_MONITOR_EXECUTION = true;
    
    private static final int DEFAULT_MAX_TIME_DIFF_SECONDS = 1000;
    
    private static final int DEFAULT_RECONCILE_CYCLE_TIME = 15;
    
    public static String getJobJson() {
        return String.format(JOB_JSON, DEFAULT_JOB_CLASS, DEFAULT_MONITOR_EXECUTION, DEFAULT_MAX_TIME_DIFF_SECONDS, DEFAULT_RECONCILE_CYCLE_TIME);
    }
    
    public static String getJobJson(final String jobClass) {
        return String.format(JOB_JSON, jobClass, DEFAULT_MONITOR_EXECUTION, DEFAULT_MAX_TIME_DIFF_SECONDS, DEFAULT_RECONCILE_CYCLE_TIME);
    }
    
    public static String getJobJson(final boolean monitorExecution) {
        return String.format(JOB_JSON, DEFAULT_JOB_CLASS, monitorExecution, DEFAULT_MAX_TIME_DIFF_SECONDS, DEFAULT_RECONCILE_CYCLE_TIME);
    }
    
    public static String getJobJson(final int maxTimeDiffSeconds) {
        return String.format(JOB_JSON, DEFAULT_JOB_CLASS, DEFAULT_MONITOR_EXECUTION, maxTimeDiffSeconds, DEFAULT_RECONCILE_CYCLE_TIME);
    }
    
    public static String getJobJson(final long reconcileCycleTime) {
        return String.format(JOB_JSON, DEFAULT_JOB_CLASS, DEFAULT_MONITOR_EXECUTION, DEFAULT_MAX_TIME_DIFF_SECONDS, reconcileCycleTime);
    }
}
