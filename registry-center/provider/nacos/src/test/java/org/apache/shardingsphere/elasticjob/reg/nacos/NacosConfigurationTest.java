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

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class NacosConfigurationTest {
    
    @Test
    void assertNewNacosConfiguration() {
        NacosConfiguration config = new NacosConfiguration("nacos://127.0.0.1:8848", "test-namespace");
        assertThat(config.getServerLists(), is("nacos://127.0.0.1:8848"));
        assertThat(config.getNamespace(), is("test-namespace"));
        assertThat(config.getTimeoutMs(), is(3000L));
    }
    
    @Test
    void assertSetters() {
        NacosConfiguration config = new NacosConfiguration("127.0.0.1:8848", "test");
        config.setUsername("nacos");
        config.setPassword("nacos");
        config.setNacosNamespace("public");
        config.setTimeoutMs(5000L);
        assertThat(config.getUsername(), is("nacos"));
        assertThat(config.getPassword(), is("nacos"));
        assertThat(config.getNacosNamespace(), is("public"));
        assertThat(config.getTimeoutMs(), is(5000L));
    }
}
