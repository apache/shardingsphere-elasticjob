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
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;

/**
 * ElasticJob startup runner.
 *
 * <p>Shuts down all scheduled job bootstraps when the application context closes.
 * Stopping in the lifecycle phase runs before the registry center beans are destroyed,
 * so no scheduled trigger fires against an already closed registry center.</p>
 */
@Setter
@Slf4j
public class ScheduleJobBootstrapStartupRunner implements CommandLineRunner, SmartLifecycle, ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    private volatile boolean running;
    
    @Override
    public void run(final String... args) {
        log.info("Starting ElasticJob Bootstrap.");
        applicationContext.getBeansOfType(ScheduleJobBootstrap.class).values().forEach(ScheduleJobBootstrap::schedule);
        log.info("ElasticJob Bootstrap started.");
    }
    
    @Override
    public void start() {
        running = true;
    }
    
    @Override
    public void stop() {
        running = false;
        applicationContext.getBeansOfType(ScheduleJobBootstrap.class).values().forEach(this::shutdownQuietly);
    }
    
    @Override
    public void stop(final Runnable callback) {
        stop();
        callback.run();
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
