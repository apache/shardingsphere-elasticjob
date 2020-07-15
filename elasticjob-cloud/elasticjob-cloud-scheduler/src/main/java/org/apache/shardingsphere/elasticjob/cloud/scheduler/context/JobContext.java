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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.context;

import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.context.ExecutionType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Job running context.
 */
@RequiredArgsConstructor
@Getter
public final class JobContext {
    
    private final CloudJobConfiguration cloudJobConfig;
    
    private final List<Integer> assignedShardingItems;
    
    private final ExecutionType type;
    
    /**
     * Create job running context from job configuration and execution type.
     *
     * @param cloudJobConfig cloud job configuration
     * @param type execution type
     * @return Job running context
     */
    public static JobContext from(final CloudJobConfiguration cloudJobConfig, final ExecutionType type) {
        int shardingTotalCount = cloudJobConfig.getJobConfig().getShardingTotalCount();
        List<Integer> shardingItems = new ArrayList<>(shardingTotalCount);
        for (int i = 0; i < shardingTotalCount; i++) {
            shardingItems.add(i);
        }
        return new JobContext(cloudJobConfig, shardingItems, type);
    }
}
