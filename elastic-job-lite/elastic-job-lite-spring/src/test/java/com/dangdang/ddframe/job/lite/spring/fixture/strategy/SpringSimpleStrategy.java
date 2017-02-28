package com.dangdang.ddframe.job.lite.spring.fixture.strategy;

import com.dangdang.ddframe.job.lite.api.strategy.JobShardingStrategy;
import com.dangdang.ddframe.job.lite.api.strategy.JobShardingStrategyOption;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author leizhenyu
 * 测试下spring加载分片类
 * 逻辑就是Average
 */
public class SpringSimpleStrategy implements JobShardingStrategy {
    @Override
    public Map<String, List<Integer>> sharding(List<String> serversList, JobShardingStrategyOption option) {
        if (serversList.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<Integer>> result = shardingAliquot(serversList, option.getShardingTotalCount());
        addAliquant(serversList, option.getShardingTotalCount(), result);
        return result;
    }


    private Map<String, List<Integer>> shardingAliquot(final List<String> serversList, final int shardingTotalCount) {
        Map<String, List<Integer>> result = new LinkedHashMap<>(serversList.size());
        int itemCountPerSharding = shardingTotalCount / serversList.size();
        int count = 0;
        for (String each : serversList) {
            List<Integer> shardingItems = new ArrayList<>(itemCountPerSharding + 1);
            for (int i = count * itemCountPerSharding; i < (count + 1) * itemCountPerSharding; i++) {
                shardingItems.add(i);
            }
            result.put(each, shardingItems);
            count++;
        }
        return result;
    }

    private void addAliquant(final List<String> serversList, final int shardingTotalCount, final Map<String, List<Integer>> shardingResult) {
        int aliquant = shardingTotalCount % serversList.size();
        int count = 0;
        for (Map.Entry<String, List<Integer>> entry : shardingResult.entrySet()) {
            if (count < aliquant) {
                entry.getValue().add(shardingTotalCount / serversList.size() * serversList.size() + count);
            }
            count++;
        }
    }
}
