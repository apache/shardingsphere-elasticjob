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
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.test.util.EmbedTestingServer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:META-INF/job/withJobType.xml", initializers = JobSpringNamespaceWithTypeTest.ScriptCommandLineInitializer.class)
class JobSpringNamespaceWithTypeTest {
    
    private static final EmbedTestingServer EMBED_TESTING_SERVER = new EmbedTestingServer(3181);
    
    private final String scriptJobName = "scriptElasticJob_job_type";
    
    @Autowired
    private CoordinatorRegistryCenter regCenter;
    
    private Scheduler scheduler;
    
    @BeforeAll
    static void init() {
        EMBED_TESTING_SERVER.start();
    }
    
    @AfterEach
    void tearDown() {
        Awaitility.await().atMost(1L, TimeUnit.MINUTES).until(() -> scheduler.getCurrentlyExecutingJobs().isEmpty());
        JobRegistry.getInstance().getJobScheduleController(scriptJobName).shutdown();
    }
    
    @Test
    void jobScriptWithJobTypeTest() throws SchedulerException {
        Awaitility.await().atMost(1L, TimeUnit.MINUTES).until(() -> regCenter.isExisted("/" + scriptJobName + "/sharding"));
        scheduler = (Scheduler) ReflectionTestUtils.getField(JobRegistry.getInstance().getJobScheduleController(scriptJobName), "scheduler");
        assertTrue(scheduler.isStarted());
    }
    
    static class ScriptCommandLineInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        
        @Override
        public void initialize(final ConfigurableApplicationContext applicationContext) {
            String command = System.getProperty("os.name", "").contains("Windows") ? "cmd /c echo test" : "echo test";
            applicationContext.getEnvironment().getPropertySources()
                    .addFirst(new MapPropertySource("testScriptCommandLine", Collections.singletonMap("script.scriptCommandLine", command)));
        }
    }
}
