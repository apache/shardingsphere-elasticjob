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

import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEventListener;
import org.apache.shardingsphere.elasticjob.test.util.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class ZookeeperRegistryCenterClosedTest {
    
    private static final DataChangedEventListener NOOP_LISTENER = event -> {
    };
    
    private static final EmbedTestingServer EMBED_TESTING_SERVER = new EmbedTestingServer();
    
    private static ZookeeperRegistryCenter zkRegCenter;
    
    @BeforeAll
    static void setUp() {
        EMBED_TESTING_SERVER.start();
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(EMBED_TESTING_SERVER.getConnectionString(), ZookeeperRegistryCenterClosedTest.class.getName());
        zookeeperConfiguration.setConnectionTimeoutMilliseconds(30000);
        zkRegCenter = new ZookeeperRegistryCenter(zookeeperConfiguration);
        zkRegCenter.init();
        zkRegCenter.persist("/test", "test");
        zkRegCenter.persist("/test/child", "child");
        zkRegCenter.addCacheData("/test");
        zkRegCenter.watch("/test", NOOP_LISTENER, null);
        zkRegCenter.close();
    }
    
    @Test
    void assertGetReturnsNull() {
        assertNull(zkRegCenter.get("/test"));
    }
    
    @Test
    void assertGetDirectlyReturnsNull() {
        assertNull(zkRegCenter.getDirectly("/test"));
    }
    
    @Test
    void assertGetChildrenKeysReturnsEmpty() {
        assertThat(zkRegCenter.getChildrenKeys("/test"), is(Collections.<String>emptyList()));
    }
    
    @Test
    void assertGetNumChildrenReturnsZero() {
        assertThat(zkRegCenter.getNumChildren("/test"), is(0));
    }
    
    @Test
    void assertIsExistedReturnsFalse() {
        assertFalse(zkRegCenter.isExisted("/test"));
    }
    
    @Test
    void assertWatchIgnoredWhenCacheIsAbsent() {
        assertDoesNotThrow(() -> zkRegCenter.watch("/no-cache", NOOP_LISTENER, null));
    }
    
    @Test
    void assertCachesAndListenersAreRemoved() {
        assertThat(getMapSize("caches"), is(0));
        assertThat(getMapSize("dataListeners"), is(0));
        assertThat(getMapSize("connStateListeners"), is(0));
    }
    
    private static int getMapSize(final String fieldName) {
        return ((Map<?, ?>) ReflectionUtils.getFieldValue(zkRegCenter, fieldName)).size();
    }
}
