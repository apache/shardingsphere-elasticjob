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

package org.apache.shardingsphere.elasticjob.reg.etcd;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Response;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.Watch.Watcher;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.internal.configuration.plugins.Plugins;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EtcdRegistryCenterTest {
    
    private static final String CACHE_PATH = "/job";
    
    private static final String CACHE_KEY = "/job/config";
    
    private EtcdRegistryCenter registryCenter;
    
    @Mock
    private Client client;
    
    @Mock
    private KV kvClient;
    
    @Mock
    private Watch watchClient;
    
    @Mock
    private Watcher watcher;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        registryCenter = new EtcdRegistryCenter(new EtcdConfiguration("http://localhost:2379", "test"));
        Plugins.getMemberAccessor().set(EtcdRegistryCenter.class.getDeclaredField("client"), registryCenter, client);
        Plugins.getMemberAccessor().set(EtcdRegistryCenter.class.getDeclaredField("kvClient"), registryCenter, kvClient);
        when(client.getWatchClient()).thenReturn(watchClient);
        when(watchClient.watch(any(ByteSequence.class), any(WatchOption.class), any(Watch.Listener.class))).thenReturn(watcher);
    }
    
    @Test
    void assertCacheWatchStartsAfterSnapshot() {
        GetResponse response = mockSnapshotResponse();
        when(kvClient.get(eq(toByteSequence(CACHE_PATH + "/")), any(GetOption.class))).thenReturn(CompletableFuture.completedFuture(response));
        registryCenter.addCacheData(CACHE_PATH);
        ArgumentCaptor<WatchOption> optionCaptor = ArgumentCaptor.forClass(WatchOption.class);
        verify(watchClient).watch(eq(toByteSequence(CACHE_PATH + "/")), optionCaptor.capture(), any(Watch.Listener.class));
        assertThat(optionCaptor.getValue().getRevision(), is(11L));
        assertThat(optionCaptor.getValue().isPrefix(), is(true));
    }
    
    @Test
    void assertCacheReflectsPutEvent() {
        GetResponse response = mockSnapshotResponse();
        when(kvClient.get(eq(toByteSequence(CACHE_PATH + "/")), any(GetOption.class))).thenReturn(CompletableFuture.completedFuture(response));
        registryCenter.addCacheData(CACHE_PATH);
        ArgumentCaptor<Watch.Listener> listenerCaptor = ArgumentCaptor.forClass(Watch.Listener.class);
        verify(watchClient).watch(eq(toByteSequence(CACHE_PATH + "/")), any(WatchOption.class), listenerCaptor.capture());
        listenerCaptor.getValue().onNext(mockWatchResponse(WatchEvent.EventType.PUT, "value-1"));
        assertThat(registryCenter.get(CACHE_KEY), is("value-1"));
    }
    
    @Test
    void assertCacheReflectsDeleteEvent() {
        GetResponse response = mockSnapshotResponse();
        when(kvClient.get(eq(toByteSequence(CACHE_PATH + "/")), any(GetOption.class))).thenReturn(CompletableFuture.completedFuture(response));
        registryCenter.addCacheData(CACHE_PATH);
        ArgumentCaptor<Watch.Listener> listenerCaptor = ArgumentCaptor.forClass(Watch.Listener.class);
        verify(watchClient).watch(eq(toByteSequence(CACHE_PATH + "/")), any(WatchOption.class), listenerCaptor.capture());
        listenerCaptor.getValue().onNext(mockWatchResponse(WatchEvent.EventType.DELETE, ""));
        GetResponse directResponse = mock(GetResponse.class);
        when(directResponse.getKvs()).thenReturn(Collections.emptyList());
        when(kvClient.get(toByteSequence(CACHE_KEY))).thenReturn(CompletableFuture.completedFuture(directResponse));
        assertNull(registryCenter.get(CACHE_KEY));
    }
    
    @Test
    void assertCacheFallsBackToDirectReadAfterWatchError() {
        GetResponse response = mockSnapshotResponse();
        when(kvClient.get(eq(toByteSequence(CACHE_PATH + "/")), any(GetOption.class))).thenReturn(CompletableFuture.completedFuture(response));
        registryCenter.addCacheData(CACHE_PATH);
        ArgumentCaptor<Watch.Listener> listenerCaptor = ArgumentCaptor.forClass(Watch.Listener.class);
        verify(watchClient).watch(eq(toByteSequence(CACHE_PATH + "/")), any(WatchOption.class), listenerCaptor.capture());
        listenerCaptor.getValue().onError(mock(Throwable.class));
        GetResponse directResponse = mockDirectResponse("value-1");
        when(kvClient.get(toByteSequence(CACHE_KEY))).thenReturn(CompletableFuture.completedFuture(directResponse));
        assertThat(registryCenter.get(CACHE_KEY), is("value-1"));
    }
    
    @Test
    void assertCacheRecoversAfterWatchReconnect() {
        GetResponse response = mockSnapshotResponse();
        when(kvClient.get(eq(toByteSequence(CACHE_PATH + "/")), any(GetOption.class))).thenReturn(CompletableFuture.completedFuture(response));
        registryCenter.addCacheData(CACHE_PATH);
        ArgumentCaptor<Watch.Listener> listenerCaptor = ArgumentCaptor.forClass(Watch.Listener.class);
        verify(watchClient).watch(eq(toByteSequence(CACHE_PATH + "/")), any(WatchOption.class), listenerCaptor.capture());
        listenerCaptor.getValue().onError(mock(Throwable.class));
        listenerCaptor.getValue().onNext(mockWatchResponse(WatchEvent.EventType.PUT, "value-1"));
        assertThat(registryCenter.get(CACHE_KEY), is("value-1"));
    }
    
    @Test
    void assertEvictedCacheIgnoresLateWatchEvent() {
        GetResponse response = mockSnapshotResponse();
        when(kvClient.get(eq(toByteSequence(CACHE_PATH + "/")), any(GetOption.class))).thenReturn(CompletableFuture.completedFuture(response));
        registryCenter.addCacheData(CACHE_PATH);
        ArgumentCaptor<Watch.Listener> listenerCaptor = ArgumentCaptor.forClass(Watch.Listener.class);
        verify(watchClient).watch(eq(toByteSequence(CACHE_PATH + "/")), any(WatchOption.class), listenerCaptor.capture());
        registryCenter.evictCacheData(CACHE_PATH);
        listenerCaptor.getValue().onNext(mock(WatchResponse.class));
        GetResponse directResponse = mockDirectResponse("value-current");
        when(kvClient.get(toByteSequence(CACHE_KEY))).thenReturn(CompletableFuture.completedFuture(directResponse));
        assertThat(registryCenter.get(CACHE_KEY), is("value-current"));
        verify(watcher).close();
    }
    
    @Test
    void assertCloseClosesCacheWatcher() {
        GetResponse response = mockSnapshotResponse();
        when(kvClient.get(eq(toByteSequence(CACHE_PATH + "/")), any(GetOption.class))).thenReturn(CompletableFuture.completedFuture(response));
        registryCenter.addCacheData(CACHE_PATH);
        registryCenter.close();
        verify(watcher).close();
        verify(client).close();
    }
    
    private GetResponse mockSnapshotResponse() {
        GetResponse result = mock(GetResponse.class);
        KeyValue keyValue = mock(KeyValue.class);
        when(keyValue.getKey()).thenReturn(toByteSequence(CACHE_KEY));
        when(keyValue.getValue()).thenReturn(toByteSequence("value-0"));
        when(result.getKvs()).thenReturn(Collections.singletonList(keyValue));
        Response.Header header = mock(Response.Header.class);
        when(header.getRevision()).thenReturn(10L);
        when(result.getHeader()).thenReturn(header);
        return result;
    }
    
    private GetResponse mockDirectResponse(final String value) {
        GetResponse result = mock(GetResponse.class);
        KeyValue keyValue = mock(KeyValue.class);
        when(keyValue.getValue()).thenReturn(toByteSequence(value));
        when(result.getKvs()).thenReturn(Collections.singletonList(keyValue));
        return result;
    }
    
    private WatchResponse mockWatchResponse(final WatchEvent.EventType type, final String value) {
        WatchEvent event = mock(WatchEvent.class);
        when(event.getEventType()).thenReturn(type);
        KeyValue keyValue = mock(KeyValue.class);
        when(keyValue.getKey()).thenReturn(toByteSequence(CACHE_KEY));
        if (WatchEvent.EventType.PUT == type) {
            when(keyValue.getValue()).thenReturn(toByteSequence(value));
        }
        when(event.getKeyValue()).thenReturn(keyValue);
        WatchResponse result = mock(WatchResponse.class);
        when(result.getEvents()).thenReturn(Collections.singletonList(event));
        return result;
    }
    
    private ByteSequence toByteSequence(final String value) {
        return ByteSequence.from(value, StandardCharsets.UTF_8);
    }
}
