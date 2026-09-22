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

package org.apache.shardingsphere.elasticjob.test.natived.it.memory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.bootstrap.type.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.bootstrap.type.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.dataflow.props.DataflowJobProperties;
import org.apache.shardingsphere.elasticjob.http.props.HttpJobProperties;
import org.apache.shardingsphere.elasticjob.kernel.tracing.config.TracingConfiguration;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.memory.MemoryConfiguration;
import org.apache.shardingsphere.elasticjob.reg.memory.MemoryRegistryCenter;
import org.apache.shardingsphere.elasticjob.script.props.ScriptJobProperties;
import org.apache.shardingsphere.elasticjob.test.natived.commons.job.dataflow.JavaDataflowJob;
import org.apache.shardingsphere.elasticjob.test.natived.commons.job.simple.JavaSimpleJob;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;

import javax.sql.DataSource;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@EnabledInNativeImage
class MemoryJavaTest {
    
    private static CoordinatorRegistryCenter regCenter;
    
    private static TracingConfiguration<DataSource> tracingConfig;
    
    @BeforeAll
    static void beforeAll() {
        regCenter = new MemoryRegistryCenter(new MemoryConfiguration("elasticjob-test-native-memory-java"));
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
    }
    
    @Test
    void testHttpJob() {
        ScheduleJobBootstrap jobBootstrap = new ScheduleJobBootstrap(regCenter, "HTTP",
                JobConfiguration.newBuilder("testMemoryJavaHttpJob", 3)
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
                JobConfiguration.newBuilder("testMemoryJavaSimpleJob", 3)
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
                JobConfiguration.newBuilder("testMemoryJavaDataflowElasticJob", 3)
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
                JobConfiguration.newBuilder("testMemoryJavaOneOffSimpleJob", 3)
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
                JobConfiguration.newBuilder("testMemoryScriptElasticJob", 3)
                        .cron("0/5 * * * * ?")
                        .setProperty(ScriptJobProperties.SCRIPT_KEY, Paths.get("src/test/resources/test-native/sh/demo.sh").toString())
                        .build());
        assertDoesNotThrow(() -> {
            jobBootstrap.schedule();
            jobBootstrap.shutdown();
        });
    }
}
