package com.dangdang.ddframe.job.plugin.sharding.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dangdang.ddframe.job.internal.sharding.strategy.JobShardingStrategy;
import com.dangdang.ddframe.job.internal.sharding.strategy.JobShardingStrategyOption;

/**
 * 根据作业名的哈希值对服务器列表进行轮转的分片策略.
 * 
 * @author weishubin
 */
public class RotateServerByNameJobShardingStrategy implements JobShardingStrategy {
    
    private AverageAllocationJobShardingStrategy averageAllocationJobShardingStrategy = new AverageAllocationJobShardingStrategy();
    
    @Override
    public Map<String, List<Integer>> sharding(final List<String> serversList, final JobShardingStrategyOption option) {
        return averageAllocationJobShardingStrategy.sharding(rotateServerList(serversList, option.getJobName()), option);
    }
    
    private List<String> rotateServerList(final List<String> serversList, final String jobName) {
        int serverSize = serversList.size();
        int offset = Math.abs(jobName.hashCode()) % serverSize;
        if (0 == offset) {
            return serversList;
        }
        List<String> result = new ArrayList<>(serverSize);
        for (int i = 0; i < serverSize; i++) {
            int index = (i + offset) % serverSize;
            result.add(serversList.get(index));
        }
        return result;
    }
}
