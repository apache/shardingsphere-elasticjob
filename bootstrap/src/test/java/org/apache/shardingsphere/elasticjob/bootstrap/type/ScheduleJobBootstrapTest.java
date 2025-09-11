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

package org.apache.shardingsphere.elasticjob.bootstrap.type;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.core.JobRunShell;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@Slf4j
class ScheduleJobBootstrapTest {
    
    private TestingServer testingServer;
    
    private CoordinatorRegistryCenter regCenter;
    
    @BeforeEach
    void beforeEach() throws Exception {
        testingServer = new TestingServer();
        try (
                CuratorZookeeperClient client = new CuratorZookeeperClient(testingServer.getConnectString(),
                        60000, 500, null,
                        new ExponentialBackoffRetry(500, 3, 1500))) {
            client.start();
            Awaitility.await().atMost(Duration.ofSeconds(30L)).ignoreExceptions().until(client::isConnected);
        }
        regCenter = new ZookeeperRegistryCenter(new ZookeeperConfiguration(testingServer.getConnectString(), "elasticjob-test"));
        regCenter.init();
    }
    
    @AfterEach
    void afterEach() throws IOException {
        regCenter.close();
        testingServer.close();
    }
    
    @Test
    void testWhenShutdownThenTaskCanCaptureInterruptedException() {
        Logger jobRunShell = (Logger) LoggerFactory.getLogger(JobRunShell.class.getName());
        Logger errorLogger = (Logger) LoggerFactory.getLogger("org.quartz.core.ErrorLogger");
        Level originJobRunShellLevel = jobRunShell.getLevel();
        Level originErrorLoggerLevel = errorLogger.getLevel();
        try {
            jobRunShell.setLevel(Level.OFF);
            errorLogger.setLevel(Level.OFF);
            testCaptureInterruptedException(1);
            testCaptureInterruptedException(2);
        } finally {
            jobRunShell.setLevel(originJobRunShellLevel);
            errorLogger.setLevel(originErrorLoggerLevel);
        }
    }
    
    @SuppressWarnings({"InfiniteLoopStatement", "BusyWait"})
    private void testCaptureInterruptedException(final int shardingTotalCount) {
        String jobName = "testTaskCaptureInterruptedTask" + shardingTotalCount;
        AtomicBoolean captured = new AtomicBoolean(false);
        AtomicBoolean running = new AtomicBoolean(false);
        LocalTime magicTime = LocalTime.now().plusSeconds(2L);
        String cronExpression = String.format("%d %d %d * * ?", magicTime.getSecond(), magicTime.getMinute(), magicTime.getHour());
        SimpleJob job = shardingContext -> {
            try {
                running.set(true);
                while (true) {
                    Thread.sleep(100L);
                }
            } catch (final InterruptedException ex) {
                captured.set(true);
                Thread.currentThread().interrupt();
            }
        };
        ScheduleJobBootstrap bootstrap = new ScheduleJobBootstrap(regCenter, job, JobConfiguration.newBuilder(jobName, shardingTotalCount).cron(cronExpression).build());
        bootstrap.schedule();
        Awaitility.await().atMost(30L, TimeUnit.SECONDS).ignoreExceptions().until(running::get);
        bootstrap.shutdown();
        Awaitility.await().atMost(10L, TimeUnit.SECONDS).ignoreExceptions().until(captured::get);
    }
}
