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

package org.apache.shardingsphere.elasticjob.cloud.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
    
    /**
     * task ID.
     */
    private final String taskId;
    
    /**
     * job name.
     */
    private final String jobName;

    /**
     * sharding total count.
     */
    private final int shardingTotalCount;

    /**
     * Job parameter.
     * Can configure multiple identical jobs, but use different parameters as different scheduling instances
     */
    private final String jobParameter;

    /**
     * Sharding items and parameters map.
     */
    private final Map<Integer, String> shardingItemParameters;

    /**
     * Job event sampling count.
     */
    private int jobEventSamplingCount;

    /**
     * Current job event sampling count.
     */
    @Setter
    private int currentJobEventSamplingCount;
    
    /**
     * Whether allow send job event.
     */
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
}
