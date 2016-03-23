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

package com.dangdang.ddframe.job.plugin.job.type.dataflow;

import com.dangdang.ddframe.job.api.JobExecutionSingleShardingContext;
import com.dangdang.ddframe.job.internal.job.dataflow.AbstractBatchDataFlowElasticJob;

/**
 * 保证同一分片顺序性的批量处理数据流程的作业.
 * 
 * @author zhangliang
 *
 * @param <T> 数据流作业处理的数据实体类型
 */
public abstract class AbstractBatchSequenceDataFlowElasticJob<T> extends AbstractBatchDataFlowElasticJob<T, JobExecutionSingleShardingContext> {
}
