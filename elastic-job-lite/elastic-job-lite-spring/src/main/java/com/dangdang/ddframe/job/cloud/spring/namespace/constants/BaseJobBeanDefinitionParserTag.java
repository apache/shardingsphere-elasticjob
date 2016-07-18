/*
 * Copyright 1999_2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE_2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 *
 */

package com.dangdang.ddframe.job.cloud.spring.namespace.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 作业基本属性解析标签.
 *
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BaseJobBeanDefinitionParserTag {
    
    public static final String REGISTRY_CENTER_REF_ATTRIBUTE = "registry-center-ref";
    
    public static final String CLASS_ATTRIBUTE = "class";
    
    public static final String CRON_ATTRIBUTE = "cron";
    
    public static final String SHARDING_TOTAL_COUNT_ATTRIBUTE = "sharding-total-count";
    
    public static final String SHARDING_ITEM_PARAMETERS_ATTRIBUTE = "sharding-item-parameters";
    
    public static final String JOB_PARAMETER_ATTRIBUTE = "job-parameter";
    
    public static final String MONITOR_EXECUTION_ATTRIBUTE = "monitor-execution";
    
    public static final String MONITOR_PORT_ATTRIBUTE = "monitor-port";
    
    public static final String FAILOVER_ATTRIBUTE = "failover";
    
    public static final String MAX_TIME_DIFF_SECONDS_ATTRIBUTE = "max-time-diff-seconds";
    
    public static final String MISFIRE_ATTRIBUTE = "misfire";
    
    public static final String JOB_SHARDING_STRATEGY_CLASS_ATTRIBUTE = "job-sharding-strategy-class";
    
    public static final String DESCRIPTION_ATTRIBUTE = "description";
    
    public static final String DISABLED_ATTRIBUTE = "disabled";
    
    public static final String OVERWRITE_ATTRIBUTE = "overwrite";
    
    public static final String LISTENER_TAG = "listener";
    
    public static final String STARTED_TIMEOUT_MILLISECONDS_ATTRIBUTE = "started-timeout-milliseconds";
    
    public static final String COMPLETED_TIMEOUT_MILLISECONDS_ATTRIBUTE = "completed-timeout-milliseconds";
}
