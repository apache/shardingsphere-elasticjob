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

import com.google.common.base.Strings;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.ClientBuilder;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.Lock;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.Watch.Watcher;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lock.LockResponse;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.base.LeaderExecutionCallback;
import org.apache.shardingsphere.elasticjob.reg.base.transaction.TransactionOperation;
import org.apache.shardingsphere.elasticjob.reg.exception.RegException;
import org.apache.shardingsphere.elasticjob.reg.exception.RegExceptionHandler;
import org.apache.shardingsphere.elasticjob.reg.listener.ConnectionStateChangedEventListener;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEventListener;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Registry center of etcd.
 */
@Slf4j
public final class EtcdRegistryCenter implements CoordinatorRegistryCenter {
    
    @Getter(AccessLevel.PROTECTED)
    private final EtcdConfiguration etcdConfig;
    
    private final Map<String, ByteSequence> cache = new ConcurrentHashMap<>();
    
    private final Map<String, List<Watcher>> watches = new ConcurrentHashMap<>();
    
    private final Map<String, List<ConnectionStateChangedEventListener>> connStateListeners = new ConcurrentHashMap<>();
    
    private final Map<Long, Long> leaseIdMap = new ConcurrentHashMap<>();
    
    private final AtomicLong sequenceCounter = new AtomicLong(0);
    
    @Getter
    private Client client;
    
    private KV kvClient;
    
    private Lease leaseClient;
    
    private Lock lockClient;
    
    public EtcdRegistryCenter(final EtcdConfiguration etcdConfig) {
        this.etcdConfig = etcdConfig;
    }
    
    @Override
    public void init() {
        log.debug("Elastic job: etcd registry center init, server lists is: {}.", etcdConfig.getServerLists());
        try {
            ClientBuilder builder = Client.builder()
                    .endpoints(etcdConfig.getServerLists().split(","))
                    .connectTimeout(Duration.ofMillis(etcdConfig.getConnectionTimeoutMilliseconds()));
            if (!Strings.isNullOrEmpty(etcdConfig.getUsername()) && !Strings.isNullOrEmpty(etcdConfig.getPassword())) {
                builder.user(ByteSequence.from(etcdConfig.getUsername(), StandardCharsets.UTF_8));
                builder.password(ByteSequence.from(etcdConfig.getPassword(), StandardCharsets.UTF_8));
            }
            if (!Strings.isNullOrEmpty(etcdConfig.getAuthority())) {
                builder.authority(etcdConfig.getAuthority());
            }
            client = builder.build();
            kvClient = client.getKVClient();
            leaseClient = client.getLeaseClient();
            lockClient = client.getLockClient();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public void close() {
        for (Long leaseId : leaseIdMap.values()) {
            try {
                leaseClient.revoke(leaseId).get();
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.warn("Failed to revoke lease: {}", leaseId, ex);
            }
        }
        leaseIdMap.clear();
        cache.clear();
        for (List<Watcher> watchList : watches.values()) {
            watchList.forEach(Watcher::close);
        }
        watches.clear();
        if (client != null) {
            client.close();
        }
    }
    
    @Override
    public String get(final String key) {
        ByteSequence cachedValue = cache.get(key);
        if (cachedValue != null) {
            return cachedValue.toString(StandardCharsets.UTF_8);
        }
        return getDirectly(key);
    }
    
    @Override
    public String getDirectly(final String key) {
        try {
            GetResponse response = kvClient.get(toByteSequence(key)).get();
            if (response.getKvs().isEmpty()) {
                return null;
            }
            return response.getKvs().get(0).getValue().toString(StandardCharsets.UTF_8);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
            return null;
        }
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        try {
            String prefix = key.endsWith("/") ? key : key + "/";
            GetOption option = GetOption.newBuilder()
                    .withPrefix(toByteSequence(prefix))
                    .withSortField(GetOption.SortTarget.KEY)
                    .withSortOrder(GetOption.SortOrder.DESCEND)
                    .build();
            GetResponse response = kvClient.get(toByteSequence(prefix), option).get();
            List<String> result = new ArrayList<>();
            for (KeyValue kv : response.getKvs()) {
                String childKey = kv.getKey().toString(StandardCharsets.UTF_8);
                String relativeKey = childKey.substring(prefix.length());
                if (!relativeKey.isEmpty() && !relativeKey.contains("/")) {
                    result.add(relativeKey);
                }
            }
            result.sort(Comparator.reverseOrder());
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
            return Collections.emptyList();
        }
    }
    
    @Override
    public int getNumChildren(final String key) {
        return getChildrenKeys(key).size();
    }
    
    @Override
    public boolean isExisted(final String key) {
        try {
            GetResponse response = kvClient.get(toByteSequence(key)).get();
            return !response.getKvs().isEmpty();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
            return false;
        }
    }
    
    @Override
    public void persist(final String key, final String value) {
        try {
            kvClient.put(toByteSequence(key), toByteSequence(value)).get();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public void update(final String key, final String value) {
        try {
            kvClient.put(toByteSequence(key), toByteSequence(value)).get();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public void persistEphemeral(final String key, final String value) {
        try {
            long leaseId = createLease(30);
            kvClient.put(toByteSequence(key), toByteSequence(value), PutOption.newBuilder().withLeaseId(leaseId).build()).get();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public String persistSequential(final String key, final String value) {
        try {
            long seq = sequenceCounter.incrementAndGet();
            String sequentialKey = key + String.format("%010d", seq);
            kvClient.put(toByteSequence(sequentialKey), toByteSequence(value)).get();
            return sequentialKey;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
            return null;
        }
    }
    
    @Override
    public void persistEphemeralSequential(final String key) {
        try {
            long leaseId = createLease(30);
            long seq = sequenceCounter.incrementAndGet();
            String sequentialKey = key + String.format("%010d", seq);
            kvClient.put(toByteSequence(sequentialKey), toByteSequence(""), PutOption.newBuilder().withLeaseId(leaseId).build()).get();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public void remove(final String key) {
        try {
            kvClient.delete(toByteSequence(key), DeleteOption.newBuilder().withPrefix(toByteSequence(key)).build()).get();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public long getRegistryCenterTime(final String key) {
        try {
            persist(key, "");
            GetResponse response = kvClient.get(toByteSequence(key)).get();
            if (!response.getKvs().isEmpty()) {
                return response.getKvs().get(0).getModRevision();
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
        throw new RegException(new IllegalStateException("Cannot get registry center time."));
    }
    
    @Override
    public Object getRawClient() {
        return client;
    }
    
    @Override
    public void executeInLeader(final String key, final LeaderExecutionCallback callback) {
        try {
            ByteSequence lockKey = toByteSequence(key);
            LockResponse lockResponse = lockClient.lock(lockKey, 30).get();
            try {
                callback.execute();
            } finally {
                lockClient.unlock(lockResponse.getKey()).get();
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            handleException(ex);
        }
    }
    
    @Override
    public void executeInTransaction(final List<TransactionOperation> transactionOperations) throws Exception {
        for (TransactionOperation operation : transactionOperations) {
            switch (operation.getType()) {
                case CHECK_EXISTS:
                    if (!isExisted(operation.getKey())) {
                        throw new RegException(new IllegalStateException("Key does not exist: " + operation.getKey()));
                    }
                    break;
                case ADD:
                    persist(operation.getKey(), operation.getValue());
                    break;
                case UPDATE:
                    update(operation.getKey(), operation.getValue());
                    break;
                case DELETE:
                    remove(operation.getKey());
                    break;
                default:
                    throw new UnsupportedOperationException(operation.toString());
            }
        }
    }
    
    @Override
    public void addCacheData(final String cachePath) {
        try {
            String prefix = cachePath.endsWith("/") ? cachePath : cachePath + "/";
            GetOption option = GetOption.newBuilder()
                    .withPrefix(toByteSequence(prefix))
                    .build();
            GetResponse response = kvClient.get(toByteSequence(prefix), option).get();
            for (KeyValue kv : response.getKvs()) {
                cache.put(kv.getKey().toString(StandardCharsets.UTF_8), kv.getValue());
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public void evictCacheData(final String cachePath) {
        String prefix = cachePath.endsWith("/") ? cachePath : cachePath + "/";
        cache.entrySet().removeIf(entry -> entry.getKey().startsWith(prefix));
    }
    
    @Override
    public Object getRawCache(final String cachePath) {
        return cache;
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener listener, final Executor executor) {
        String prefix = key.endsWith("/") ? key : key + "/";
        WatchOption option = WatchOption.newBuilder()
                .withPrefix(toByteSequence(prefix))
                .build();
        Watcher watcher = client.getWatchClient().watch(toByteSequence(prefix), option, new EtcdWatchListener(key, listener, executor));
        watches.computeIfAbsent(key, k -> new ArrayList<>()).add(watcher);
    }
    
    @Override
    public void removeDataListeners(final String key) {
        List<Watcher> watchList = watches.remove(key);
        if (watchList != null) {
            watchList.forEach(Watcher::close);
        }
    }
    
    @Override
    public void addConnectionStateChangedEventListener(final String key,
                                                       final ConnectionStateChangedEventListener listener) {
        connStateListeners.computeIfAbsent(key, k -> new ArrayList<>()).add(listener);
    }
    
    @Override
    public void removeConnStateListener(final String key) {
        connStateListeners.remove(key);
    }
    
    private long createLease(final long ttlSeconds) {
        try {
            LeaseGrantResponse response = leaseClient.grant(ttlSeconds).get();
            long leaseId = response.getID();
            leaseIdMap.put(leaseId, leaseId);
            return leaseId;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw new RegException(ex);
        }
    }
    
    private ByteSequence toByteSequence(final String value) {
        return ByteSequence.from(value, StandardCharsets.UTF_8);
    }
    
    private Type getTypeFromWatchEvent(final WatchEvent event) {
        switch (event.getEventType()) {
            case PUT:
                return event.getPrevKV().getValue().isEmpty() ? Type.ADDED : Type.UPDATED;
            case DELETE:
                return Type.DELETED;
            default:
                return Type.IGNORED;
        }
    }
    
    private void handleException(final Exception ex) {
        if (ex instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        } else {
            throw new RegException(ex);
        }
    }
    
    private class EtcdWatchListener implements Watch.Listener {
        
        private final String key;
        
        private final DataChangedEventListener listener;
        
        private final Executor executor;
        
        EtcdWatchListener(final String key, final DataChangedEventListener listener, final Executor executor) {
            this.key = key;
            this.listener = listener;
            this.executor = executor;
        }
        
        @Override
        public void onNext(final WatchResponse response) {
            for (WatchEvent event : response.getEvents()) {
                Type type = getTypeFromWatchEvent(event);
                String eventKey = event.getKeyValue().getKey().toString(StandardCharsets.UTF_8);
                String eventValue = event.getKeyValue().getValue().toString(StandardCharsets.UTF_8);
                if (executor != null) {
                    executor.execute(() -> listener.onChange(new DataChangedEvent(type, eventKey, eventValue)));
                } else {
                    listener.onChange(new DataChangedEvent(type, eventKey, eventValue));
                }
            }
        }
        
        @Override
        public void onError(final Throwable throwable) {
            log.error("Watch error for key: {}", key, throwable);
        }
        
        @Override
        public void onCompleted() {
            log.debug("Watch completed for key: {}", key);
        }
    }
}
