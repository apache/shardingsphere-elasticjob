/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.reg.zookeeper;

import com.dangdang.ddframe.reg.zookeeper.fixture.TestNestedServer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ZookeeperRegistryCenterMiscellaneousTest {
    
    private static ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(TestNestedServer.getConnectionString(), ZookeeperRegistryCenterMiscellaneousTest.class.getName());
    
    private static ZookeeperRegistryCenter zkRegCenter;
    
    @BeforeClass
    public static void setUp() {
        TestNestedServer.start();
        zkConfig.setConnectionTimeoutMilliseconds(30000);
        zkRegCenter = new ZookeeperRegistryCenter(zkConfig);
        zkRegCenter.init();
        zkRegCenter.addCacheData("/test");
    }
    
    @AfterClass
    public static void tearDown() {
        zkRegCenter.close();
    }
    
    @Test
    public void assertGetRawClient() {
        assertThat(zkRegCenter.getRawClient(), instanceOf(CuratorFramework.class));
        assertThat(((CuratorFramework) zkRegCenter.getRawClient()).getNamespace(), is(ZookeeperRegistryCenterMiscellaneousTest.class.getName()));
    }
    
    @Test
    public void assertGetRawCache() {
        assertThat(zkRegCenter.getRawCache("/test"), instanceOf(TreeCache.class));
    }
    
    @Test
    public void assertGetZkConfig() {
        ZookeeperConfiguration expected = new ZookeeperConfiguration();
        ZookeeperRegistryCenter zkRegCenter = new ZookeeperRegistryCenter(expected);
        assertThat(zkRegCenter.getZkConfig(), is(expected));
    }
}
