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

package org.apache.shardingsphere.elasticjob.reg.zookeeper;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ZookeeperRegistryCenterCreatorTest {
    
    private final ZookeeperRegistryCenterCreator creator = new ZookeeperRegistryCenterCreator();
    
    @Test
    void assertSupportsZookeeperConnectString() {
        assertThat(creator.supports("localhost:2181"), is(true));
    }
    
    @Test
    void assertSupportsMultipleZookeeperConnectString() {
        assertThat(creator.supports("host1:2181,host2:2181"), is(true));
    }
    
    @Test
    void assertSupportsOtherConnectStrings() {
        assertThat(creator.supports("nacos://127.0.0.1:8848"), is(true));
        assertThat(creator.supports("memory://localhost"), is(true));
    }
    
    @Test
    void assertNotSupportsEtcdConnectString() {
        assertThat(creator.supports("http://localhost:2379"), is(false));
    }
    
    @Test
    void assertIsDefault() {
        assertThat(creator.isDefault(), is(true));
    }
    
    @Test
    void assertCreate() {
        assertThat(creator.create("localhost:2181", "test", null) instanceof ZookeeperRegistryCenter, is(true));
    }
}
