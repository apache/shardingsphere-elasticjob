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

package com.dangdang.ddframe.job.internal.job.dataflow;

import java.util.List;

import com.dangdang.ddframe.job.internal.job.AbstractJobExecutionShardingContext;

/**
 * 批量处理数据的接口.
 * 
 * @author zhangliang
 *
 * @param <T> 数据流作业处理的数据实体类型
 * @param <C> 作业运行时分片上下文类型
 */
public interface BatchProcessable<T, C extends AbstractJobExecutionShardingContext> {
    
    /**
     * 批量处理数据.
     * 
     * @param shardingContext 作业分片规则配置上下文
     * @param data 待处理的数据集合
     * @return 成功处理的数据数
     */
    int processData(final C shardingContext, final List<T> data);
}
