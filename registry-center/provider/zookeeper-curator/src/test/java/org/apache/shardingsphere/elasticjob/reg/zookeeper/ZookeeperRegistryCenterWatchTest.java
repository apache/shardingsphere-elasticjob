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

import org.apache.curator.utils.ThreadUtils;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.env.RegistryCenterEnvironmentPreparer;
import org.apache.shardingsphere.elasticjob.test.util.EmbedTestingServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

class ZookeeperRegistryCenterWatchTest {
    
    private static final EmbedTestingServer EMBED_TESTING_SERVER = new EmbedTestingServer(9181);
    
    private static final ZookeeperConfiguration ZOOKEEPER_CONFIGURATION = new ZookeeperConfiguration(EMBED_TESTING_SERVER.getConnectionString(), ZookeeperRegistryCenterWatchTest.class.getName());
    
    private static ZookeeperRegistryCenter zkRegCenter;
    
    @BeforeAll
    static void setUp() {
        EMBED_TESTING_SERVER.start();
        zkRegCenter = new ZookeeperRegistryCenter(ZOOKEEPER_CONFIGURATION);
        ZOOKEEPER_CONFIGURATION.setConnectionTimeoutMilliseconds(30000);
        zkRegCenter.init();
        RegistryCenterEnvironmentPreparer.persist(zkRegCenter);
    }
    
    @AfterAll
    static void tearDown() {
        zkRegCenter.close();
    }
    
    @Test
    @Timeout(value = 10000L, unit = TimeUnit.MILLISECONDS)
    void assertWatchWithoutExecutor() throws InterruptedException {
        CountDownLatch waitingForCountDownValue = new CountDownLatch(1);
        String key = "/test-watch-without-executor";
        zkRegCenter.addCacheData(key);
        CountDownLatch waitingForWatchReady = new CountDownLatch(1);
        zkRegCenter.watch(key, event -> {
            waitingForWatchReady.countDown();
            if (DataChangedEvent.Type.UPDATED == event.getType() && "countDown".equals(event.getValue())) {
                waitingForCountDownValue.countDown();
            }
        }, null);
        zkRegCenter.persist(key, "");
        waitingForWatchReady.await();
        zkRegCenter.update(key, "countDown");
        waitingForCountDownValue.await();
    }
    
    @Test
    @Timeout(value = 10000L, unit = TimeUnit.MILLISECONDS)
    void assertWatchWithExecutor() throws InterruptedException {
        CountDownLatch waitingForCountDownValue = new CountDownLatch(1);
        String key = "/test-watch-with-executor";
        zkRegCenter.addCacheData(key);
        CountDownLatch waitingForWatchReady = new CountDownLatch(1);
        String threadNamePrefix = "ListenerNotify";
        ThreadFactory threadFactory = ThreadUtils.newGenericThreadFactory(threadNamePrefix);
        ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);
        zkRegCenter.watch(key, event -> {
            assertThat(Thread.currentThread().getName(), startsWith(threadNamePrefix));
            waitingForWatchReady.countDown();
            if (DataChangedEvent.Type.UPDATED == event.getType() && "countDown".equals(event.getValue())) {
                waitingForCountDownValue.countDown();
            }
        }, executor);
        zkRegCenter.persist(key, "");
        waitingForWatchReady.await();
        zkRegCenter.update(key, "countDown");
        waitingForCountDownValue.await();
        executor.shutdown();
    }
}
