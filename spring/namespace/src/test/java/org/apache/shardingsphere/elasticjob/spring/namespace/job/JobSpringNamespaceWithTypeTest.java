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

package org.apache.shardingsphere.elasticjob.spring.namespace.job;

import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.spring.namespace.test.AbstractZookeeperJUnitJupiterSpringContextTests;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/job/withJobType.xml")
class JobSpringNamespaceWithTypeTest extends AbstractZookeeperJUnitJupiterSpringContextTests {
    
    private final String scriptJobName = "scriptElasticJob_job_type";
    
    @Autowired
    private CoordinatorRegistryCenter regCenter;
    
    private Scheduler scheduler;
    
    @AfterEach
    void tearDown() {
        Awaitility.await().atMost(1L, TimeUnit.MINUTES).untilAsserted(() -> assertThat(scheduler.getCurrentlyExecutingJobs().isEmpty(), is(true)));
        JobRegistry.getInstance().getJobScheduleController(scriptJobName).shutdown();
    }
    
    @Test
    void jobScriptWithJobTypeTest() throws SchedulerException {
        Awaitility.await().atMost(1L, TimeUnit.MINUTES).untilAsserted(() -> assertThat(regCenter.isExisted("/" + scriptJobName + "/sharding"), is(true)));
        scheduler = (Scheduler) ReflectionTestUtils.getField(JobRegistry.getInstance().getJobScheduleController(scriptJobName), "scheduler");
        assertTrue(scheduler.isStarted());
    }
}
