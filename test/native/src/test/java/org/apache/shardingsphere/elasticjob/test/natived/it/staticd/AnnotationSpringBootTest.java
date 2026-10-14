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

import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.shardingsphere.elasticjob.bootstrap.type.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.bootstrap.type.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.spring.core.scanner.ElasticJobScan;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Annotation job scanner test for native image.
 * Verifies schedule and one-off jobs declared with {@code @ElasticJobConfiguration}
 * are registered via {@code @ElasticJobScan} in a native image.
 * Web environment is disabled since only bean registration is asserted.
 */
@DirtiesContext
@SpringBootTest(classes = AnnotationSpringBootTest.TestApp.class,
        properties = {"spring.main.banner-mode=off", "spring.main.allow-bean-definition-overriding=true"},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledInNativeImage
class AnnotationSpringBootTest {
    
    private static TestingServer testingServer;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @DynamicPropertySource
    static void elasticjobProperties(final DynamicPropertyRegistry registry) {
        registry.add("elasticjob.regCenter.serverLists", () -> testingServer.getConnectString());
    }
    
    @BeforeAll
    static void beforeAll() throws Exception {
        testingServer = new TestingServer();
        try (
                CuratorZookeeperClient client = new CuratorZookeeperClient(testingServer.getConnectString(),
                        60 * 1000, 500, null,
                        new ExponentialBackoffRetry(500, 3, 500 * 3))) {
            client.start();
            Awaitility.await().atMost(Duration.ofMillis(500 * 60)).ignoreExceptions().until(client::isConnected);
        }
    }
    
    @AfterAll
    static void afterAll() throws IOException {
        testingServer.close();
    }
    
    @Test
    void testScheduleJobScanned() {
        assertNotNull(applicationContext);
        assertNotNull(applicationContext.getBean("nativeAnnotationSimpleScheduleJob", ScheduleJobBootstrap.class));
    }
    
    @Test
    void testOneOffJobScanned() {
        assertNotNull(applicationContext);
        assertNotNull(applicationContext.getBean("nativeAnnotationOneOffScheduleJob", OneOffJobBootstrap.class));
    }
    
    @Configuration(proxyBeanMethods = false)
    @ElasticJobScan(basePackages = "org.apache.shardingsphere.elasticjob.test.natived.commons.job.annotation")
    static class TestApp {
        
        @Bean(initMethod = "init", destroyMethod = "close")
        ZookeeperRegistryCenter registryCenter(@Value("${elasticjob.regCenter.serverLists}") final String serverLists) {
            return new ZookeeperRegistryCenter(new ZookeeperConfiguration(serverLists, "elasticjob-test-native-annotation-springboot"));
        }
    }
}
