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

package org.apache.shardingsphere.elasticjob.lite.springboot.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ElasticJob {

    /**
     * Enum type of JobBootstrap.
     *
     * @return JobBootstrapType
     */
    JobBootstrapType jobBootstrapType();

    /**
     * Job name.
     *
     * @return jobName
     */
    String jobName();

    /**
     * Cron.
     *
     * @return cron
     */
    String cron() default "";

    /**
     * Count of sharding.
     *
     * @return shardingTotalCount
     */
    int shardingTotalCount();

    /**
     * Parameters of sharding.
     *
     * @return shardingItemParameters
     */
    String shardingItemParameters() default "";

    /**
     * Parameter of Job.
     *
     * @return jobParameter
     */
    String jobParameter() default "";

    /**
     * Monitor execution.
     *
     * @return monitorExecution
     */
    boolean monitorExecution() default true;

    /**
     * Failover.
     *
     * @return failover
     */
    boolean failover() default false;

    /**
     * Misfire.
     *
     * @return misfire
     */
    boolean misfire() default true;

    /**
     * Max time diff seconds.
     *
     * @return maxTimeDiffSeconds
     */
    int maxTimeDiffSeconds() default -1;

    /**
     * Reconcile Interval Minutes.
     *
     * @return reconcileIntervalMinutes
     */
    int reconcileIntervalMinutes() default 10;

    /**
     * Job sharding strategy type.
     *
     * @return jobShardingStrategyType
     */
    String jobShardingStrategyType() default "";

    /**
     * Job executor service handler type.
     *
     * @return jobExecutorServiceHandlerType
     */
    String jobExecutorServiceHandlerType() default "";

    /**
     * Job error handler type.
     *
     * @return jobExecutorServiceHandlerType
     */
    String jobErrorHandlerType() default "";

    /**
     * Description.
     *
     * @return descriptionn
     */
    String description() default "";

    /**
     * Properties of job.
     *
     * @return properties
     */
    ElasticJobProperty[] props() default {};

    /**
     * If disabled.
     *
     * @return disabled
     */
    boolean disabled() default false;

    /**
     * If overwrite.
     *
     * @return overwrite
     */
    boolean overwrite() default false;
}
