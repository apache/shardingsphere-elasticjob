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

package org.apache.shardingsphere.elasticjob.test.natived.it.etcd;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.bootstrap.type.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.bootstrap.type.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.dataflow.props.DataflowJobProperties;
import org.apache.shardingsphere.elasticjob.http.props.HttpJobProperties;
import org.apache.shardingsphere.elasticjob.kernel.tracing.config.TracingConfiguration;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.etcd.EtcdConfiguration;
import org.apache.shardingsphere.elasticjob.reg.etcd.EtcdRegistryCenter;
import org.apache.shardingsphere.elasticjob.script.props.ScriptJobProperties;
import org.apache.shardingsphere.elasticjob.test.natived.commons.job.dataflow.JavaDataflowJob;
import org.apache.shardingsphere.elasticjob.test.natived.commons.job.simple.JavaSimpleJob;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@EnabledInNativeImage
class EtcdJavaTest {
    
    private static GenericContainer<?> etcdContainer;
    
    private static CoordinatorRegistryCenter regCenter;
    
    private static TracingConfiguration<DataSource> tracingConfig;
    
    @BeforeAll
    static void beforeAll() {
        etcdContainer = new GenericContainer<>(DockerImageName.parse("quay.io/coreos/etcd:v3.5.18"))
                .withExposedPorts(2379)
                .withCommand("etcd", "--advertise-client-urls", "http://0.0.0.0:2379", "--listen-client-urls", "http://0.0.0.0:2379")
                .waitingFor(Wait.forLogMessage(".*ready to serve client requests.*", 1));
        etcdContainer.start();
        String serverLists = "http://127.0.0.1:" + etcdContainer.getMappedPort(2379);
        regCenter = new EtcdRegistryCenter(new EtcdConfiguration(serverLists, "elasticjob-test-native-etcd-java"));
        regCenter.init();
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
        if (null != etcdContainer) {
            etcdContainer.stop();
        }
    }
    
    @Test
    void testHttpJob() {
        ScheduleJobBootstrap jobBootstrap = new ScheduleJobBootstrap(regCenter, "HTTP",
                JobConfiguration.newBuilder("testEtcdJavaHttpJob", 3)
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
                JobConfiguration.newBuilder("testEtcdJavaSimpleJob", 3)
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
                JobConfiguration.newBuilder("testEtcdJavaDataflowElasticJob", 3)
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
                JobConfiguration.newBuilder("testEtcdJavaOneOffSimpleJob", 3)
                        .shardingItemParameters("0=Norddorf,1=Bordeaux,2=Somerset")
                        .addExtraConfigurations(tracingConfig)
                        .build());
        assertDoesNotThrow(() -> {
            jobBootstrap.execute();
            jobBootstrap.shutdown();
        });
    }
    
    @Test
    void testScriptJob() {
        ScheduleJobBootstrap jobBootstrap = new ScheduleJobBootstrap(regCenter, "SCRIPT",
                JobConfiguration.newBuilder("testEtcdScriptElasticJob", 3)
                        .cron("0/5 * * * * ?")
                        .setProperty(ScriptJobProperties.SCRIPT_KEY, Paths.get("src/test/resources/test-native/sh/demo.sh").toString())
                        .addExtraConfigurations(tracingConfig)
                        .build());
        assertDoesNotThrow(() -> {
            jobBootstrap.schedule();
            jobBootstrap.shutdown();
        });
    }
}
