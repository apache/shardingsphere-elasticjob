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
import org.apache.shardingsphere.elasticjob.reg.zookeeper.fixture.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.util.ZookeeperRegistryCenterTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.startsWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class ZookeeperRegistryCenterWatchTest {
    
    private static final ZookeeperConfiguration ZOOKEEPER_CONFIGURATION = new ZookeeperConfiguration(EmbedTestingServer.getConnectionString(), ZookeeperRegistryCenterWatchTest.class.getName());
    
    private static ZookeeperRegistryCenter zkRegCenter;
    
    @BeforeClass
    public static void setUp() {
        EmbedTestingServer.start();
        zkRegCenter = new ZookeeperRegistryCenter(ZOOKEEPER_CONFIGURATION);
        ZOOKEEPER_CONFIGURATION.setConnectionTimeoutMilliseconds(30000);
        zkRegCenter.init();
        ZookeeperRegistryCenterTestUtil.persist(zkRegCenter);
    }
    
    @AfterClass
    public static void tearDown() {
        zkRegCenter.close();
    }

    @Test(timeout = 10000L)
    public void assertWatchWithoutExecutor() throws InterruptedException {
        CountDownLatch waitingForCountDownValue = new CountDownLatch(1);
        String key = "/test-watch-without-executor";
        zkRegCenter.persist(key, "");
        zkRegCenter.addCacheData(key);
        CountDownLatch waitingForWatchReady = new CountDownLatch(1);
        zkRegCenter.watch(key, event -> {
            waitingForWatchReady.countDown();
            if (DataChangedEvent.Type.UPDATED == event.getType() && "countDown".equals(event.getValue())) {
                waitingForCountDownValue.countDown();
            }
        }, null);
        waitingForWatchReady.await();
        zkRegCenter.update(key, "countDown");
        waitingForCountDownValue.await();
    }
    
    @Test(timeout = 10000L)
    public void assertWatchWithExecutor() throws InterruptedException {
        CountDownLatch waitingForCountDownValue = new CountDownLatch(1);
        String key = "/test-watch-with-executor";
        zkRegCenter.persist(key, "");
        zkRegCenter.addCacheData(key);
        CountDownLatch waitingForWatchReady = new CountDownLatch(1);
        String threadNamePrefix = "ListenerNotify";
        ThreadFactory threadFactory = ThreadUtils.newGenericThreadFactory(threadNamePrefix);
        Executor executor = Executors.newSingleThreadExecutor(threadFactory);
        zkRegCenter.watch(key, event -> {
            assertThat(Thread.currentThread().getName(), startsWith(threadNamePrefix));
            waitingForWatchReady.countDown();
            if (DataChangedEvent.Type.UPDATED == event.getType() && "countDown".equals(event.getValue())) {
                waitingForCountDownValue.countDown();
            }
        }, executor);
        waitingForWatchReady.await();
        zkRegCenter.update(key, "countDown");
        waitingForCountDownValue.await();
    }
}
