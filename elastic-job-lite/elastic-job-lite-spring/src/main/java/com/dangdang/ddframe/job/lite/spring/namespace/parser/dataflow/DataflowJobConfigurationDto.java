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

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.api.JobCoreConfiguration;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJob;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.namespace.parser.common.AbstractJobConfigurationDto;

/**
 * 数据流作业配置命名空间对象.
 *
 * @author caohao
 * @author zhangliang
 */
final class DataflowJobConfigurationDto extends AbstractJobConfigurationDto {
    
    private final Class<? extends DataflowJob> jobClass;
    
    private final DataflowJobConfiguration.DataflowType dataflowType;
    
    private final Boolean streamingProcess;
    
    private final Integer concurrentDataProcessThreadCount;
    
    DataflowJobConfigurationDto(final String jobName, final String cron, final int shardingTotalCount, final Class<? extends DataflowJob> jobClass, 
                                       final DataflowJobConfiguration.DataflowType dataflowType, final Boolean streamingProcess, final Integer concurrentDataProcessThreadCount) {
        super(jobName, cron, shardingTotalCount);
        this.jobClass = jobClass;
        this.dataflowType = dataflowType;
        this.streamingProcess = streamingProcess;
        this.concurrentDataProcessThreadCount = concurrentDataProcessThreadCount;
    }
    
    @Override
    protected JobConfiguration toJobConfiguration(final JobCoreConfiguration jobCoreConfig) {
        return null == concurrentDataProcessThreadCount
                ? new DataflowJobConfiguration(jobCoreConfig, jobClass, dataflowType, null == streamingProcess ? false : streamingProcess)
                : new DataflowJobConfiguration(jobCoreConfig, jobClass, dataflowType, null == streamingProcess ? false : streamingProcess, concurrentDataProcessThreadCount);
    }
}
