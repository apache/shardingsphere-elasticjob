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

package org.apache.shardingsphere.elasticjob.infra.handler.threadpool;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Job executor service handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobExecutorServiceHandlerFactory {
    
    private static final Map<String, JobExecutorServiceHandler> HANDLERS = new LinkedHashMap<>();
    
    private static final String DEFAULT_HANDLER = "CPU";
    
    static {
        for (JobExecutorServiceHandler each : ServiceLoader.load(JobExecutorServiceHandler.class)) {
            HANDLERS.put(each.getType(), each);
        }
    }
    
    /**
     * Get job executor service handler.
     *
     * @param type executor service handler type
     * @return executor service handler
     */
    public static JobExecutorServiceHandler getHandler(final String type) {
        if (Strings.isNullOrEmpty(type)) {
            return HANDLERS.get(DEFAULT_HANDLER);
        }
        if (!HANDLERS.containsKey(type)) {
            throw new JobConfigurationException("Can not find executor service handler type '%s'.", type);
        }
        return HANDLERS.get(type);
    }
}
