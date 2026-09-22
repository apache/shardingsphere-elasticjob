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

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MemoryRegistryCenterCreatorTest {
    
    private final MemoryRegistryCenterCreator creator = new MemoryRegistryCenterCreator();
    
    @Test
    void assertSupportsMemoryUrl() {
        assertThat(creator.supports("memory://localhost"), is(true));
    }
    
    @Test
    void assertNotSupportsZookeeperConnectString() {
        assertThat(creator.supports("localhost:2181"), is(false));
    }
    
    @Test
    void assertNotSupportsEtcdConnectString() {
        assertThat(creator.supports("http://localhost:2379"), is(false));
    }
    
    @Test
    void assertNotSupportsNacosConnectString() {
        assertThat(creator.supports("nacos://127.0.0.1:8848"), is(false));
    }
    
    @Test
    void assertIsNotDefault() {
        assertThat(creator.isDefault(), is(false));
    }
    
    @Test
    void assertCreate() {
        assertThat(creator.create("memory://localhost", "test", null) instanceof MemoryRegistryCenter, is(true));
    }
}
