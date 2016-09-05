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
import java.util.List;
import java.util.Map;

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
