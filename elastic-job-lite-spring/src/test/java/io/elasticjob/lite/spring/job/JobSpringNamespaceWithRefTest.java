/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.elasticjob.lite.spring.job;

import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import io.elasticjob.lite.spring.fixture.job.ref.RefFooSimpleElasticJob;
import io.elasticjob.lite.spring.test.AbstractZookeeperJUnit4SpringContextTests;
import io.elasticjob.lite.util.concurrent.BlockUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;

import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/job/withJobRef.xml")
public final class JobSpringNamespaceWithRefTest extends AbstractZookeeperJUnit4SpringContextTests {
    
    private final String simpleJobName = "simpleElasticJob_job_ref";
    
    @Resource
    private CoordinatorRegistryCenter regCenter;
    
    @Before
    @After
    public void reset() {
        RefFooSimpleElasticJob.reset();
    }
    
    @After
    public void tearDown() {
        JobRegistry.getInstance().shutdown(simpleJobName);
    }
    
    @Test
    public void assertSpringJobBean() {
        assertSimpleElasticJobBean();
    }

    private void assertSimpleElasticJobBean() {
        while (!RefFooSimpleElasticJob.isCompleted()) {
            BlockUtils.sleep(100L);
        }
        assertTrue(RefFooSimpleElasticJob.isCompleted());
        assertTrue(regCenter.isExisted("/" + simpleJobName + "/sharding"));
    }
}
