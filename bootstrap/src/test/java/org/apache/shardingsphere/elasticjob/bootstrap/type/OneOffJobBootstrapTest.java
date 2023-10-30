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

package org.apache.shardingsphere.elasticjob.bootstrap.type;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobScheduler;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.elasticjob.test.util.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OneOffJobBootstrapTest {
    
    private static final EmbedTestingServer EMBED_TESTING_SERVER = new EmbedTestingServer(7181);
    
    private static final ZookeeperConfiguration ZOOKEEPER_CONFIGURATION = new ZookeeperConfiguration(EMBED_TESTING_SERVER.getConnectionString(), OneOffJobBootstrapTest.class.getSimpleName());
    
    private static final int SHARDING_TOTAL_COUNT = 3;
    
    private ZookeeperRegistryCenter zkRegCenter;
    
    @BeforeAll
    static void init() {
        EMBED_TESTING_SERVER.start();
    }
    
    @BeforeEach
    void setUp() {
        zkRegCenter = new ZookeeperRegistryCenter(ZOOKEEPER_CONFIGURATION);
        zkRegCenter.init();
    }
    
    @AfterEach
    void tearDown() {
        zkRegCenter.close();
    }
    
    @Test
    void assertConfigFailedWithCron() {
        assertThrows(IllegalArgumentException.class, () -> new OneOffJobBootstrap(zkRegCenter, (SimpleJob) shardingContext -> {
        }, JobConfiguration.newBuilder("test_one_off_job_execute_with_config_cron", SHARDING_TOTAL_COUNT).cron("0/5 * * * * ?").build()));
    }
    
    @Test
    void assertExecute() {
        AtomicInteger counter = new AtomicInteger(0);
        final OneOffJobBootstrap oneOffJobBootstrap = new OneOffJobBootstrap(zkRegCenter,
                (SimpleJob) shardingContext -> counter.incrementAndGet(), JobConfiguration.newBuilder("test_one_off_job_execute", SHARDING_TOTAL_COUNT).build());
        oneOffJobBootstrap.execute();
        blockUtilFinish(oneOffJobBootstrap, counter);
        assertThat(counter.get(), is(SHARDING_TOTAL_COUNT));
        ((JobScheduler) ReflectionUtils.getFieldValue(oneOffJobBootstrap, "jobScheduler")).shutdown();
    }
    
    @Test
    void assertShutdown() throws SchedulerException {
        OneOffJobBootstrap oneOffJobBootstrap = new OneOffJobBootstrap(zkRegCenter, (SimpleJob) shardingContext -> {
        }, JobConfiguration.newBuilder("test_one_off_job_shutdown", SHARDING_TOTAL_COUNT).build());
        oneOffJobBootstrap.shutdown();
        assertTrue(getScheduler(oneOffJobBootstrap).isShutdown());
    }
    
    private Scheduler getScheduler(final OneOffJobBootstrap oneOffJobBootstrap) {
        JobScheduler jobScheduler = (JobScheduler) ReflectionUtils.getFieldValue(oneOffJobBootstrap, "jobScheduler");
        return (Scheduler) ReflectionUtils.getFieldValue(jobScheduler.getJobScheduleController(), "scheduler");
    }
    
    private void blockUtilFinish(final OneOffJobBootstrap oneOffJobBootstrap, final AtomicInteger counter) {
        Scheduler scheduler = getScheduler(oneOffJobBootstrap);
        Awaitility.await().pollDelay(100L, TimeUnit.MILLISECONDS).until(() -> 0 != counter.get() && scheduler.getCurrentlyExecutingJobs().isEmpty());
    }
}
