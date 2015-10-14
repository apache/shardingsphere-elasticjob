package com.dangdang.ddframe.job.internal.sharding.strategy;

import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 作业分片策略选项.
 * 
 * @author zhangliang
 */
@Getter
@RequiredArgsConstructor
public final class JobShardingStrategyOption {
    
    /**
     * 作业名称.
     */
    private final String jobName;
    
    /**
     * 作业分片总数.
     */
    private final int shardingTotalCount;
    
    /**
     * 分片序列号和个性化参数对照表.
     */
    private final Map<Integer, String> shardingItemParameters;
}
