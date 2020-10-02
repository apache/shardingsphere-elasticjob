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

package org.apache.shardingsphere.elasticjob.error.handler;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.infra.spi.ElasticJobServiceLoader;

import java.util.Optional;

/**
 * Job error handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobErrorHandlerFactory {
    
    private static final String DEFAULT_HANDLER = "LOG";
    
    static {
        ElasticJobServiceLoader.register(JobErrorHandler.class);
    }
    
    /**
     * Get job error handler.
     *
     * @param type job error handler type
     * @return job error handler
     */
    public static Optional<JobErrorHandler> createHandler(final String type) {
        if (Strings.isNullOrEmpty(type)) {
            return newHandlerInstance(DEFAULT_HANDLER);
        }
        return newHandlerInstance(type);
    }
    
    private static Optional<JobErrorHandler> newHandlerInstance(final String type) {
        return ElasticJobServiceLoader.newServiceInstances(JobErrorHandler.class).stream().filter(handler -> handler.getType().equalsIgnoreCase(type)).findFirst();
    }
}
