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

package org.apache.shardingsphere.elasticjob.spring.boot.reg;

import org.apache.curator.test.InstanceSpec;
import org.apache.shardingsphere.elasticjob.reg.etcd.EtcdConfiguration;
import org.apache.shardingsphere.elasticjob.reg.memory.MemoryConfiguration;
import org.apache.shardingsphere.elasticjob.reg.nacos.NacosConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class RegistryCenterPropertiesTest {
    
    @Test
    void assertDefaultValues() {
        RegistryCenterProperties properties = new RegistryCenterProperties();
        assertThat(properties.getType(), is("zookeeper"));
        assertThat(properties.getServerLists(), is(nullValue()));
        assertThat(properties.getNamespace(), is(nullValue()));
        assertThat(properties.getBaseSleepTimeMilliseconds(), is(1000));
        assertThat(properties.getMaxSleepTimeMilliseconds(), is(3000));
        assertThat(properties.getMaxRetries(), is(3));
        assertThat(properties.getSessionTimeoutMilliseconds(), is(0));
        assertThat(properties.getConnectionTimeoutMilliseconds(), is(0));
        assertThat(properties.getTimeoutMs(), is(0L));
        assertThat(properties.isEnsembleTracker(), is(true));
        assertThat(properties.isSsl(), is(false));
    }
    
    @Test
    void assertToZookeeperConfigurationWithDefaultValues() {
        RegistryCenterProperties properties = newRegistryCenterProperties("localhost:" + InstanceSpec.getRandomPort());
        ZookeeperConfiguration actual = ZookeeperRegistryCenterConfiguration.toZookeeperConfiguration(properties);
        assertThat(actual.getServerLists(), is(properties.getServerLists()));
        assertThat(actual.getNamespace(), is(properties.getNamespace()));
        assertThat(actual.getBaseSleepTimeMilliseconds(), is(1000));
        assertThat(actual.getMaxSleepTimeMilliseconds(), is(3000));
        assertThat(actual.getMaxRetries(), is(3));
        assertThat(actual.getSessionTimeoutMilliseconds(), is(0));
        assertThat(actual.getConnectionTimeoutMilliseconds(), is(0));
        assertThat(actual.getDigest(), is(nullValue()));
        assertThat(actual.isEnsembleTracker(), is(true));
    }
    
    @Test
    void assertToZookeeperConfiguration() {
        ZookeeperConfiguration actual = ZookeeperRegistryCenterConfiguration.toZookeeperConfiguration(newFullProperties());
        assertThat(actual.getServerLists(), is("host1:2181,host2:2181"));
        assertThat(actual.getNamespace(), is("test"));
        assertThat(actual.getBaseSleepTimeMilliseconds(), is(2000));
        assertThat(actual.getMaxSleepTimeMilliseconds(), is(4000));
        assertThat(actual.getMaxRetries(), is(5));
        assertThat(actual.getSessionTimeoutMilliseconds(), is(5000));
        assertThat(actual.getConnectionTimeoutMilliseconds(), is(6000));
        assertThat(actual.getDigest(), is("user:digest"));
        assertThat(actual.isEnsembleTracker(), is(false));
    }
    
    @Test
    void assertToEtcdConfigurationWithDefaultValues() {
        RegistryCenterProperties properties = newRegistryCenterProperties("http://localhost:2379");
        EtcdConfiguration actual = EtcdRegistryCenterConfiguration.toEtcdConfiguration(properties);
        assertThat(actual.getServerLists(), is(properties.getServerLists()));
        assertThat(actual.getNamespace(), is(properties.getNamespace()));
        assertThat(actual.getConnectionTimeoutMilliseconds(), is(5000L));
        assertThat(actual.getUsername(), is(nullValue()));
        assertThat(actual.getPassword(), is(nullValue()));
        assertThat(actual.isSsl(), is(false));
        assertThat(actual.getAuthority(), is(nullValue()));
    }
    
    @Test
    void assertToEtcdConfiguration() {
        EtcdConfiguration actual = EtcdRegistryCenterConfiguration.toEtcdConfiguration(newFullProperties());
        assertThat(actual.getServerLists(), is("host1:2181,host2:2181"));
        assertThat(actual.getNamespace(), is("test"));
        assertThat(actual.getConnectionTimeoutMilliseconds(), is(6000L));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("password"));
        assertThat(actual.isSsl(), is(true));
        assertThat(actual.getAuthority(), is("etcd-server:2379"));
    }
    
    @Test
    void assertToNacosConfigurationWithDefaultValues() {
        RegistryCenterProperties properties = newRegistryCenterProperties("localhost:8848");
        NacosConfiguration actual = NacosRegistryCenterConfiguration.toNacosConfiguration(properties);
        assertThat(actual.getServerLists(), is(properties.getServerLists()));
        assertThat(actual.getNamespace(), is(properties.getNamespace()));
        assertThat(actual.getTenant(), is(nullValue()));
        assertThat(actual.getUsername(), is(nullValue()));
        assertThat(actual.getPassword(), is(nullValue()));
        assertThat(actual.getTimeoutMs(), is(3000L));
    }
    
    @Test
    void assertToNacosConfiguration() {
        NacosConfiguration actual = NacosRegistryCenterConfiguration.toNacosConfiguration(newFullProperties());
        assertThat(actual.getServerLists(), is("host1:2181,host2:2181"));
        assertThat(actual.getNamespace(), is("test"));
        assertThat(actual.getTenant(), is("public-namespace"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("password"));
        assertThat(actual.getTimeoutMs(), is(5000L));
    }
    
    @Test
    void assertToMemoryConfiguration() {
        RegistryCenterProperties properties = newRegistryCenterProperties("localhost:8848");
        MemoryConfiguration actual = MemoryRegistryCenterConfiguration.toMemoryConfiguration(properties);
        assertThat(actual.getNamespace(), is(properties.getNamespace()));
    }
    
    private static RegistryCenterProperties newRegistryCenterProperties(final String serverLists) {
        RegistryCenterProperties properties = new RegistryCenterProperties();
        properties.setServerLists(serverLists);
        properties.setNamespace("test");
        return properties;
    }
    
    private static RegistryCenterProperties newFullProperties() {
        RegistryCenterProperties properties = newRegistryCenterProperties("host1:2181,host2:2181");
        properties.setBaseSleepTimeMilliseconds(2000);
        properties.setMaxSleepTimeMilliseconds(4000);
        properties.setMaxRetries(5);
        properties.setSessionTimeoutMilliseconds(5000);
        properties.setConnectionTimeoutMilliseconds(6000);
        properties.setDigest("user:digest");
        properties.setEnsembleTracker(false);
        properties.setUsername("root");
        properties.setPassword("password");
        properties.setSsl(true);
        properties.setAuthority("etcd-server:2379");
        properties.setTenant("public-namespace");
        properties.setTimeoutMs(5000L);
        return properties;
    }
}
