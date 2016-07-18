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
 */

package com.dangdang.ddframe.job.cloud.internal.job.dataflow;

import java.util.List;

import com.dangdang.ddframe.job.cloud.internal.statistics.ProcessCountStatistics;
import com.dangdang.ddframe.job.cloud.exception.JobException;
import com.dangdang.ddframe.job.cloud.internal.job.AbstractJobExecutionShardingContext;

/**
 * 用于批量处理数据流程的作业抽象类.
 * 
 * @author zhangliang
 * 
 * @param <T> 数据流作业处理的数据实体类型
 * @param <C> 作业运行时分片上下文类型
 */
public abstract class AbstractBatchDataFlowElasticJob<T, C extends AbstractJobExecutionShardingContext> extends AbstractDataFlowElasticJob<T, C> implements BatchProcessable<T, C> {
    
    @Override
    protected final void processDataWithStatistics(final C shardingContext, final List<T> data) {
        int successCount = 0;
        try {
            successCount = processData(shardingContext, data);
        // CHECKSTYLE:OFF
        } catch (final Exception ex) {
        // CHECKSTYLE:ON
            throw new JobException(ex);
        } finally {
            ProcessCountStatistics.incrementProcessSuccessCount(shardingContext.getJobName(), successCount);
            ProcessCountStatistics.incrementProcessFailureCount(shardingContext.getJobName(), data.size() - successCount);
        }
    }
}
