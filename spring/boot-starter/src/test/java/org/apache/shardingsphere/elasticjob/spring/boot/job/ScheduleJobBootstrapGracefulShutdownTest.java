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

package org.apache.shardingsphere.elasticjob.spring.boot.job;

import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.elasticjob.spi.executor.item.param.ShardingContext;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that scheduled job bootstraps are shut down when the application context closes,
 * so that no scheduled trigger fires afterwards.
 */
class ScheduleJobBootstrapGracefulShutdownTest {
    
    private static final String JOB_NAME = "gracefulShutdownJob";
    
    private static TestingServer testingServer;
    
    @BeforeAll
    static void init() throws Exception {
        testingServer = new TestingServer();
        try (
                CuratorZookeeperClient client = new CuratorZookeeperClient(testingServer.getConnectString(),
                        60000, 500, null,
                        new ExponentialBackoffRetry(500, 3, 1500))) {
            client.start();
            Awaitility.await().atMost(Duration.ofSeconds(30L)).ignoreExceptions().until(client::isConnected);
        }
    }
    
    @AfterAll
    static void afterAll() throws IOException {
        testingServer.close();
    }
    
    @Test
    void assertScheduledJobsShutDownOnContextClose() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ElasticJobAutoConfiguration.class))
                .withUserConfiguration(TestJobConfiguration.class)
                .withPropertyValues(
                        "elasticjob.reg-center.serverLists=" + testingServer.getConnectString(),
                        "elasticjob.reg-center.namespace=graceful-shutdown-test",
                        "elasticjob.jobs.gracefulShutdownJob.elasticJobClass=org.apache.shardingsphere.elasticjob.spring.boot.job.ScheduleJobBootstrapGracefulShutdownTest$CountingJob",
                        "elasticjob.jobs.gracefulShutdownJob.cron=0/1 * * * * ?",
                        "elasticjob.jobs.gracefulShutdownJob.shardingTotalCount=1")
                .run(context -> {
                    context.getBean(ScheduleJobBootstrapStartupRunner.class).run();
                    assertFalse(JobRegistry.getInstance().isShutdown(JOB_NAME));
                });
        assertTrue(JobRegistry.getInstance().isShutdown(JOB_NAME));
    }
    
    @Test
    void assertStopPausesAndStartResumesWithoutShutdown() {
        String jobName = "stopStartJob";
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ElasticJobAutoConfiguration.class))
                .withUserConfiguration(TestJobConfiguration.class)
                .withPropertyValues(
                        "elasticjob.reg-center.serverLists=" + testingServer.getConnectString(),
                        "elasticjob.reg-center.namespace=stop-start-test",
                        "elasticjob.jobs.stopStartJob.elasticJobClass=org.apache.shardingsphere.elasticjob.spring.boot.job.ScheduleJobBootstrapGracefulShutdownTest$CountingJob",
                        "elasticjob.jobs.stopStartJob.cron=0/1 * * * * ?",
                        "elasticjob.jobs.stopStartJob.shardingTotalCount=1")
                .run(context -> {
                    ScheduleJobBootstrapStartupRunner runner = context.getBean(ScheduleJobBootstrapStartupRunner.class);
                    runner.run();
                    assertFalse(JobRegistry.getInstance().isShutdown(jobName));
                    assertFalse(JobRegistry.getInstance().getJobScheduleController(jobName).isPaused());
                    ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) context.getSourceApplicationContext();
                    applicationContext.stop();
                    assertFalse(runner.isRunning());
                    assertFalse(JobRegistry.getInstance().isShutdown(jobName));
                    assertTrue(JobRegistry.getInstance().getJobScheduleController(jobName).isPaused());
                    applicationContext.start();
                    assertTrue(runner.isRunning());
                    assertFalse(JobRegistry.getInstance().isShutdown(jobName));
                    assertFalse(JobRegistry.getInstance().getJobScheduleController(jobName).isPaused());
                });
        assertTrue(JobRegistry.getInstance().isShutdown(jobName));
    }
    
    @Configuration
    static class TestJobConfiguration {
        
        @Bean
        CountingJob countingJob() {
            return new CountingJob();
        }
    }
    
    public static class CountingJob implements SimpleJob {
        
        @Override
        public void execute(final ShardingContext shardingContext) {
        }
    }
}
