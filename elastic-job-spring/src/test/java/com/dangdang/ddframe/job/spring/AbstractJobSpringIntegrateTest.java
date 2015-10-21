/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.spring;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dangdang.ddframe.job.fixture.SimpleElasticJob;
import com.dangdang.ddframe.job.fixture.ThroughputDataFlowElasticJob;
import com.dangdang.ddframe.job.internal.statistics.ProcessCountStatistics;
import com.dangdang.ddframe.job.schedule.JobRegistry;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.test.AbstractZookeeperJUnit4SpringContextTests;
import com.dangdang.ddframe.test.WaitingUtils;

public abstract class AbstractJobSpringIntegrateTest extends AbstractZookeeperJUnit4SpringContextTests {
    
    @Resource
    private CoordinatorRegistryCenter regCenter;
    
    @Before
    @After
    public void reset() {
        SimpleElasticJob.reset();
        ThroughputDataFlowElasticJob.reset();
        ProcessCountStatistics.reset("testJob");
    }
    
    @After
    public void tearDown() {
        JobRegistry.getInstance().getJob("simpleElasticJob").shutdown();
        JobRegistry.getInstance().getJob("throughputDataFlowElasticJob").shutdown();
        WaitingUtils.waitingLongTime();
    }
    
    @Test
    public void assertSpringJobBean() {
        assertSimpleElasticJobBean();
        assertThroughputDataFlowElasticJobBean();
    }
    
    private void assertSimpleElasticJobBean() {
        while (!SimpleElasticJob.isCompleted() || null == SimpleElasticJob.getJobValue()) {
            WaitingUtils.waitingShortTime();
        }
        assertTrue(SimpleElasticJob.isCompleted());
        assertThat(SimpleElasticJob.getJobValue(), is("simple"));
        assertTrue(regCenter.isExisted("/simpleElasticJob/execution"));
    }
    
    private void assertThroughputDataFlowElasticJobBean() {
        while (!ThroughputDataFlowElasticJob.isCompleted()) {
            WaitingUtils.waitingShortTime();
        }
        assertTrue(ThroughputDataFlowElasticJob.isCompleted());
        assertTrue(regCenter.isExisted("/throughputDataFlowElasticJob/execution"));
    }
}
