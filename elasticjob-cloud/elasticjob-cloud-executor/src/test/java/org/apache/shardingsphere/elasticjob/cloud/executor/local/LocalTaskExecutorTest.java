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

package org.apache.shardingsphere.elasticjob.cloud.executor.local;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.executor.fixture.TestDataflowJob;
import org.apache.shardingsphere.elasticjob.cloud.executor.fixture.TestSimpleJob;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.script.props.ScriptJobProperties;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class LocalTaskExecutorTest {
    
    @Before
    public void setUp() {
        TestSimpleJob.setShardingContext(null);
    }
    
    @Test
    public void assertSimpleJob() {
        new LocalTaskExecutor(new TestSimpleJob(), JobConfiguration.newBuilder(TestSimpleJob.class.getSimpleName(), 3).cron("*/2 * * * * ?").build(), 1).execute();
        assertThat(TestSimpleJob.getShardingContext().getJobName(), is(TestSimpleJob.class.getSimpleName()));
        assertThat(TestSimpleJob.getShardingContext().getShardingItem(), is(1));
        assertThat(TestSimpleJob.getShardingContext().getShardingTotalCount(), is(3));
        assertThat(TestSimpleJob.getShardingContext().getShardingItem(), is(1));
        assertNull(TestSimpleJob.getShardingContext().getShardingParameter());
        assertThat(TestSimpleJob.getShardingContext().getJobParameter(), is(""));
    }
    
    @Test
    public void assertSpringSimpleJob() {
        new LocalTaskExecutor(new TestSimpleJob(), 
                JobConfiguration.newBuilder(TestSimpleJob.class.getSimpleName(), 3).cron("*/2 * * * * ?")
                        .shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou").jobParameter("dbName=dangdang").build(), 1).execute();
        assertThat(TestSimpleJob.getShardingContext().getJobName(), is(TestSimpleJob.class.getSimpleName()));
        assertThat(TestSimpleJob.getShardingContext().getShardingTotalCount(), is(3));
        assertThat(TestSimpleJob.getShardingContext().getJobParameter(), is("dbName=dangdang"));
        assertThat(TestSimpleJob.getShardingContext().getShardingItem(), is(1));
        assertThat(TestSimpleJob.getShardingParameters().size(), is(1));
        assertThat(TestSimpleJob.getShardingParameters().iterator().next(), is("Shanghai"));
    }
    
    @Test
    public void assertDataflowJob() {
        TestDataflowJob dataflowJob = new TestDataflowJob();
        new LocalTaskExecutor(dataflowJob, JobConfiguration.newBuilder(TestDataflowJob.class.getSimpleName(), 10).cron("*/2 * * * * ?").build(), 5).execute();
        assertFalse(dataflowJob.getOutput().isEmpty());
        for (String each : dataflowJob.getOutput()) {
            assertTrue(each.endsWith("-d"));
        }
    }
    
    @Test
    public void assertScriptJob() {
        new LocalTaskExecutor(new TestDataflowJob(), JobConfiguration.newBuilder("TestScriptJob", 3).cron("*/2 * * * * ?")
                .setProperty(ScriptJobProperties.SCRIPT_KEY, "echo test").build(), 1).execute();
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertNotExistsJobClass() {
        new LocalTaskExecutor("not exist", JobConfiguration.newBuilder("not exist", 3).cron("*/2 * * * * ?").build(), 1).execute();
    }
}
