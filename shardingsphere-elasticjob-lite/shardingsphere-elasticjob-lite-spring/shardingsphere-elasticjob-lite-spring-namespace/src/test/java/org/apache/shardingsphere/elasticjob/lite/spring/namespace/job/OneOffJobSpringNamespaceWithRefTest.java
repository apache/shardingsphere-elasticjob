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

import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.spring.namespace.fixture.job.ref.RefFooSimpleElasticJob;
import org.apache.shardingsphere.elasticjob.lite.spring.namespace.test.AbstractZookeeperJUnit4SpringContextTests;
import org.apache.shardingsphere.elasticjob.infra.concurrent.BlockUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;

import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/job/oneOffWithJobRef.xml")
public final class OneOffJobSpringNamespaceWithRefTest extends AbstractZookeeperJUnit4SpringContextTests {
    
    private final String oneOffSimpleJobName = "oneOffSimpleElasticJobRef";
    
    @Resource
    private CoordinatorRegistryCenter regCenter;

    @Before
    @After
    public void reset() {
        RefFooSimpleElasticJob.reset();
    }
    
    @After
    public void tearDown() {
        JobRegistry.getInstance().shutdown(oneOffSimpleJobName);
    }
    
    @Test
    public void assertSpringJobBean() {
        OneOffJobBootstrap bootstrap = applicationContext.getBean(oneOffSimpleJobName, OneOffJobBootstrap.class);
        bootstrap.execute();
        assertOneOffSimpleElasticJobBean();
    }

    private void assertOneOffSimpleElasticJobBean() {
        while (!RefFooSimpleElasticJob.isCompleted()) {
            BlockUtils.waitingShortTime();
        }
        assertTrue(RefFooSimpleElasticJob.isCompleted());
        assertTrue(regCenter.isExisted("/" + oneOffSimpleJobName + "/sharding"));
    }
}
