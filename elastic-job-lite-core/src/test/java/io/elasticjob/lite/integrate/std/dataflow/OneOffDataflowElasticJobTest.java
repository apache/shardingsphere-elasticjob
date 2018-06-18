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

package io.elasticjob.lite.integrate.std.dataflow;

import io.elasticjob.lite.config.LiteJobConfiguration;
import io.elasticjob.lite.integrate.AbstractBaseStdJobAutoInitTest;
import io.elasticjob.lite.integrate.WaitingUtils;
import io.elasticjob.lite.integrate.fixture.dataflow.OneOffDataflowElasticJob;
import io.elasticjob.lite.fixture.util.JobConfigurationUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public final class OneOffDataflowElasticJobTest extends AbstractBaseStdJobAutoInitTest {
    
    public OneOffDataflowElasticJobTest() {
        super(OneOffDataflowElasticJob.class);
    }
    
    @Before
    @After
    public void reset() {
        OneOffDataflowElasticJob.reset();
    }
    
    @Override
    protected void setLiteJobConfig(final LiteJobConfiguration liteJobConfig) {
        JobConfigurationUtil.setFieldValue(liteJobConfig.getTypeConfig().getCoreConfig(), "misfire", false);
        JobConfigurationUtil.setFieldValue(liteJobConfig.getTypeConfig(), "streamingProcess", false);
    }
    
    @Test
    public void assertJobInit() {
        while (!OneOffDataflowElasticJob.isCompleted()) {
            WaitingUtils.waitingShortTime();
        }
        assertTrue(getRegCenter().isExisted("/" + getJobName() + "/sharding"));
    }
}
