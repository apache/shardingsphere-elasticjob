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

import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
import com.dangdang.ddframe.job.lite.api.strategy.JobShardingStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 根据作业名的哈希值奇偶数决定IP升降序算法的分片策略.
 * 
 * <p>
 * 作业名的哈希值为奇数则IP升序.
 * 作业名的哈希值为偶数则IP降序.
 * 用于不同的作业平均分配负载至不同的服务器.
 * 如: 
 * 1. 如果有3台服务器, 分成2片, 作业名称的哈希值为奇数, 则每台服务器分到的分片是: 1=[0], 2=[1], 3=[].
 * 2. 如果有3台服务器, 分成2片, 作业名称的哈希值为偶数, 则每台服务器分到的分片是: 3=[0], 2=[1], 1=[].
 * </p>
 * 
 * @author zhangliang
 */
public final class OdevitySortByNameJobShardingStrategy implements JobShardingStrategy {
    
    private AverageAllocationJobShardingStrategy averageAllocationJobShardingStrategy = new AverageAllocationJobShardingStrategy();
    
    @Override
    public Map<JobInstance, List<Integer>> sharding(final List<JobInstance> jobInstances, final String jobName, final int shardingTotalCount) {
        long jobNameHash = jobName.hashCode();
        if (0 == jobNameHash % 2) {
            Collections.reverse(jobInstances);
        }
        return averageAllocationJobShardingStrategy.sharding(jobInstances, jobName, shardingTotalCount);
    }
}
