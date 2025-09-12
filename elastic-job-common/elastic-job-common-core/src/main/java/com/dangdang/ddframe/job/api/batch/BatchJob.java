package com.dangdang.ddframe.job.api.batch;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.ShardingContext;

import java.util.List;
import java.util.Set;

/**
 * 批处理作业接口.
 *
 * @param <T> 数据类型
 */
public interface BatchJob<T> extends ElasticJob {

    /**
     * 获取数据.
     *
     * @param shardingContext 分片上下文
     * @param shardingItems 分片项集合
     * @return 数据列表
     */
    List<T> fetchData(ShardingContext shardingContext, Set<Integer> shardingItems);

    /**
     * 处理数据.
     *
     * @param shardingContext 分片上下文
     * @param data 数据列表
     */
    void processData(ShardingContext shardingContext, List<T> data);
}
