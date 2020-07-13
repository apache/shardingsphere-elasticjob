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

package org.apache.shardingsphere.elasticjob.api.listener;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;

import java.io.Serializable;
import java.util.Map;

/**
 * Sharding contexts.
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class ShardingContexts implements Serializable {
    
    private static final long serialVersionUID = -4585977349142082152L;
    
    private final String taskId;
    
    private final String jobName;
    
    private final int shardingTotalCount;
    
    private final String jobParameter;
    
    private final Map<Integer, String> shardingItemParameters;
    
    private int jobEventSamplingCount;
    
    @Setter
    private int currentJobEventSamplingCount;
    
    @Setter
    private boolean allowSendJobEvent = true;
    
    public ShardingContexts(final String taskId, final String jobName, final int shardingTotalCount, final String jobParameter,
                            final Map<Integer, String> shardingItemParameters, final int jobEventSamplingCount) {
        this.taskId = taskId;
        this.jobName = jobName;
        this.shardingTotalCount = shardingTotalCount;
        this.jobParameter = jobParameter;
        this.shardingItemParameters = shardingItemParameters;
        this.jobEventSamplingCount = jobEventSamplingCount;
    }
    
    /**
     * Create sharding context.
     * 
     * @param shardingItem sharding item
     * @return sharding context
     */
    public ShardingContext createShardingContext(final int shardingItem) {
        return new ShardingContext(jobName, taskId, shardingTotalCount, jobParameter, shardingItem, shardingItemParameters.get(shardingItem));
    }
}
