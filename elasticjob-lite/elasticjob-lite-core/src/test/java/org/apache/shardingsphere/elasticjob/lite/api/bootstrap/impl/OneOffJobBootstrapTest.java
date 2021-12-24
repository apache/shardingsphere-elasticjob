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

package org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.fixture.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduler;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class OneOffJobBootstrapTest {

    private static final ZookeeperConfiguration ZOOKEEPER_CONFIGURATION = new ZookeeperConfiguration(EmbedTestingServer.getConnectionString(), OneOffJobBootstrapTest.class.getSimpleName());

    private static final int SHARDING_TOTAL_COUNT = 3;

    private ZookeeperRegistryCenter zkRegCenter;

    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }

    @Before
    public void setUp() {
        zkRegCenter = new ZookeeperRegistryCenter(ZOOKEEPER_CONFIGURATION);
        zkRegCenter.init();
    }

    @After
    public void teardown() {
        zkRegCenter.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertConfigFailedWithCron() {
        new OneOffJobBootstrap(zkRegCenter, (SimpleJob) shardingContext -> {
        }, JobConfiguration.newBuilder("test_one_off_job_execute_with_config_cron", SHARDING_TOTAL_COUNT).cron("0/5 * * * * ?").build());
    }

    @Test
    public void assertExecute() {
        AtomicInteger counter = new AtomicInteger(0);
        final OneOffJobBootstrap oneOffJobBootstrap = new OneOffJobBootstrap(zkRegCenter, (SimpleJob) shardingContext -> {
            counter.incrementAndGet();
        }, JobConfiguration.newBuilder("test_one_off_job_execute", SHARDING_TOTAL_COUNT).build());
        oneOffJobBootstrap.execute();
        blockUtilFinish(oneOffJobBootstrap, counter);
        assertThat(counter.get(), is(SHARDING_TOTAL_COUNT));
        getJobScheduler(oneOffJobBootstrap).shutdown();
    }

    @Test
    public void assertShutdown() throws SchedulerException {
        OneOffJobBootstrap oneOffJobBootstrap = new OneOffJobBootstrap(zkRegCenter, (SimpleJob) shardingContext -> {
        }, JobConfiguration.newBuilder("test_one_off_job_shutdown", SHARDING_TOTAL_COUNT).build());
        oneOffJobBootstrap.shutdown();
        assertTrue(getScheduler(oneOffJobBootstrap).isShutdown());
    }

    @SneakyThrows
    private JobScheduler getJobScheduler(final OneOffJobBootstrap oneOffJobBootstrap) {
        Field field = OneOffJobBootstrap.class.getDeclaredField("jobScheduler");
        field.setAccessible(true);
        return (JobScheduler) field.get(oneOffJobBootstrap);
    }

    @SneakyThrows
    private Scheduler getScheduler(final OneOffJobBootstrap oneOffJobBootstrap) {
        JobScheduler jobScheduler = getJobScheduler(oneOffJobBootstrap);
        Field schedulerField = JobScheduleController.class.getDeclaredField("scheduler");
        schedulerField.setAccessible(true);
        return (Scheduler) schedulerField.get(jobScheduler.getJobScheduleController());
    }

    @SneakyThrows
    private void blockUtilFinish(final OneOffJobBootstrap oneOffJobBootstrap, final AtomicInteger counter) {
        Scheduler scheduler = getScheduler(oneOffJobBootstrap);
        while (0 == counter.get() || !scheduler.getCurrentlyExecutingJobs().isEmpty()) {
            Thread.sleep(100);
        }
    }
}
