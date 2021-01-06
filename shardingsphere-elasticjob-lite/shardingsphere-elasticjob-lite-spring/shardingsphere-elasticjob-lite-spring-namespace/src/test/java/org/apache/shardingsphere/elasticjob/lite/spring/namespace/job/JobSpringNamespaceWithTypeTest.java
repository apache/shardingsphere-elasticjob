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

package org.apache.shardingsphere.elasticjob.lite.spring.namespace.job;

import static org.junit.Assert.assertTrue;
import javax.annotation.Resource;
import org.apache.shardingsphere.elasticjob.infra.concurrent.BlockUtils;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.spring.namespace.test.AbstractZookeeperJUnit4SpringContextTests;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.After;
import org.junit.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

@ContextConfiguration(locations = "classpath:META-INF/job/withJobType.xml")
public final class JobSpringNamespaceWithTypeTest extends AbstractZookeeperJUnit4SpringContextTests {
    
    private final String scriptJobName = "scriptElasticJob_job_type";
    
    @Resource
    private CoordinatorRegistryCenter regCenter;
    
    private Scheduler scheduler;

    @After
    public void tearDown() throws SchedulerException {
        while (!scheduler.getCurrentlyExecutingJobs().isEmpty()) {
            BlockUtils.waitingShortTime();
        }
        JobRegistry.getInstance().getJobScheduleController(scriptJobName).shutdown();
    }
    
    @Test
    public void jobScriptWithJobTypeTest() throws SchedulerException {
        while (!regCenter.isExisted("/" + scriptJobName + "/sharding")) {
            BlockUtils.waitingShortTime();
        }
        scheduler = (Scheduler) ReflectionTestUtils.getField(JobRegistry.getInstance().getJobScheduleController(scriptJobName), "scheduler");
        assertTrue(scheduler.isStarted());
    }
}
