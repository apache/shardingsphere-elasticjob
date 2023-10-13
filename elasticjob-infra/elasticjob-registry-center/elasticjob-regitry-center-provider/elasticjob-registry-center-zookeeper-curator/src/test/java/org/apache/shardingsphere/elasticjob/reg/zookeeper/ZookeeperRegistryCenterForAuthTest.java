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
import org.apache.shardingsphere.elasticjob.reg.zookeeper.fixture.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.util.ZookeeperRegistryCenterTestUtil;
import org.apache.zookeeper.KeeperException.NoAuthException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ZookeeperRegistryCenterForAuthTest {
    
    private static final String NAME_SPACE = ZookeeperRegistryCenterForAuthTest.class.getName();
    
    private static final ZookeeperConfiguration ZOOKEEPER_CONFIGURATION = new ZookeeperConfiguration(EmbedTestingServer.getConnectionString(), NAME_SPACE);
    
    private static ZookeeperRegistryCenter zkRegCenter;
    
    @BeforeAll
    public static void setUp() {
        EmbedTestingServer.start();
        ZOOKEEPER_CONFIGURATION.setDigest("digest:password");
        ZOOKEEPER_CONFIGURATION.setSessionTimeoutMilliseconds(5000);
        ZOOKEEPER_CONFIGURATION.setConnectionTimeoutMilliseconds(5000);
        zkRegCenter = new ZookeeperRegistryCenter(ZOOKEEPER_CONFIGURATION);
        zkRegCenter.init();
        ZookeeperRegistryCenterTestUtil.persist(zkRegCenter);
    }
    
    @AfterAll
    public static void tearDown() {
        zkRegCenter.close();
    }
    
    @Test
    public void assertInitWithDigestSuccess() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(EmbedTestingServer.getConnectionString())
                .retryPolicy(new RetryOneTime(2000))
                .authorization("digest", "digest:password".getBytes()).build();
        client.start();
        client.blockUntilConnected();
        assertThat(client.getData().forPath("/" + ZookeeperRegistryCenterForAuthTest.class.getName() + "/test/deep/nested"), is("deepNested".getBytes()));
    }
    
    @Test
    public void assertInitWithDigestFailure() {
        assertThrows(NoAuthException.class, () -> {
            CuratorFramework client = CuratorFrameworkFactory.newClient(EmbedTestingServer.getConnectionString(), new RetryOneTime(2000));
            client.start();
            client.blockUntilConnected();
            client.getData().forPath("/" + ZookeeperRegistryCenterForAuthTest.class.getName() + "/test/deep/nested");
        });
    }
}
