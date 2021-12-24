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
import org.apache.shardingsphere.elasticjob.infra.spi.ElasticJobServiceLoader;

/**
 * Job sharding strategy factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobShardingStrategyFactory {
    
    private static final String DEFAULT_STRATEGY = "AVG_ALLOCATION";
    
    static {
        ElasticJobServiceLoader.registerTypedService(JobShardingStrategy.class);
    }
    
    /**
     * Get job sharding strategy.
     * 
     * @param type job sharding strategy type
     * @return job sharding strategy
     */
    public static JobShardingStrategy getStrategy(final String type) {
        if (Strings.isNullOrEmpty(type)) {
            return ElasticJobServiceLoader.getCachedTypedServiceInstance(JobShardingStrategy.class, DEFAULT_STRATEGY).get();
        }
        return ElasticJobServiceLoader.getCachedTypedServiceInstance(JobShardingStrategy.class, type)
                .orElseThrow(() -> new JobConfigurationException("Cannot find sharding strategy using type '%s'.", type));
    }
}
