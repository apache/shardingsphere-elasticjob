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

package org.apache.shardingsphere.elasticjob.tracing.listener;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.apache.shardingsphere.elasticjob.tracing.exception.TracingConfigurationException;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Tracing listener factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TracingListenerFactory {
    
    private static final Map<String, TracingListenerConfiguration> LISTENER_CONFIGS = new HashMap<>();
    
    static {
        for (TracingListenerConfiguration each : ServiceLoader.load(TracingListenerConfiguration.class)) {
            LISTENER_CONFIGS.put(each.getType(), each);
        }
    }
    
    /**
     * Get tracing listener.
     *
     * @param tracingConfig tracing configuration
     * @return tracing listener
     * @throws TracingConfigurationException tracing configuration exception
     */
    @SuppressWarnings("unchecked")
    public static TracingListener getListener(final TracingConfiguration tracingConfig) throws TracingConfigurationException {
        if (Strings.isNullOrEmpty(tracingConfig.getType()) || !LISTENER_CONFIGS.containsKey(tracingConfig.getType())) {
            throw new TracingConfigurationException(String.format("Can not find executor service handler type '%s'.", tracingConfig.getType()));
        }
        return LISTENER_CONFIGS.get(tracingConfig.getType()).createTracingListener(tracingConfig.getStorage());
    }
}
