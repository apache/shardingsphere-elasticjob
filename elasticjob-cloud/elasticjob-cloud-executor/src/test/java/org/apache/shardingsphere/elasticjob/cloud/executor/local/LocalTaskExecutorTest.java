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

import org.apache.shardingsphere.elasticjob.cloud.config.script.ScriptJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.executor.local.fixture.TestDataflowJob;
import org.apache.shardingsphere.elasticjob.cloud.executor.local.fixture.TestSimpleJob;
import org.apache.shardingsphere.elasticjob.cloud.config.JobCoreConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.dataflow.DataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.simple.SimpleJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.exception.JobSystemException;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;

public final class LocalTaskExecutorTest {
    
    @Before
    public void setUp() {
        TestSimpleJob.setShardingContext(null);
        TestDataflowJob.setInput(null);
        TestDataflowJob.setOutput(null);
    }
    
    @Test
    public void assertSimpleJob() {
        new LocalTaskExecutor(new LocalCloudJobConfiguration(new SimpleJobConfiguration(JobCoreConfiguration
                .newBuilder(TestSimpleJob.class.getSimpleName(), "*/2 * * * * ?", 3).build(), TestSimpleJob.class.getName()), 1)).execute();
        Assert.assertThat(TestSimpleJob.getShardingContext().getJobName(), Is.is(TestSimpleJob.class.getSimpleName()));
        Assert.assertThat(TestSimpleJob.getShardingContext().getShardingItem(), Is.is(1));
        Assert.assertThat(TestSimpleJob.getShardingContext().getShardingTotalCount(), Is.is(3));
        Assert.assertThat(TestSimpleJob.getShardingContext().getShardingItem(), Is.is(1));
        Assert.assertNull(TestSimpleJob.getShardingContext().getShardingParameter());
        Assert.assertThat(TestSimpleJob.getShardingContext().getJobParameter(), Is.is(""));
    }
    
    @Test
    public void assertSpringSimpleJob() {
        new LocalTaskExecutor(new LocalCloudJobConfiguration(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(
                TestSimpleJob.class.getSimpleName(), "*/2 * * * * ?", 3).shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou").jobParameter("dbName=dangdang").build(), 
                TestSimpleJob.class.getName()), 1, "testSimpleJob", "applicationContext.xml")).execute();
        Assert.assertThat(TestSimpleJob.getShardingContext().getJobName(), Is.is(TestSimpleJob.class.getSimpleName()));
        Assert.assertThat(TestSimpleJob.getShardingContext().getShardingTotalCount(), Is.is(3));
        Assert.assertThat(TestSimpleJob.getShardingContext().getJobParameter(), Is.is("dbName=dangdang"));
        Assert.assertThat(TestSimpleJob.getShardingContext().getShardingItem(), Is.is(1));
        Assert.assertThat(TestSimpleJob.getShardingParameters().size(), Is.is(1));
        Assert.assertThat(TestSimpleJob.getShardingParameters().iterator().next(), Is.is("Shanghai"));
    }
    
    @Test
    public void assertDataflowJob() {
        TestDataflowJob.setInput(Arrays.asList("1", "2", "3"));
        new LocalTaskExecutor(new LocalCloudJobConfiguration(new DataflowJobConfiguration(JobCoreConfiguration
                .newBuilder(TestDataflowJob.class.getSimpleName(), "*/2 * * * * ?", 10).build(), TestDataflowJob.class.getName(), false), 5)).execute();
        Assert.assertFalse(TestDataflowJob.getOutput().isEmpty());
        for (String each : TestDataflowJob.getOutput()) {
            Assert.assertTrue(each.endsWith("-d"));
        }
    }
    
    @Test
    public void assertScriptJob() throws IOException {
        new LocalTaskExecutor(new LocalCloudJobConfiguration(
                new ScriptJobConfiguration(JobCoreConfiguration.newBuilder("TestScriptJob", "*/2 * * * * ?", 3).build(), buildScriptCommandLine()), 1)).execute();
    }
    
    private static String buildScriptCommandLine() throws IOException {
        if (System.getProperties().getProperty("os.name").contains("Windows")) {
            return Paths.get(LocalTaskExecutorTest.class.getResource("/script/TestScriptJob.bat").getPath().substring(1)).toString();
        }
        Path result = Paths.get(LocalTaskExecutorTest.class.getResource("/script/TestScriptJob.sh").getPath());
        Files.setPosixFilePermissions(result, PosixFilePermissions.fromString("rwxr-xr-x"));
        return result.toString();
    }
    
    @Test(expected = JobSystemException.class)
    public void assertNotExistsJobClass() {
        new LocalTaskExecutor(new LocalCloudJobConfiguration(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("not exist", "*/2 * * * * ?", 3).build(), "not exist"), 1)).execute();
    }
}
