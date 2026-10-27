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

package org.apache.shardingsphere.elasticjob.spring.boot.job;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.bootstrap.type.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.kernel.infra.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.reg.exception.RegException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.ContextClosedEvent;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ElasticJob startup runner.
 *
 * <p>Stops all scheduled job bootstraps when the application context closes.
 * Stopping in the lifecycle phase runs before the registry center beans are destroyed,
 * so no scheduled trigger fires against an already closed registry center.</p>
 *
 * <p>A regular {@code stop()} only pauses the triggers so that a subsequent
 * {@code start()} can resume them. Permanent cleanup happens on actual context
 * closure via {@link ContextClosedEvent} with {@link #destroy()} as fallback,
 * because a Quartz scheduler cannot be restarted once shut down.</p>
 */
@Setter
@Slf4j
public class ScheduleJobBootstrapStartupRunner implements CommandLineRunner, SmartLifecycle, ApplicationContextAware, ApplicationListener<ContextClosedEvent>, DisposableBean {
    
    private ApplicationContext applicationContext;
    
    private volatile boolean running;
    
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    
    @Override
    public void run(final String... args) {
        log.info("Starting ElasticJob Bootstrap.");
        getJobBootstraps().forEach(ScheduleJobBootstrap::schedule);
        running = true;
        log.info("ElasticJob Bootstrap started.");
    }
    
    @Override
    public void start() {
        if (shutdown.get()) {
            return;
        }
        getJobBootstraps().forEach(this::resumeQuietly);
        running = true;
    }
    
    @Override
    public void stop() {
        running = false;
        getJobBootstraps().forEach(this::pauseQuietly);
    }
    
    @Override
    public void stop(final Runnable callback) {
        stop();
        callback.run();
    }
    
    @Override
    public void onApplicationEvent(final ContextClosedEvent event) {
        if (null != applicationContext && event.getApplicationContext() == applicationContext) {
            shutdown();
        }
    }
    
    @Override
    public void destroy() {
        shutdown();
    }
    
    private void shutdown() {
        if (!shutdown.compareAndSet(false, true)) {
            return;
        }
        running = false;
        getJobBootstraps().forEach(this::shutdownQuietly);
    }
    
    private Collection<ScheduleJobBootstrap> getJobBootstraps() {
        return applicationContext.getBeansOfType(ScheduleJobBootstrap.class).values();
    }
    
    private void pauseQuietly(final ScheduleJobBootstrap jobBootstrap) {
        try {
            jobBootstrap.pause();
        } catch (final JobSystemException | RegException ex) {
            log.warn("Failed to pause scheduled job bootstrap.", ex);
        }
    }
    
    private void resumeQuietly(final ScheduleJobBootstrap jobBootstrap) {
        try {
            jobBootstrap.resume();
        } catch (final JobSystemException | RegException ex) {
            log.warn("Failed to resume scheduled job bootstrap.", ex);
        }
    }
    
    private void shutdownQuietly(final ScheduleJobBootstrap jobBootstrap) {
        try {
            jobBootstrap.shutdown();
        } catch (final JobSystemException | RegException ex) {
            log.warn("Failed to shut down scheduled job bootstrap.", ex);
        }
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
