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

package org.apache.shardingsphere.elasticjob.spring.boot.tracing;

import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.shardingsphere.elasticjob.kernel.tracing.config.TracingConfiguration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import javax.sql.DataSource;
import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(properties = "spring.main.banner-mode=off")
@SpringBootApplication
@ActiveProfiles("tracing")
class TracingConfigurationTest {
    
    private static TestingServer testingServer;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @DynamicPropertySource
    static void elasticjobProperties(final DynamicPropertyRegistry registry) {
        registry.add("elasticjob.regCenter.serverLists", () -> testingServer.getConnectString());
    }
    
    @BeforeAll
    static void init() throws Exception {
        testingServer = new TestingServer();
        try (
                CuratorZookeeperClient client = new CuratorZookeeperClient(testingServer.getConnectString(),
                        60000, 500, null,
                        new ExponentialBackoffRetry(500, 3, 1500))) {
            client.start();
            Awaitility.await().atMost(Duration.ofSeconds(30L)).ignoreExceptions().until(client::isConnected);
        }
    }
    
    @AfterAll
    static void afterAll() throws IOException {
        testingServer.close();
    }
    
    @Test
    void assertNotRDBConfiguration() {
        assertNotNull(applicationContext);
        assertFalse(applicationContext.containsBean("tracingDataSource"));
        ObjectProvider<Object> provider = applicationContext.getBeanProvider(ResolvableType.forClassWithGenerics(TracingConfiguration.class, DataSource.class));
        assertNull(provider.getIfAvailable());
    }
}
