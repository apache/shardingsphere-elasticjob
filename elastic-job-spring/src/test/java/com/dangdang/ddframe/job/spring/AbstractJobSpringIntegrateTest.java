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
import com.dangdang.ddframe.job.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.internal.statistics.ProcessCountStatistics;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.test.AbstractZookeeperJUnit4SpringContextTests;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractJobSpringIntegrateTest extends AbstractZookeeperJUnit4SpringContextTests {
    
    private final String simpleJobName;
    
    private final String throughputDataFlowJobName;
    
    @Resource
    private CoordinatorRegistryCenter regCenter;
    
    @Before
    @After
    public void reset() {
        SimpleElasticJob.reset();
        ThroughputDataFlowElasticJob.reset();
        ProcessCountStatistics.reset(throughputDataFlowJobName);
    }
    
    @After
    public void tearDown() {
        JobRegistry.getInstance().getJobScheduler(simpleJobName).shutdown();
        JobRegistry.getInstance().getJobScheduler(throughputDataFlowJobName).shutdown();
    }
    
    @Test
    public void assertSpringJobBean() {
        assertSimpleElasticJobBean();
        assertThroughputDataFlowElasticJobBean();
    }
    
    private void assertSimpleElasticJobBean() {
        while (!SimpleElasticJob.isCompleted() || null == SimpleElasticJob.getJobValue()) {
            sleep(100L);
        }
        assertTrue(SimpleElasticJob.isCompleted());
        assertThat(SimpleElasticJob.getJobValue(), is("simple"));
        assertTrue(regCenter.isExisted("/" + simpleJobName + "/execution"));
    }
    
    private void assertThroughputDataFlowElasticJobBean() {
        while (!ThroughputDataFlowElasticJob.isCompleted()) {
            sleep(100L);
        }
        assertTrue(ThroughputDataFlowElasticJob.isCompleted());
        assertTrue(regCenter.isExisted("/" + throughputDataFlowJobName + "/execution"));
    }
    
    private static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
