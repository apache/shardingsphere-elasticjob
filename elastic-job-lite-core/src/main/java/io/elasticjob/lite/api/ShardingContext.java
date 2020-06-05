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

package io.elasticjob.lite.api;

import io.elasticjob.lite.executor.ShardingContexts;
import lombok.Getter;
import lombok.ToString;

/**
 * Sharding context.
 */
@Getter
@ToString
public final class ShardingContext {
    
    /**
     * job name.
     */
    private final String jobName;
    
    /**
     * task ID.
     */
    private final String taskId;
    
    /**
     * sharding total count.
     */
    private final int shardingTotalCount;
    
    /**
     * job parameter.
     * 
     * <p>Can configure for same job class, but use different parameter for different job schedule instance.</p>
     * 
     */
    private final String jobParameter;
    
    /**
     * Sharding item assigned for this sharding.
     */
    private final int shardingItem;
    
    /**
     * Sharding parameter assigned for this sharding.
     */
    private final String shardingParameter;
    
    public ShardingContext(final ShardingContexts shardingContexts, final int shardingItem) {
        jobName = shardingContexts.getJobName();
        taskId = shardingContexts.getTaskId();
        shardingTotalCount = shardingContexts.getShardingTotalCount();
        jobParameter = shardingContexts.getJobParameter();
        this.shardingItem = shardingItem;
        shardingParameter = shardingContexts.getShardingItemParameters().get(shardingItem);
    }
}
