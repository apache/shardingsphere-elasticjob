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

package com.dangdang.ddframe.job.util.json;


/**
 * @author leizhenyu
 *
 * Job 配置属性的常量类
 *
 */
public class JobCoreConfigurationConstants {

    //JOB Core Configuration
    public static final String JOB_NAME = "jobName";

    public static final String JOB_CRON = "cron";

    public static final String SHARDING_TOTAL_COUNT = "shardingTotalCount";

    public static final String SHARDING_ITEM_PARAMETERS = "shardingItemParameters";

    public static final String JOB_PARAMETER = "jobParameter";

    public static final String FAIL_OVER  = "failover";

    public static final String MIS_FIRE = "misfire";

    public static final String DESCRIPTION = "description";

    public static final String JOB_PROPERTIES = "jobProperties";

    public static final String JOB_TYPE = "jobType";

    public static final String JOB_CLASS = "jobClass";

    public static final String STREAMING_PROCESS = "streamingProcess";

    public static final String SCRIPT_COMMAND_LINE = "scriptCommandLine";

    public static final String JOB_EXCEPTION_HANDLER = "job_exception_handler";

    public static final String EXECUTOR_SERVICE_HANDLER = "executor_service_handler";

}
