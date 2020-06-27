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

package org.apache.shardingsphere.elasticjob.lite.integrate.std.dataflow;

import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.executor.type.impl.DataflowJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.integrate.AbstractBaseStdJobAutoInitTest;
import org.apache.shardingsphere.elasticjob.lite.integrate.WaitingUtils;
import org.apache.shardingsphere.elasticjob.lite.integrate.fixture.dataflow.OneOffDataflowElasticJob;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
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
    protected void setJobConfiguration(final JobConfiguration jobConfig) {
        ReflectionUtils.setFieldValue(jobConfig, "misfire", false);
        jobConfig.getProps().setProperty(DataflowJobExecutor.STREAM_PROCESS_KEY, Boolean.FALSE.toString());
    }
    
    @Test
    public void assertJobInit() {
        while (!OneOffDataflowElasticJob.isCompleted()) {
            WaitingUtils.waitingShortTime();
        }
        assertTrue(getRegCenter().isExisted("/" + getJobName() + "/sharding"));
    }
}
