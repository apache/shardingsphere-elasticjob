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

import org.apache.curator.test.TestingServer;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Verifies that the Spring Namespace {@code elasticjob:zookeeper} tag creates a working ZooKeeper registry center.
 *
 * <p>The ZooKeeper server is started in process by the test, so no external service is required.
 * The server is shut down the same way as in {@link SpringBootDTest}.</p>
 */
@DirtiesContext
@SpringBootTest(classes = SpringNamespaceZookeeperRegistryCenterTest.TestApp.class)
@EnabledInNativeImage
class SpringNamespaceZookeeperRegistryCenterTest {
    
    private static TestingServer testingServer;
    
    @Autowired
    private ObjectProvider<CoordinatorRegistryCenter> coordinatorRegistryCenterProvider;
    
    @BeforeAll
    static void beforeAll() throws Exception {
        testingServer = new TestingServer();
    }
    
    @AfterAll
    static void afterAll() throws IOException {
        testingServer.close();
    }
    
    @Test
    void assertZookeeperRegistryCenter() {
        CoordinatorRegistryCenter regCenter = coordinatorRegistryCenterProvider.getIfAvailable();
        assertInstanceOf(ZookeeperRegistryCenter.class, regCenter);
        Objects.requireNonNull(regCenter);
        regCenter.persist("/test", "value");
        assertThat(regCenter.get("/test"), is("value"));
    }
    
    @SpringBootApplication(scanBasePackages = "org.apache.shardingsphere.elasticjob.test.natived")
    @ImportResource("classpath:META-INF/native-namespace/zookeeper.xml")
    static class TestApp {
        
        /**
         * The AOT build phase instantiates this post-processor as well. The server list is only resolved
         * when the tests actually execute (native image), so that no stale address is baked into the generated context.
         *
         * @return property sources placeholder configurer
         */
        @Bean
        static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
            PropertySourcesPlaceholderConfigurer result = new PropertySourcesPlaceholderConfigurer();
            result.setIgnoreUnresolvablePlaceholders(true);
            if (null != System.getProperty("org.graalvm.nativeimage.imagecode")) {
                Properties props = new Properties();
                props.setProperty("zookeeper.serverLists", testingServer.getConnectString());
                result.setProperties(props);
            }
            return result;
        }
    }
}
