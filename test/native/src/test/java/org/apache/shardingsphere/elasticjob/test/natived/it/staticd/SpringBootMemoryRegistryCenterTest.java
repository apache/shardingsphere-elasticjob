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

package org.apache.shardingsphere.elasticjob.test.natived.it.staticd;

import org.apache.curator.test.InstanceSpec;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.memory.MemoryRegistryCenter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Verifies that {@code elasticjob.reg-center.type} dispatches to the in-memory registry center.
 * The {@link SpringBootDTest} covers the default ZooKeeper branch, which keeps {@code elasticjob.regCenter.*} backwards compatible.
 */
@DirtiesContext
@SpringBootTest(classes = SpringBootMemoryRegistryCenterTest.TestApp.class,
        properties = {"elasticjob.reg-center.type=memory", "elasticjob.reg-center.namespace=elasticjob-native-springboot-memory"})
@EnabledInNativeImage
class SpringBootMemoryRegistryCenterTest {
    
    @Autowired
    private ObjectProvider<CoordinatorRegistryCenter> coordinatorRegistryCenterProvider;
    
    @Autowired
    private ObjectProvider<MemoryRegistryCenter> memoryRegistryCenterProvider;
    
    @DynamicPropertySource
    static void elasticjobProperties(final DynamicPropertyRegistry registry) {
        registry.add("elasticjob.dump.port", InstanceSpec::getRandomPort);
    }
    
    @Test
    void testMemoryRegistryCenter() {
        assertInstanceOf(MemoryRegistryCenter.class, coordinatorRegistryCenterProvider.getIfAvailable());
        assertInstanceOf(MemoryRegistryCenter.class, memoryRegistryCenterProvider.getIfAvailable());
    }
    
    @SpringBootApplication(scanBasePackages = "org.apache.shardingsphere.elasticjob.test.natived")
    static class TestApp {
    }
}
