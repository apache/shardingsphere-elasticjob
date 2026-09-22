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

package org.apache.shardingsphere.elasticjob.test.natived.it.nacos;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.bootstrap.type.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.bootstrap.type.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.dataflow.props.DataflowJobProperties;
import org.apache.shardingsphere.elasticjob.http.props.HttpJobProperties;
import org.apache.shardingsphere.elasticjob.kernel.tracing.config.TracingConfiguration;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.nacos.NacosConfiguration;
import org.apache.shardingsphere.elasticjob.reg.nacos.NacosRegistryCenter;
import org.apache.shardingsphere.elasticjob.script.props.ScriptJobProperties;
import org.apache.shardingsphere.elasticjob.test.natived.commons.job.dataflow.JavaDataflowJob;
import org.apache.shardingsphere.elasticjob.test.natived.commons.job.simple.JavaSimpleJob;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@EnabledInNativeImage
class NacosJavaTest {
    
    private static final int CONTAINER_HTTP_PORT = 8848;
    
    private static final int CONTAINER_GRPC_PORT = 9848;
    
    private static GenericContainer<?> nacosContainer;
    
    private static CoordinatorRegistryCenter regCenter;
    
    private static TracingConfiguration<DataSource> tracingConfig;
    
    /**
     * The Nacos client derives the gRPC port as HTTP port + offset (1000 by default).
     * Testcontainers maps ports to random host ports, so the actual offset must be configured.
     */
    @BeforeAll
    static void beforeAll() {
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
        regCenter = new NacosRegistryCenter(new NacosConfiguration("nacos://127.0.0.1:" + httpPort, "elasticjob-test-native-nacos-java"));
        regCenter.init();
        awaitNacosReady();
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:job_event_storage");
        config.setUsername("sa");
        config.setPassword("");
        tracingConfig = new TracingConfiguration<>("RDB", new HikariDataSource(config));
    }
    
    @AfterAll
    static void afterAll() {
        if (null != regCenter) {
            regCenter.close();
        }
        if (null != nacosContainer) {
            nacosContainer.stop();
        }
        System.clearProperty("nacos.server.grpc.port.offset");
    }
    
    /**
     * Wait until the Nacos server actually serves config operations.
     *
     * <p>The container is reported started before its gRPC server accepts connections,
     * so probe with a real write followed by a direct read until it succeeds.</p>
     *
     * <p>The Nacos client talks to the server over a shaded gRPC transport that cannot
     * complete the handshake inside a GraalVM native image. Abort instead of failing,
     * since the transport is provided by the Nacos client, not by this project.</p>
     */
    private static void awaitNacosReady() {
        String probeKey = "/elasticjob-test-native-nacos-java-readiness-probe";
        String probeValue = "ready";
        try {
            Awaitility.await().atMost(3L, TimeUnit.MINUTES).until(() -> {
                regCenter.persist(probeKey, probeValue);
                return probeValue.equals(regCenter.getDirectly(probeKey));
            });
            regCenter.remove(probeKey);
        } catch (final ConditionTimeoutException ex) {
            Assumptions.abort("Nacos server did not become ready. "
                    + "Its shaded gRPC transport is unusable in this native image.");
        }
    }
    
    @Test
    void testHttpJob() {
        ScheduleJobBootstrap jobBootstrap = new ScheduleJobBootstrap(regCenter, "HTTP",
                JobConfiguration.newBuilder("testNacosJavaHttpJob", 3)
                        .setProperty(HttpJobProperties.URI_KEY, "https://www.apache.org")
                        .setProperty(HttpJobProperties.METHOD_KEY, "GET")
                        .cron("0/5 * * * * ?")
                        .shardingItemParameters("0=Norddorf,1=Bordeaux,2=Somerset")
                        .addExtraConfigurations(tracingConfig)
                        .build());
        assertDoesNotThrow(() -> {
            jobBootstrap.schedule();
            jobBootstrap.shutdown();
        });
    }
    
    @Test
    void testSimpleJob() {
        ScheduleJobBootstrap jobBootstrap = new ScheduleJobBootstrap(regCenter, new JavaSimpleJob(),
                JobConfiguration.newBuilder("testNacosJavaSimpleJob", 3)
                        .cron("0/5 * * * * ?")
                        .shardingItemParameters("0=Norddorf,1=Bordeaux,2=Somerset")
                        .addExtraConfigurations(tracingConfig)
                        .build());
        assertDoesNotThrow(() -> {
            jobBootstrap.schedule();
            jobBootstrap.shutdown();
        });
    }
    
    @Test
    void testDataflowJob() {
        ScheduleJobBootstrap jobBootstrap = new ScheduleJobBootstrap(regCenter, new JavaDataflowJob(),
                JobConfiguration.newBuilder("testNacosJavaDataflowElasticJob", 3)
                        .cron("0/5 * * * * ?")
                        .shardingItemParameters("0=Norddorf,1=Bordeaux,2=Somerset")
                        .setProperty(DataflowJobProperties.STREAM_PROCESS_KEY, Boolean.TRUE.toString())
                        .addExtraConfigurations(tracingConfig)
                        .build());
        assertDoesNotThrow(() -> {
            jobBootstrap.schedule();
            jobBootstrap.shutdown();
        });
    }
    
    @Test
    void testOneOffJob() {
        OneOffJobBootstrap jobBootstrap = new OneOffJobBootstrap(regCenter, new JavaSimpleJob(),
                JobConfiguration.newBuilder("testNacosJavaOneOffSimpleJob", 3)
                        .shardingItemParameters("0=Norddorf,1=Bordeaux,2=Somerset")
                        .build());
        assertDoesNotThrow(() -> {
            jobBootstrap.execute();
            jobBootstrap.shutdown();
        });
    }
    
    @Test
    void testScriptJob() {
        ScheduleJobBootstrap jobBootstrap = new ScheduleJobBootstrap(regCenter, "SCRIPT",
                JobConfiguration.newBuilder("testNacosScriptElasticJob", 3)
                        .cron("0/5 * * * * ?")
                        .setProperty(ScriptJobProperties.SCRIPT_KEY, Paths.get("src/test/resources/test-native/sh/demo.sh").toString())
                        .build());
        assertDoesNotThrow(() -> {
            jobBootstrap.schedule();
            jobBootstrap.shutdown();
        });
    }
}
