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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.shardingsphere.elasticjob.test.util.EmbedTestingServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ZookeeperRegistryCenterMiscellaneousTest {
    
    private static final EmbedTestingServer EMBED_TESTING_SERVER = new EmbedTestingServer();
    
    private static ZookeeperConfiguration zookeeperConfiguration;
    
    private static ZookeeperRegistryCenter zkRegCenter;
    
    @BeforeAll
    static void setUp() {
        EMBED_TESTING_SERVER.start();
        zookeeperConfiguration = new ZookeeperConfiguration(EMBED_TESTING_SERVER.getConnectionString(), ZookeeperRegistryCenterMiscellaneousTest.class.getName());
        zookeeperConfiguration.setConnectionTimeoutMilliseconds(30000);
        zkRegCenter = new ZookeeperRegistryCenter(zookeeperConfiguration);
        zkRegCenter.init();
        zkRegCenter.addCacheData("/test");
    }
    
    @AfterAll
    static void tearDown() {
        zkRegCenter.close();
    }
    
    @Test
    void assertGetRawClient() {
        assertThat(zkRegCenter.getRawClient(), instanceOf(CuratorFramework.class));
        assertThat(((CuratorFramework) zkRegCenter.getRawClient()).getNamespace(), is(ZookeeperRegistryCenterMiscellaneousTest.class.getName()));
    }
    
    @Test
    void assertGetRawCache() {
        assertThat(zkRegCenter.getRawCache("/test"), instanceOf(CuratorCache.class));
    }
    
    @Test
    void assertGetZkConfig() {
        ZookeeperRegistryCenter zkRegCenter = new ZookeeperRegistryCenter(zookeeperConfiguration);
        assertThat(zkRegCenter.getZkConfig(), is(zookeeperConfiguration));
    }
}
