package com.dangdang.ddframe.job.lite.internal.sharding.strategy.fixture;

import java.util.List;
import java.util.Map;

import com.dangdang.ddframe.job.lite.internal.sharding.strategy.JobShardingStrategy;
import com.dangdang.ddframe.job.lite.internal.sharding.strategy.JobShardingStrategyOption;

public class InvalidJobShardingStrategy implements JobShardingStrategy {
    
    public InvalidJobShardingStrategy(final String input) {
    }
    
    @Override
    public Map<String, List<Integer>> sharding(final List<String> serversList, final JobShardingStrategyOption option) {
        return null;
    }
}
