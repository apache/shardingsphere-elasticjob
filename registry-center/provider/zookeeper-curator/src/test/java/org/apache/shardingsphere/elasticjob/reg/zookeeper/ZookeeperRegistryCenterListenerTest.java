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
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZookeeperRegistryCenterListenerTest {
    
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
    
    @BeforeEach
    void setUp() {
        regCenter = new ZookeeperRegistryCenter(null);
        ReflectionUtils.setFieldValue(regCenter, "caches", caches);
        ReflectionUtils.setFieldValue(regCenter, "client", client);
    }
    
    @Test
    void testAddConnectionStateChangedEventListener() {
        when(client.getConnectionStateListenable()).thenReturn(connStateListenable);
        regCenter.addConnectionStateChangedEventListener(jobPath, null);
        verify(client.getConnectionStateListenable()).addListener(any());
        assertEquals(1, getConnStateListeners().get(jobPath).size());
    }
    
    @Test
    void testWatch() {
        when(caches.get(jobPath + "/")).thenReturn(cache);
        when(cache.listenable()).thenReturn(dataListenable);
        regCenter.watch(jobPath, null, null);
        verify(cache.listenable()).addListener(any());
        assertEquals(1, getDataListeners().get(jobPath).size());
    }
    
    @Test
    void testRemoveDataListenersNonCache() {
        when(cache.listenable()).thenReturn(dataListenable);
        regCenter.removeDataListeners(jobPath);
        verify(cache.listenable(), never()).removeListener(any());
        assertNull(getDataListeners().get(jobPath));
    }
    
    @Test
    void testRemoveDataListenersHasCache() {
        when(caches.get(jobPath + "/")).thenReturn(cache);
        when(cache.listenable()).thenReturn(dataListenable);
        List<CuratorCacheListener> list = new ArrayList<>();
        list.add(null);
        list.add(null);
        getDataListeners().put(jobPath, list);
        regCenter.removeDataListeners(jobPath);
        assertNull(getDataListeners().get(jobPath));
        verify(cache.listenable(), times(2)).removeListener(null);
    }
    
    @Test
    void testRemoveDataListenersHasCacheEmptyListeners() {
        when(caches.get(jobPath + "/")).thenReturn(cache);
        when(cache.listenable()).thenReturn(dataListenable);
        regCenter.removeDataListeners(jobPath);
        assertNull(getDataListeners().get(jobPath));
        verify(cache.listenable(), never()).removeListener(null);
    }
    
    @Test
    void testRemoveConnStateListener() {
        when(client.getConnectionStateListenable()).thenReturn(connStateListenable);
        List<ConnectionStateListener> list = new ArrayList<>();
        list.add(null);
        list.add(null);
        getConnStateListeners().put(jobPath, list);
        assertEquals(2, getConnStateListeners().get(jobPath).size());
        regCenter.removeConnStateListener(jobPath);
        assertNull(getConnStateListeners().get(jobPath));
        verify(client.getConnectionStateListenable(), times(2)).removeListener(null);
    }
    
    @Test
    void testRemoveConnStateListenerEmptyListeners() {
        when(client.getConnectionStateListenable()).thenReturn(connStateListenable);
        regCenter.removeConnStateListener(jobPath);
        assertNull(getConnStateListeners().get(jobPath));
        verify(client.getConnectionStateListenable(), never()).removeListener(null);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, List<ConnectionStateListener>> getConnStateListeners() {
        return (Map<String, List<ConnectionStateListener>>) ReflectionUtils.getFieldValue(regCenter, "connStateListeners");
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, List<CuratorCacheListener>> getDataListeners() {
        return (Map<String, List<CuratorCacheListener>>) ReflectionUtils.getFieldValue(regCenter, "dataListeners");
    }
}
