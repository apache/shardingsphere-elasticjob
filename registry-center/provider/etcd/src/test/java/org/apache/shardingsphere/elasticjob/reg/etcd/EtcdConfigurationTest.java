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

import org.apache.curator.test.InstanceSpec;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class EtcdConfigurationTest {
    
    @Test
    void assertNewEtcdConfigurationForServerListsAndNamespace() {
        int randomPort = InstanceSpec.getRandomPort();
        EtcdConfiguration etcdConfig = new EtcdConfiguration("http://localhost:" + randomPort, "myNamespace");
        assertThat(etcdConfig.getServerLists(), is("http://localhost:" + randomPort));
        assertThat(etcdConfig.getNamespace(), is("myNamespace"));
        assertThat(etcdConfig.getConnectionTimeoutMilliseconds(), is(5000L));
        assertThat(etcdConfig.getUsername(), is(nullValue()));
        assertThat(etcdConfig.getPassword(), is(nullValue()));
        assertThat(etcdConfig.getAuthority(), is(nullValue()));
    }
    
    @Test
    void assertSetConnectionTimeoutMilliseconds() {
        EtcdConfiguration etcdConfig = new EtcdConfiguration("http://localhost:" + InstanceSpec.getRandomPort(), "myNamespace");
        etcdConfig.setConnectionTimeoutMilliseconds(10000L);
        assertThat(etcdConfig.getConnectionTimeoutMilliseconds(), is(10000L));
    }
    
    @Test
    void assertSetUsernameAndPassword() {
        EtcdConfiguration etcdConfig = new EtcdConfiguration("http://localhost:" + InstanceSpec.getRandomPort(), "myNamespace");
        etcdConfig.setUsername("root");
        etcdConfig.setPassword("password");
        assertThat(etcdConfig.getUsername(), is("root"));
        assertThat(etcdConfig.getPassword(), is("password"));
    }
    
    @Test
    void assertSetAuthority() {
        EtcdConfiguration etcdConfig = new EtcdConfiguration("http://localhost:" + InstanceSpec.getRandomPort(), "myNamespace");
        etcdConfig.setAuthority("localhost:2379");
        assertThat(etcdConfig.getAuthority(), is("localhost:2379"));
    }
}
