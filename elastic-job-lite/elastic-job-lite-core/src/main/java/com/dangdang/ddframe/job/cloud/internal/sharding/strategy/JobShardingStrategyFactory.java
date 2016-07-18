package com.dangdang.ddframe.job.cloud.internal.sharding.strategy;

import com.dangdang.ddframe.job.cloud.plugin.sharding.strategy.AverageAllocationJobShardingStrategy;
import com.dangdang.ddframe.job.cloud.exception.JobShardingStrategyClassConfigurationException;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 作业分片策略工厂.
 * 
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobShardingStrategyFactory {
    
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
