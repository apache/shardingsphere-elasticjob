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

package org.apache.shardingsphere.elasticjob.reg.curator2;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.api.transaction.CuratorTransactionBridge;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.base.LeaderExecutionCallback;
import org.apache.shardingsphere.elasticjob.reg.base.transaction.TransactionOperation;
import org.apache.shardingsphere.elasticjob.reg.exception.RegException;
import org.apache.shardingsphere.elasticjob.reg.exception.RegExceptionHandler;
import org.apache.shardingsphere.elasticjob.reg.listener.ConnectionStateChangedEventListener;
import org.apache.shardingsphere.elasticjob.reg.listener.ConnectionStateChangedEventListener.State;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEventListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Registry center of ZooKeeper.
 */
@Slf4j
public final class ZookeeperRegistryCenter implements CoordinatorRegistryCenter {

    @Getter(AccessLevel.PROTECTED)
    private final ZookeeperConfiguration zkConfig;

    private final Map<String, TreeCache> caches = new ConcurrentHashMap<>();

    @Getter
    private CuratorFramework client;

    public ZookeeperRegistryCenter(final ZookeeperConfiguration zkConfig) {
        this.zkConfig = zkConfig;
    }

    @Override
    public void init() {
        log.debug("Elastic job: zookeeper registry center init, server lists is: {}.", zkConfig.getServerLists());
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
            .connectString(zkConfig.getServerLists())
            .retryPolicy(new ExponentialBackoffRetry(
                zkConfig.getBaseSleepTimeMilliseconds(),
                zkConfig.getMaxRetries(),
                zkConfig.getMaxSleepTimeMilliseconds()))
            .namespace(zkConfig.getNamespace());
        if (0 != zkConfig.getSessionTimeoutMilliseconds()) {
            builder.sessionTimeoutMs(zkConfig.getSessionTimeoutMilliseconds());
        }
        if (0 != zkConfig.getConnectionTimeoutMilliseconds()) {
            builder.connectionTimeoutMs(zkConfig.getConnectionTimeoutMilliseconds());
        }
        if (!Strings.isNullOrEmpty(zkConfig.getDigest())) {
            builder.authorization(
                    "digest",
                    zkConfig.getDigest().getBytes(StandardCharsets.UTF_8))
                .aclProvider(new ACLProvider() {

                    @Override public List<ACL> getDefaultAcl() {
                        return ZooDefs.Ids.CREATOR_ALL_ACL;
                    }

                    @Override public List<ACL> getAclForPath(final String path) {
                        return ZooDefs.Ids.CREATOR_ALL_ACL;
                    }
                });
        }
        client = builder.build();
        client.start();
        try {
            if (!client.blockUntilConnected(
                zkConfig.getMaxSleepTimeMilliseconds() * zkConfig.getMaxRetries(),
                TimeUnit.MILLISECONDS)) {
                client.close();
                throw new KeeperException.OperationTimeoutException();
            }
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }

    @Override
    public void close() {
        for (Entry<String, TreeCache> each : caches.entrySet()) {
            each.getValue().close();
        }
        waitForCacheClose();
        CloseableUtils.closeQuietly(client);
    }

    /*
     * sleep 500ms, let cache client close first and then client, otherwise will throw exception
     * reference：https://issues.apache.org/jira/browse/CURATOR-157
     */
    private void waitForCacheClose() {
        try {
            Thread.sleep(500L);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String get(final String key) {
        TreeCache cache = findCuratorCache(key);
        if (null == cache) {
            return getDirectly(key);
        }
        Optional<ChildData> resultInCache = Optional.ofNullable(cache.getCurrentData(key));
        return resultInCache.map(v -> null == v.getData() ? null : new String(v.getData(), StandardCharsets.UTF_8)).orElseGet(() -> getDirectly(key));
    }

    private TreeCache findCuratorCache(final String key) {
        for (Entry<String, TreeCache> entry : caches.entrySet()) {
            if (key.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public String getDirectly(final String key) {
        try {
            return new String(client.getData().forPath(key), StandardCharsets.UTF_8);
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
            return null;
        }
    }

    @Override
    public List<String> getChildrenKeys(final String key) {
        try {
            List<String> result = client.getChildren().forPath(key);
            result.sort(Comparator.reverseOrder());
            return result;
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
            return Collections.emptyList();
        }
    }

    @Override
    public int getNumChildren(final String key) {
        try {
            Stat stat = client.checkExists().forPath(key);
            if (null != stat) {
                return stat.getNumChildren();
            }
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
        return 0;
    }

    @Override
    public boolean isExisted(final String key) {
        try {
            return null != client.checkExists().forPath(key);
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
            return false;
        }
    }

    @Override
    public void persist(final String key, final String value) {
        try {
            if (!isExisted(key)) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                    .forPath(key, value.getBytes(StandardCharsets.UTF_8));
            } else {
                update(key, value);
            }
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }

    @Override
    public void update(final String key, final String value) {
        try {
            CuratorTransaction transaction = client.inTransaction();
            transaction.check().forPath(key)
                .and().setData().forPath(key, value.getBytes(StandardCharsets.UTF_8))
                .and().commit();
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }

    @Override
    public void persistEphemeral(final String key, final String value) {
        try {
            if (isExisted(key)) {
                client.delete().deletingChildrenIfNeeded().forPath(key);
            }
            client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL).forPath(key, value.getBytes(StandardCharsets.UTF_8));
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }

    @Override
    public String persistSequential(final String key, final String value) {
        try {
            return client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(key, value.getBytes(StandardCharsets.UTF_8));
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
        return null;
    }

    @Override
    public void persistEphemeralSequential(final String key) {
        try {
            client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(key);
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }

    @Override
    public void remove(final String key) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(key);
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }

    @Override
    public long getRegistryCenterTime(final String key) {
        long result = 0L;
        try {
            persist(key, "");
            result = client.checkExists().forPath(key).getMtime();
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
        Preconditions.checkState(0L != result, "Cannot get registry center time.");
        return result;
    }

    @Override
    public Object getRawClient() {
        return client;
    }

    @Override
    public void addConnectionStateChangedEventListener(final ConnectionStateChangedEventListener listener) {
        CoordinatorRegistryCenter coordinatorRegistryCenter = this;
        client.getConnectionStateListenable().addListener((client, newState) -> {
            State state;
            switch (newState) {
                case CONNECTED:
                    state = State.CONNECTED;
                    break;
                case LOST:
                case SUSPENDED:
                    state = State.UNAVAILABLE;
                    break;
                case RECONNECTED:
                    state = State.RECONNECTED;
                    break;
                case READ_ONLY:
                default:
                    throw new IllegalStateException("Illegal registry center connection state: " + newState);
            }
            listener.onStateChanged(coordinatorRegistryCenter, state);
        });
    }

    @Override
    public void executeInTransaction(final List<TransactionOperation> transactionOperations) throws Exception {
        CuratorTransaction transaction = client.inTransaction();
        CuratorTransactionBridge transactionBridge = null;
        for (TransactionOperation each : transactionOperations) {
            try {
                switch (each.getType()) {
                    case CHECK_EXISTS:
                        if (transactionBridge == null) {
                            transactionBridge = transaction.check().forPath(each.getKey());
                        } else {
                            transactionBridge = transactionBridge.and().check().forPath(each.getKey());
                        }
                        break;
                    case ADD:
                        if (transactionBridge == null) {
                            transactionBridge = transaction.create()
                                .forPath(each.getKey(), each.getValue().getBytes(StandardCharsets.UTF_8));
                        } else {
                            transactionBridge = transactionBridge.and().create()
                                .forPath(each.getKey(), each.getValue().getBytes(StandardCharsets.UTF_8));
                        }
                        break;
                    case UPDATE:
                        if (transactionBridge == null) {
                            transactionBridge = transaction.setData()
                                .forPath(each.getKey(), each.getValue().getBytes(StandardCharsets.UTF_8));
                        } else {
                            transactionBridge = transactionBridge.and().setData()
                                .forPath(each.getKey(), each.getValue().getBytes(StandardCharsets.UTF_8));
                        }
                        break;
                    case DELETE:
                        if (transactionBridge == null) {
                            transactionBridge = transaction.delete().forPath(each.getKey());
                        } else {
                            transactionBridge = transactionBridge.and().delete().forPath(each.getKey());
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException(each.toString());
                }
                //CHECKSTYLE:OFF
            } catch (final Exception ex) {
                //CHECKSTYLE:ON
                throw new RegException(ex);
            }
        }

        if (transactionBridge != null) {
            transactionBridge.and().commit();
        }
    }

    @Override
    public void addCacheData(final String cachePath) {
        TreeCache cache = new TreeCache(client, cachePath);
        try {
            cache.start();
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
        caches.put(cachePath + "/", cache);
    }

    @Override
    public void evictCacheData(final String cachePath) {
        TreeCache cache = caches.remove(cachePath + "/");
        if (null != cache) {
            cache.close();
        }
    }

    @Override
    public Object getRawCache(final String cachePath) {
        return caches.get(cachePath + "/");
    }

    @Override
    public void executeInLeader(final String key, final LeaderExecutionCallback callback) {
        try (LeaderLatch latch = new LeaderLatch(client, key)) {
            latch.start();
            latch.await();
            callback.execute();
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            handleException(ex);
        }
    }

    @Override
    public void watch(final String key, final DataChangedEventListener listener, final Executor executor) {
        TreeCache cache = caches.get(key + "/");
        TreeCacheListener cacheListener = (framework, event) -> {
            Type type = getTypeFromCuratorType(event);
            if (Type.IGNORED == type) {
                return;
            }
            String path = event.getData().getPath();
            if (path.isEmpty()) {
                return;
            }
            byte[] data = event.getData().getData();
            listener.onChange(new DataChangedEvent(type, path, null == data ? "" : new String(data, StandardCharsets.UTF_8)));
        };
        if (executor != null) {
            cache.getListenable().addListener(cacheListener, executor);
        } else {
            cache.getListenable().addListener(cacheListener);
        }
    }

    private Type getTypeFromCuratorType(final TreeCacheEvent event) {
        switch (event.getType()) {
            case NODE_ADDED:
                return Type.ADDED;
            case NODE_REMOVED:
                return Type.DELETED;
            case NODE_UPDATED:
                return Type.UPDATED;
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
}
