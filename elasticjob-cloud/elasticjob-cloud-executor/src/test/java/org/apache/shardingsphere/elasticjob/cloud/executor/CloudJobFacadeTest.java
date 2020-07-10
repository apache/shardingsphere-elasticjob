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

package org.apache.shardingsphere.elasticjob.cloud.executor;

import org.apache.shardingsphere.elasticjob.cloud.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.cloud.api.JobType;
import org.apache.shardingsphere.elasticjob.cloud.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.exception.JobExecutionEnvironmentException;
import org.apache.shardingsphere.elasticjob.tracing.JobEventBus;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent.State;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CloudJobFacadeTest {
    
    private ShardingContexts shardingContexts;
    
    private JobConfigurationContext jobConfig;
    
    @Mock
    private JobEventBus eventBus;
    
    private JobFacade jobFacade;
    
    @Before
    public void setUp() {
        shardingContexts = getShardingContexts();
        jobConfig = new JobConfigurationContext(getJobConfigurationMap(JobType.SIMPLE, false));
        jobFacade = new CloudJobFacade(shardingContexts, jobConfig, eventBus);
    }
    
    private ShardingContexts getShardingContexts() {
        Map<Integer, String> shardingItemParameters = new HashMap<>(1, 1);
        shardingItemParameters.put(0, "A");
        return new ShardingContexts("fake_task_id", "test_job", 3, "", shardingItemParameters);
    }
    
    private Map<String, String> getJobConfigurationMap(final JobType jobType, final boolean streamingProcess) {
        Map<String, String> result = new HashMap<>(10, 1);
        result.put("jobName", "test_job");
        result.put("jobClass", ElasticJob.class.getCanonicalName());
        result.put("jobType", jobType.name());
        result.put("streamingProcess", Boolean.toString(streamingProcess));
        return result;
    }
    
    @Test
    public void assertLoadJobRootConfiguration() {
        assertThat(jobFacade.loadJobRootConfiguration(true), is(jobConfig));
    }
    
    @Test
    public void assertCheckJobExecutionEnvironment() throws JobExecutionEnvironmentException {
        jobFacade.checkJobExecutionEnvironment();
    }
    
    @Test
    public void assertFailoverIfNecessary() {
        jobFacade.failoverIfNecessary();
    }
    
    @Test
    public void assertRegisterJobBegin() {
        jobFacade.registerJobBegin(null);
    }
    
    @Test
    public void assertRegisterJobCompleted() {
        jobFacade.registerJobCompleted(null);
    }
    
    @Test
    public void assertGetShardingContext() {
        assertThat(jobFacade.getShardingContexts(), is(shardingContexts));
    }
    
    @Test
    public void assertMisfireIfNecessary() {
        jobFacade.misfireIfRunning(null);
    }
    
    @Test
    public void assertClearMisfire() {
        jobFacade.clearMisfire(null);
    }
    
    @Test
    public void assertIsExecuteMisfired() {
        assertFalse(jobFacade.isExecuteMisfired(null));
    }
    
    @Test
    public void assertIsEligibleForJobRunningWhenIsNotDataflowJob() {
        assertFalse(jobFacade.isEligibleForJobRunning());
    }
    
    @Test
    public void assertIsEligibleForJobRunningWhenIsDataflowJobAndIsNotStreamingProcess() {
        assertFalse(new CloudJobFacade(shardingContexts, new JobConfigurationContext(getJobConfigurationMap(JobType.DATAFLOW, false)), new JobEventBus()).isEligibleForJobRunning());
    }
    
    @Test
    public void assertIsEligibleForJobRunningWhenIsDataflowJobAndIsStreamingProcess() {
        assertTrue(new CloudJobFacade(shardingContexts, new JobConfigurationContext(getJobConfigurationMap(JobType.DATAFLOW, true)), new JobEventBus()).isEligibleForJobRunning());
    }
    
    @Test
    public void assertIsNeedSharding() {
        assertFalse(jobFacade.isNeedSharding());
    }
    
    @Test
    public void assertBeforeJobExecuted() {
        jobFacade.beforeJobExecuted(null);
    }
    
    @Test
    public void assertAfterJobExecuted() {
        jobFacade.afterJobExecuted(null);
    }
    
    @Test
    public void assertPostJobExecutionEvent() {
        JobExecutionEvent jobExecutionEvent = new JobExecutionEvent("localhost", "127.0.0.1", "fake_task_id", "test_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        jobFacade.postJobExecutionEvent(jobExecutionEvent);
        verify(eventBus).post(jobExecutionEvent);
    }
    
    @Test
    public void assertPostJobStatusTraceEvent() {
        jobFacade.postJobStatusTraceEvent(String.format("%s@-@0@-@%s@-@fake_slave_id@-@0", "test_job", ExecutionType.READY), State.TASK_RUNNING, "message is empty.");
    }
}
