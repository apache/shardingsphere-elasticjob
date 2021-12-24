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

package org.apache.shardingsphere.elasticjob.executor.item;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.executor.item.impl.ClassedJobItemExecutor;
import org.apache.shardingsphere.elasticjob.executor.item.impl.TypedJobItemExecutor;
import org.apache.shardingsphere.elasticjob.infra.spi.ElasticJobServiceLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;

/**
 * Job item executor factory.
 */
@SuppressWarnings("rawtypes")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobItemExecutorFactory {
    
    private static final Map<Class, ClassedJobItemExecutor> CLASSED_EXECUTORS = new HashMap<>();
    
    static {
        ElasticJobServiceLoader.registerTypedService(TypedJobItemExecutor.class);
        ServiceLoader.load(ClassedJobItemExecutor.class).forEach(each -> CLASSED_EXECUTORS.put(each.getElasticJobClass(), each));
    }
    
    /**
     * Get executor.
     * 
     * @param elasticJobClass elastic job class
     * @return job item executor
     */
    @SuppressWarnings("unchecked")
    public static JobItemExecutor getExecutor(final Class<? extends ElasticJob> elasticJobClass) {
        for (Entry<Class, ClassedJobItemExecutor> entry : CLASSED_EXECUTORS.entrySet()) {
            if (entry.getKey().isAssignableFrom(elasticJobClass)) {
                return entry.getValue();
            }
        }
        throw new JobConfigurationException("Can not find executor for elastic job class `%s`", elasticJobClass.getName());
    }
    
    /**
     * Get executor.
     *
     * @param elasticJobType elastic job type
     * @return job item executor
     */
    public static JobItemExecutor getExecutor(final String elasticJobType) {
        return ElasticJobServiceLoader.getCachedTypedServiceInstance(TypedJobItemExecutor.class, elasticJobType)
                .orElseThrow(() -> new JobConfigurationException("Cannot find executor for elastic job type `%s`", elasticJobType));
    }
}
