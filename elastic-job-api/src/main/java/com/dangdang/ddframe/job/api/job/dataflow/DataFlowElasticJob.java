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

package com.dangdang.ddframe.job.api.job.dataflow;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.ShardingContext;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * 保用于处理数据流程的作业接口.
 * 
 * @author zhangliang
 * 
 * @param <T> 数据流作业处理的数据实体类型
 */
public interface DataflowElasticJob<T> extends ElasticJob {
    
    /**
     * 获取待处理的数据.
     * 
     * @param shardingContext 作业分片规则配置上下文
     * @return 待处理的数据集合
     */
    List<T> fetchData(final ShardingContext shardingContext);
    
    /**
     * 处理数据.
     *
     * @param shardingContext 作业分片规则配置上下文
     * @param data 待处理的数据集合
     */
    void processData(final ShardingContext shardingContext, final List<T> data);
    
    /**
     * 更新数据处理位置.
     * 
     * @param item 分片项
     * @param offset 数据处理位置
     */
    void updateOffset(final int item, final String offset);

    /**
     * 获取线程执行服务.
     */
    ExecutorService getExecutorService();
}
