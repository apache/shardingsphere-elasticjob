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

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Job sharding sharding factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobShardingStrategyFactory {
    
    private static final Map<String, JobShardingStrategy> STRATEGIES = new LinkedHashMap<>();
    
    private static final String DEFAULT_STRATEGY = "AVG_ALLOCATION";
    
    static {
        for (JobShardingStrategy each : ServiceLoader.load(JobShardingStrategy.class)) {
            STRATEGIES.put(each.getType(), each);
        }
    }
    
    /**
     * Get job sharding sharding.
     * 
     * @param type job sharding sharding type
     * @return job sharding sharding
     */
    public static JobShardingStrategy getStrategy(final String type) {
        if (Strings.isNullOrEmpty(type)) {
            return STRATEGIES.get(DEFAULT_STRATEGY);
        }
        if (!STRATEGIES.containsKey(type)) {
            throw new JobConfigurationException("Can not find sharding sharding type '%s'.", type);
        }
        return STRATEGIES.get(type);
    }
}
