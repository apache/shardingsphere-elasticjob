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

package com.dangdang.ddframe.job.lite.api.strategy.impl;

import com.dangdang.ddframe.job.lite.api.strategy.JobShardingStrategy;
import com.dangdang.ddframe.job.lite.api.strategy.JobShardingStrategyOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 基于平均分配算法的分片策略.
 * 
 * <p>
 * 如果分片不能整除, 则不能整除的多余分片将依次追加到序号小的服务器.
 * 如: 
 * 1. 如果有3台服务器, 分成9片, 则每台服务器分到的分片是: 1=[0,1,2], 2=[3,4,5], 3=[6,7,8].
 * 2. 如果有3台服务器, 分成8片, 则每台服务器分到的分片是: 1=[0,1,6], 2=[2,3,7], 3=[4,5].
 * 3. 如果有3台服务器, 分成10片, 则每台服务器分到的分片是: 1=[0,1,2,9], 2=[3,4,5], 3=[6,7,8].
 * </p>
 * 
 * @author zhangliang
 */
public final class AverageAllocationJobShardingStrategy implements JobShardingStrategy {
    
    @Override
    public Map<String, List<Integer>> sharding(final List<String> serversList, final JobShardingStrategyOption option) {
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
        for (Entry<String, List<Integer>> entry : shardingResult.entrySet()) {
            if (count < aliquant) {
                entry.getValue().add(shardingTotalCount / serversList.size() * serversList.size() + count);
            }
            count++;
        }
    }
}
