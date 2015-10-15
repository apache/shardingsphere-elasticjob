package com.dangdang.ddframe.job.plugin.sharding.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dangdang.ddframe.job.internal.sharding.strategy.JobShardingStrategy;
import com.dangdang.ddframe.job.internal.sharding.strategy.JobShardingStrategyOption;

/**
 * 根据作业名的哈希值对服务器列表进行轮转的分片策略.
 * 
 * <p>
 * 可实现一定的服务器负载均衡
 * </p>
 * 
 * @author weishubin
 */
public class RotateServerByNameJobShardingStrategy implements JobShardingStrategy {
private AverageAllocationJobShardingStrategy averageAllocationJobShardingStrategy = new AverageAllocationJobShardingStrategy();
    
    @Override
    public Map<String, List<Integer>> sharding(final List<String> serversList, final JobShardingStrategyOption option) {
    	List<String> roated = rotateServerList(serversList, option.getJobName());
        return averageAllocationJobShardingStrategy.sharding(roated, option);
    }
    
    private List<String> rotateServerList(List<String> serversList, String jobName) {
    	int serverSize = serversList.size();
    	long jobNameHash = jobName.hashCode();
    	int offset = (int) (jobNameHash % serverSize);
    	if (offset == 0) {
    		return serversList;
    	}
    	List<String> result = new ArrayList<String>(serverSize);
    	for (int i = 0; i < serverSize; i++) {
    		int index = (i + offset) % serverSize;
    		result.add(serversList.get(index));
    	}
    	return result;
    }
}
