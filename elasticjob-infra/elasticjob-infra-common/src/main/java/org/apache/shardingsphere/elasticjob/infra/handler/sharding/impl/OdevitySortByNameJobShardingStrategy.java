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

package org.apache.shardingsphere.elasticjob.infra.handler.sharding.impl;

import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobShardingStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Sharding sharding which for hash with job name to determine IP asc or desc.
 * 
 * <p>
 * IP address asc if job name' hashcode is odd;
 * IP address desc if job name' hashcode is even.
 * Used to average assign to job server.
 * 
 * For example: 
 * 1. If there are 3 job servers with 2 sharding item, and the hash value of job name is odd, then each server is divided into: 1 = [0], 2 = [1], 3 = [];
 * 2. If there are 3 job servers with 2 sharding item, and the hash value of job name is even, then each server is divided into: 3 = [0], 2 = [1], 1 = [].
 * </p>
 */
public final class OdevitySortByNameJobShardingStrategy implements JobShardingStrategy {
    
    private final AverageAllocationJobShardingStrategy averageAllocationJobShardingStrategy = new AverageAllocationJobShardingStrategy();
    
    @Override
    public Map<JobInstance, List<Integer>> sharding(final List<JobInstance> jobInstances, final String jobName, final int shardingTotalCount) {
        long jobNameHash = jobName.hashCode();
        if (0 == jobNameHash % 2) {
            Collections.reverse(jobInstances);
        }
        return averageAllocationJobShardingStrategy.sharding(jobInstances, jobName, shardingTotalCount);
    }
    
    @Override
    public String getType() {
        return "ODEVITY";
    }
}
