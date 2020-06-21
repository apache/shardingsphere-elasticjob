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

package org.apache.shardingsphere.elasticjob.lite.api.strategy;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.lite.exception.JobConfigurationException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Job sharding strategy factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobShardingStrategyFactory {
    
    private static final Map<String, JobShardingStrategy> JOB_SHARDING_STRATEGIES = new LinkedHashMap<>();
    
    static {
        for (JobShardingStrategy each : ServiceLoader.load(JobShardingStrategy.class)) {
            JOB_SHARDING_STRATEGIES.put(each.getType(), each);
        }
    }
    
    /**
     * Get job sharding strategy.
     * 
     * @param type job sharding strategy type
     * @return Job sharding strategy
     */
    public static JobShardingStrategy getStrategy(final String type) {
        if (Strings.isNullOrEmpty(type)) {
            return JOB_SHARDING_STRATEGIES.values().iterator().next();
        }
        if (!JOB_SHARDING_STRATEGIES.containsKey(type)) {
            throw new JobConfigurationException("Can not find sharding strategy type '%s'.", type);
        }
        return JOB_SHARDING_STRATEGIES.get(type);
    }
}
