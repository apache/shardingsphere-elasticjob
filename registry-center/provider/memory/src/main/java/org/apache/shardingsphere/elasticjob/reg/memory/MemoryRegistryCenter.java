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

package org.apache.shardingsphere.elasticjob.reg.memory;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.base.LeaderExecutionCallback;
import org.apache.shardingsphere.elasticjob.reg.base.transaction.TransactionOperation;
import org.apache.shardingsphere.elasticjob.reg.exception.RegException;
import org.apache.shardingsphere.elasticjob.reg.listener.ConnectionStateChangedEventListener;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Registry center of memory.
 *
 * <p>All instances sharing the same namespace share the same in-memory data.
 * It is designed for unit tests and local development without any external server.</p>
 */
@Slf4j
public final class MemoryRegistryCenter implements CoordinatorRegistryCenter {
    
    private static final Map<String, ConcurrentHashMap<String, String>> NAMESPACE_DATA = new ConcurrentHashMap<>();
    
    private static final Map<String, CopyOnWriteArrayList<MemoryWatcher>> NAMESPACE_WATCHERS = new ConcurrentHashMap<>();
    
    private static final Map<String, ReentrantLock> LEADER_LOCKS = new ConcurrentHashMap<>();
    
    private static final AtomicLong SEQUENCE_COUNTER = new AtomicLong(0);
    
    @Getter(AccessLevel.PROTECTED)
    private final MemoryConfiguration memoryConfig;
    
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    
    private final Set<String> cachedPrefixes = ConcurrentHashMap.newKeySet();
    
    private final Set<String> ephemeralKeys = ConcurrentHashMap.newKeySet();
    
    private final Set<String> watchedKeys = ConcurrentHashMap.newKeySet();
    
    private final Map<String, List<ConnectionStateChangedEventListener>> connStateListeners = new ConcurrentHashMap<>();
    
    public MemoryRegistryCenter(final MemoryConfiguration memoryConfig) {
        this.memoryConfig = memoryConfig;
    }
    
    @Override
    public void init() {
        log.debug("Elastic job: memory registry center init, namespace is: {}.", memoryConfig.getNamespace());
        getDataMap();
        getWatcherList();
    }
    
    @Override
    public void close() {
        for (String each : new ArrayList<>(ephemeralKeys)) {
            remove(each);
        }
        ephemeralKeys.clear();
        cache.clear();
        cachedPrefixes.clear();
        CopyOnWriteArrayList<MemoryWatcher> watchers = NAMESPACE_WATCHERS.get(memoryConfig.getNamespace());
        if (null != watchers) {
            watchers.removeIf(each -> each.getOwner() == this);
        }
        watchedKeys.clear();
    }
    
    @Override
    public String get(final String key) {
        String cachedValue = cache.get(key);
        if (null != cachedValue) {
            return cachedValue;
        }
        return getDirectly(key);
    }
    
    @Override
    public String getDirectly(final String key) {
        return getDataMap().get(key);
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        String prefix = key.endsWith("/") ? key : key + "/";
        Set<String> result = ConcurrentHashMap.newKeySet();
        for (String each : getDataMap().keySet()) {
            if (!each.startsWith(prefix)) {
                continue;
            }
            String relativeKey = each.substring(prefix.length());
            if (relativeKey.isEmpty()) {
                continue;
            }
            int slashIndex = relativeKey.indexOf('/');
            String immediateChild = -1 == slashIndex ? relativeKey : relativeKey.substring(0, slashIndex);
            if (!immediateChild.isEmpty()) {
                result.add(immediateChild);
            }
        }
        List<String> sortedResult = new ArrayList<>(result);
        sortedResult.sort(Comparator.reverseOrder());
        return sortedResult;
    }
    
    @Override
    public int getNumChildren(final String key) {
        return getChildrenKeys(key).size();
    }
    
    @Override
    public boolean isExisted(final String key) {
        if ("/".equals(key) || "".equals(key)) {
            return true;
        }
        return getDataMap().containsKey(key);
    }
    
    @Override
    public void persist(final String key, final String value) {
        String previous = getDataMap().put(key, value);
        refreshCacheIfWatched(key, value);
        fireEvent(key, null == previous ? Type.ADDED : Type.UPDATED, value);
    }
    
    @Override
    public void update(final String key, final String value) {
        persist(key, value);
    }
    
    @Override
    public void persistEphemeral(final String key, final String value) {
        persist(key, value);
        ephemeralKeys.add(key);
    }
    
    @Override
    public String persistSequential(final String key, final String value) {
        String sequentialKey = key + String.format("%010d", SEQUENCE_COUNTER.incrementAndGet());
        persist(sequentialKey, value);
        return sequentialKey;
    }
    
    @Override
    public void persistEphemeralSequential(final String key) {
        String sequentialKey = key + String.format("%010d", SEQUENCE_COUNTER.incrementAndGet());
        persist(sequentialKey, "");
        ephemeralKeys.add(sequentialKey);
    }
    
    @Override
    public void remove(final String key) {
        if ("/".equals(key) || "".equals(key)) {
            for (String each : new ArrayList<>(getDataMap().keySet())) {
                removeSingle(each);
            }
            return;
        }
        String prefix = key + "/";
        List<String> toRemove = new ArrayList<>();
        for (String each : getDataMap().keySet()) {
            if (each.equals(key) || each.startsWith(prefix)) {
                toRemove.add(each);
            }
        }
        toRemove.sort(Comparator.reverseOrder());
        for (String each : toRemove) {
            removeSingle(each);
        }
    }
    
    private void removeSingle(final String key) {
        String removed = getDataMap().remove(key);
        if (null == removed) {
            return;
        }
        cache.remove(key);
        ephemeralKeys.remove(key);
        fireEvent(key, Type.DELETED, removed);
    }
    
    @Override
    public long getRegistryCenterTime(final String key) {
        return System.currentTimeMillis();
    }
    
    @Override
    public Object getRawClient() {
        return getDataMap();
    }
    
    @Override
    public void executeInLeader(final String key, final LeaderExecutionCallback callback) {
        ReentrantLock lock = LEADER_LOCKS.computeIfAbsent(memoryConfig.getNamespace() + key, ignored -> new ReentrantLock());
        lock.lock();
        try {
            callback.execute();
        } finally {
            lock.unlock();
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
        String prefix = cachePath.endsWith("/") ? cachePath : cachePath + "/";
        cachedPrefixes.add(prefix);
        for (Map.Entry<String, String> entry : getDataMap().entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                cache.put(entry.getKey(), entry.getValue());
            }
        }
    }
    
    @Override
    public void evictCacheData(final String cachePath) {
        String prefix = cachePath.endsWith("/") ? cachePath : cachePath + "/";
        cachedPrefixes.remove(prefix);
        cache.keySet().removeIf(each -> each.startsWith(prefix));
    }
    
    @Override
    public Object getRawCache(final String cachePath) {
        return cache;
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener listener, final Executor executor) {
        String normalized = normalizeWatchKey(key);
        watchedKeys.add(normalized);
        getWatcherList().add(new MemoryWatcher(this, normalized, listener, executor));
    }
    
    @Override
    public void removeDataListeners(final String key) {
        String normalized = normalizeWatchKey(key);
        watchedKeys.remove(normalized);
        CopyOnWriteArrayList<MemoryWatcher> watchers = NAMESPACE_WATCHERS.get(memoryConfig.getNamespace());
        if (null != watchers) {
            watchers.removeIf(each -> each.getOwner() == this && each.getWatchedKey().equals(normalized));
        }
    }
    
    @Override
    public void addConnectionStateChangedEventListener(final String key, final ConnectionStateChangedEventListener listener) {
        connStateListeners.computeIfAbsent(key, ignored -> new ArrayList<>()).add(listener);
    }
    
    @Override
    public void removeConnStateListener(final String key) {
        connStateListeners.remove(key);
    }
    
    private ConcurrentHashMap<String, String> getDataMap() {
        return NAMESPACE_DATA.computeIfAbsent(memoryConfig.getNamespace(), ignored -> new ConcurrentHashMap<>());
    }
    
    private CopyOnWriteArrayList<MemoryWatcher> getWatcherList() {
        return NAMESPACE_WATCHERS.computeIfAbsent(memoryConfig.getNamespace(), ignored -> new CopyOnWriteArrayList<>());
    }
    
    private void refreshCacheIfWatched(final String key, final String value) {
        for (String prefix : cachedPrefixes) {
            if (key.startsWith(prefix)) {
                cache.put(key, value);
                break;
            }
        }
    }
    
    private void fireEvent(final String eventKey, final Type type, final String value) {
        CopyOnWriteArrayList<MemoryWatcher> watchers = NAMESPACE_WATCHERS.get(memoryConfig.getNamespace());
        if (null == watchers || watchers.isEmpty()) {
            return;
        }
        DataChangedEvent event = new DataChangedEvent(type, eventKey, value);
        for (MemoryWatcher each : watchers) {
            if (isWatched(each.getWatchedKey(), eventKey)) {
                if (null == each.getExecutor()) {
                    each.getListener().onChange(event);
                } else {
                    each.getExecutor().execute(() -> each.getListener().onChange(event));
                }
            }
        }
    }
    
    private boolean isWatched(final String watchedKey, final String eventKey) {
        return eventKey.equals(watchedKey) || eventKey.startsWith(watchedKey + "/");
    }
    
    private String normalizeWatchKey(final String key) {
        if (null == key || key.isEmpty()) {
            return "/";
        }
        if (key.endsWith("/") && key.length() > 1) {
            return key.substring(0, key.length() - 1);
        }
        return key;
    }
    
    /**
     * Clear all memory data, only for tests.
     */
    public static void clear() {
        NAMESPACE_DATA.clear();
        NAMESPACE_WATCHERS.clear();
        LEADER_LOCKS.clear();
        SEQUENCE_COUNTER.set(0);
    }
    
    /**
     * Clear data of specified namespace, only for tests.
     *
     * @param namespace namespace
     */
    public static void clear(final String namespace) {
        ConcurrentHashMap<String, String> data = NAMESPACE_DATA.get(namespace);
        if (null != data) {
            data.clear();
        }
        CopyOnWriteArrayList<MemoryWatcher> watchers = NAMESPACE_WATCHERS.get(namespace);
        if (null != watchers) {
            watchers.clear();
        }
    }
    
    private static final class MemoryWatcher {
        
        @Getter
        private final MemoryRegistryCenter owner;
        
        @Getter
        private final String watchedKey;
        
        @Getter
        private final DataChangedEventListener listener;
        
        @Getter
        private final Executor executor;
        
        MemoryWatcher(final MemoryRegistryCenter owner, final String watchedKey, final DataChangedEventListener listener, final Executor executor) {
            this.owner = owner;
            this.watchedKey = watchedKey;
            this.listener = listener;
            this.executor = executor;
        }
    }
}
