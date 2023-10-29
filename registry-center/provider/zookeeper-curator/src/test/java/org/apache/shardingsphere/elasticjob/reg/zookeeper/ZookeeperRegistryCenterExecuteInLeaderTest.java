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

package org.apache.shardingsphere.elasticjob.reg.zookeeper;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.reg.base.LeaderExecutionCallback;
import org.apache.shardingsphere.elasticjob.test.util.EmbedTestingServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

class ZookeeperRegistryCenterExecuteInLeaderTest {
    
    private static final EmbedTestingServer EMBED_TESTING_SERVER = new EmbedTestingServer(9181);
    
    private static final ZookeeperConfiguration ZOOKEEPER_CONFIGURATION =
            new ZookeeperConfiguration(EMBED_TESTING_SERVER.getConnectionString(), ZookeeperRegistryCenterExecuteInLeaderTest.class.getName());
    
    private static ZookeeperRegistryCenter zkRegCenter;
    
    @BeforeAll
    static void setUp() {
        EMBED_TESTING_SERVER.start();
        zkRegCenter = new ZookeeperRegistryCenter(ZOOKEEPER_CONFIGURATION);
        ZOOKEEPER_CONFIGURATION.setConnectionTimeoutMilliseconds(30000);
        zkRegCenter.init();
    }
    
    @AfterAll
    static void tearDown() {
        zkRegCenter.close();
    }
    
    @Test
    @Timeout(value = 10000L, unit = TimeUnit.MILLISECONDS)
    void assertExecuteInLeader() throws InterruptedException {
        final int threads = 10;
        CountDownLatch countDownLatch = new CountDownLatch(threads);
        SerialOnlyExecutionCallback serialOnlyExecutionCallback = new SerialOnlyExecutionCallback(countDownLatch, Thread.currentThread());
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            executorService.execute(() -> zkRegCenter.executeInLeader("/leader", serialOnlyExecutionCallback));
        }
        executorService.shutdown();
        countDownLatch.await();
    }
    
    @RequiredArgsConstructor
    private static class SerialOnlyExecutionCallback implements LeaderExecutionCallback {
        
        private final AtomicBoolean executing = new AtomicBoolean(false);
        
        private final CountDownLatch countDownLatch;
        
        private final Thread waitingThread;
        
        @Override
        public void execute() {
            if (executing.get() || !executing.compareAndSet(false, true)) {
                handleConcurrentExecution();
            }
            try {
                Thread.sleep(100L);
            } catch (final InterruptedException ex) {
                waitingThread.interrupt();
            }
            countDownLatch.countDown();
            if (!executing.compareAndSet(true, false)) {
                handleConcurrentExecution();
            }
        }
        
        private void handleConcurrentExecution() {
            waitingThread.interrupt();
            throw new IllegalStateException("Callback is executing concurrently");
        }
    }
}
