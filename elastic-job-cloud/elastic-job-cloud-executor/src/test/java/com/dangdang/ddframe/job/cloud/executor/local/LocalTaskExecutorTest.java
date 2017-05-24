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

import com.dangdang.ddframe.job.api.script.ScriptJob;
import com.dangdang.ddframe.job.cloud.executor.local.fixture.TestDataflowJob;
import com.dangdang.ddframe.job.cloud.executor.local.fixture.TestSimpleJob;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.Executors;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
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
        LocalTaskExecutor.builder().jobClass(TestSimpleJob.class).shardingItemExpression("0").build().runTransient();
        assertThat(TestSimpleJob.getShardingContext().getJobName(), is(TestSimpleJob.class.getSimpleName()));
        assertThat(TestSimpleJob.getShardingContext().getShardingItem(), is(0));
        assertThat(TestSimpleJob.getShardingContext().getShardingTotalCount(), is(1));
        assertThat(TestSimpleJob.getShardingContext().getShardingParameter(), is(""));
        assertThat(TestSimpleJob.getShardingContext().getJobParameter(), is(""));
    }
    
    @Test
    public void assertDaemonSimpleJob() throws Exception {
        final LocalTaskExecutor localTaskExecutor = LocalTaskExecutor.builder().jobClass(TestSimpleJob.class).shardingItemExpression("2=Beijing").shardingTotalCount(10).jobParameter("dbName=dangdang")
                .applicationContext("applicationContext.xml").beanName("testSimpleJob").build();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(4000);
                } catch (final InterruptedException ignored) {
                }
                localTaskExecutor.stopDaemon();
            }
        });
        localTaskExecutor.runDaemon("*/2 * * * * ?");
        assertThat(TestSimpleJob.getShardingContext().getJobName(), is(TestSimpleJob.class.getSimpleName()));
        assertThat(TestSimpleJob.getShardingContext().getShardingItem(), is(2));
        assertThat(TestSimpleJob.getShardingContext().getShardingTotalCount(), is(10));
        assertThat(TestSimpleJob.getShardingContext().getShardingParameter(), is("Beijing"));
        assertThat(TestSimpleJob.getShardingContext().getJobParameter(), is("dbName=dangdang"));
    }
    
    @Test
    public void assertDataflow() throws Exception {
        TestDataflowJob.setInput(Arrays.asList("1", "2", "3"));
        LocalTaskExecutor.builder().jobClass(TestDataflowJob.class).shardingItemExpression("0").build().runTransient();
        assertFalse(TestDataflowJob.getOutput().isEmpty());
        for (String each : TestDataflowJob.getOutput()) {
            assertTrue(each.endsWith("-d"));
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertUnsupportedScriptJob() throws Exception {
        LocalTaskExecutor.builder().jobClass(ScriptJob.class).shardingItemExpression("0").build().runTransient();
    }
    
    @Test(expected = NullPointerException.class)
    public void assertLackJobClass() throws Exception {
        LocalTaskExecutor.builder().build().runTransient();
    }
    
    @Test(expected = NullPointerException.class)
    public void assertLackShardingItemExpression() throws Exception {
        LocalTaskExecutor.builder().jobClass(TestSimpleJob.class).build().runTransient();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertLackApplicationContext() throws Exception {
        LocalTaskExecutor.builder().jobClass(ScriptJob.class).shardingItemExpression("0").beanName("test").build().runTransient();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertLackBeanName() throws Exception {
        LocalTaskExecutor.builder().jobClass(ScriptJob.class).shardingItemExpression("0").applicationContext("applicationContext.xml").build().runTransient();
    }
}
