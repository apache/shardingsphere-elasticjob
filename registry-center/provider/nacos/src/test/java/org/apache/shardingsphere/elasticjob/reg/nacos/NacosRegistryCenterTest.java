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

package org.apache.shardingsphere.elasticjob.reg.nacos;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.ConfigFuzzyWatchChangeEvent;
import com.alibaba.nacos.api.config.listener.FuzzyWatchEventWatcher;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.client.config.common.GroupKey;
import org.apache.shardingsphere.elasticjob.reg.base.transaction.TransactionOperation;
import org.apache.shardingsphere.elasticjob.reg.exception.RegException;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NacosRegistryCenterTest {
    
    private static final String GROUP = "test-namespace";
    
    private static final String KEY = "/job/config";
    
    @Mock
    private ConfigService configService;
    
    private NacosRegistryCenter registryCenter;
    
    @BeforeEach
    void setUp() {
        NacosConfiguration config = new NacosConfiguration("nacos://127.0.0.1:8848", GROUP);
        registryCenter = new NacosRegistryCenter(config, configService);
    }
    
    @Test
    void assertKeyCodecRoundtrip() {
        assertThat(NacosRegistryCenter.toOriginalKey(NacosRegistryCenter.toDataId("/job/config")), is("/job/config"));
        assertThat(NacosRegistryCenter.toOriginalKey(NacosRegistryCenter.toDataId("/job/servers/192.168.0.1@-@1234")), is("/job/servers/192.168.0.1@-@1234"));
        assertThat(NacosRegistryCenter.toOriginalKey(NacosRegistryCenter.toDataId("/job/中文")), is("/job/中文"));
        assertThat(NacosRegistryCenter.normalizeKey("/job/"), is("/job"));
        assertThat(NacosRegistryCenter.normalizeKey(""), is("/"));
        assertThat(NacosRegistryCenter.resolveServerAddr("nacos://127.0.0.1:8848"), is("127.0.0.1:8848"));
        assertThat(NacosRegistryCenter.resolveServerAddr("127.0.0.1:8848"), is("127.0.0.1:8848"));
    }
    
    @Test
    void assertPersistAndGet() throws Exception {
        String dataId = NacosRegistryCenter.toDataId(KEY);
        when(configService.getConfig(eq(dataId), eq(GROUP), anyLong())).thenReturn("value");
        registryCenter.persist(KEY, "value");
        verify(configService).publishConfig(eq(dataId), eq(GROUP), eq("value"));
        assertThat(registryCenter.get(KEY), is("value"));
        assertThat(registryCenter.getDirectly(KEY), is("value"));
    }
    
    @Test
    void assertUpdate() throws Exception {
        String dataId = NacosRegistryCenter.toDataId(KEY);
        when(configService.getConfig(eq(dataId), eq(GROUP), anyLong())).thenReturn("value-1");
        registryCenter.update(KEY, "value-1");
        verify(configService).publishConfig(eq(dataId), eq(GROUP), eq("value-1"));
        assertThat(registryCenter.get(KEY), is("value-1"));
    }
    
    @Test
    void assertIsExisted() throws Exception {
        String dataId = NacosRegistryCenter.toDataId(KEY);
        assertThat(registryCenter.isExisted("/"), is(true));
        when(configService.getConfig(eq(dataId), eq(GROUP), anyLong())).thenReturn(null);
        assertThat(registryCenter.isExisted(KEY), is(false));
        when(configService.getConfig(eq(dataId), eq(GROUP), anyLong())).thenReturn("value");
        assertThat(registryCenter.isExisted(KEY), is(true));
    }
    
    @Test
    void assertGetChildrenKeys() throws Exception {
        String parentDataId = NacosRegistryCenter.toDataId("/job");
        Set<String> groupKeys = new HashSet<>(Arrays.asList(
                GroupKey.getKey(NacosRegistryCenter.toDataId("/job/config"), GROUP),
                GroupKey.getKey(NacosRegistryCenter.toDataId("/job/servers/192.168.0.1"), GROUP),
                GroupKey.getKey(NacosRegistryCenter.toDataId("/job/servers/192.168.0.2"), GROUP)));
        when(configService.fuzzyWatchWithGroupKeys(eq(parentDataId + ".*"), eq(GROUP), any(FuzzyWatchEventWatcher.class)))
                .thenReturn(CompletableFuture.completedFuture(groupKeys));
        doNothing().when(configService).cancelFuzzyWatch(anyString(), anyString(), any(FuzzyWatchEventWatcher.class));
        List<String> children = registryCenter.getChildrenKeys("/job");
        assertThat(children.size(), is(2));
        assertTrue(children.contains("config"));
        assertTrue(children.contains("servers"));
        assertThat(registryCenter.getNumChildren("/job"), is(2));
    }
    
    @Test
    void assertGetChildrenKeysOfRoot() throws Exception {
        Set<String> groupKeys = new HashSet<>(Arrays.asList(
                GroupKey.getKey(NacosRegistryCenter.toDataId("/job-a/config"), GROUP),
                GroupKey.getKey(NacosRegistryCenter.toDataId("/job-b/config"), GROUP)));
        when(configService.fuzzyWatchWithGroupKeys(eq("*"), eq(GROUP), any(FuzzyWatchEventWatcher.class)))
                .thenReturn(CompletableFuture.completedFuture(groupKeys));
        doNothing().when(configService).cancelFuzzyWatch(anyString(), anyString(), any(FuzzyWatchEventWatcher.class));
        List<String> children = registryCenter.getChildrenKeys("/");
        assertThat(children.size(), is(2));
        assertTrue(children.contains("job-a"));
        assertTrue(children.contains("job-b"));
    }
    
    @Test
    void assertRemove() throws Exception {
        String dataId = NacosRegistryCenter.toDataId(KEY);
        when(configService.fuzzyWatchWithGroupKeys(eq(dataId + ".*"), eq(GROUP), any(FuzzyWatchEventWatcher.class)))
                .thenReturn(CompletableFuture.completedFuture(Collections.emptySet()));
        doNothing().when(configService).cancelFuzzyWatch(anyString(), anyString(), any(FuzzyWatchEventWatcher.class));
        when(configService.removeConfig(anyString(), eq(GROUP))).thenReturn(true);
        registryCenter.remove(KEY);
        verify(configService).removeConfig(eq(dataId), eq(GROUP));
    }
    
    @Test
    void assertPersistEphemeralAndClose() throws Exception {
        when(configService.removeConfig(anyString(), eq(GROUP))).thenReturn(true);
        when(configService.fuzzyWatchWithGroupKeys(anyString(), eq(GROUP), any(FuzzyWatchEventWatcher.class)))
                .thenReturn(CompletableFuture.completedFuture(Collections.emptySet()));
        doNothing().when(configService).cancelFuzzyWatch(anyString(), anyString(), any(FuzzyWatchEventWatcher.class));
        doNothing().when(configService).shutDown();
        String dataId = NacosRegistryCenter.toDataId(KEY);
        AtomicInteger getCalls = new AtomicInteger();
        when(configService.getConfig(eq(dataId), eq(GROUP), anyLong())).thenAnswer(invocation -> 0 == getCalls.getAndIncrement() ? "ephemeral" : null);
        registryCenter.persistEphemeral(KEY, "ephemeral");
        verify(configService).publishConfig(eq(dataId), eq(GROUP), eq("ephemeral"));
        registryCenter.close();
        verify(configService).removeConfig(eq(dataId), eq(GROUP));
    }
    
    @Test
    void assertPersistSequential() throws Exception {
        when(configService.publishConfig(anyString(), eq(GROUP), anyString())).thenReturn(true);
        when(configService.getConfig(anyString(), eq(GROUP), anyLong())).thenReturn("a", "b");
        String first = registryCenter.persistSequential("/job/seq-", "a");
        String second = registryCenter.persistSequential("/job/seq-", "b");
        assertThat(first, is(notNullValue()));
        assertThat(second, is(notNullValue()));
        assertTrue(!first.equals(second));
    }
    
    @Test
    void assertGetRegistryCenterTime() {
        assertTrue(registryCenter.getRegistryCenterTime("/job") > 0L);
    }
    
    @Test
    void assertExecuteInLeader() {
        AtomicInteger counter = new AtomicInteger();
        registryCenter.executeInLeader("/job/leader", counter::incrementAndGet);
        assertThat(counter.get(), is(1));
    }
    
    @Test
    void assertExecuteInTransaction() throws Exception {
        String existedDataId = NacosRegistryCenter.toDataId("/job/existed");
        String addedDataId = NacosRegistryCenter.toDataId("/job/added");
        when(configService.getConfig(eq(existedDataId), eq(GROUP), anyLong())).thenReturn("v", "v2");
        AtomicInteger addedGetCalls = new AtomicInteger();
        when(configService.getConfig(eq(addedDataId), eq(GROUP), anyLong())).thenAnswer(invocation -> 0 == addedGetCalls.getAndIncrement() ? "a" : null);
        when(configService.publishConfig(anyString(), eq(GROUP), anyString())).thenReturn(true);
        when(configService.removeConfig(anyString(), eq(GROUP))).thenReturn(true);
        when(configService.fuzzyWatchWithGroupKeys(anyString(), eq(GROUP), any(FuzzyWatchEventWatcher.class)))
                .thenReturn(CompletableFuture.completedFuture(Collections.emptySet()));
        doNothing().when(configService).cancelFuzzyWatch(anyString(), anyString(), any(FuzzyWatchEventWatcher.class));
        registryCenter.executeInTransaction(Arrays.asList(
                TransactionOperation.opCheckExists("/job/existed"),
                TransactionOperation.opAdd("/job/added", "a"),
                TransactionOperation.opUpdate("/job/existed", "v2"),
                TransactionOperation.opDelete("/job/added")));
        verify(configService).publishConfig(eq(NacosRegistryCenter.toDataId("/job/added")), eq(GROUP), eq("a"));
    }
    
    @Test
    void assertExecuteInTransactionCheckExistsFailure() throws Exception {
        when(configService.getConfig(anyString(), eq(GROUP), anyLong())).thenReturn(null);
        assertThrows(RegException.class, () -> registryCenter.executeInTransaction(
                Collections.singletonList(TransactionOperation.opCheckExists("/not-existed"))));
    }
    
    @Test
    void assertWatch() throws Exception {
        when(configService.fuzzyWatchWithGroupKeys(anyString(), eq(GROUP), any(FuzzyWatchEventWatcher.class)))
                .thenReturn(CompletableFuture.completedFuture(Collections.emptySet()));
        doNothing().when(configService).cancelFuzzyWatch(anyString(), anyString(), any(FuzzyWatchEventWatcher.class));
        List<DataChangedEvent> events = new CopyOnWriteArrayList<>();
        registryCenter.watch("/job", events::add, null);
        ArgumentCaptor<FuzzyWatchEventWatcher> watcherCaptor = ArgumentCaptor.forClass(FuzzyWatchEventWatcher.class);
        verify(configService).fuzzyWatch(eq(NacosRegistryCenter.toDataId("/job") + ".*"), eq(GROUP), watcherCaptor.capture());
        String childDataId = NacosRegistryCenter.toDataId("/job/config");
        when(configService.getConfig(eq(childDataId), eq(GROUP), anyLong())).thenReturn("v1");
        watcherCaptor.getValue().onEvent(ConfigFuzzyWatchChangeEvent.build("public", GROUP, childDataId, "ADD_CONFIG", "FUZZY_WATCH_RESOURCE_CHANGED"));
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getType(), is(DataChangedEvent.Type.ADDED));
        assertThat(events.get(0).getKey(), is("/job/config"));
        registryCenter.removeDataListeners("/job");
    }
    
    @Test
    void assertCache() throws Exception {
        String dataId = NacosRegistryCenter.toDataId(KEY);
        Set<String> groupKeys = new HashSet<>(Collections.singletonList(GroupKey.getKey(dataId, GROUP)));
        when(configService.fuzzyWatchWithGroupKeys(anyString(), eq(GROUP), any(FuzzyWatchEventWatcher.class)))
                .thenReturn(CompletableFuture.completedFuture(groupKeys));
        doNothing().when(configService).cancelFuzzyWatch(anyString(), anyString(), any(FuzzyWatchEventWatcher.class));
        when(configService.getConfig(eq(dataId), eq(GROUP), anyLong())).thenReturn("cached");
        registryCenter.addCacheData("/job");
        assertThat(registryCenter.get(KEY), is("cached"));
        Map<?, ?> rawCache = (Map<?, ?>) registryCenter.getRawCache("/job");
        assertThat(rawCache.get(KEY), is("cached"));
        ArgumentCaptor<Listener> listenerCaptor = ArgumentCaptor.forClass(Listener.class);
        verify(configService).addListener(eq(dataId), eq(GROUP), listenerCaptor.capture());
        listenerCaptor.getValue().receiveConfigInfo("updated");
        assertThat(registryCenter.get(KEY), is("updated"));
        registryCenter.evictCacheData("/job");
        verify(configService).removeListener(eq(dataId), eq(GROUP), any(Listener.class));
    }
    
    @Test
    void assertGetRawClient() {
        assertThat(registryCenter.getRawClient(), is(configService));
    }
    
    @Test
    void assertGetDirectlyOfRoot() {
        assertThat(registryCenter.getDirectly("/"), is(nullValue()));
    }
}
