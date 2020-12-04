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

package org.apache.shardingsphere.elasticjob.restful;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.elasticjob.restful.handler.ExceptionHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Configuration for {@link NettyRestfulService}.
 */
@Getter
@RequiredArgsConstructor
public final class NettyRestfulServiceConfiguration {
    
    private final int port;
    
    @Setter
    private String host;
    
    /**
     * If trailing slash sensitive, <code>/foo/bar</code> is not equals to <code>/foo/bar/</code>.
     */
    @Setter
    private boolean trailingSlashSensitive;
    
    private final List<Filter> filterInstances = new LinkedList<>();
    
    private final List<RestfulController> controllerInstances = new LinkedList<>();
    
    private final Map<Class<? extends Throwable>, ExceptionHandler<? extends Throwable>> exceptionHandlers = new HashMap<>();
    
    /**
     * Add instances of {@link Filter}.
     *
     * @param instances instances of Filter
     */
    public void addFilterInstances(final Filter... instances) {
        filterInstances.addAll(Arrays.asList(instances));
    }
    
    /**
     * Add instances of RestfulController.
     *
     * @param instances instances of RestfulController
     */
    public void addControllerInstances(final RestfulController... instances) {
        controllerInstances.addAll(Arrays.asList(instances));
    }
    
    /**
     * Add an instance of ExceptionHandler for specific exception.
     *
     * @param exceptionType    The type of exception to handle
     * @param exceptionHandler Instance of ExceptionHandler
     * @param <E>              The type of exception to handle
     */
    public <E extends Throwable> void addExceptionHandler(final Class<E> exceptionType, final ExceptionHandler<E> exceptionHandler) {
        Preconditions.checkState(!exceptionHandlers.containsKey(exceptionType), "ExceptionHandler for %s has already existed.", exceptionType.getName());
        exceptionHandlers.put(exceptionType, exceptionHandler);
    }
}
