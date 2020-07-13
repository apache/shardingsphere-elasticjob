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

package org.apache.shardingsphere.elasticjob.infra.handler.sharding;

import org.apache.shardingsphere.elasticjob.infra.spi.TypedSPI;

import java.util.List;
import java.util.Map;

/**
 * Job sharding sharding.
 */
public interface JobShardingStrategy extends TypedSPI {
    
    /**
     * Sharding job.
     * 
     * @param jobInstances all job instances which participate in sharding
     * @param jobName job name
     * @param shardingTotalCount sharding total count
     * @return sharding result
     */
    Map<JobInstance, List<Integer>> sharding(List<JobInstance> jobInstances, String jobName, int shardingTotalCount);
}
