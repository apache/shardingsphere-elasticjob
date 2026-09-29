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

import org.apache.shardingsphere.elasticjob.bootstrap.type.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.memory.MemoryRegistryCenter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DirtiesContext
@SpringBootTest(classes = ElasticJobSpringBootMemoryRegistryCenterTest.TestApplication.class, properties = "spring.main.banner-mode=off")
@ActiveProfiles("memory")
class ElasticJobSpringBootMemoryRegistryCenterTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    void assertRegistryCenterProperties() {
        RegistryCenterProperties actual = applicationContext.getBean(RegistryCenterProperties.class);
        assertThat(actual.getType(), is("memory"));
        assertThat(actual.getNamespace(), is("elasticjob-spring-boot-starter-memory"));
    }
    
    @Test
    void assertMemoryRegistryCenter() {
        MemoryRegistryCenter memoryRegistryCenter = applicationContext.getBean(MemoryRegistryCenter.class);
        assertInstanceOf(MemoryRegistryCenter.class, memoryRegistryCenter);
        assertThat(applicationContext.getBean(CoordinatorRegistryCenter.class), is(memoryRegistryCenter));
        memoryRegistryCenter.persist("/test", "value");
        assertThat(memoryRegistryCenter.get("/test"), is("value"));
        assertFalse(applicationContext.containsBean("zookeeperRegistryCenter"));
    }
    
    @Test
    void assertJobBootstrapCreation() {
        assertNotNull(applicationContext.getBean("printTestJobBean", OneOffJobBootstrap.class));
    }
    
    @Configuration
    @EnableAutoConfiguration
    static class TestApplication {
    }
}
