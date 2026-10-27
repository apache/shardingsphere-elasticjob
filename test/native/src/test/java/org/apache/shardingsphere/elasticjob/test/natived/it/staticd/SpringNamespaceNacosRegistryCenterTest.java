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
import org.apache.shardingsphere.elasticjob.reg.nacos.NacosConfiguration;
import org.apache.shardingsphere.elasticjob.reg.nacos.NacosRegistryCenter;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Objects;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Verifies that the Spring Namespace {@code elasticjob:nacos} tag creates a working Nacos registry center.
 *
 * <p>The user-defined {@code CoordinatorRegistryCenter} bean from the imported XML takes precedence over the
 * Spring Boot auto-configured one, so no {@code elasticjob.reg-center.*} properties are required here.</p>
 */
@DirtiesContext
@SpringBootTest(classes = SpringNamespaceNacosRegistryCenterTest.TestApp.class)
@EnabledInNativeImage
class SpringNamespaceNacosRegistryCenterTest {
    
    private static final int CONTAINER_HTTP_PORT = 8848;
    
    private static final int CONTAINER_GRPC_PORT = 9848;
    
    private static GenericContainer<?> nacosContainer;
    
    @Autowired
    private ObjectProvider<CoordinatorRegistryCenter> coordinatorRegistryCenterProvider;
    
    @DynamicPropertySource
    static void elasticjobProperties(final DynamicPropertyRegistry registry) {
        registry.add("elasticjob.dump.port", InstanceSpec::getRandomPort);
    }
    
    @AfterAll
    static void afterAll() {
        System.clearProperty("nacos.server.grpc.port.offset");
    }
    
    @Test
    void assertNacosRegistryCenter() {
        CoordinatorRegistryCenter regCenter = coordinatorRegistryCenterProvider.getIfAvailable();
        assertInstanceOf(NacosRegistryCenter.class, regCenter);
        Objects.requireNonNull(regCenter);
        String key = "/test";
        String value = "value";
        Awaitility.await().atMost(Duration.ofMinutes(3)).until(() -> {
            regCenter.persist(key, value);
            return value.equals(regCenter.getDirectly(key));
        });
        regCenter.remove(key);
    }
    
    /**
     * Starts the Nacos container on first use.
     *
     * <p>The Nacos client derives the gRPC port as HTTP port + offset (1000 by default).
     * Testcontainers maps ports to random host ports, so the actual offset must be configured.</p>
     *
     * @return server lists of the Nacos container
     */
    static String nacosServerLists() {
        if (null == nacosContainer) {
            nacosContainer = new GenericContainer<>(DockerImageName.parse("nacos/nacos-server:v3.2.4"))
                    .withExposedPorts(CONTAINER_HTTP_PORT, CONTAINER_GRPC_PORT)
                    .withEnv("MODE", "standalone")
                    .withEnv("JVM_XMS", "256m")
                    .withEnv("JVM_XMX", "512m")
                    .withEnv("NACOS_AUTH_ENABLE", "false")
                    .withEnv("NACOS_AUTH_TOKEN", "dGVzdC10b2tlbi1mb3ItZWxhc3RpY2pvYi12ZXJpZnk=")
                    .withEnv("NACOS_AUTH_IDENTITY_KEY", "elasticjob")
                    .withEnv("NACOS_AUTH_IDENTITY_VALUE", "elasticjob")
                    .waitingFor(Wait.forLogMessage(".*Nacos Console started successfully.*", 1).withStartupTimeout(Duration.ofMinutes(5)));
            nacosContainer.start();
            int httpPort = nacosContainer.getMappedPort(CONTAINER_HTTP_PORT);
            int grpcPort = nacosContainer.getMappedPort(CONTAINER_GRPC_PORT);
            System.setProperty("nacos.server.grpc.port.offset", String.valueOf(grpcPort - httpPort));
            awaitConfigurationServiceReady("nacos://127.0.0.1:" + httpPort);
        }
        return "nacos://127.0.0.1:" + nacosContainer.getMappedPort(CONTAINER_HTTP_PORT);
    }
    
    /**
     * The container is reported started before its gRPC server accepts connections,
     * so probe with a real write followed by a direct read until it succeeds.
     *
     * @param serverLists server lists of the Nacos container
     */
    private static void awaitConfigurationServiceReady(final String serverLists) {
        CoordinatorRegistryCenter probe = new NacosRegistryCenter(new NacosConfiguration(serverLists, "elasticjob-native-nacos-readiness"));
        probe.init();
        try {
            Awaitility.await().atMost(Duration.ofMinutes(3)).ignoreExceptions().until(() -> {
                probe.persist("/probe", "ready");
                return "ready".equals(probe.getDirectly("/probe"));
            });
            probe.remove("/probe");
        } finally {
            probe.close();
        }
    }
    
    @SpringBootApplication(scanBasePackages = "org.apache.shardingsphere.elasticjob.test.natived")
    @ImportResource("classpath:META-INF/native-namespace/nacos.xml")
    static class TestApp {
        
        /**
         * The AOT build phase instantiates this post-processor as well. Starting a container
         * and resolving the dynamic port there would bake a stale port into the generated
         * context, so only resolve when tests actually execute (native image or agent).
         *
         * @return Property sources placeholder configurer
         */
        @Bean
        static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
            PropertySourcesPlaceholderConfigurer result = new PropertySourcesPlaceholderConfigurer();
            result.setIgnoreUnresolvablePlaceholders(true);
            if (null != System.getProperty("org.graalvm.nativeimage.imagecode")) {
                Properties props = new Properties();
                props.setProperty("nacos.serverLists", nacosServerLists());
                result.setProperties(props);
            }
            return result;
        }
    }
}
