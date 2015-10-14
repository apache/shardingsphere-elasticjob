package com.dangdang.ddframe.job.internal.sharding.strategy;

import com.dangdang.ddframe.job.exception.JobShardingStrategyClassConfigurationException;
import com.dangdang.ddframe.job.plugin.sharding.strategy.AverageAllocationJobShardingStrategy;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * 作业分片策略工厂.
 * 
 * @author zhangliang
 */
public final class JobShardingStrategyFactory {
    
    private JobShardingStrategyFactory() {
    }
    
    /**
     * 获取 作业分片策略实例.
     * 
     * @param jobShardingStrategyClassName 作业分片策略类名
     * @return 作业分片策略实例
     */
    public static JobShardingStrategy getStrategy(final String jobShardingStrategyClassName) {
        if (Strings.isNullOrEmpty(jobShardingStrategyClassName)) {
            return new AverageAllocationJobShardingStrategy();
        }
        try {
            Class<?> jobShardingStrategyClass = Class.forName(jobShardingStrategyClassName);
            Preconditions.checkState(JobShardingStrategy.class.isAssignableFrom(jobShardingStrategyClass), String.format("Class [%s] is not job strategy class", jobShardingStrategyClassName));
            return (JobShardingStrategy) jobShardingStrategyClass.newInstance();
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new JobShardingStrategyClassConfigurationException(ex);
        }
    }
}
