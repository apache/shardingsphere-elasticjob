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
import org.apache.shardingsphere.elasticjob.reg.etcd.EtcdRegistryCenter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Verifies that {@code elasticjob.reg-center.type} dispatches to the etcd registry center.
 */
@DirtiesContext
@SpringBootTest(classes = SpringBootEtcdRegistryCenterTest.TestApp.class,
        properties = {"elasticjob.reg-center.type=etcd", "elasticjob.reg-center.namespace=elasticjob-native-springboot-etcd"})
@EnabledInNativeImage
class SpringBootEtcdRegistryCenterTest {
    
    private static final int CONTAINER_CLIENT_PORT = 2379;
    
    private static GenericContainer<?> etcdContainer;
    
    @Autowired
    private ObjectProvider<CoordinatorRegistryCenter> coordinatorRegistryCenterProvider;
    
    @Autowired
    private ObjectProvider<EtcdRegistryCenter> etcdRegistryCenterProvider;
    
    @DynamicPropertySource
    static void elasticjobProperties(final DynamicPropertyRegistry registry) {
        registry.add("elasticjob.reg-center.server-lists", SpringBootEtcdRegistryCenterTest::etcdServerLists);
        registry.add("elasticjob.dump.port", InstanceSpec::getRandomPort);
    }
    
    /**
     * Starts the etcd container on first use.
     *
     * <p>The container is started lazily instead of in a {@code BeforeAll} callback because dynamic property
     * suppliers may be evaluated before lifecycle callbacks in Spring Boot tests.</p>
     *
     * @return server lists of the etcd container
     */
    static String etcdServerLists() {
        if (null == etcdContainer) {
            etcdContainer = new GenericContainer<>(DockerImageName.parse("quay.io/coreos/etcd:v3.5.18"))
                    .withExposedPorts(CONTAINER_CLIENT_PORT)
                    .withCommand("etcd", "--advertise-client-urls", "http://0.0.0.0:2379", "--listen-client-urls", "http://0.0.0.0:2379")
                    .waitingFor(Wait.forLogMessage(".*ready to serve client requests.*", 1));
            etcdContainer.start();
        }
        return "http://127.0.0.1:" + etcdContainer.getMappedPort(CONTAINER_CLIENT_PORT);
    }
    
    @Test
    void testEtcdRegistryCenter() {
        assertInstanceOf(EtcdRegistryCenter.class, coordinatorRegistryCenterProvider.getIfAvailable());
        assertInstanceOf(EtcdRegistryCenter.class, etcdRegistryCenterProvider.getIfAvailable());
        CoordinatorRegistryCenter regCenter = coordinatorRegistryCenterProvider.getIfAvailable();
        Objects.requireNonNull(regCenter);
        regCenter.persist("/test", "value");
        assertThat(regCenter.get("/test"), is("value"));
    }
    
    @SpringBootApplication(scanBasePackages = "org.apache.shardingsphere.elasticjob.test.natived")
    static class TestApp {
    }
}
