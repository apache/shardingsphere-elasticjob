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

package com.dangdang.ddframe.job.api.type.simple;

import com.dangdang.ddframe.job.api.SimpleElasticJob;
import com.dangdang.ddframe.job.api.internal.config.AbstractJobConfiguration;
import com.dangdang.ddframe.job.api.internal.config.JobType;

/**
 * 简单作业配置信息.
 * 
 * @author caohao
 */
public final class SimpleJobConfiguration<T extends SimpleElasticJob> extends AbstractJobConfiguration {
    
    @SuppressWarnings("unchecked")
    //CHECKSTYLE:OFF
    private SimpleJobConfiguration(final String jobName, final Class<? extends T> jobClass, final String cron, final int shardingTotalCount, 
                                   final String shardingItemParameters, final String jobParameter, final boolean isFailover, final boolean isMisfire, final String description) {
    //CHECKSTYLE:ON
        super(jobName, JobType.SIMPLE, jobClass, cron, shardingTotalCount, shardingItemParameters, jobParameter, isFailover, isMisfire, description);
    }
    
    public static class Builder extends AbstractJobConfiguration.Builder<SimpleJobConfiguration, SimpleElasticJob, Builder> {
        
        public Builder(final String jobName, final Class<? extends SimpleElasticJob> jobClass, final String cron, final int shardingTotalCount) {
            super(jobName, JobType.SIMPLE, jobClass, cron, shardingTotalCount);
        }
        
        @Override
        @SuppressWarnings("unchecked")
        protected SimpleJobConfiguration buildInternal() {
            return new SimpleJobConfiguration(
                    getJobName(), getJobClass(), getCron(), getShardingTotalCount(), getShardingItemParameters(), getJobParameter(), isFailover(), isMisfire(), getDescription());
        }
    }
}
