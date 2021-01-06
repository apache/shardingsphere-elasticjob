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
import org.apache.shardingsphere.elasticjob.dataflow.props.DataflowJobProperties;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.script.props.ScriptJobProperties;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public final class LocalTaskExecutorTest {
    
    @Test
    public void assertSimpleJob() {
        TestSimpleJob simpleJob = new TestSimpleJob();
        new LocalTaskExecutor(simpleJob, JobConfiguration.newBuilder(TestSimpleJob.class.getSimpleName(), 3)
                .cron("*/2 * * * * ?").shardingItemParameters("0=A,1=B").build(), 1).execute();
        assertThat(simpleJob.getShardingParameters(), is(Collections.singletonList("B")));
    }
    
    @Test
    public void assertDataflowJob() {
        TestDataflowJob dataflowJob = new TestDataflowJob();
        new LocalTaskExecutor(dataflowJob, JobConfiguration.newBuilder(TestDataflowJob.class.getSimpleName(), 3)
                .cron("*/2 * * * * ?").setProperty(DataflowJobProperties.STREAM_PROCESS_KEY, Boolean.FALSE.toString()).build(), 1).execute();
        assertFalse(dataflowJob.getOutput().isEmpty());
        assertThat(dataflowJob.getOutput(), is(Collections.singletonList("1-d")));
    }
    
    @Test
    public void assertScriptJob() {
        new LocalTaskExecutor(new TestDataflowJob(), JobConfiguration.newBuilder("TestScriptJob", 3)
                .cron("*/2 * * * * ?").setProperty(ScriptJobProperties.SCRIPT_KEY, "echo test").build(), 1).execute();
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertNotExistsJobType() {
        new LocalTaskExecutor("not exist", JobConfiguration.newBuilder("not exist", 3).cron("*/2 * * * * ?").build(), 1).execute();
    }
}
