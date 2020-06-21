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

package org.apache.shardingsphere.elasticjob.lite.config;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Job core configuration.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class JobCoreConfiguration {
    
    private final String jobName;
    
    private final String cron;
    
    private final int shardingTotalCount;
    
    private final String shardingItemParameters;
    
    private final String jobParameter;
    
    private final boolean failover;
    
    private final boolean misfire;
    
    private final String jobExecutorServiceHandlerType;
    
    private final String jobExceptionHandlerType;
    
    private final String description;
    
    /**
     * Create simple job configuration builder.
     *
     * @param jobName job name
     * @param cron cron expression for job trigger
     * @param shardingTotalCount sharding total count
     * @return simple job configuration builder
     */
    public static Builder newBuilder(final String jobName, final String cron, final int shardingTotalCount) {
        return new Builder(jobName, cron, shardingTotalCount);
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {
        
        private final String jobName;
        
        private final String cron;
        
        private final int shardingTotalCount;
        
        private String shardingItemParameters = "";
        
        private String jobParameter = "";
        
        private boolean failover;
        
        private boolean misfire = true;
    
        private String jobExecutorServiceHandlerType;
    
        private String jobExceptionHandlerType;
        
        private String description = "";
        
        /**
         * Set mapper of sharding items and sharding parameters.
         *
         * <p>
         * sharding item and sharding parameter split by =, multiple sharding items and sharding parameters split by comma, just like map.
         * Sharding item start from zero, cannot equal to great than sharding total count.
         * 
         * For example:
         * 0=a,1=b,2=c
         * </p>
         *
         * @param shardingItemParameters mapper of sharding items and sharding parameters
         *
         * @return job configuration builder
         */
        public Builder shardingItemParameters(final String shardingItemParameters) {
            if (null != shardingItemParameters) {
                this.shardingItemParameters = shardingItemParameters;
            }
            return this;
        }
        
        /**
         * Set job parameter.
         *
         * @param jobParameter job parameter
         *
         * @return job configuration builder
         */
        public Builder jobParameter(final String jobParameter) {
            if (null != jobParameter) {
                this.jobParameter = jobParameter;
            }
            return this;
        }
        
        /**
         * Set enable failover.
         *
         * <p>
         * Only for `monitorExecution` enabled.
         * </p> 
         *
         * @param failover enable or disable failover
         *
         * @return job configuration builder
         */
        public Builder failover(final boolean failover) {
            this.failover = failover;
            return this;
        }
        
        /**
         * Set enable misfire.
         *
         * @param misfire enable or disable misfire
         *
         * @return job configuration builder
         */
        public Builder misfire(final boolean misfire) {
            this.misfire = misfire;
            return this;
        }
        
        /**
         * Set job exception handler type.
         *
         * @param jobExceptionHandlerType job exception handler type
         *
         * @return job configuration builder
         */
        public Builder jobExceptionHandlerType(final String jobExceptionHandlerType) {
            this.jobExceptionHandlerType = jobExceptionHandlerType;
            return this;
        }
        
        /**
         * Set job executor service handler type.
         *
         * @param jobExecutorServiceHandlerType job executor service handler type
         *
         * @return job configuration builder
         */
        public Builder jobExecutorServiceHandlerType(final String jobExecutorServiceHandlerType) {
            this.jobExecutorServiceHandlerType = jobExecutorServiceHandlerType;
            return this;
        }
        
        /**
         * Set job description.
         *
         * @param description job description
         *
         * @return job configuration builder
         */
        public Builder description(final String description) {
            if (null != description) {
                this.description = description;
            }
            return this;
        }
        
        /**
         * Build Job.
         *
         * @return job configuration builder
         */
        public final JobCoreConfiguration build() {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(jobName), "jobName can not be empty.");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(cron), "cron can not be empty.");
            Preconditions.checkArgument(shardingTotalCount > 0, "shardingTotalCount should larger than zero.");
            return new JobCoreConfiguration(
                    jobName, cron, shardingTotalCount, shardingItemParameters, jobParameter, failover, misfire, jobExecutorServiceHandlerType, jobExceptionHandlerType, description);
        }
    }
}
