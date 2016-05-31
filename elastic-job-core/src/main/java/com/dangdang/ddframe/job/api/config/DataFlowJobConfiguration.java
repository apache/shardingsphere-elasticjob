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

package com.dangdang.ddframe.job.api.config;

import com.dangdang.ddframe.job.api.DataFlowElasticJob;
import com.dangdang.ddframe.job.internal.job.JobType;
import lombok.Getter;
import lombok.Setter;

/**
 * 数据流作业配置信息.
 * 
 * @author caohao
 */
@Getter
@Setter
public final class DataFlowJobConfiguration<T extends DataFlowElasticJob> extends BaseJobConfiguration<T> {
    
    private final JobType jobType = JobType.DATA_FLOW;

    private int processCountIntervalSeconds = 300;
    
    private int fetchDataCount = 1;
    
    private int concurrentDataProcessThreadCount = 1;
    
    public DataFlowJobConfiguration(final String jobName, final Class<T> jobClass, final int shardingTotalCount, final String cron) {
        super(jobName, jobClass, shardingTotalCount, cron);
    }
}
