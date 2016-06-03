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

package com.dangdang.ddframe.job.spring.namespace.parser.dataflow;

import com.dangdang.ddframe.job.api.DataFlowElasticJob;
import com.dangdang.ddframe.job.api.config.impl.DataFlowJobConfiguration;
import com.dangdang.ddframe.job.api.config.impl.DataFlowJobConfiguration.DataFlowJobConfigurationBuilder;
import com.dangdang.ddframe.job.internal.job.JobType;
import com.dangdang.ddframe.job.spring.namespace.parser.common.AbstractJobConfigurationDto;
import lombok.Getter;
import lombok.Setter;

/**
 * 数据流作业配置命名空间对象.
 *
 * @author caohao
 */
@Getter
@Setter
final class DataFlowJobConfigurationDto extends AbstractJobConfigurationDto<DataFlowJobConfiguration, DataFlowElasticJob, DataFlowJobConfigurationBuilder> {
    
    private Integer processCountIntervalSeconds;
    
    private Integer fetchDataCount;
    
    private Integer concurrentDataProcessThreadCount;
    
    DataFlowJobConfigurationDto(final String jobName, final Class<? extends DataFlowElasticJob> jobClass, final Integer shardingTotalCount, final String cron) {
        super(jobName, JobType.DATA_FLOW, jobClass, shardingTotalCount, cron);
    }
    
    @Override
    public DataFlowJobConfiguration toJobConfiguration() {
        return createBuilder().build();
    }
    
    @Override
    protected DataFlowJobConfigurationBuilder createCustomizedBuilder() {
        return new DataFlowJobConfigurationBuilder(getJobName(), getJobClass(), getShardingTotalCount(), getCron());
    }
    
    @Override
    protected DataFlowJobConfigurationBuilder buildCustomizedProperties(final DataFlowJobConfigurationBuilder builder) {
        if (null != getProcessCountIntervalSeconds()) {
            builder.processCountIntervalSeconds(getProcessCountIntervalSeconds());
        }
        if (null != getFetchDataCount()) {
            builder.fetchDataCount(getFetchDataCount());
        }
        if (null != getConcurrentDataProcessThreadCount()) {
            builder.concurrentDataProcessThreadCount(getConcurrentDataProcessThreadCount());
        }
        return builder;
    }
}
