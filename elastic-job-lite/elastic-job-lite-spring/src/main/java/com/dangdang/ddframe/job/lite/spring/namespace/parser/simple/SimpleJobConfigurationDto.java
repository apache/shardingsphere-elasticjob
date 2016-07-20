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

package com.dangdang.ddframe.job.lite.spring.namespace.parser.simple;

import com.dangdang.ddframe.job.api.type.simple.AbstractSimpleElasticJob;
import com.dangdang.ddframe.job.lite.api.config.impl.JobType;
import com.dangdang.ddframe.job.lite.api.config.impl.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.namespace.parser.common.AbstractJobConfigurationDto;
import lombok.Getter;
import lombok.Setter;

/**
 * 简单作业配置命名空间对象.
 *
 * @author caohao
 */
@Getter
@Setter
class SimpleJobConfigurationDto extends AbstractJobConfigurationDto<SimpleJobConfiguration, AbstractSimpleElasticJob, SimpleJobConfiguration.SimpleJobConfigurationBuilder> {
    
    SimpleJobConfigurationDto(final String jobName, final Class<? extends AbstractSimpleElasticJob> jobClass, final int shardingTotalCount, final String cron) {
        super(jobName, JobType.SIMPLE, jobClass, shardingTotalCount, cron);
    }
    
    @Override
    public SimpleJobConfiguration toJobConfiguration() {
        return createBuilder().build();
    }
    
    @Override
    protected SimpleJobConfiguration.SimpleJobConfigurationBuilder createCustomizedBuilder() {
        return new SimpleJobConfiguration.SimpleJobConfigurationBuilder(getJobName(), getJobClass(), getShardingTotalCount(), getCron());
    }
    
    @Override
    protected SimpleJobConfiguration.SimpleJobConfigurationBuilder buildCustomizedProperties(final SimpleJobConfiguration.SimpleJobConfigurationBuilder builder) {
        return builder;
    }
}
