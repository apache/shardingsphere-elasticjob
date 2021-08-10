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

package org.apache.shardingsphere.elasticjob.lite.spring.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation that specify a jo of elastic.
 */
@Documented
@Repeatable(ElasticSchedules.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ElasticScheduled {
    
    /**
     * Job name.
     * @return jobName
     */
    String jobName();
    
    /**
     * CRON expression, control the job trigger time.
     * @return cron
     */
    String cron() default "";
    
    /**
     * Sharding total count.
     * @return shardingTotalCount
     */
    int shardingTotalCount();
    
    /**
     * Sharding item parameters.
     * @return shardingItemParameters
     */
    String shardingItemParameters() default "";
    
    /**
     * Job parameter.
     * @return jobParameter
     */
    String jobParameter() default "";
    
    /**
     * Monitor job execution status.
     * @return monitorExecution
     */
    boolean monitorExecution() default true;
    
    /**
     * Enable or disable job failover.
     * @return failover
     */
    boolean failover() default false;
    
    /**
     * Enable or disable the missed task to re-execute.
     * @return misfire
     */
    boolean misfire() default true;
    
    /**
     * The maximum value for time difference between server and registry center in seconds.
     * @return maxTimeDiffSeconds
     */
    int maxTimeDiffSeconds() default -1;
    
    /**
     * Service scheduling interval in minutes for repairing job server inconsistent state.
     * @return reconcileIntervalMinutes
     */
    int reconcileIntervalMinutes() default 10;
    
    /**
     * Job sharding strategy type.
     * @return jobShardingStrategyType
     */
    String jobShardingStrategyType() default "AVG_ALLOCATION";
    
    /**
     * Job thread pool handler type.
     * @return jobExecutorServiceHandlerType
     */
    String jobExecutorServiceHandlerType() default "CPU";
    
    /**
     * Job thread pool handler type.
     * @return jobErrorHandlerType
     */
    String jobErrorHandlerType() default "";
    
    /**
     * Job listener types.
     * @return jobListenerTypes
     */
    String[] jobListenerTypes() default {};
    
    /**
     * Job description.
     * @return description
     */
    String description() default "";
    
    /**
     * Job properties.
     * @return props
     */
    ElasticJobProp[] props() default {};
    
    /**
     * Enable or disable start the job.
     * @return disabled
     */
    boolean disabled() default false;
    
    /**
     * Enable or disable local configuration override registry center configuration.
     * @return overwrite
     */
    boolean overwrite() default false;
}
