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

import com.dangdang.ddframe.job.fixture.OneOffElasticJob;
import com.dangdang.ddframe.job.fixture.PerpetualElasticJob;
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
        OneOffElasticJob.reset();
        PerpetualElasticJob.reset();
        ProcessCountStatistics.reset("testJob");
    }
    
    @After
    public void tearDown() {
        JobRegistry.getInstance().getJob("oneOffElasticJob").shutdown();
        JobRegistry.getInstance().getJob("perpetualElasticJob").shutdown();
        WaitingUtils.waitingLongTime();
    }
    
    @Test
    public void assertSpringJobBean() {
        assertOneOffElasticJobBean();
        assertPerpetualElasticJobBean();
    }
    
    private void assertOneOffElasticJobBean() {
        while (!OneOffElasticJob.isCompleted() || null == OneOffElasticJob.getJobValue()) {
            WaitingUtils.waitingShortTime();
        }
        assertTrue(OneOffElasticJob.isCompleted());
        assertThat(OneOffElasticJob.getJobValue(), is("oneOff"));
        assertTrue(regCenter.isExisted("/oneOffElasticJob/execution"));
    }
    
    private void assertPerpetualElasticJobBean() {
        while (!PerpetualElasticJob.isCompleted()) {
            WaitingUtils.waitingShortTime();
        }
        assertTrue(PerpetualElasticJob.isCompleted());
        assertTrue(regCenter.isExisted("/perpetualElasticJob/execution"));
    }
}
