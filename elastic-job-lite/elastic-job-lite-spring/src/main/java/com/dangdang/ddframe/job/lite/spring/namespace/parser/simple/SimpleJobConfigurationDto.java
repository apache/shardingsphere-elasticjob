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

import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.namespace.parser.common.AbstractJobConfigurationDto;

/**
 * 简单作业配置命名空间对象.
 *
 * @author caohao
 * @author zhangliang
 */
final class SimpleJobConfigurationDto extends AbstractJobConfigurationDto {
    
    private final Class<? extends SimpleJob> jobClass;
    
    SimpleJobConfigurationDto(final String jobName, final String cron, final int shardingTotalCount, final Class<? extends SimpleJob> jobClass) {
        super(jobName, cron, shardingTotalCount);
        this.jobClass = jobClass;
    }
    
    @Override
    protected JobTypeConfiguration toJobConfiguration(final JobCoreConfiguration jobCoreConfig) {
        return new SimpleJobConfiguration(jobCoreConfig, jobClass.getCanonicalName());
    }
}
