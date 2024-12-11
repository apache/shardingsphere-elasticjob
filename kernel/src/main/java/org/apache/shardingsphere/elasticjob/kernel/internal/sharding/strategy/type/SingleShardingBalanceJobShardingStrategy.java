/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.kernel.internal.sharding.strategy.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.strategy.JobShardingStrategy;

/**
 * Single sharding Balance strategy, referenced of ROUND_ROBIN strategy.
 * <pre>
 * it resolves the problem which ROUND_ROBIN is stick with the certain one job instance
 * for the hashcode of job name is a constant value. while with SINGLE_SHARDING_BALANCE, it allows
 * the job running on all the job instances each one by one, just like loop the job instances.
 *
 * this is the real round robin balance job running in the job instance dimension.
 * </pre>
 *
 */
public class SingleShardingBalanceJobShardingStrategy implements JobShardingStrategy {
    
    private final AverageAllocationJobShardingStrategy averageAllocationJobShardingStrategy = new AverageAllocationJobShardingStrategy();
    
    @Override
    public Map<JobInstance, List<Integer>> sharding(final List<JobInstance> jobInstances, final String jobName, final int shardingTotalCount) {
        int shardingUnitsSize = jobInstances.size();
        int offset = Math.abs(jobName.hashCode() + ((Long) System.currentTimeMillis()).intValue()) % shardingUnitsSize;
        
        List<JobInstance> result = new ArrayList<>(shardingUnitsSize);
        for (int i = 0; i < shardingUnitsSize; i++) {
            int index = (i + offset) % shardingUnitsSize;
            result.add(jobInstances.get(index));
        }
        
        return averageAllocationJobShardingStrategy.sharding(result, jobName, shardingTotalCount);
    }
    
    @Override
    public String getType() {
        return "SINGLE_SHARDING_BALANCE";
    }
    
}
