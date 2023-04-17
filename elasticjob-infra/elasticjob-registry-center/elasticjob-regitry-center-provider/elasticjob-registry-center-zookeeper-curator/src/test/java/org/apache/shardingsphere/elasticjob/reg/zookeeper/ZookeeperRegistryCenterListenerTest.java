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
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.util.ZookeeperRegistryCenterTestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ZookeeperRegistryCenterListenerTest {

    @Mock
    private Map<String, CuratorCache> caches;

    @Mock
    private CuratorFramework client;

    @Mock
    private CuratorCache cache;

    @Mock
    private Listenable<ConnectionStateListener> connStateListenable;

    @Mock
    private Listenable<CuratorCacheListener> dataListenable;

    private ZookeeperRegistryCenter regCenter;

    private final String jobPath = "/test_job";

    @Before
    public void setUp() {
        regCenter = new ZookeeperRegistryCenter(null);
        ZookeeperRegistryCenterTestUtil.setFieldValue(regCenter, "caches", caches);
        ZookeeperRegistryCenterTestUtil.setFieldValue(regCenter, "client", client);
    }

    @Test
    public void testAddConnectionStateChangedEventListener() throws Exception {
        when(client.getConnectionStateListenable()).thenReturn(connStateListenable);
        regCenter.addConnectionStateChangedEventListener(jobPath, null);
        verify(client.getConnectionStateListenable()).addListener(any());
        assertEquals(1, regCenter.getConnStateListeners().get(jobPath).size());
    }

    @Test
    public void testWatch() throws Exception {
        when(caches.get(jobPath + "/")).thenReturn(cache);
        when(cache.listenable()).thenReturn(dataListenable);
        regCenter.watch(jobPath, null, null);
        verify(cache.listenable()).addListener(any());
        assertEquals(1, regCenter.getDataListeners().get(jobPath).size());
    }

    @Test
    public void testRemoveDataListenersNonCache() throws Exception {
        when(cache.listenable()).thenReturn(dataListenable);
        regCenter.removeDataListeners(jobPath);
        verify(cache.listenable(), never()).removeListener(any());
        assertNull(regCenter.getDataListeners().get(jobPath));
    }

    @Test
    public void testRemoveDataListenersHasCache() throws Exception {
        when(caches.get(jobPath + "/")).thenReturn(cache);
        when(cache.listenable()).thenReturn(dataListenable);
        List<CuratorCacheListener> list = new ArrayList<>();
        list.add(null);
        list.add(null);
        regCenter.getDataListeners().put(jobPath, list);
        regCenter.removeDataListeners(jobPath);
        assertNull(regCenter.getDataListeners().get(jobPath));
        verify(cache.listenable(), times(2)).removeListener(null);
    }

    @Test
    public void testRemoveDataListenersHasCacheEmptyListeners() throws Exception {
        when(caches.get(jobPath + "/")).thenReturn(cache);
        when(cache.listenable()).thenReturn(dataListenable);
        regCenter.removeDataListeners(jobPath);
        assertNull(regCenter.getDataListeners().get(jobPath));
        verify(cache.listenable(), never()).removeListener(null);

    }

    @Test
    public void testRemoveConnStateListener() throws Exception {
        when(client.getConnectionStateListenable()).thenReturn(connStateListenable);
        List<ConnectionStateListener> list = new ArrayList<>();
        list.add(null);
        list.add(null);
        regCenter.getConnStateListeners().put(jobPath, list);
        assertEquals(2, regCenter.getConnStateListeners().get(jobPath).size());
        regCenter.removeConnStateListener(jobPath);
        assertNull(regCenter.getDataListeners().get(jobPath));
        verify(client.getConnectionStateListenable(), times(2)).removeListener(null);
    }

    @Test
    public void testRemoveConnStateListenerEmptyListeners() throws Exception {
        when(client.getConnectionStateListenable()).thenReturn(connStateListenable);
        regCenter.removeConnStateListener(jobPath);
        assertNull(regCenter.getConnStateListeners().get(jobPath));
        verify(client.getConnectionStateListenable(), never()).removeListener(null);
    }
}
