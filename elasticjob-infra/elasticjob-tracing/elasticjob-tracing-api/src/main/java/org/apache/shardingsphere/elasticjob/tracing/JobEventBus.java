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

package org.apache.shardingsphere.elasticjob.tracing;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.apache.shardingsphere.elasticjob.tracing.event.JobEvent;
import org.apache.shardingsphere.elasticjob.tracing.exception.TracingConfigurationException;
import org.apache.shardingsphere.elasticjob.tracing.listener.TracingListenerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Job event bus.
 */
@Slf4j
public final class JobEventBus {
    
    private final ExecutorService executorService;
    
    private final EventBus eventBus;
    
    private volatile boolean isRegistered;
    
    public JobEventBus() {
        executorService = null;
        eventBus = null;
    }
    
    public JobEventBus(final TracingConfiguration tracingConfig) {
        executorService = createExecutorService(Runtime.getRuntime().availableProcessors() * 2);
        eventBus = new AsyncEventBus(executorService);
        register(tracingConfig);
    }
    
    private ExecutorService createExecutorService(final int threadSize) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(threadSize, threadSize, 5L, TimeUnit.MINUTES, 
                new LinkedBlockingQueue<>(), new BasicThreadFactory.Builder().namingPattern(String.join("-", "job-event", "%s")).build());
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        return MoreExecutors.listeningDecorator(MoreExecutors.getExitingExecutorService(threadPoolExecutor));
    }
    
    private void register(final TracingConfiguration tracingConfig) {
        try {
            eventBus.register(TracingListenerFactory.getListener(tracingConfig));
            isRegistered = true;
        } catch (final TracingConfigurationException ex) {
            log.error("Elastic job: create tracing listener failure, error is: ", ex);
        }
    }
    
    /**
     * Post event.
     *
     * @param event job event
     */
    public void post(final JobEvent event) {
        if (isRegistered && !executorService.isShutdown()) {
            eventBus.post(event);
        }
    }
}
