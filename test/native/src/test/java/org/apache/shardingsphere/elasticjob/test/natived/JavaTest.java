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

package org.apache.shardingsphere.elasticjob.test.natived;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.bootstrap.type.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.bootstrap.type.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.dataflow.props.DataflowJobProperties;
import org.apache.shardingsphere.elasticjob.http.props.HttpJobProperties;
import org.apache.shardingsphere.elasticjob.kernel.tracing.config.TracingConfiguration;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.script.props.ScriptJobProperties;
import org.apache.shardingsphere.elasticjob.test.natived.commons.job.dataflow.JavaDataflowJob;
import org.apache.shardingsphere.elasticjob.test.natived.commons.job.simple.JavaSimpleJob;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@EnabledInNativeImage
class JavaTest {
    
    private static TestingServer testingServer;
    
    private static CoordinatorRegistryCenter regCenter;
    
    private static TracingConfiguration<DataSource> tracingConfig;
    
    /**
     * TODO Internally in {@link org.apache.curator.test.TestingServer},
     *  {@code Files.createTempDirectory(DirectoryUtils.class.getSimpleName()).toFile())} calls {@link java.nio.file.Path#toFile()},
     *  which is undesirable in both JAR and GraalVM Native Image,
     *  see <a href="https://github.com/oracle/graal/issues/7804">oracle/graal#7804</a>.
     *  ElasticJob believe this requires changes on the apache/curator side.
     *
     * @throws Exception errors
     */
    @BeforeAll
    static void beforeAll() throws Exception {
        testingServer = new TestingServer();
        try (
                CuratorZookeeperClient client = new CuratorZookeeperClient(testingServer.getConnectString(),
                        60 * 1000, 500, null,
                        new ExponentialBackoffRetry(500, 3, 500 * 3))) {
            client.start();
            Awaitility.await().atMost(Duration.ofMillis(500 * 60)).until(client::isConnected);
        }
        regCenter = new ZookeeperRegistryCenter(new ZookeeperConfiguration(testingServer.getConnectString(), "elasticjob-test-native-java"));
        regCenter.init();
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:job_event_storage");
        config.setUsername("sa");
        config.setPassword("");
        tracingConfig = new TracingConfiguration<>("RDB", new HikariDataSource(config));
    }
    
    @AfterAll
    static void afterAll() throws IOException {
        regCenter.close();
        testingServer.close();
    }
    
    @Test
    void testHttpJob() {
        ScheduleJobBootstrap jobBootstrap = new ScheduleJobBootstrap(regCenter, "HTTP",
                JobConfiguration.newBuilder("testJavaHttpJob", 3)
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
                JobConfiguration.newBuilder("testJavaSimpleJob", 3)
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
                JobConfiguration.newBuilder("testJavaDataflowElasticJob", 3)
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
                JobConfiguration.newBuilder("testJavaOneOffSimpleJob", 3)
                        .shardingItemParameters("0=Norddorf,1=Bordeaux,2=Somerset")
                        .addExtraConfigurations(tracingConfig)
                        .build());
        assertDoesNotThrow(() -> {
            jobBootstrap.execute();
            jobBootstrap.shutdown();
        });
    }
    
    @Test
    @EnabledOnOs(OS.LINUX)
    void testScriptJob() {
        ScheduleJobBootstrap jobBootstrap = new ScheduleJobBootstrap(regCenter, "SCRIPT",
                JobConfiguration.newBuilder("scriptElasticJob", 3)
                        .cron("0/5 * * * * ?")
                        .setProperty(ScriptJobProperties.SCRIPT_KEY, Paths.get("src/test/resources/script/demo.sh").toString())
                        .addExtraConfigurations(tracingConfig)
                        .build());
        assertDoesNotThrow(() -> {
            jobBootstrap.schedule();
            jobBootstrap.shutdown();
        });
    }
}
