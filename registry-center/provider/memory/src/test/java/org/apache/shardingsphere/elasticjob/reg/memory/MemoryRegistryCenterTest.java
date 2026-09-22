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

import org.apache.shardingsphere.elasticjob.reg.base.transaction.TransactionOperation;
import org.apache.shardingsphere.elasticjob.reg.exception.RegException;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryRegistryCenterTest {
    
    private String namespace;
    
    private MemoryRegistryCenter registryCenter;
    
    @BeforeEach
    void setUp() {
        namespace = "test-" + UUID.randomUUID();
        registryCenter = new MemoryRegistryCenter(new MemoryConfiguration(namespace));
        registryCenter.init();
    }
    
    @AfterEach
    void tearDown() {
        registryCenter.close();
        MemoryRegistryCenter.clear(namespace);
    }
    
    @Test
    void assertPersistAndGet() {
        registryCenter.persist("/job/config", "value");
        assertThat(registryCenter.get("/job/config"), is("value"));
        assertThat(registryCenter.getDirectly("/job/config"), is("value"));
    }
    
    @Test
    void assertUpdate() {
        registryCenter.persist("/job/config", "value-0");
        registryCenter.update("/job/config", "value-1");
        assertThat(registryCenter.get("/job/config"), is("value-1"));
    }
    
    @Test
    void assertIsExisted() {
        assertThat(registryCenter.isExisted("/"), is(true));
        assertThat(registryCenter.isExisted("/not-existed"), is(false));
        registryCenter.persist("/job/config", "value");
        assertThat(registryCenter.isExisted("/job/config"), is(true));
    }
    
    @Test
    void assertGetChildrenKeys() {
        registryCenter.persist("/job/servers/192.168.0.1", "a");
        registryCenter.persist("/job/servers/192.168.0.2", "b");
        registryCenter.persist("/job/config", "c");
        List<String> children = registryCenter.getChildrenKeys("/job");
        assertThat(children.size(), is(2));
        assertTrue(children.contains("servers"));
        assertTrue(children.contains("config"));
        assertThat(registryCenter.getNumChildren("/job"), is(2));
        List<String> servers = registryCenter.getChildrenKeys("/job/servers");
        assertThat(servers.size(), is(2));
    }
    
    @Test
    void assertRemove() {
        registryCenter.persist("/job/config", "value");
        registryCenter.persist("/job/servers/192.168.0.1", "a");
        registryCenter.remove("/job/servers");
        assertThat(registryCenter.isExisted("/job/servers/192.168.0.1"), is(false));
        assertThat(registryCenter.isExisted("/job/config"), is(true));
        registryCenter.remove("/job/config");
        assertThat(registryCenter.get("/job/config"), is(nullValue()));
    }
    
    @Test
    void assertPersistEphemeralAndClose() {
        registryCenter.persistEphemeral("/job/instance", "ephemeral");
        assertThat(registryCenter.get("/job/instance"), is("ephemeral"));
        registryCenter.close();
        MemoryRegistryCenter another = new MemoryRegistryCenter(new MemoryConfiguration(namespace));
        try {
            another.init();
            assertThat(another.get("/job/instance"), is(nullValue()));
        } finally {
            another.close();
        }
    }
    
    @Test
    void assertPersistSequential() {
        String first = registryCenter.persistSequential("/job/seq-", "a");
        String second = registryCenter.persistSequential("/job/seq-", "b");
        assertThat(first, is(notNullValue()));
        assertThat(second, is(notNullValue()));
        assertTrue(!first.equals(second));
        assertThat(registryCenter.get(first), is("a"));
        assertThat(registryCenter.get(second), is("b"));
    }
    
    @Test
    void assertPersistEphemeralSequential() {
        registryCenter.persistEphemeralSequential("/job/eseq-");
        assertThat(registryCenter.getNumChildren("/job") >= 1, is(true));
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
        registryCenter.persist("/job/existed", "v");
        registryCenter.executeInTransaction(Arrays.asList(
                TransactionOperation.opCheckExists("/job/existed"),
                TransactionOperation.opAdd("/job/added", "a"),
                TransactionOperation.opUpdate("/job/existed", "v2"),
                TransactionOperation.opDelete("/job/added")));
        assertThat(registryCenter.get("/job/existed"), is("v2"));
        assertThat(registryCenter.isExisted("/job/added"), is(false));
    }
    
    @Test
    void assertExecuteInTransactionCheckExistsFailure() {
        assertThrows(RegException.class, () -> registryCenter.executeInTransaction(Collections.singletonList(TransactionOperation.opCheckExists("/not-existed"))));
    }
    
    @Test
    void assertWatch() {
        List<DataChangedEvent> events = new CopyOnWriteArrayList<>();
        registryCenter.watch("/job", events::add, null);
        registryCenter.persist("/job/config", "v1");
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getType(), is(DataChangedEvent.Type.ADDED));
        assertThat(events.get(0).getKey(), is("/job/config"));
        registryCenter.update("/job/config", "v2");
        assertThat(events.size(), is(2));
        assertThat(events.get(1).getType(), is(DataChangedEvent.Type.UPDATED));
        registryCenter.remove("/job/config");
        assertThat(events.size(), is(3));
        assertThat(events.get(2).getType(), is(DataChangedEvent.Type.DELETED));
        registryCenter.removeDataListeners("/job");
        registryCenter.persist("/job/another", "v");
        assertThat(events.size(), is(3));
    }
    
    @Test
    void assertShareDataInSameNamespace() {
        MemoryRegistryCenter another = new MemoryRegistryCenter(new MemoryConfiguration(namespace));
        try {
            another.init();
            registryCenter.persist("/job/shared", "shared-value");
            assertThat(another.get("/job/shared"), is("shared-value"));
        } finally {
            another.close();
        }
    }
    
    @Test
    void assertIsolateDataInDifferentNamespaces() {
        String otherNamespace = namespace + "-other";
        MemoryRegistryCenter another = new MemoryRegistryCenter(new MemoryConfiguration(otherNamespace));
        try {
            another.init();
            registryCenter.persist("/job/isolated", "v");
            assertThat(another.get("/job/isolated"), is(nullValue()));
        } finally {
            another.close();
            MemoryRegistryCenter.clear(otherNamespace);
        }
    }
    
    @Test
    void assertCache() {
        registryCenter.persist("/job/config", "cached");
        registryCenter.addCacheData("/job");
        assertThat(registryCenter.get("/job/config"), is("cached"));
        Map<?, ?> rawCache = (Map<?, ?>) registryCenter.getRawCache("/job");
        assertThat(rawCache.get("/job/config"), is("cached"));
        registryCenter.persist("/job/servers/1", "s");
        assertThat(registryCenter.get("/job/servers/1"), is("s"));
        registryCenter.evictCacheData("/job");
        assertThat(registryCenter.get("/job/config"), is("cached"));
    }
    
    @Test
    void assertGetRawClient() {
        assertThat(registryCenter.getRawClient(), is(notNullValue()));
    }
}
