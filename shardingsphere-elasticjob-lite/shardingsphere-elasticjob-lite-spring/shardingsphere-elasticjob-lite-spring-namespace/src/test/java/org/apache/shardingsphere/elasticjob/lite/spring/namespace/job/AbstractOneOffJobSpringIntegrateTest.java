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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.infra.concurrent.BlockUtils;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.spring.namespace.fixture.job.DataflowElasticJob;
import org.apache.shardingsphere.elasticjob.lite.spring.namespace.fixture.job.FooSimpleElasticJob;
import org.apache.shardingsphere.elasticjob.lite.spring.namespace.test.AbstractZookeeperJUnit4SpringContextTests;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertTrue;

@RequiredArgsConstructor
public abstract class AbstractOneOffJobSpringIntegrateTest extends AbstractZookeeperJUnit4SpringContextTests {
    
    private final String simpleJobName;
    
    private final String throughputDataflowJobName;
    
    @Resource
    private CoordinatorRegistryCenter regCenter;
    
    @Before
    @After
    public void reset() {
        FooSimpleElasticJob.reset();
        DataflowElasticJob.reset();
    }
    
    @After
    public void tearDown() {
        JobRegistry.getInstance().shutdown(simpleJobName);
        JobRegistry.getInstance().shutdown(throughputDataflowJobName);
    }
    
    @Test
    public void assertSpringJobBean() {
        assertSimpleElasticJobBean();
        assertThroughputDataflowElasticJobBean();
    }
    
    private void assertSimpleElasticJobBean() {
        OneOffJobBootstrap bootstrap = applicationContext.getBean(simpleJobName, OneOffJobBootstrap.class);
        bootstrap.execute();
        while (!FooSimpleElasticJob.isCompleted()) {
            BlockUtils.waitingShortTime();
        }
        assertTrue(FooSimpleElasticJob.isCompleted());
        assertTrue(regCenter.isExisted("/" + simpleJobName + "/sharding"));
    }
    
    private void assertThroughputDataflowElasticJobBean() {
        OneOffJobBootstrap bootstrap = applicationContext.getBean(throughputDataflowJobName, OneOffJobBootstrap.class);
        bootstrap.execute();
        while (!DataflowElasticJob.isCompleted()) {
            BlockUtils.waitingShortTime();
        }
        assertTrue(DataflowElasticJob.isCompleted());
        assertTrue(regCenter.isExisted("/" + throughputDataflowJobName + "/sharding"));
    }
}
