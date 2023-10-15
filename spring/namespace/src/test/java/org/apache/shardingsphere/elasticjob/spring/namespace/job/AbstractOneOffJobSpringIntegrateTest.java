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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.kernel.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.spring.namespace.fixture.job.DataflowElasticJob;
import org.apache.shardingsphere.elasticjob.spring.namespace.fixture.job.FooSimpleElasticJob;
import org.apache.shardingsphere.elasticjob.spring.namespace.test.AbstractZookeeperJUnitJupiterSpringContextTests;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor
public abstract class AbstractOneOffJobSpringIntegrateTest extends AbstractZookeeperJUnitJupiterSpringContextTests {
    
    private final String simpleJobName;
    
    private final String throughputDataflowJobName;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private CoordinatorRegistryCenter regCenter;
    
    @BeforeEach
    @AfterEach
    void reset() {
        FooSimpleElasticJob.reset();
        DataflowElasticJob.reset();
    }
    
    @AfterEach
    void tearDown() {
        JobRegistry.getInstance().shutdown(simpleJobName);
        JobRegistry.getInstance().shutdown(throughputDataflowJobName);
    }
    
    @Test
    void assertSpringJobBean() {
        assertSimpleElasticJobBean();
        assertThroughputDataflowElasticJobBean();
    }
    
    private void assertSimpleElasticJobBean() {
        OneOffJobBootstrap bootstrap = applicationContext.getBean(simpleJobName, OneOffJobBootstrap.class);
        bootstrap.execute();
        Awaitility.await().atMost(5L, TimeUnit.MINUTES).untilAsserted(() -> assertThat(FooSimpleElasticJob.isCompleted(), is(true)));
        assertTrue(FooSimpleElasticJob.isCompleted());
        assertTrue(regCenter.isExisted("/" + simpleJobName + "/sharding"));
    }
    
    private void assertThroughputDataflowElasticJobBean() {
        OneOffJobBootstrap bootstrap = applicationContext.getBean(throughputDataflowJobName, OneOffJobBootstrap.class);
        bootstrap.execute();
        Awaitility.await().atMost(5L, TimeUnit.MINUTES).untilAsserted(() -> assertThat(DataflowElasticJob.isCompleted(), is(true)));
        assertTrue(DataflowElasticJob.isCompleted());
        assertTrue(regCenter.isExisted("/" + throughputDataflowJobName + "/sharding"));
    }
}
