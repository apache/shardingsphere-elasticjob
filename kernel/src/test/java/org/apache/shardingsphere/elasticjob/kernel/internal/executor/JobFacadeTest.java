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

package org.apache.shardingsphere.elasticjob.kernel.internal.executor;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.infra.exception.JobExecutionEnvironmentException;
import org.apache.shardingsphere.elasticjob.kernel.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.kernel.api.listener.fixture.ElasticJobListenerCaller;
import org.apache.shardingsphere.elasticjob.kernel.api.listener.fixture.TestElasticJobListener;
import org.apache.shardingsphere.elasticjob.kernel.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.kernel.internal.failover.FailoverService;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.ExecutionContextService;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.ExecutionService;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.kernel.internal.tracing.JobTracingEventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobFacadeTest {
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private ShardingService shardingService;
    
    @Mock
    private ExecutionContextService executionContextService;
    
    @Mock
    private ExecutionService executionService;
    
    @Mock
    private FailoverService failoverService;
    
    @Mock
    private JobTracingEventBus jobTracingEventBus;
    
    @Mock
    private ElasticJobListenerCaller caller;
    
    private JobFacade jobFacade;
    
    private StringBuilder orderResult;
    
    @BeforeEach
    void setUp() {
        orderResult = new StringBuilder();
        jobFacade = new JobFacade(null, "test_job",
                Arrays.asList(new TestElasticJobListener(caller, "l1", 2, orderResult), new TestElasticJobListener(caller, "l2", 1, orderResult)), null);
        ReflectionUtils.setFieldValue(jobFacade, "configService", configService);
        ReflectionUtils.setFieldValue(jobFacade, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(jobFacade, "executionContextService", executionContextService);
        ReflectionUtils.setFieldValue(jobFacade, "executionService", executionService);
        ReflectionUtils.setFieldValue(jobFacade, "failoverService", failoverService);
        ReflectionUtils.setFieldValue(jobFacade, "jobTracingEventBus", jobTracingEventBus);
    }
    
    @Test
    void assertLoad() {
        JobConfiguration expected = JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build();
        when(configService.load(true)).thenReturn(expected);
        assertThat(jobFacade.loadJobConfiguration(true), is(expected));
    }
    
    @Test
    void assertCheckMaxTimeDiffSecondsTolerable() throws JobExecutionEnvironmentException {
        jobFacade.checkJobExecutionEnvironment();
        verify(configService).checkMaxTimeDiffSecondsTolerable();
    }
    
    @Test
    void assertFailoverIfUnnecessary() {
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(false).build());
        jobFacade.failoverIfNecessary();
        verify(failoverService, times(0)).failoverIfNecessary();
    }
    
    @Test
    void assertFailoverIfNecessary() {
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(true).monitorExecution(true).build());
        jobFacade.failoverIfNecessary();
        verify(failoverService).failoverIfNecessary();
    }
    
    @Test
    void assertRegisterJobBegin() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.emptyMap());
        jobFacade.registerJobBegin(shardingContexts);
        verify(executionService).registerJobBegin(shardingContexts);
    }
    
    @Test
    void assertRegisterJobCompletedWhenFailoverDisabled() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(false).build());
        jobFacade.registerJobCompleted(shardingContexts);
        verify(executionService).registerJobCompleted(shardingContexts);
        verify(failoverService, times(0)).updateFailoverComplete(shardingContexts.getShardingItemParameters().keySet());
    }
    
    @Test
    void assertRegisterJobCompletedWhenFailoverEnabled() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(true).monitorExecution(true).build());
        jobFacade.registerJobCompleted(shardingContexts);
        verify(executionService).registerJobCompleted(shardingContexts);
        verify(failoverService).updateFailoverComplete(shardingContexts.getShardingItemParameters().keySet());
    }
    
    @Test
    void assertGetShardingContextWhenIsFailoverEnableAndFailover() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(true).monitorExecution(true).build());
        when(failoverService.getLocalFailoverItems()).thenReturn(Collections.singletonList(1));
        when(executionContextService.getJobShardingContext(Collections.singletonList(1))).thenReturn(shardingContexts);
        assertThat(jobFacade.getShardingContexts(), is(shardingContexts));
        verify(shardingService, times(0)).shardingIfNecessary();
    }
    
    @Test
    void assertGetShardingContextWhenIsFailoverEnableAndNotFailover() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(true).monitorExecution(true).build());
        when(failoverService.getLocalFailoverItems()).thenReturn(Collections.emptyList());
        when(shardingService.getLocalShardingItems()).thenReturn(Lists.newArrayList(0, 1));
        when(failoverService.getLocalTakeOffItems()).thenReturn(Collections.singletonList(0));
        when(executionContextService.getJobShardingContext(Collections.singletonList(1))).thenReturn(shardingContexts);
        assertThat(jobFacade.getShardingContexts(), is(shardingContexts));
        verify(shardingService).shardingIfNecessary();
    }
    
    @Test
    void assertGetShardingContextWhenIsFailoverDisable() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(false).build());
        when(shardingService.getLocalShardingItems()).thenReturn(Arrays.asList(0, 1));
        when(executionContextService.getJobShardingContext(Arrays.asList(0, 1))).thenReturn(shardingContexts);
        assertThat(jobFacade.getShardingContexts(), is(shardingContexts));
        verify(shardingService).shardingIfNecessary();
    }
    
    @Test
    void assertGetShardingContextWhenHasDisabledItems() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(false).build());
        when(shardingService.getLocalShardingItems()).thenReturn(Lists.newArrayList(0, 1));
        when(executionService.getDisabledItems(Arrays.asList(0, 1))).thenReturn(Collections.singletonList(1));
        when(executionContextService.getJobShardingContext(Collections.singletonList(0))).thenReturn(shardingContexts);
        assertThat(jobFacade.getShardingContexts(), is(shardingContexts));
        verify(shardingService).shardingIfNecessary();
    }
    
    @Test
    void assertMisfireIfRunning() {
        when(executionService.misfireIfHasRunningItems(Arrays.asList(0, 1))).thenReturn(true);
        assertThat(jobFacade.misfireIfRunning(Arrays.asList(0, 1)), is(true));
    }
    
    @Test
    void assertClearMisfire() {
        jobFacade.clearMisfire(Arrays.asList(0, 1));
        verify(executionService).clearMisfire(Arrays.asList(0, 1));
    }
    
    @Test
    void assertIsNeedSharding() {
        when(shardingService.isNeedSharding()).thenReturn(true);
        assertThat(jobFacade.isNeedSharding(), is(true));
    }
    
    @Test
    void assertBeforeJobExecuted() {
        jobFacade.beforeJobExecuted(new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.emptyMap()));
        verify(caller, times(2)).before();
        assertThat(orderResult.toString(), is("l2l1"));
    }
    
    @Test
    void assertAfterJobExecuted() {
        jobFacade.afterJobExecuted(new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.emptyMap()));
        verify(caller, times(2)).after();
        assertThat(orderResult.toString(), is("l2l1"));
    }
    
    @Test
    void assertPostJobExecutionEvent() {
        jobFacade.postJobExecutionEvent(null);
        verify(jobTracingEventBus).post(null);
    }
}
