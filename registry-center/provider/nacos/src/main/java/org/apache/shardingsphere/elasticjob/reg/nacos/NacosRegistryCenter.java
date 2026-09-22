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

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractFuzzyWatchEventWatcher;
import com.alibaba.nacos.api.config.listener.ConfigFuzzyWatchChangeEvent;
import com.alibaba.nacos.api.config.listener.FuzzyWatchEventWatcher;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.config.common.GroupKey;
import com.google.common.base.Strings;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Registry center of Nacos.
 *
 * <p>Each ElasticJob key (for example {@code /my-job/config}) is stored as one Nacos config.
 * The ElasticJob namespace is mapped to the Nacos config group.
 * Because the Nacos {@code dataId} only accepts letters, digits and {@code _-. :},
 * every path segment is encoded with Base64URL and joined with {@code .}.</p>
 */
@Slf4j
public final class NacosRegistryCenter implements CoordinatorRegistryCenter {
    
    private static final long FUZZY_LIST_TIMEOUT_SECONDS = 10L;
    
    private static final Map<String, ReentrantLock> LEADER_LOCKS = new ConcurrentHashMap<>();
    
    @Getter(AccessLevel.PROTECTED)
    private final NacosConfiguration nacosConfig;
    
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    
    private final Map<String, NacosCacheWatch> cacheWatches = new ConcurrentHashMap<>();
    
    private final Map<String, List<NacosDataWatch>> watches = new ConcurrentHashMap<>();
    
    private final Map<String, List<ConnectionStateChangedEventListener>> connStateListeners = new ConcurrentHashMap<>();
    
    private final Set<String> ephemeralKeys = ConcurrentHashMap.newKeySet();
    
    private final AtomicLong sequenceCounter = new AtomicLong(0);
    
    @Getter
    private ConfigService configService;
    
    private volatile boolean closed;
    
    public NacosRegistryCenter(final NacosConfiguration nacosConfig) {
        this.nacosConfig = nacosConfig;
    }
    
    NacosRegistryCenter(final NacosConfiguration nacosConfig, final ConfigService configService) {
        this.nacosConfig = nacosConfig;
        this.configService = configService;
    }
    
    @Override
    public void init() {
        log.debug("Elastic job: nacos registry center init, server lists is: {}.", nacosConfig.getServerLists());
        if (null != configService) {
            return;
        }
        try {
            Properties props = new Properties();
            props.setProperty(PropertyKeyConst.SERVER_ADDR, resolveServerAddr(nacosConfig.getServerLists()));
            if (!Strings.isNullOrEmpty(nacosConfig.getNacosNamespace())) {
                props.setProperty(PropertyKeyConst.NAMESPACE, nacosConfig.getNacosNamespace());
            }
            if (!Strings.isNullOrEmpty(nacosConfig.getUsername())) {
                props.setProperty(PropertyKeyConst.USERNAME, nacosConfig.getUsername());
            }
            if (!Strings.isNullOrEmpty(nacosConfig.getPassword())) {
                props.setProperty(PropertyKeyConst.PASSWORD, nacosConfig.getPassword());
            }
            configService = NacosFactory.createConfigService(props);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public void close() {
        closed = true;
        for (String each : new ArrayList<>(ephemeralKeys)) {
            try {
                remove(each);
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.warn("Failed to remove ephemeral key: {}", each, ex);
            }
        }
        ephemeralKeys.clear();
        for (NacosCacheWatch each : new ArrayList<>(cacheWatches.values())) {
            each.close();
        }
        cacheWatches.clear();
        cache.clear();
        for (List<NacosDataWatch> each : new ArrayList<>(watches.values())) {
            for (NacosDataWatch watch : each) {
                watch.close();
            }
        }
        watches.clear();
        if (null != configService) {
            try {
                configService.shutDown();
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.warn("Failed to shutdown nacos config service.", ex);
            }
        }
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
        String normalized = normalizeKey(key);
        if ("/".equals(normalized)) {
            return null;
        }
        try {
            return configService.getConfig(toDataId(normalized), getGroup(), nacosConfig.getTimeoutMs());
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
            return null;
        }
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        String normalized = normalizeKey(key);
        try {
            String encodedPrefix = "/".equals(normalized) ? "" : toDataId(normalized);
            List<String> dataIds = listDataIds(encodedPrefix);
            String prefix = "/".equals(normalized) ? "/" : normalized + "/";
            Set<String> result = ConcurrentHashMap.newKeySet();
            for (String dataId : dataIds) {
                String originalKey = toOriginalKey(dataId);
                if (!originalKey.startsWith(prefix)) {
                    continue;
                }
                String relativeKey = originalKey.substring(prefix.length());
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
        String normalized = normalizeKey(key);
        if ("/".equals(normalized) || "".equals(key)) {
            return true;
        }
        try {
            return null != configService.getConfig(toDataId(normalized), getGroup(), nacosConfig.getTimeoutMs());
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
            return false;
        }
    }
    
    @Override
    public void persist(final String key, final String value) {
        String normalized = normalizeKey(key);
        if ("/".equals(normalized)) {
            return;
        }
        try {
            String content = null == value ? "" : value;
            publishAndVerify(toDataId(normalized), getGroup(), content);
            refreshCacheIfWatched(normalized, content);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public void update(final String key, final String value) {
        persist(key, value);
    }
    
    @Override
    public void persistEphemeral(final String key, final String value) {
        persist(key, value);
        ephemeralKeys.add(normalizeKey(key));
    }
    
    @Override
    public String persistSequential(final String key, final String value) {
        String normalized = normalizeKey(key);
        String sequentialKey = normalized + String.format("%010d", sequenceCounter.incrementAndGet());
        persist(sequentialKey, value);
        return sequentialKey;
    }
    
    @Override
    public void persistEphemeralSequential(final String key) {
        String normalized = normalizeKey(key);
        String sequentialKey = normalized + String.format("%010d", sequenceCounter.incrementAndGet());
        persist(sequentialKey, "");
        ephemeralKeys.add(sequentialKey);
    }
    
    @Override
    public void remove(final String key) {
        String normalized = normalizeKey(key);
        try {
            if ("/".equals(normalized)) {
                for (String dataId : listDataIds("")) {
                    configService.removeConfig(dataId, getGroup());
                    cache.remove(toOriginalKey(dataId));
                }
                ephemeralKeys.clear();
                return;
            }
            String encodedPrefix = toDataId(normalized);
            for (String dataId : listDataIds(encodedPrefix)) {
                String originalKey = toOriginalKey(dataId);
                if (originalKey.equals(normalized) || originalKey.startsWith(normalized + "/")) {
                    removeAndVerify(dataId, getGroup());
                    cache.remove(originalKey);
                    ephemeralKeys.remove(originalKey);
                }
            }
            removeAndVerify(encodedPrefix, getGroup());
            cache.remove(normalized);
            ephemeralKeys.remove(normalized);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public long getRegistryCenterTime(final String key) {
        return System.currentTimeMillis();
    }
    
    @Override
    public Object getRawClient() {
        return configService;
    }
    
    @Override
    public void executeInLeader(final String key, final LeaderExecutionCallback callback) {
        ReentrantLock lock = LEADER_LOCKS.computeIfAbsent(getGroup() + key, ignored -> new ReentrantLock());
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
        String normalized = normalizeKey(cachePath);
        String prefix = "/".equals(normalized) ? "/" : normalized + "/";
        String encodedPrefix = "/".equals(normalized) ? "" : toDataId(normalized);
        NacosCacheWatch previous = cacheWatches.remove(prefix);
        if (null != previous) {
            previous.close();
        }
        NacosCacheWatch watch = new NacosCacheWatch(prefix, encodedPrefix);
        try {
            for (String dataId : listDataIds(encodedPrefix)) {
                String originalKey = toOriginalKey(dataId);
                if (!originalKey.startsWith(prefix) && !"/".equals(prefix)) {
                    continue;
                }
                String content = configService.getConfig(dataId, getGroup(), nacosConfig.getTimeoutMs());
                if (null != content) {
                    cache.put(originalKey, content);
                    watch.addContentListener(dataId, originalKey);
                }
            }
            watch.startFuzzyWatch();
            if (!closed) {
                cacheWatches.put(prefix, watch);
            } else {
                watch.close();
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            watch.close();
            RegExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public void evictCacheData(final String cachePath) {
        String normalized = normalizeKey(cachePath);
        String prefix = "/".equals(normalized) ? "/" : normalized + "/";
        NacosCacheWatch watch = cacheWatches.remove(prefix);
        if (null != watch) {
            watch.close();
        }
        cache.keySet().removeIf(each -> "/".equals(prefix) || each.startsWith(prefix));
    }
    
    @Override
    public Object getRawCache(final String cachePath) {
        return cache;
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener listener, final Executor executor) {
        String normalized = normalizeKey(key);
        NacosDataWatch watch = new NacosDataWatch(normalized, listener, executor);
        try {
            watch.start();
            watches.computeIfAbsent(normalized, ignored -> new CopyOnWriteArrayList<>()).add(watch);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            watch.close();
            RegExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public void removeDataListeners(final String key) {
        String normalized = normalizeKey(key);
        List<NacosDataWatch> watchList = watches.remove(normalized);
        if (null != watchList) {
            for (NacosDataWatch each : watchList) {
                each.close();
            }
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
    
    private String getGroup() {
        return Strings.isNullOrEmpty(nacosConfig.getNamespace()) ? "DEFAULT_GROUP" : nacosConfig.getNamespace();
    }
    
    private void refreshCacheIfWatched(final String normalizedKey, final String value) {
        for (Map.Entry<String, NacosCacheWatch> entry : cacheWatches.entrySet()) {
            if (normalizedKey.startsWith(entry.getKey()) || "/".equals(entry.getKey())) {
                cache.put(normalizedKey, value);
            }
        }
    }
    
    /**
     * Publish a config and wait until it becomes visible for reading.
     *
     * <p>The Nacos server propagates newly published configs to its read path asynchronously,
     * so an immediate read after publish may still return {@code null}.
     * ElasticJob performs read-after-write everywhere, hence the wait here.</p>
     *
     * @param dataId dataId
     * @param group group
     * @param content content
     * @throws NacosException if Nacos client fails
     */
    private void publishAndVerify(final String dataId, final String group, final String content) throws NacosException {
        configService.publishConfig(dataId, group, content);
        long deadline = System.currentTimeMillis() + nacosConfig.getTimeoutMs();
        for (;;) {
            if (content.equals(configService.getConfig(dataId, group, nacosConfig.getTimeoutMs()))) {
                return;
            }
            if (System.currentTimeMillis() >= deadline) {
                log.warn("Elastic job: nacos config {} in group {} is still not visible after publish.", dataId, group);
                return;
            }
            if (!sleepQuietly()) {
                return;
            }
        }
    }
    
    private void removeAndVerify(final String dataId, final String group) throws NacosException {
        configService.removeConfig(dataId, group);
        long deadline = System.currentTimeMillis() + nacosConfig.getTimeoutMs();
        for (;;) {
            if (null == configService.getConfig(dataId, group, nacosConfig.getTimeoutMs())) {
                return;
            }
            if (System.currentTimeMillis() >= deadline) {
                log.warn("Elastic job: nacos config {} in group {} is still visible after remove.", dataId, group);
                return;
            }
            if (!sleepQuietly()) {
                return;
            }
        }
    }
    
    private boolean sleepQuietly() {
        try {
            Thread.sleep(100L);
            return true;
            // CHECKSTYLE:OFF
        } catch (final InterruptedException ex) {
            // CHECKSTYLE:ON
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    private List<String> listDataIds(final String encodedPrefix) throws NacosException {
        String pattern = null == encodedPrefix || encodedPrefix.isEmpty() ? "*" : encodedPrefix + ".*";
        String group = getGroup();
        NoopFuzzyWatcher watcher = new NoopFuzzyWatcher();
        Future<Set<String>> future = configService.fuzzyWatchWithGroupKeys(pattern, group, watcher);
        try {
            Set<String> groupKeys = future.get(FUZZY_LIST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (null == groupKeys || groupKeys.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> result = new ArrayList<>(groupKeys.size());
            for (String groupKey : groupKeys) {
                try {
                    String[] parsed = GroupKey.parseKey(groupKey);
                    if (null != parsed && parsed.length > 0 && !Strings.isNullOrEmpty(parsed[0])) {
                        result.add(parsed[0]);
                    }
                    // CHECKSTYLE:OFF
                } catch (final Exception ignored) {
                    // CHECKSTYLE:ON
                    log.debug("Elastic job: failed to parse nacos group key: {}.", groupKey);
                }
            }
            return result;
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new NacosException(-400, "Interrupted when listing Nacos configs.", ex);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw new NacosException(-400, "Failed to list Nacos configs.", ex);
        } finally {
            try {
                configService.cancelFuzzyWatch(pattern, group, watcher);
                // CHECKSTYLE:OFF
            } catch (final Exception ignored) {
                // CHECKSTYLE:ON
                log.debug("Elastic job: failed to cancel nacos fuzzy watch for pattern: {}.", pattern);
            }
        }
    }
    
    static String resolveServerAddr(final String serverLists) {
        if (null == serverLists) {
            return "";
        }
        String result = serverLists.trim();
        if (result.startsWith("nacos://")) {
            result = result.substring("nacos://".length());
        }
        return result;
    }
    
    static String normalizeKey(final String key) {
        if (null == key || key.isEmpty() || "/".equals(key)) {
            return "/";
        }
        String result = key.trim();
        if (!result.startsWith("/")) {
            result = "/" + result;
        }
        while (result.endsWith("/") && result.length() > 1) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
    
    static String toDataId(final String key) {
        String normalized = normalizeKey(key);
        if ("/".equals(normalized)) {
            return "";
        }
        String[] segments = normalized.substring(1).split("/", -1);
        StringBuilder result = new StringBuilder();
        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }
            if (result.length() > 0) {
                result.append('.');
            }
            result.append(Base64.getUrlEncoder().withoutPadding().encodeToString(segment.getBytes(StandardCharsets.UTF_8)));
        }
        return result.toString();
    }
    
    static String toOriginalKey(final String dataId) {
        if (null == dataId || dataId.isEmpty()) {
            return "/";
        }
        String[] segments = dataId.split("\\.", -1);
        StringBuilder result = new StringBuilder();
        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }
            result.append('/').append(new String(Base64.getUrlDecoder().decode(padBase64(segment)), StandardCharsets.UTF_8));
        }
        return 0 == result.length() ? "/" : result.toString();
    }
    
    private static String padBase64(final String value) {
        String result = value;
        int padding = result.length() % 4;
        if (2 == padding) {
            result += "==";
        } else if (3 == padding) {
            result += "=";
        }
        return result;
    }
    
    private static final class NoopFuzzyWatcher extends AbstractFuzzyWatchEventWatcher {
        
        @Override
        public void onEvent(final ConfigFuzzyWatchChangeEvent event) {
        }
    }
    
    private final class NacosCacheWatch {
        
        private final String prefix;
        
        private final String encodedPrefix;
        
        private final Map<String, Listener> contentListeners = new ConcurrentHashMap<>();
        
        private volatile FuzzyWatchEventWatcher fuzzyWatcher;
        
        NacosCacheWatch(final String prefix, final String encodedPrefix) {
            this.prefix = prefix;
            this.encodedPrefix = encodedPrefix;
        }
        
        void addContentListener(final String dataId, final String originalKey) throws NacosException {
            Listener listener = new Listener() {
                
                @Override
                public Executor getExecutor() {
                    return null;
                }
                
                @Override
                public void receiveConfigInfo(final String content) {
                    if (null == content) {
                        cache.remove(originalKey);
                    } else {
                        cache.put(originalKey, content);
                    }
                }
            };
            configService.addListener(dataId, getGroup(), listener);
            contentListeners.put(dataId, listener);
        }
        
        void startFuzzyWatch() throws NacosException {
            String pattern = encodedPrefix.isEmpty() ? "*" : encodedPrefix + ".*";
            FuzzyWatchEventWatcher watcher = new AbstractFuzzyWatchEventWatcher() {
                
                @Override
                public void onEvent(final ConfigFuzzyWatchChangeEvent event) {
                    handleFuzzyEvent(event);
                }
            };
            configService.fuzzyWatch(pattern, getGroup(), watcher);
            fuzzyWatcher = watcher;
        }
        
        void handleFuzzyEvent(final ConfigFuzzyWatchChangeEvent event) {
            if (null == event || Strings.isNullOrEmpty(event.getDataId())) {
                return;
            }
            String originalKey = toOriginalKey(event.getDataId());
            if (!"/".equals(prefix) && !originalKey.startsWith(prefix)) {
                return;
            }
            String changedType = event.getChangedType();
            boolean deleted = null != changedType && changedType.contains("DELETE");
            if (deleted) {
                cache.remove(originalKey);
                Listener listener = contentListeners.remove(event.getDataId());
                if (null != listener) {
                    configService.removeListener(event.getDataId(), getGroup(), listener);
                }
                return;
            }
            try {
                String content = configService.getConfig(event.getDataId(), getGroup(), nacosConfig.getTimeoutMs());
                if (null == content) {
                    cache.remove(originalKey);
                } else {
                    cache.put(originalKey, content);
                    if (!contentListeners.containsKey(event.getDataId())) {
                        addContentListener(event.getDataId(), originalKey);
                    }
                }
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.debug("Elastic job: failed to refresh nacos cache for key: {}.", originalKey, ex);
            }
        }
        
        void close() {
            for (Map.Entry<String, Listener> entry : contentListeners.entrySet()) {
                configService.removeListener(entry.getKey(), getGroup(), entry.getValue());
            }
            contentListeners.clear();
            if (null != fuzzyWatcher) {
                String pattern = encodedPrefix.isEmpty() ? "*" : encodedPrefix + ".*";
                try {
                    configService.cancelFuzzyWatch(pattern, getGroup(), fuzzyWatcher);
                    // CHECKSTYLE:OFF
                } catch (final Exception ignored) {
                    // CHECKSTYLE:ON
                    log.debug("Elastic job: failed to cancel nacos cache fuzzy watch for pattern: {}.", pattern);
                }
                fuzzyWatcher = null;
            }
        }
    }
    
    private final class NacosDataWatch {
        
        private final String watchedKey;
        
        private final DataChangedEventListener listener;
        
        private final Executor executor;
        
        private final Map<String, Listener> contentListeners = new ConcurrentHashMap<>();
        
        private volatile FuzzyWatchEventWatcher fuzzyWatcher;
        
        NacosDataWatch(final String watchedKey, final DataChangedEventListener listener, final Executor executor) {
            this.watchedKey = watchedKey;
            this.listener = listener;
            this.executor = executor;
        }
        
        void start() throws NacosException {
            String normalizedPrefix = "/".equals(watchedKey) ? "/" : watchedKey + "/";
            String encodedPrefix = "/".equals(watchedKey) ? "" : toDataId(watchedKey);
            for (String dataId : listDataIds(encodedPrefix)) {
                String originalKey = toOriginalKey(dataId);
                if (originalKey.startsWith(normalizedPrefix)) {
                    addContentListener(dataId, originalKey);
                }
            }
            String pattern = encodedPrefix.isEmpty() ? "*" : encodedPrefix + ".*";
            FuzzyWatchEventWatcher watcher = new AbstractFuzzyWatchEventWatcher() {
                
                @Override
                public void onEvent(final ConfigFuzzyWatchChangeEvent event) {
                    handleFuzzyEvent(event, normalizedPrefix);
                }
            };
            configService.fuzzyWatch(pattern, getGroup(), watcher);
            fuzzyWatcher = watcher;
        }
        
        void addContentListener(final String dataId, final String originalKey) throws NacosException {
            Listener contentListener = new Listener() {
                
                @Override
                public Executor getExecutor() {
                    return null;
                }
                
                @Override
                public void receiveConfigInfo(final String content) {
                    dispatch(new DataChangedEvent(Type.UPDATED, originalKey, null == content ? "" : content));
                }
            };
            configService.addListener(dataId, getGroup(), contentListener);
            contentListeners.put(dataId, contentListener);
        }
        
        void handleFuzzyEvent(final ConfigFuzzyWatchChangeEvent event, final String normalizedPrefix) {
            if (null == event || Strings.isNullOrEmpty(event.getDataId())) {
                return;
            }
            String originalKey = toOriginalKey(event.getDataId());
            if (!originalKey.startsWith(normalizedPrefix)) {
                return;
            }
            String changedType = event.getChangedType();
            boolean deleted = null != changedType && changedType.contains("DELETE");
            if (deleted) {
                Listener contentListener = contentListeners.remove(event.getDataId());
                if (null != contentListener) {
                    configService.removeListener(event.getDataId(), getGroup(), contentListener);
                }
                dispatch(new DataChangedEvent(Type.DELETED, originalKey, ""));
                return;
            }
            try {
                String content = configService.getConfig(event.getDataId(), getGroup(), nacosConfig.getTimeoutMs());
                boolean isNew = !contentListeners.containsKey(event.getDataId());
                if (isNew) {
                    addContentListener(event.getDataId(), originalKey);
                }
                dispatch(new DataChangedEvent(isNew ? Type.ADDED : Type.UPDATED, originalKey, null == content ? "" : content));
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.debug("Elastic job: failed to handle nacos watch event for key: {}.", originalKey, ex);
            }
        }
        
        void dispatch(final DataChangedEvent event) {
            if (null == executor) {
                listener.onChange(event);
            } else {
                executor.execute(() -> listener.onChange(event));
            }
        }
        
        void close() {
            for (Map.Entry<String, Listener> entry : contentListeners.entrySet()) {
                configService.removeListener(entry.getKey(), getGroup(), entry.getValue());
            }
            contentListeners.clear();
            if (null != fuzzyWatcher) {
                String encodedPrefix = "/".equals(watchedKey) ? "" : toDataId(watchedKey);
                String pattern = encodedPrefix.isEmpty() ? "*" : encodedPrefix + ".*";
                try {
                    configService.cancelFuzzyWatch(pattern, getGroup(), fuzzyWatcher);
                    // CHECKSTYLE:OFF
                } catch (final Exception ignored) {
                    // CHECKSTYLE:ON
                    log.debug("Elastic job: failed to cancel nacos data fuzzy watch for pattern: {}.", pattern);
                }
                fuzzyWatcher = null;
            }
        }
    }
}
