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
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
    public void assertTransientSimpleJob() throws Exception {
        LocalCloudJobConfiguration configuration = new LocalCloudJobConfiguration(new SimpleJobConfiguration(JobCoreConfiguration
                .newBuilder(TestSimpleJob.class.getSimpleName(), "*/2 * * * * ?", 1).build(), TestSimpleJob.class.getName()), TRANSIENT);
        Future<Integer> future = new LocalTaskExecutor(configuration).run();
        assertFalse(future.isCancelled());
        assertThat(future.get(), is(1));
        assertTrue(future.isDone());
        assertThat(TestSimpleJob.getShardingContext().getJobName(), is(TestSimpleJob.class.getSimpleName()));
        assertThat(TestSimpleJob.getShardingContext().getShardingItem(), is(0));
        assertThat(TestSimpleJob.getShardingContext().getShardingTotalCount(), is(1));
        assertNull(TestSimpleJob.getShardingContext().getShardingParameter());
        assertThat(TestSimpleJob.getShardingContext().getJobParameter(), is(""));
    }
    
    @Test
    public void assertDaemonSimpleJob() throws Exception {
        LocalCloudJobConfiguration configuration = new LocalCloudJobConfiguration(new SimpleJobConfiguration(JobCoreConfiguration
                .newBuilder(TestSimpleJob.class.getSimpleName(), "*/2 * * * * ?", 3)
                .shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou").jobParameter("dbName=dangdang").build(), TestSimpleJob.class.getName()), DAEMON, "testSimpleJob", "applicationContext.xml");
        Future<Integer> future = new LocalTaskExecutor(configuration).run();
        assertTrue(future.isCancelled());
        assertThat(future.get(4, TimeUnit.SECONDS), is(0));
        assertFalse(future.isDone());
        assertTrue(future.cancel(true));
        assertTrue(future.isDone());
        assertThat(TestSimpleJob.getShardingContext().getJobName(), is(TestSimpleJob.class.getSimpleName()));
        assertThat(TestSimpleJob.getShardingContext().getShardingTotalCount(), is(3));
        assertThat(TestSimpleJob.getShardingContext().getJobParameter(), is("dbName=dangdang"));
        assertThat(TestSimpleJob.getShardingParameters().size(), is(3));
    }
    
    @Test
    public void assertDataflow() throws Exception {
        TestDataflowJob.setInput(Arrays.asList("1", "2", "3"));
        LocalCloudJobConfiguration configuration = new LocalCloudJobConfiguration(new DataflowJobConfiguration(JobCoreConfiguration
                .newBuilder(TestDataflowJob.class.getSimpleName(), "*/2 * * * * ?", 1).build(), TestDataflowJob.class.getName(), false), TRANSIENT);
        Future<Integer> future = new LocalTaskExecutor(configuration).run();
        assertThat(future.get(), is(1));
        assertFalse(TestDataflowJob.getOutput().isEmpty());
        for (String each : TestDataflowJob.getOutput()) {
            assertTrue(each.endsWith("-d"));
        }
    }
}
