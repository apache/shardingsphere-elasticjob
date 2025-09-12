package com.dangdang.ddframe.job.api.batch;

import com.dangdang.ddframe.job.api.ElasticJob;

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
     * @param shardingItems 分片项
     * @return 数据列表
     */
    List<T> fetchData(Set<Integer> shardingItems);

    /**
     * 处理数据.
     *
     * @param data 数据
     */
    void processData(List<T> data);
}
