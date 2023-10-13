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

package org.apache.shardingsphere.elasticjob.cloud.executor.facade;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.facade.CloudJobFacade;
import org.apache.shardingsphere.elasticjob.dataflow.props.DataflowJobProperties;
import org.apache.shardingsphere.elasticjob.executor.JobFacade;
import org.apache.shardingsphere.elasticjob.infra.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.infra.exception.JobExecutionEnvironmentException;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.tracing.JobTracingEventBus;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CloudJobFacadeTest {
    
    private ShardingContexts shardingContexts;
    
    @Mock
    private JobTracingEventBus jobTracingEventBus;
    
    private JobFacade jobFacade;
    
    @BeforeEach
    void setUp() {
        shardingContexts = getShardingContexts();
        jobFacade = new CloudJobFacade(shardingContexts, getJobConfiguration(), jobTracingEventBus);
    }
    
    private ShardingContexts getShardingContexts() {
        Map<Integer, String> shardingItemParameters = new HashMap<>(1, 1);
        shardingItemParameters.put(0, "A");
        return new ShardingContexts("fake_task_id", "test_job", 3, "", shardingItemParameters);
    }
    
    private JobConfiguration getJobConfiguration() {
        return JobConfiguration.newBuilder("test_job", 1).setProperty(DataflowJobProperties.STREAM_PROCESS_KEY, Boolean.FALSE.toString()).build();
    }
    
    @Test
    void assertCheckJobExecutionEnvironment() throws JobExecutionEnvironmentException {
        jobFacade.checkJobExecutionEnvironment();
    }
    
    @Test
    void assertFailoverIfNecessary() {
        jobFacade.failoverIfNecessary();
    }
    
    @Test
    void assertRegisterJobBegin() {
        jobFacade.registerJobBegin(null);
    }
    
    @Test
    void assertRegisterJobCompleted() {
        jobFacade.registerJobCompleted(null);
    }
    
    @Test
    void assertGetShardingContext() {
        assertThat(jobFacade.getShardingContexts(), is(shardingContexts));
    }
    
    @Test
    void assertMisfireIfNecessary() {
        jobFacade.misfireIfRunning(null);
    }
    
    @Test
    void assertClearMisfire() {
        jobFacade.clearMisfire(null);
    }
    
    @Test
    void assertIsExecuteMisfired() {
        assertFalse(jobFacade.isExecuteMisfired(null));
    }
    
    @Test
    void assertIsNeedSharding() {
        assertFalse(jobFacade.isNeedSharding());
    }
    
    @Test
    void assertBeforeJobExecuted() {
        jobFacade.beforeJobExecuted(null);
    }
    
    @Test
    void assertAfterJobExecuted() {
        jobFacade.afterJobExecuted(null);
    }
    
    @Test
    void assertPostJobExecutionEvent() {
        JobExecutionEvent jobExecutionEvent = new JobExecutionEvent("localhost", "127.0.0.1", "fake_task_id", "test_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        jobFacade.postJobExecutionEvent(jobExecutionEvent);
        verify(jobTracingEventBus).post(jobExecutionEvent);
    }
    
    @Test
    void assertPostJobStatusTraceEvent() {
        jobFacade.postJobStatusTraceEvent(String.format("%s@-@0@-@%s@-@fake_slave_id@-@0", "test_job", ExecutionType.READY), State.TASK_RUNNING, "message is empty.");
    }
}
