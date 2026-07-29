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
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
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
    void assertCacheFallsBackToDirectReadAfterWatchCompletion() {
        GetResponse response = mockSnapshotResponse();
        when(kvClient.get(eq(toByteSequence(CACHE_PATH + "/")), any(GetOption.class))).thenReturn(CompletableFuture.completedFuture(response));
        registryCenter.addCacheData(CACHE_PATH);
        ArgumentCaptor<Watch.Listener> listenerCaptor = ArgumentCaptor.forClass(Watch.Listener.class);
        verify(watchClient).watch(eq(toByteSequence(CACHE_PATH + "/")), any(WatchOption.class), listenerCaptor.capture());
        listenerCaptor.getValue().onCompleted();
        GetResponse directResponse = mockDirectResponse("value-1");
        when(kvClient.get(toByteSequence(CACHE_KEY))).thenReturn(CompletableFuture.completedFuture(directResponse));
        assertThat(registryCenter.get(CACHE_KEY), is("value-1"));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertCacheRetainedAfterPreviousWatchCompletion() throws Exception {
        GetResponse initialResponse = mockSnapshotResponse();
        GetResponse refreshedResponse = mockSnapshotResponse();
        KeyValue refreshedKeyValue = refreshedResponse.getKvs().get(0);
        when(refreshedKeyValue.getValue()).thenReturn(toByteSequence("value-1"));
        when(kvClient.get(eq(toByteSequence(CACHE_PATH + "/")), any(GetOption.class))).thenReturn(
                CompletableFuture.completedFuture(initialResponse), CompletableFuture.completedFuture(refreshedResponse));
        Map<String, ByteSequence> cachedValues = new ConcurrentHashMap<>();
        Map<String, ByteSequence> blockingCache = mock(Map.class, AdditionalAnswers.delegatesTo(cachedValues));
        CountDownLatch evictionStarted = new CountDownLatch(1);
        CountDownLatch continueEviction = new CountDownLatch(1);
        doAnswer(invocation -> {
            evictionStarted.countDown();
            if (!continueEviction.await(5L, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting to continue cache eviction");
            }
            return cachedValues.remove((String) invocation.getArgument(0));
        }).when(blockingCache).remove(CACHE_KEY);
        Plugins.getMemberAccessor().set(EtcdRegistryCenter.class.getDeclaredField("cache"), registryCenter, blockingCache);
        registryCenter.addCacheData(CACHE_PATH);
        ArgumentCaptor<Watch.Listener> listenerCaptor = ArgumentCaptor.forClass(Watch.Listener.class);
        verify(watchClient).watch(eq(toByteSequence(CACHE_PATH + "/")), any(WatchOption.class), listenerCaptor.capture());
        FutureTask<Void> completionTask = new FutureTask<>(() -> {
            listenerCaptor.getValue().onCompleted();
            return null;
        });
        Thread completionThread = new Thread(completionTask);
        completionThread.start();
        assertThat(evictionStarted.await(5L, TimeUnit.SECONDS), is(true));
        FutureTask<Void> registrationTask = new FutureTask<>(() -> {
            registryCenter.addCacheData(CACHE_PATH);
            return null;
        });
        Thread registrationThread = new Thread(registrationTask);
        registrationThread.start();
        try {
            Awaitility.await().atMost(5L, TimeUnit.SECONDS).until(() -> {
                ByteSequence cachedValue = cachedValues.get(CACHE_KEY);
                boolean refreshedValueCached = null != cachedValue && "value-1".equals(cachedValue.toString(StandardCharsets.UTF_8));
                ThreadInfo threadInfo = ManagementFactory.getThreadMXBean().getThreadInfo(registrationThread.getId());
                boolean registrationBlockedByCompletion =
                        null != threadInfo && Thread.State.BLOCKED == threadInfo.getThreadState() && completionThread.getId() == threadInfo.getLockOwnerId();
                return refreshedValueCached || registrationBlockedByCompletion;
            });
        } finally {
            continueEviction.countDown();
            completionTask.get(5L, TimeUnit.SECONDS);
            registrationTask.get(5L, TimeUnit.SECONDS);
        }
        Map<String, ByteSequence> actualCache = (Map<String, ByteSequence>) registryCenter.getRawCache(CACHE_PATH);
        assertThat(actualCache.get(CACHE_KEY), is(toByteSequence("value-1")));
    }
    
    @Test
    void assertCacheLocksAreBounded() throws ReflectiveOperationException {
        GetResponse response = mock(GetResponse.class);
        when(response.getKvs()).thenReturn(Collections.emptyList());
        Response.Header header = mock(Response.Header.class);
        when(header.getRevision()).thenReturn(10L);
        when(response.getHeader()).thenReturn(header);
        when(kvClient.get(any(ByteSequence.class), any(GetOption.class))).thenReturn(CompletableFuture.completedFuture(response));
        int cachePathCount = 128;
        for (int each = 0; each < cachePathCount; each++) {
            String cachePath = "/job-" + each;
            registryCenter.addCacheData(cachePath);
            registryCenter.evictCacheData(cachePath);
        }
        Object cacheLocks = Plugins.getMemberAccessor().get(EtcdRegistryCenter.class.getDeclaredField("cacheLocks"), registryCenter);
        int actualCacheLockCount = cacheLocks instanceof Map ? ((Map<?, ?>) cacheLocks).size() : ((Object[]) cacheLocks).length;
        assertThat(actualCacheLockCount, lessThan(cachePathCount));
    }
    
    @Test
    void assertCacheRetainsUnchangedDataAfterWatchReconnect() {
        GetResponse response = mockSnapshotResponse();
        KeyValue cachedKeyValue = response.getKvs().get(0);
        KeyValue unchangedKeyValue = mock(KeyValue.class);
        when(unchangedKeyValue.getKey()).thenReturn(toByteSequence(CACHE_PATH + "/unchanged"));
        when(unchangedKeyValue.getValue()).thenReturn(toByteSequence("stable"));
        when(response.getKvs()).thenReturn(Arrays.asList(cachedKeyValue, unchangedKeyValue));
        when(kvClient.get(eq(toByteSequence(CACHE_PATH + "/")), any(GetOption.class))).thenReturn(CompletableFuture.completedFuture(response));
        registryCenter.addCacheData(CACHE_PATH);
        ArgumentCaptor<Watch.Listener> listenerCaptor = ArgumentCaptor.forClass(Watch.Listener.class);
        verify(watchClient).watch(eq(toByteSequence(CACHE_PATH + "/")), any(WatchOption.class), listenerCaptor.capture());
        listenerCaptor.getValue().onError(mock(Throwable.class));
        listenerCaptor.getValue().onNext(mockWatchResponse(WatchEvent.EventType.PUT, "value-1"));
        assertThat(registryCenter.get(CACHE_KEY), is("value-1"));
        assertThat(registryCenter.get(CACHE_PATH + "/unchanged"), is("stable"));
    }
    
    @Test
    void assertDifferentCacheSnapshotsDoNotBlockEachOther() throws Exception {
        CompletableFuture<GetResponse> firstSnapshot = new CompletableFuture<>();
        CountDownLatch firstSnapshotStarted = new CountDownLatch(1);
        doAnswer(invocation -> {
            firstSnapshotStarted.countDown();
            return firstSnapshot;
        }).when(kvClient).get(eq(toByteSequence("/job-a/")), any(GetOption.class));
        GetResponse emptySnapshot = mock(GetResponse.class);
        when(emptySnapshot.getKvs()).thenReturn(Collections.emptyList());
        Response.Header header = mock(Response.Header.class);
        when(header.getRevision()).thenReturn(10L);
        when(emptySnapshot.getHeader()).thenReturn(header);
        when(kvClient.get(eq(toByteSequence("/job-b/")), any(GetOption.class))).thenReturn(CompletableFuture.completedFuture(emptySnapshot));
        CompletableFuture<Void> firstRegistration = CompletableFuture.runAsync(() -> registryCenter.addCacheData("/job-a"));
        assertThat(firstSnapshotStarted.await(5L, TimeUnit.SECONDS), is(true));
        CompletableFuture<Void> secondRegistration = CompletableFuture.runAsync(() -> registryCenter.addCacheData("/job-b"));
        try {
            secondRegistration.get(5L, TimeUnit.SECONDS);
        } finally {
            firstSnapshot.complete(emptySnapshot);
            firstRegistration.get(5L, TimeUnit.SECONDS);
        }
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
    
    @Test
    void assertCacheWatcherCloseWithoutDeadlockOnWatchError() throws InterruptedException {
        GetResponse response = mockSnapshotResponse();
        when(kvClient.get(eq(toByteSequence(CACHE_PATH + "/")), any(GetOption.class))).thenReturn(CompletableFuture.completedFuture(response));
        registryCenter.addCacheData(CACHE_PATH);
        ArgumentCaptor<Watch.Listener> listenerCaptor = ArgumentCaptor.forClass(Watch.Listener.class);
        verify(watchClient).watch(eq(toByteSequence(CACHE_PATH + "/")), any(WatchOption.class), listenerCaptor.capture());
        Object watchLock = new Object();
        CountDownLatch watchLockAcquired = new CountDownLatch(1);
        CountDownLatch closeStarted = new CountDownLatch(1);
        CompletableFuture<Void> continueErrorCallback = new CompletableFuture<>();
        doAnswer(invocation -> {
            closeStarted.countDown();
            synchronized (watchLock) {
                return null;
            }
        }).when(watcher).close();
        CountDownLatch errorCallbackCompleted = new CountDownLatch(1);
        CompletableFuture.runAsync(() -> {
            synchronized (watchLock) {
                watchLockAcquired.countDown();
                continueErrorCallback.join();
                listenerCaptor.getValue().onError(new RuntimeException("watch failed"));
            }
            errorCallbackCompleted.countDown();
        });
        assertThat(watchLockAcquired.await(5L, TimeUnit.SECONDS), is(true));
        CountDownLatch evictionCompleted = new CountDownLatch(1);
        CompletableFuture.runAsync(() -> {
            registryCenter.evictCacheData(CACHE_PATH);
            evictionCompleted.countDown();
        });
        assertThat(closeStarted.await(5L, TimeUnit.SECONDS), is(true));
        continueErrorCallback.complete(null);
        assertThat(errorCallbackCompleted.await(5L, TimeUnit.SECONDS), is(true));
        assertThat(evictionCompleted.await(5L, TimeUnit.SECONDS), is(true));
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
