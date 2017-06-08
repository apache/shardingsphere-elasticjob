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

package com.dangdang.ddframe.job.cloud.executor.local;

import com.dangdang.ddframe.job.cloud.executor.local.fixture.TestDataflowJob;
import com.dangdang.ddframe.job.cloud.executor.local.fixture.TestSimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.exception.JobConfigurationException;
import com.dangdang.ddframe.job.exception.JobSystemException;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.dangdang.ddframe.job.cloud.executor.local.LocalCloudJobExecutionType.DAEMON;
import static com.dangdang.ddframe.job.cloud.executor.local.LocalCloudJobExecutionType.TRANSIENT;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class LocalTaskExecutorTest {
    
    @Before
    public void setUp() throws Exception {
        TestSimpleJob.setShardingContext(null);
        TestDataflowJob.setInput(null);
        TestDataflowJob.setOutput(null);
    }
    
    @Test
    public void assertSimpleJob() throws Exception {
        new LocalTaskExecutor(new LocalCloudJobConfiguration(new SimpleJobConfiguration(JobCoreConfiguration
                .newBuilder(TestSimpleJob.class.getSimpleName(), "*/2 * * * * ?", 3).build(), TestSimpleJob.class.getName()), TRANSIENT, 1)).execute();
        assertThat(TestSimpleJob.getShardingContext().getJobName(), is(TestSimpleJob.class.getSimpleName()));
        assertThat(TestSimpleJob.getShardingContext().getShardingItem(), is(1));
        assertThat(TestSimpleJob.getShardingContext().getShardingTotalCount(), is(3));
        assertNull(TestSimpleJob.getShardingContext().getShardingParameter());
        assertThat(TestSimpleJob.getShardingContext().getJobParameter(), is(""));
    }
    
    @Test(expected = JobSystemException.class)
    public void assertNotExistsJobClass() throws Exception {
        new LocalTaskExecutor(new LocalCloudJobConfiguration(new SimpleJobConfiguration(JobCoreConfiguration
                .newBuilder("not exist", "*/2 * * * * ?", 3).build(), "not exist"), TRANSIENT, 1)).execute();
    }
    
    @Test
    public void assertSpringSimpleJob() throws Exception {
        new LocalTaskExecutor(new LocalCloudJobConfiguration(new SimpleJobConfiguration(JobCoreConfiguration
                .newBuilder(TestSimpleJob.class.getSimpleName(), "*/2 * * * * ?", 3)
                .shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou").jobParameter("dbName=dangdang").build(), TestSimpleJob.class
                .getName()), DAEMON, 1, "testSimpleJob", "applicationContext.xml")).execute();
        assertThat(TestSimpleJob.getShardingContext().getJobName(), is(TestSimpleJob.class.getSimpleName()));
        assertThat(TestSimpleJob.getShardingContext().getShardingTotalCount(), is(3));
        assertThat(TestSimpleJob.getShardingContext().getJobParameter(), is("dbName=dangdang"));
        assertThat(TestSimpleJob.getShardingParameters().size(), is(1));
        assertThat(TestSimpleJob.getShardingParameters().iterator().next(), is("Shanghai"));
    }
    
    @Test
    public void assertDataflow() throws Exception {
        TestDataflowJob.setInput(Arrays.asList("1", "2", "3"));
        new LocalTaskExecutor(new LocalCloudJobConfiguration(new DataflowJobConfiguration(JobCoreConfiguration
                .newBuilder(TestDataflowJob.class.getSimpleName(), "*/2 * * * * ?", 10).build(), TestDataflowJob.class.getName(), false), TRANSIENT, 5)).execute();
        assertFalse(TestDataflowJob.getOutput().isEmpty());
        for (String each : TestDataflowJob.getOutput()) {
            assertTrue(each.endsWith("-d"));
        }
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertScriptEmpty() throws Exception {
        new LocalTaskExecutor(new LocalCloudJobConfiguration(new ScriptJobConfiguration(JobCoreConfiguration
                .newBuilder(TestDataflowJob.class.getSimpleName(), "*/2 * * * * ?", 10).build(), ""), TRANSIENT, 5)).execute();
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertScriptNotExists() throws Exception {
        new LocalTaskExecutor(new LocalCloudJobConfiguration(new ScriptJobConfiguration(JobCoreConfiguration
                .newBuilder(TestDataflowJob.class.getSimpleName(), "*/2 * * * * ?", 10).build(), "not_exists_file param1"), TRANSIENT, 5)).execute();
    }
}
