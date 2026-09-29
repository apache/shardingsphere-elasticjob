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
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Verifies that the Spring Namespace {@code elasticjob:memory} tag creates a working in-memory registry center.
 *
 * <p>The user-defined {@code CoordinatorRegistryCenter} bean from the imported XML takes precedence over the
 * Spring Boot auto-configured one, so no {@code elasticjob.reg-center.*} properties are required here.</p>
 */
@DirtiesContext
@SpringBootTest(classes = SpringNamespaceMemoryRegistryCenterTest.TestApp.class)
@EnabledInNativeImage
class SpringNamespaceMemoryRegistryCenterTest {
    
    @Autowired
    private ObjectProvider<CoordinatorRegistryCenter> coordinatorRegistryCenterProvider;
    
    @DynamicPropertySource
    static void elasticjobProperties(final DynamicPropertyRegistry registry) {
        registry.add("elasticjob.dump.port", InstanceSpec::getRandomPort);
    }
    
    @Test
    void assertMemoryRegistryCenter() {
        CoordinatorRegistryCenter regCenter = coordinatorRegistryCenterProvider.getIfAvailable();
        assertInstanceOf(MemoryRegistryCenter.class, regCenter);
        Objects.requireNonNull(regCenter);
        regCenter.persist("/test", "value");
        assertThat(regCenter.get("/test"), is("value"));
    }
    
    @SpringBootApplication(scanBasePackages = "org.apache.shardingsphere.elasticjob.test.natived")
    @ImportResource("classpath:META-INF/native-namespace/memory.xml")
    static class TestApp {
    }
}
