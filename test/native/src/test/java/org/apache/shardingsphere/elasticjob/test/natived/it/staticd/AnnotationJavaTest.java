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
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.bootstrap.type.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.bootstrap.type.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.kernel.internal.annotation.JobAnnotationBuilder;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.test.natived.commons.job.annotation.NativeAnnotationOneOffJob;
import org.apache.shardingsphere.elasticjob.test.natived.commons.job.annotation.NativeAnnotationSimpleJob;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;

import java.io.IOException;
import java.time.Duration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledInNativeImage
class AnnotationJavaTest {
    
    private static TestingServer testingServer;
    
    private static CoordinatorRegistryCenter regCenter;
    
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
        regCenter = new ZookeeperRegistryCenter(new ZookeeperConfiguration(testingServer.getConnectString(), "elasticjob-test-native-annotation-java"));
        regCenter.init();
    }
    
    @AfterAll
    static void afterAll() throws IOException {
        regCenter.close();
        testingServer.close();
    }
    
    @Test
    void testGenerateJobConfigurationFromAnnotation() {
        JobConfiguration jobConfig = JobAnnotationBuilder.generateJobConfiguration(NativeAnnotationSimpleJob.class);
        assertThat(jobConfig.getJobName(), is("nativeAnnotationSimpleScheduleJob"));
        assertThat(jobConfig.getCron(), is("0/5 * * * * ?"));
        assertThat(jobConfig.getShardingTotalCount(), is(3));
        assertThat(jobConfig.getLabel(), is("native-label"));
        assertTrue(jobConfig.isStaticSharding());
        JobConfiguration oneOffConfig = JobAnnotationBuilder.generateJobConfiguration(NativeAnnotationOneOffJob.class);
        assertThat(oneOffConfig.getJobName(), is("nativeAnnotationOneOffScheduleJob"));
        assertNull(oneOffConfig.getCron());
    }
    
    @Test
    void testScheduleJobWithAnnotation() {
        ScheduleJobBootstrap jobBootstrap = new ScheduleJobBootstrap(regCenter, new NativeAnnotationSimpleJob());
        assertDoesNotThrow(() -> {
            jobBootstrap.schedule();
            jobBootstrap.shutdown();
        });
    }
    
    @Test
    void testOneOffJobWithAnnotation() {
        OneOffJobBootstrap jobBootstrap = new OneOffJobBootstrap(regCenter, new NativeAnnotationOneOffJob());
        assertDoesNotThrow(() -> {
            jobBootstrap.execute();
            jobBootstrap.shutdown();
        });
    }
}
