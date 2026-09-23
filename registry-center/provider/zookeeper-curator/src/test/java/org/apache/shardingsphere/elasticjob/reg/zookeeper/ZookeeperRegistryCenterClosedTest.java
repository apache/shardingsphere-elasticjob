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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class ZookeeperRegistryCenterClosedTest {
    
    private static final DataChangedEventListener NOOP_LISTENER = event -> {
    };
    
    private static ZookeeperRegistryCenter zkRegCenter;
    
    @BeforeAll
    static void setUp() {
        EmbedTestingServer embedTestingServer = new EmbedTestingServer();
        embedTestingServer.start();
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(embedTestingServer.getConnectionString(), ZookeeperRegistryCenterClosedTest.class.getName());
        zookeeperConfiguration.setConnectionTimeoutMilliseconds(30000);
        zkRegCenter = new ZookeeperRegistryCenter(zookeeperConfiguration);
        zkRegCenter.init();
        zkRegCenter.persist("/test", "test");
        zkRegCenter.addCacheData("/test");
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
    void assertWatchIgnoredWhenNoCache() {
        assertDoesNotThrow(() -> zkRegCenter.watch("/test", NOOP_LISTENER, null));
    }
}
