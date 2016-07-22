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

package com.dangdang.ddframe.job.lite.spring.namespace.parser.dataflow;

import com.dangdang.ddframe.job.api.job.dataflow.DataflowElasticJob;
import com.dangdang.ddframe.job.api.job.dataflow.DataflowType;
import com.dangdang.ddframe.job.lite.api.config.impl.DataflowJobConfiguration;
import com.dangdang.ddframe.job.lite.api.config.impl.JobType;
import com.dangdang.ddframe.job.lite.spring.namespace.parser.common.AbstractJobConfigurationDto;
import lombok.Getter;
import lombok.Setter;

/**
 * 数据流作业配置命名空间对象.
 *
 * @author caohao
 */
@Getter
@Setter
final class DataflowJobConfigurationDto extends AbstractJobConfigurationDto<DataflowJobConfiguration, DataflowElasticJob, DataflowJobConfiguration.DataflowJobConfigurationBuilder> {
    
    private DataflowType dataflowType;
    
    private Integer processCountIntervalSeconds;
    
    private Integer concurrentDataProcessThreadCount;
    
    private Boolean streamingProcess;
    
    DataflowJobConfigurationDto(final String jobName, final Class<? extends DataflowElasticJob> jobClass, final Integer shardingTotalCount, final String cron, final DataflowType dataflowType) {
        super(jobName, JobType.DATA_FLOW, jobClass, shardingTotalCount, cron);
        this.dataflowType = dataflowType;
    }
    
    @Override
    public DataflowJobConfiguration toJobConfiguration() {
        return createBuilder().build();
    }
    
    @Override
    protected DataflowJobConfiguration.DataflowJobConfigurationBuilder createCustomizedBuilder() {
        return new DataflowJobConfiguration.DataflowJobConfigurationBuilder(getJobName(), getJobClass(), getShardingTotalCount(), getCron(), getDataflowType());
    }
    
    @Override
    protected DataflowJobConfiguration.DataflowJobConfigurationBuilder buildCustomizedProperties(final DataflowJobConfiguration.DataflowJobConfigurationBuilder builder) {
        if (null != getProcessCountIntervalSeconds()) {
            builder.processCountIntervalSeconds(getProcessCountIntervalSeconds());
        }
        if (null != getConcurrentDataProcessThreadCount()) {
            builder.concurrentDataProcessThreadCount(getConcurrentDataProcessThreadCount());
        }
        if (null != getStreamingProcess()) {
            builder.streamingProcess(getStreamingProcess());
        }
        return builder;
    }
}
