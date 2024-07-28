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
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.env.RegistryCenterEnvironmentPreparer;
import org.apache.shardingsphere.elasticjob.test.util.EmbedTestingServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZookeeperRegistryCenterModifyTest {
    
    private static final EmbedTestingServer EMBED_TESTING_SERVER = new EmbedTestingServer();
    
    private static ZookeeperRegistryCenter zkRegCenter;
    
    @BeforeAll
    static void setUp() {
        EMBED_TESTING_SERVER.start();
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(EMBED_TESTING_SERVER.getConnectionString(), ZookeeperRegistryCenterModifyTest.class.getName());
        zkRegCenter = new ZookeeperRegistryCenter(zookeeperConfiguration);
        zookeeperConfiguration.setConnectionTimeoutMilliseconds(30000);
        zkRegCenter.init();
        RegistryCenterEnvironmentPreparer.persist(zkRegCenter);
    }
    
    @AfterAll
    static void tearDown() {
        zkRegCenter.close();
    }
    
    @Test
    void assertPersist() {
        zkRegCenter.persist("/test", "test_update");
        zkRegCenter.persist("/persist/new", "new_value");
        assertThat(zkRegCenter.get("/test"), is("test_update"));
        assertThat(zkRegCenter.get("/persist/new"), is("new_value"));
    }
    
    @Test
    void assertUpdate() {
        zkRegCenter.persist("/update", "before_update");
        zkRegCenter.update("/update", "after_update");
        assertThat(zkRegCenter.getDirectly("/update"), is("after_update"));
    }
    
    @Test
    void assertPersistEphemeral() throws Exception {
        zkRegCenter.persist("/persist", "persist_value");
        zkRegCenter.persistEphemeral("/ephemeral", "ephemeral_value");
        assertThat(zkRegCenter.get("/persist"), is("persist_value"));
        assertThat(zkRegCenter.get("/ephemeral"), is("ephemeral_value"));
        zkRegCenter.close();
        CuratorFramework client = CuratorFrameworkFactory.newClient(EMBED_TESTING_SERVER.getConnectionString(), new RetryOneTime(2000));
        client.start();
        client.blockUntilConnected();
        assertThat(client.getData().forPath("/" + ZookeeperRegistryCenterModifyTest.class.getName() + "/persist"), is("persist_value".getBytes()));
        assertNull(client.checkExists().forPath("/" + ZookeeperRegistryCenterModifyTest.class.getName() + "/ephemeral"));
        zkRegCenter.init();
    }
    
    @Test
    void assertPersistSequential() throws Exception {
        assertThat(zkRegCenter.persistSequential("/sequential/test_sequential", "test_value"), startsWith("/sequential/test_sequential"));
        assertThat(zkRegCenter.persistSequential("/sequential/test_sequential", "test_value"), startsWith("/sequential/test_sequential"));
        try (CuratorFramework client = CuratorFrameworkFactory.newClient(EMBED_TESTING_SERVER.getConnectionString(), new RetryOneTime(2000))) {
            client.start();
            client.blockUntilConnected();
            List<String> actual = client.getChildren().forPath("/" + ZookeeperRegistryCenterModifyTest.class.getName() + "/sequential");
            assertThat(actual.size(), is(2));
            for (String each : actual) {
                assertThat(each, startsWith("test_sequential"));
                assertThat(zkRegCenter.get("/sequential/" + each), startsWith("test_value"));
            }
            zkRegCenter.remove("/sequential");
            assertFalse(zkRegCenter.isExisted("/sequential"));
        }
    }
    
    @Test
    void assertPersistEphemeralSequential() throws Exception {
        zkRegCenter.persistEphemeralSequential("/sequential/test_ephemeral_sequential");
        zkRegCenter.persistEphemeralSequential("/sequential/test_ephemeral_sequential");
        try (CuratorFramework client = CuratorFrameworkFactory.newClient(EMBED_TESTING_SERVER.getConnectionString(), new RetryOneTime(2000))) {
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
    }
    
    @Test
    void assertRemove() {
        zkRegCenter.remove("/test");
        assertFalse(zkRegCenter.isExisted("/test"));
    }
}
