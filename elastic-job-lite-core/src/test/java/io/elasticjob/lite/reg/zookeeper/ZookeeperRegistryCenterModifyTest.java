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

package io.elasticjob.lite.reg.zookeeper;

import io.elasticjob.lite.fixture.EmbedTestingServer;
import io.elasticjob.lite.reg.zookeeper.util.ZookeeperRegistryCenterTestUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ZookeeperRegistryCenterModifyTest {
    
    private static final ZookeeperConfiguration ZOOKEEPER_CONFIGURATION = new ZookeeperConfiguration(EmbedTestingServer.getConnectionString(), ZookeeperRegistryCenterModifyTest.class.getName());
    
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
    
    @Test
    public void assertPersist() {
        zkRegCenter.persist("/test", "test_update");
        zkRegCenter.persist("/persist/new", "new_value");
        assertThat(zkRegCenter.get("/test"), is("test_update"));
        assertThat(zkRegCenter.get("/persist/new"), is("new_value"));
    }
    
    @Test
    public void assertUpdate() {
        zkRegCenter.persist("/update", "before_update");
        zkRegCenter.update("/update", "after_update");
        assertThat(zkRegCenter.getDirectly("/update"), is("after_update"));
    }
    
    @Test
    public void assertPersistEphemeral() throws Exception {
        zkRegCenter.persist("/persist", "persist_value");
        zkRegCenter.persistEphemeral("/ephemeral", "ephemeral_value");
        assertThat(zkRegCenter.get("/persist"), is("persist_value"));
        assertThat(zkRegCenter.get("/ephemeral"), is("ephemeral_value"));
        zkRegCenter.close();
        CuratorFramework client = CuratorFrameworkFactory.newClient(EmbedTestingServer.getConnectionString(), new RetryOneTime(2000));
        client.start();
        client.blockUntilConnected();
        assertThat(client.getData().forPath("/" + ZookeeperRegistryCenterModifyTest.class.getName() + "/persist"), is("persist_value".getBytes()));
        assertNull(client.checkExists().forPath("/" + ZookeeperRegistryCenterModifyTest.class.getName() + "/ephemeral"));
        zkRegCenter.init();
    }
    
    
    @Test
    public void assertPersistSequential() throws Exception {
        assertThat(zkRegCenter.persistSequential("/sequential/test_sequential", "test_value"), startsWith("/sequential/test_sequential"));
        assertThat(zkRegCenter.persistSequential("/sequential/test_sequential", "test_value"), startsWith("/sequential/test_sequential"));
        CuratorFramework client = CuratorFrameworkFactory.newClient(EmbedTestingServer.getConnectionString(), new RetryOneTime(2000));
        client.start();
        client.blockUntilConnected();
        List<String> actual = client.getChildren().forPath("/" + ZookeeperRegistryCenterModifyTest.class.getName() + "/sequential");
        assertThat(actual.size(), is(2));
        for (String each : actual) {
            assertThat(each, startsWith("test_sequential"));
            assertThat(zkRegCenter.get("/sequential/" + each), startsWith("test_value"));
        }
    }
    
    @Test
    public void assertPersistEphemeralSequential() throws Exception {
        zkRegCenter.persistEphemeralSequential("/sequential/test_ephemeral_sequential");
        zkRegCenter.persistEphemeralSequential("/sequential/test_ephemeral_sequential");
        CuratorFramework client = CuratorFrameworkFactory.newClient(EmbedTestingServer.getConnectionString(), new RetryOneTime(2000));
        client.start();
        client.blockUntilConnected();
        List<String> actual = client.getChildren().forPath("/" + ZookeeperRegistryCenterModifyTest.class.getName() + "/sequential");
        assertThat(actual.size(), is(2));
        for (String each : actual) {
            assertThat(each, startsWith("test_ephemeral_sequential"));
        }
        zkRegCenter.close();
        actual = client.getChildren().forPath("/" + ZookeeperRegistryCenterModifyTest.class.getName() + "/sequential");
        assertTrue(actual.isEmpty());
        zkRegCenter.init();
    }
    
    @Test
    public void assertRemove() {
        zkRegCenter.remove("/test");
        assertFalse(zkRegCenter.isExisted("/test"));
    }
}
