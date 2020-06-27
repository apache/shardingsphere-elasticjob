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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.lite.api.JobType;

/**
 * ElasticJob configuration.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobConfiguration {
    
    private final JobType jobType;
    
    private final JobTypeConfiguration typeConfig;
    
    private final boolean monitorExecution;
    
    private final int maxTimeDiffSeconds;
    
    private final int monitorPort;
    
    private final String jobShardingStrategyType;
    
    private final int reconcileIntervalMinutes;
    
    private final boolean disabled;
    
    private final boolean overwrite;
    
    /**
     * Get job name.
     * 
     * @return job name
     */
    public String getJobName() {
        return typeConfig.getCoreConfig().getJobName();
    }
    
    /**
     * Get is enable or disable failover.
     *
     * @return is enable failover
     */
    public boolean isFailover() {
        return typeConfig.getCoreConfig().isFailover();
    }
    
    /**
     * Create ElasticJob lite configuration builder.
     * 
     * @param jobType job type
     * @param jobConfig job configuration
     * @return ElasticJob lite configuration builder
     */
    public static Builder newBuilder(final JobType jobType, final JobTypeConfiguration jobConfig) {
        return new Builder(jobType, jobConfig);
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {
    
        private final JobType jobType;
        
        private final JobTypeConfiguration jobConfig;
    
        private boolean monitorExecution = true;
        
        private int maxTimeDiffSeconds = -1;
        
        private int monitorPort = -1;
        
        private String jobShardingStrategyType = "";
        
        private boolean disabled;
        
        private boolean overwrite;
        
        private int reconcileIntervalMinutes = 10;
    
        /**
         * Set enable or disable monitor execution.
         *
         * <p>
         * For short interval job, it is better to disable monitor execution to improve performance. 
         * It can't guarantee repeated data fetch and can't failover if disable monitor execution, please keep idempotence in job.
         * 
         * For long interval job, it is better to enable monitor execution to guarantee fetch data exactly once.
         * </p>
         *
         * @param monitorExecution monitor job execution status 
         *
         * @return ElasticJob lite configuration builder
         */
        public Builder monitorExecution(final boolean monitorExecution) {
            this.monitorExecution = monitorExecution;
            return this;
        }
        
        /**
         * Set max tolerate time different seconds between job server and registry center.
         *
         * <p>
         * ElasticJob will throw exception if exceed max tolerate time different seconds.
         * -1 means do not check.
         * </p>
         *
         * @param maxTimeDiffSeconds max tolerate time different seconds between job server and registry center
         *
         * @return ElasticJob lite configuration builder
         */
        public Builder maxTimeDiffSeconds(final int maxTimeDiffSeconds) {
            this.maxTimeDiffSeconds = maxTimeDiffSeconds;
            return this;
        }
        
        /**
         * Set job monitor port.
         * 
         * @param monitorPort job monitor port
         *
         * @return ElasticJob lite configuration builder
         */
        public Builder monitorPort(final int monitorPort) {
            this.monitorPort = monitorPort;
            return this;
        }
        
        /**
         * Set job sharding sharding type.
         *
         * <p>
         * Default for {@code AverageAllocationJobShardingStrategy}.
         * </p>
         *
         * @param jobShardingStrategyType job sharding sharding type
         *
         * @return ElasticJob lite configuration builder
         */
        public Builder jobShardingStrategyType(final String jobShardingStrategyType) {
            if (null != jobShardingStrategyType) {
                this.jobShardingStrategyType = jobShardingStrategyType;
            }
            return this;
        }
    
        /**
         * Set reconcile interval minutes for job sharding status.
         *
         * <p>
         * Monitor the status of the job server at regular intervals, and resharding if incorrect.
         * </p>
         *
         * @param reconcileIntervalMinutes reconcile interval minutes for job sharding status
         *
         * @return ElasticJob lite configuration builder
         */
        public Builder reconcileIntervalMinutes(final int reconcileIntervalMinutes) {
            this.reconcileIntervalMinutes = reconcileIntervalMinutes;
            return this;
        }
        
        /**
         * Set whether disable job when start.
         * 
         * <p>
         * Using in job deploy, start job together after deploy.
         * </p>
         *
         * @param disabled whether disable job when start
         *
         * @return ElasticJob lite configuration builder
         */
        public Builder disabled(final boolean disabled) {
            this.disabled = disabled;
            return this;
        }
        
        /**
         * Set whether overwrite local configuration to registry center when job startup. 
         * 
         * <p>
         *  If overwrite enabled, every startup will use local configuration.
         * </p>
         *
         * @param overwrite whether overwrite local configuration to registry center when job startup
         *
         * @return ElasticJob lite configuration builder
         */
        public Builder overwrite(final boolean overwrite) {
            this.overwrite = overwrite;
            return this;
        }
        
        /**
         * Build ElasticJob lite configuration.
         * 
         * @return ElasticJob lite configuration
         */
        public final JobConfiguration build() {
            return new JobConfiguration(jobType, jobConfig, monitorExecution, maxTimeDiffSeconds, monitorPort, jobShardingStrategyType, reconcileIntervalMinutes, disabled, overwrite);
        }
    }
}
