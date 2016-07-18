/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 *
 */

package com.dangdang.ddframe.job.lite.api.config.impl;

import com.dangdang.ddframe.job.lite.plugin.job.type.simple.AbstractSimpleElasticJob;

/**
 * 简单作业配置信息.
 * 
 * @author caohao
 */
public final class SimpleJobConfiguration<T extends AbstractSimpleElasticJob> extends AbstractJobConfiguration {
    
    @SuppressWarnings("unchecked")
    //CHECKSTYLE:OFF
    private SimpleJobConfiguration(final String jobName, final Class<? extends T> jobClass, final int shardingTotalCount, final String cron, 
                                 final String shardingItemParameters, final String jobParameter, final boolean monitorExecution, final int maxTimeDiffSeconds,
                                 final boolean isFailover, final boolean isMisfire, final int monitorPort, final String jobShardingStrategyClass, final String description,
                                 final boolean disabled, final boolean overwrite) {
        super(jobName, JobType.SIMPLE, jobClass, shardingTotalCount, cron, shardingItemParameters, jobParameter, monitorExecution, maxTimeDiffSeconds, isFailover, isMisfire, 
                monitorPort, jobShardingStrategyClass, description, disabled, overwrite);
    }
    //CHECKSTYLE:ON
    
    public static class SimpleJobConfigurationBuilder extends AbstractJobConfigurationBuilder<SimpleJobConfiguration, AbstractSimpleElasticJob, SimpleJobConfigurationBuilder> {
        
        public SimpleJobConfigurationBuilder(final String jobName, final Class<? extends AbstractSimpleElasticJob> jobClass, final int shardingTotalCount, final String cron) {
            super(jobName, JobType.SIMPLE, jobClass, shardingTotalCount, cron);
        }
        
        @Override
        @SuppressWarnings("unchecked")
        protected SimpleJobConfiguration buildInternal() {
            return new SimpleJobConfiguration(getJobName(), getJobClass(), getShardingTotalCount(), getCron(), getShardingItemParameters(), getJobParameter(),
                isMonitorExecution(), getMaxTimeDiffSeconds(), isFailover(), isMisfire(), getMonitorPort(), getJobShardingStrategyClass(),
                getDescription(), isDisabled(), isOverwrite());
        }
    }
}
