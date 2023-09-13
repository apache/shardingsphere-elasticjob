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

package org.apache.shardingsphere.elasticjob.lite.reachability.metadata.fixture;

import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.dataflow.job.DataflowJob;
import org.apache.shardingsphere.elasticjob.dataflow.props.DataflowJobProperties;
import org.apache.shardingsphere.elasticjob.http.props.HttpJobProperties;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.reachability.metadata.fixture.entity.TOrderPOJO;
import org.apache.shardingsphere.elasticjob.lite.reachability.metadata.fixture.repository.VirtualTOrderRepository;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElasticjobLiteCoreTest {
    private static final int PORT = 6891;

    private static final VirtualTOrderRepository VIRTUAL_T_ORDER_REPOSITORY = new VirtualTOrderRepository();

    private static volatile TestingServer testingServer;

    private static CoordinatorRegistryCenter registryCenter;

    @BeforeAll
    static void beforeAll() throws Exception {
        testingServer = new TestingServer(PORT, true);
        try (CuratorZookeeperClient client = new CuratorZookeeperClient(testingServer.getConnectString(),
                60 * 1000, 500, null,
                new ExponentialBackoffRetry(500, 3, 500 * 3))) {
            client.start();
            Awaitility.await()
                    .atMost(Duration.ofMillis(500 * 60))
                    .untilAsserted(() -> assertTrue(client.isConnected()));
        // CHECKSTYLE:OFF
        } catch (Exception e) {
            // CHECKSTYLE:ON
            throw new RuntimeException(e);
        }
        registryCenter = new ZookeeperRegistryCenter(new ZookeeperConfiguration(testingServer.getConnectString(), "elasticjob-lite-core-java-test"));
        registryCenter.init();
    }

    @AfterAll
    static void afterAll() throws IOException {
        registryCenter.close();
        testingServer.close();
    }

    @Test
    void testJavaHttpJob() {
        ScheduleJobBootstrap jobBootstrap = new ScheduleJobBootstrap(registryCenter, "HTTP",
                JobConfiguration.newBuilder("testJavaHttpJob", 3)
                        .setProperty(HttpJobProperties.URI_KEY, "https://google.com")
                        .setProperty(HttpJobProperties.METHOD_KEY, "GET")
                        .cron("0/5 * * * * ?")
                        .shardingItemParameters("0=Norddorf,1=Bordeaux,2=Somerset")
                        .build()
        );
        assertDoesNotThrow(() -> {
            jobBootstrap.schedule();
            jobBootstrap.shutdown();
        });
    }

    @Test
    void testJavaSimpleJob() {
        ScheduleJobBootstrap jobBootstrap = new ScheduleJobBootstrap(registryCenter,
                (SimpleJob) shardingContext -> {
                    assertThat(shardingContext.getShardingItem(), is(3));
                    VIRTUAL_T_ORDER_REPOSITORY.findTodoData(shardingContext.getShardingParameter(), 10)
                            .forEach(each -> VIRTUAL_T_ORDER_REPOSITORY.setCompleted(each.getId()));
                },
                JobConfiguration.newBuilder("testJavaSimpleJob", 3)
                        .cron("0/5 * * * * ?")
                        .shardingItemParameters("0=Norddorf,1=Bordeaux,2=Somerset")
                        .build()
        );
        assertDoesNotThrow(() -> {
            jobBootstrap.schedule();
            jobBootstrap.shutdown();
        });
    }

    @Test
    void testJavaDataflowElasticJob() {
        ScheduleJobBootstrap jobBootstrap = new ScheduleJobBootstrap(registryCenter, new DataflowJob<TOrderPOJO>() {
            @Override
            public List<TOrderPOJO> fetchData(final ShardingContext shardingContext) {
                assertThat(shardingContext.getShardingItem(), is(3));
                return VIRTUAL_T_ORDER_REPOSITORY.findTodoData(shardingContext.getShardingParameter(), 10);
            }

            @Override
            public void processData(final ShardingContext shardingContext, final List<TOrderPOJO> data) {
                assertThat(shardingContext.getShardingItem(), is(3));
                data.stream().mapToLong(TOrderPOJO::getId).forEach(VIRTUAL_T_ORDER_REPOSITORY::setCompleted);
            }
        },
                JobConfiguration.newBuilder("testJavaDataflowElasticJob", 3)
                        .cron("0/5 * * * * ?")
                        .shardingItemParameters("0=Norddorf,1=Bordeaux,2=Somerset")
                        .setProperty(DataflowJobProperties.STREAM_PROCESS_KEY, Boolean.TRUE.toString())
                        .build()
        );
        assertDoesNotThrow(() -> {
            jobBootstrap.schedule();
            jobBootstrap.shutdown();
        });
    }

    @Test
    void testJavaOneOffSimpleJob() {
        OneOffJobBootstrap jobBootstrap = new OneOffJobBootstrap(registryCenter,
                (SimpleJob) shardingContext -> {
                    assertThat(shardingContext.getShardingItem(), is(3));
                    VIRTUAL_T_ORDER_REPOSITORY.findTodoData(shardingContext.getShardingParameter(), 10)
                            .forEach(each -> VIRTUAL_T_ORDER_REPOSITORY.setCompleted(each.getId()));
                },
                JobConfiguration.newBuilder("testJavaOneOffSimpleJob", 3)
                        .shardingItemParameters("0=Norddorf,1=Bordeaux,2=Somerset")
                        .build()
        );
        assertDoesNotThrow(() -> {
            jobBootstrap.execute();
            jobBootstrap.shutdown();
        });
    }
}
