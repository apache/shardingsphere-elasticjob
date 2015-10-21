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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.KeeperException.NoAuthException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dangdang.ddframe.test.NestedZookeeperServers;
import com.dangdang.ddframe.test.TestEnvironmentException;

public final class ZookeeperRegistryCenterForAuthTest {
    
    
    private ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(NestedZookeeperServers.ZK_CONNECTION_STRING, "zkRegTestCenter", 1000, 3000, 3);
    
    private ZookeeperRegistryCenter zkRegCenter;
    
    @Before
    public void setUp() {
        NestedZookeeperServers.getInstance().startServerIfNotStarted();
        zkConfig.setSessionTimeoutMilliseconds(5000);
        zkConfig.setConnectionTimeoutMilliseconds(5000);
        zkRegCenter = new ZookeeperRegistryCenter(zkConfig);
    }
    
    @After
    public void tearDown() {
        zkRegCenter.close();
        clear();
    }
    
    private void clear() {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(NestedZookeeperServers.ZK_CONNECTION_STRING).retryPolicy(new RetryOneTime(2000)).authorization("digest", "digest:correct".getBytes()).build();
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
    public void initWithDigestSuccess() throws Exception {
        zkConfig.setLocalPropertiesPath("conf/reg/local.properties");
        zkConfig.setDigest("digest:correct");
        zkRegCenter.init();
        zkRegCenter.close();
        CuratorFramework client = CuratorFrameworkFactory.builder()
            .connectString(NestedZookeeperServers.ZK_CONNECTION_STRING)
            .retryPolicy(new RetryOneTime(2000))
            .authorization("digest", "digest:correct".getBytes()).build();
        client.start();
        client.blockUntilConnected();
        assertThat(client.getData().forPath("/zkRegTestCenter/test/deep/nested"), is("deepNested".getBytes()));
    }
    
    @Test(expected = NoAuthException.class)
    public void initWithDigestFail() throws Exception {
        zkConfig.setLocalPropertiesPath("conf/reg/local.properties");
        zkConfig.setDigest("digest:correct");
        zkRegCenter.init();
        zkRegCenter.close();
        CuratorFramework client = CuratorFrameworkFactory.newClient(NestedZookeeperServers.ZK_CONNECTION_STRING, new RetryOneTime(2000));
        client.start();
        client.blockUntilConnected();
        client.getData().forPath("/zkRegTestCenter/test/deep/nested");
    }
}
