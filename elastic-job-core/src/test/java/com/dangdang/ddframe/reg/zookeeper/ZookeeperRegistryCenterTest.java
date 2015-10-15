/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.reg.zookeeper;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.RetryOneTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dangdang.ddframe.reg.exception.LocalPropertiesFileNotFoundException;
import com.dangdang.ddframe.reg.exception.RegException;
import com.dangdang.ddframe.test.NestedZookeeperServers;
import com.dangdang.ddframe.test.TestEnvironmentException;

public final class ZookeeperRegistryCenterTest {
    
    private ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(NestedZookeeperServers.ZK_CONNECTION_STRING, "zkRegTestCenter", 1000, 3000, 3);
    
    private ZookeeperRegistryCenter zkRegCenter;
    
    @Before
    public void setUp() {
        NestedZookeeperServers.getInstance().startServerIfNotStarted();
        zkRegCenter = new ZookeeperRegistryCenter(zkConfig);
    }
    
    @After
    public void tearDown() {
        zkRegCenter.close();
        clear();
    }
    
    private void clear() {
        CuratorFramework client = CuratorFrameworkFactory.newClient(NestedZookeeperServers.ZK_CONNECTION_STRING, new RetryOneTime(2000));
        client.start();
        try {
            client.blockUntilConnected();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        try {
            if (null != client.checkExists().forPath("/zkRegTestCenter")) {
                client.delete().deletingChildrenIfNeeded().forPath("/zkRegTestCenter");
            }
        // CHECKSTYLE:OFF
        } catch (final Exception ex) {
        // CHECKSTYLE:ON
            throw new TestEnvironmentException(ex);
        }
    }
    
    @Test
    public void initWithoutLocalProperties() {
        zkRegCenter.init();
        assertFalse(zkRegCenter.isExisted("/notExisted"));
    }
    
    @Test(expected = LocalPropertiesFileNotFoundException.class)
    public void initWithLocalPropertiesCannotFound() {
        zkConfig.setLocalPropertiesPath("conf/reg/notExisted.properties");
        try {
            zkRegCenter.init();
        } catch (final RegException ex) {
            throw (LocalPropertiesFileNotFoundException) ex.getCause();
        }
    }
    
    @Test
    public void initWithLocalProperties() {
        zkConfig.setLocalPropertiesPath("conf/reg/local.properties");
        zkRegCenter.init();
        assertThat(zkRegCenter.get("/test"), is("test"));
        assertThat(zkRegCenter.get("/test/deep/nested"), is("deepNested"));
    }
    
    @Test
    public void initWithLocalPropertiesAndCannotOverwrite() {
        createInitData();
        zkConfig.setLocalPropertiesPath("conf/reg/local_overwrite.properties");
        zkRegCenter.init();
        assertThat(zkRegCenter.get("/test"), is("test"));
        assertThat(zkRegCenter.get("/test/deep/nested"), is("deepNested"));
        assertThat(zkRegCenter.get("/new"), is("new"));
    }
    
    @Test
    public void initWithLocalPropertiesAndCanOverwrite() {
        createInitData();
        zkConfig.setLocalPropertiesPath("conf/reg/local_overwrite.properties");
        zkConfig.setOverwrite(true);
        zkRegCenter.init();
        assertThat(zkRegCenter.get("/test"), is("test_overwrite"));
        assertThat(zkRegCenter.get("/test/deep/nested"), is("deepNested_overwrite"));
        assertThat(zkRegCenter.get("/new"), is("new"));
    }
    
    private void createInitData() {
        zkConfig.setLocalPropertiesPath("conf/reg/local.properties");
        zkRegCenter.init();
        zkRegCenter.close();
    }
    
    @Test
    public void getChildrenKeys() {
        zkConfig.setLocalPropertiesPath("conf/reg/local.properties");
        zkRegCenter.init();
        assertThat(zkRegCenter.getChildrenKeys("/test"), is(Arrays.asList("child", "deep")));
        assertThat(zkRegCenter.getChildrenKeys("/test/deep"), is(Arrays.asList("nested")));
        assertThat(zkRegCenter.getChildrenKeys("/test/child"), is(Collections.EMPTY_LIST));
        assertThat(zkRegCenter.getChildrenKeys("/test/notExisted"), is(Collections.EMPTY_LIST));
    }
    
    @Test
    public void isExisted() {
        zkConfig.setLocalPropertiesPath("conf/reg/local.properties");
        zkRegCenter.init();
        assertTrue(zkRegCenter.isExisted("/test"));
        assertTrue(zkRegCenter.isExisted("/test/deep/nested"));
        assertFalse(zkRegCenter.isExisted("/notExisted"));
    }
    
    @Test
    public void persist() {
        zkConfig.setLocalPropertiesPath("conf/reg/local.properties");
        zkRegCenter.init();
        zkRegCenter.persist("/test", "test_update");
        zkRegCenter.persist("/persist/new", "new_value");
        assertThat(zkRegCenter.get("/test"), is("test_update"));
        assertThat(zkRegCenter.get("/persist/new"), is("new_value"));
    }
    
    @Test
    public void update() {
        zkConfig.setLocalPropertiesPath("conf/reg/local.properties");
        zkRegCenter.init();
        zkRegCenter.update("/test", "test_update");
        assertThat(zkRegCenter.getDirectly("/test"), is("test_update"));
    }
    
    @Test
    public void persistEphemeral() throws Exception {
        zkRegCenter.init();
        zkRegCenter.persist("/test_persist", "test_persist_value");
        zkRegCenter.persistEphemeral("/test_ephemeral", "test_ephemeral_value");
        assertThat(zkRegCenter.get("/test_persist"), is("test_persist_value"));
        assertThat(zkRegCenter.get("/test_ephemeral"), is("test_ephemeral_value"));
        zkRegCenter.close();
        CuratorFramework client = CuratorFrameworkFactory.newClient(NestedZookeeperServers.ZK_CONNECTION_STRING, new RetryOneTime(2000));
        client.start();
        client.blockUntilConnected();
        assertThat(client.getData().forPath("/zkRegTestCenter/test_persist"), is("test_persist_value".getBytes()));
        assertNull(client.checkExists().forPath("/zkRegTestCenter/test_ephemeral"));
    }
    
    @Test
    public void persistEphemeralSequential() throws Exception {
        zkRegCenter.init();
        zkRegCenter.persistEphemeralSequential("/sequential/test_sequential");
        zkRegCenter.persistEphemeralSequential("/sequential/test_sequential");
        CuratorFramework client = CuratorFrameworkFactory.newClient(NestedZookeeperServers.ZK_CONNECTION_STRING, new RetryOneTime(2000));
        client.start();
        client.blockUntilConnected();
        List<String> actual = client.getChildren().forPath("/zkRegTestCenter/sequential");
        assertThat(actual.size(), is(2));
        for (String each : actual) {
            assertThat(each, startsWith("test_sequential"));
        }
        zkRegCenter.close();
        actual = client.getChildren().forPath("/zkRegTestCenter/sequential");
        assertTrue(actual.isEmpty());
    }
    
    @Test
    public void remove() {
        zkConfig.setLocalPropertiesPath("conf/reg/local.properties");
        zkRegCenter.init();
        zkRegCenter.remove("/test");
        assertFalse(zkRegCenter.isExisted("/test"));
    }
    
    @Test
    public void getRegistryCenterTime() {
        zkRegCenter.init();
        assertTrue(zkRegCenter.getRegistryCenterTime("/_systemTime/current") <= System.currentTimeMillis());
    }
    
    @Test
    public void getRawClient() {
        zkRegCenter.init();
        assertThat(zkRegCenter.getRawClient(), instanceOf(CuratorFramework.class));
        assertThat(((CuratorFramework) zkRegCenter.getRawClient()).getNamespace(), is("zkRegTestCenter"));
    }
    
    @Test
    public void getRawCache() {
        zkRegCenter.init();
        assertThat(zkRegCenter.getRawCache(), instanceOf(TreeCache.class));
    }
}
