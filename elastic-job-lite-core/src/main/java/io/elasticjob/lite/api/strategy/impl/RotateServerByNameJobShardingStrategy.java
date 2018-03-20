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

package io.elasticjob.lite.api.strategy.impl;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.api.strategy.JobShardingStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 根据作业名的哈希值对服务器列表进行轮转的分片策略.
 * 
 * @author weishubin
 */
public final class RotateServerByNameJobShardingStrategy implements JobShardingStrategy {
    
    private AverageAllocationJobShardingStrategy averageAllocationJobShardingStrategy = new AverageAllocationJobShardingStrategy();
    
    @Override
    public Map<JobInstance, List<Integer>> sharding(final List<JobInstance> jobInstances, final String jobName, final int shardingTotalCount) {
        return averageAllocationJobShardingStrategy.sharding(rotateServerList(jobInstances, jobName), jobName, shardingTotalCount);
    }
    
    private List<JobInstance> rotateServerList(final List<JobInstance> shardingUnits, final String jobName) {
        int shardingUnitsSize = shardingUnits.size();
        int offset = Math.abs(jobName.hashCode()) % shardingUnitsSize;
        if (0 == offset) {
            return shardingUnits;
        }
        List<JobInstance> result = new ArrayList<>(shardingUnitsSize);
        for (int i = 0; i < shardingUnitsSize; i++) {
            int index = (i + offset) % shardingUnitsSize;
            result.add(shardingUnits.get(index));
        }
        return result;
    }
}
