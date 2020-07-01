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

package org.apache.shardingsphere.elasticjob.lite.integrate.std.simple;

import org.apache.shardingsphere.elasticjob.lite.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.lite.api.JobType;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.integrate.AbstractBaseStdJobTest;
import org.apache.shardingsphere.elasticjob.lite.integrate.fixture.simple.FooSimpleElasticJob;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public final class DisabledJobTest extends AbstractBaseStdJobTest {
    
    public DisabledJobTest() {
        super(new FooSimpleElasticJob(), true);
    }
    
    @Before
    @After
    public void reset() {
        FooSimpleElasticJob.reset();
    }
    
    @Override
    protected JobConfiguration getJobConfiguration(final ElasticJob elasticJob, final String jobName) {
        return JobConfiguration.newBuilder(jobName, JobType.SIMPLE, 3)
                .cron("0/1 * * * * ?").shardingItemParameters("0=A,1=B,2=C").jobErrorHandlerType("IGNORE").disabled(true).overwrite(true).build();
    }
    
    @Test
    public void assertJobInit() {
        scheduleJob();
        assertRegCenterCommonInfoWithDisabled();
    }
}
