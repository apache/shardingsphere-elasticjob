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

import org.apache.shardingsphere.elasticjob.kernel.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.spring.namespace.fixture.job.ref.RefFooSimpleElasticJob;
import org.apache.shardingsphere.elasticjob.test.util.EmbedTestingServer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:META-INF/job/oneOffWithJobRef.xml")
class OneOffJobSpringNamespaceWithRefTest {
    
    private static final EmbedTestingServer EMBED_TESTING_SERVER = new EmbedTestingServer(3181);
    
    private final String oneOffSimpleJobName = "oneOffSimpleElasticJobRef";
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private CoordinatorRegistryCenter regCenter;
    
    @BeforeAll
    static void init() {
        EMBED_TESTING_SERVER.start();
    }
    
    @BeforeEach
    @AfterEach
    void reset() {
        RefFooSimpleElasticJob.reset();
    }
    
    @AfterEach
    void tearDown() {
        JobRegistry.getInstance().shutdown(oneOffSimpleJobName);
    }
    
    @Test
    void assertSpringJobBean() {
        OneOffJobBootstrap bootstrap = applicationContext.getBean(oneOffSimpleJobName, OneOffJobBootstrap.class);
        bootstrap.execute();
        assertOneOffSimpleElasticJobBean();
    }
    
    private void assertOneOffSimpleElasticJobBean() {
        Awaitility.await().atMost(1L, TimeUnit.MINUTES).untilAsserted(() -> assertThat(RefFooSimpleElasticJob.isCompleted(), is(true)));
        assertTrue(RefFooSimpleElasticJob.isCompleted());
        assertTrue(regCenter.isExisted("/" + oneOffSimpleJobName + "/sharding"));
    }
}
