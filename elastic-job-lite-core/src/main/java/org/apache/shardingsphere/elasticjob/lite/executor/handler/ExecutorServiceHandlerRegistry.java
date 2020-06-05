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

package org.apache.shardingsphere.elasticjob.lite.executor.handler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Executor service handler registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExecutorServiceHandlerRegistry {
    
    private static final Map<String, ExecutorService> REGISTRY = new HashMap<>();
    
    /**
     * Get executor service handler.
     * 
     * @param jobName job name
     * @param executorServiceHandler executor service handler
     * @return executor service
     */
    public static synchronized ExecutorService getExecutorServiceHandler(final String jobName, final ExecutorServiceHandler executorServiceHandler) {
        if (!REGISTRY.containsKey(jobName)) {
            REGISTRY.put(jobName, executorServiceHandler.createExecutorService(jobName));
        }
        return REGISTRY.get(jobName);
    }
    
    /**
     * Remove executor service from executor service handler registry.
     *
     * @param jobName job name
     */
    public static synchronized void remove(final String jobName) {
        REGISTRY.remove(jobName);
    }
}
