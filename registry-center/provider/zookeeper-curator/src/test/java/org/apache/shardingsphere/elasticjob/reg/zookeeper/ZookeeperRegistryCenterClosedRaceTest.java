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
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.shardingsphere.elasticjob.reg.exception.RegException;
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZookeeperRegistryCenterClosedRaceTest {
    
    @Mock
    private CuratorFramework client;
    
    @Mock
    private GetDataBuilder getDataBuilder;
    
    @Mock
    private GetChildrenBuilder getChildrenBuilder;
    
    @Mock
    private ExistsBuilder existsBuilder;
    
    @Test
    void assertGetDirectlyReturnsNullWhenClosedAfterCheck() throws Exception {
        when(client.getState()).thenReturn(CuratorFrameworkState.STARTED, CuratorFrameworkState.STOPPED);
        when(client.getData()).thenReturn(getDataBuilder);
        when(getDataBuilder.forPath("/test")).thenThrow(new IllegalStateException("Client is not started or has been closed"));
        assertNull(newRegCenter().getDirectly("/test"));
    }
    
    @Test
    void assertGetChildrenKeysReturnsEmptyWhenClosedAfterCheck() throws Exception {
        when(client.getState()).thenReturn(CuratorFrameworkState.STARTED, CuratorFrameworkState.STOPPED);
        when(client.getChildren()).thenReturn(getChildrenBuilder);
        when(getChildrenBuilder.forPath("/test")).thenThrow(new IllegalStateException("Client is not started or has been closed"));
        assertThat(newRegCenter().getChildrenKeys("/test").isEmpty(), is(true));
    }
    
    @Test
    void assertIsExistedReturnsFalseWhenClosedAfterCheck() throws Exception {
        when(client.getState()).thenReturn(CuratorFrameworkState.STARTED, CuratorFrameworkState.STOPPED);
        when(client.checkExists()).thenReturn(existsBuilder);
        when(existsBuilder.forPath("/test")).thenThrow(new IllegalStateException("Client is not started or has been closed"));
        assertFalse(newRegCenter().isExisted("/test"));
    }
    
    @Test
    void assertGetDirectlyStillThrowsWhenNotClosed() throws Exception {
        when(client.getState()).thenReturn(CuratorFrameworkState.STARTED);
        when(client.getData()).thenReturn(getDataBuilder);
        when(getDataBuilder.forPath("/test")).thenThrow(new RuntimeException("boom"));
        ZookeeperRegistryCenter regCenter = newRegCenter();
        assertThrows(RegException.class, () -> regCenter.getDirectly("/test"));
    }
    
    private ZookeeperRegistryCenter newRegCenter() {
        ZookeeperRegistryCenter result = new ZookeeperRegistryCenter(null);
        ReflectionUtils.setFieldValue(result, "client", client);
        return result;
    }
}
