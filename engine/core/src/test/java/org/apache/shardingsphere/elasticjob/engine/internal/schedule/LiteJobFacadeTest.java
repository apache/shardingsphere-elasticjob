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

package org.apache.shardingsphere.elasticjob.engine.internal.schedule;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.exception.JobExecutionEnvironmentException;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.engine.api.listener.fixture.ElasticJobListenerCaller;
import org.apache.shardingsphere.elasticjob.engine.api.listener.fixture.TestElasticJobListener;
import org.apache.shardingsphere.elasticjob.engine.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.engine.internal.failover.FailoverService;
import org.apache.shardingsphere.elasticjob.engine.internal.sharding.ExecutionContextService;
import org.apache.shardingsphere.elasticjob.engine.internal.sharding.ExecutionService;
import org.apache.shardingsphere.elasticjob.engine.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.engine.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.tracing.JobTracingEventBus;
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
class LiteJobFacadeTest {
    
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
    
    private LiteJobFacade liteJobFacade;
    
    private StringBuilder orderResult;
    
    @BeforeEach
    void setUp() {
        orderResult = new StringBuilder();
        TestElasticJobListener l1 = new TestElasticJobListener(caller, "l1", 2, orderResult);
        TestElasticJobListener l2 = new TestElasticJobListener(caller, "l2", 1, orderResult);
        liteJobFacade = new LiteJobFacade(null, "test_job", Lists.newArrayList(l1, l2), null);
        ReflectionUtils.setFieldValue(liteJobFacade, "configService", configService);
        ReflectionUtils.setFieldValue(liteJobFacade, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(liteJobFacade, "executionContextService", executionContextService);
        ReflectionUtils.setFieldValue(liteJobFacade, "executionService", executionService);
        ReflectionUtils.setFieldValue(liteJobFacade, "failoverService", failoverService);
        ReflectionUtils.setFieldValue(liteJobFacade, "jobTracingEventBus", jobTracingEventBus);
    }
    
    @Test
    void assertLoad() {
        JobConfiguration expected = JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build();
        when(configService.load(true)).thenReturn(expected);
        assertThat(liteJobFacade.loadJobConfiguration(true), is(expected));
    }
    
    @Test
    void assertCheckMaxTimeDiffSecondsTolerable() throws JobExecutionEnvironmentException {
        liteJobFacade.checkJobExecutionEnvironment();
        verify(configService).checkMaxTimeDiffSecondsTolerable();
    }
    
    @Test
    void assertFailoverIfUnnecessary() {
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(false).build());
        liteJobFacade.failoverIfNecessary();
        verify(failoverService, times(0)).failoverIfNecessary();
    }
    
    @Test
    void assertFailoverIfNecessary() {
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(true).monitorExecution(true).build());
        liteJobFacade.failoverIfNecessary();
        verify(failoverService).failoverIfNecessary();
    }
    
    @Test
    void assertRegisterJobBegin() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.emptyMap());
        liteJobFacade.registerJobBegin(shardingContexts);
        verify(executionService).registerJobBegin(shardingContexts);
    }
    
    @Test
    void assertRegisterJobCompletedWhenFailoverDisabled() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(false).build());
        liteJobFacade.registerJobCompleted(shardingContexts);
        verify(executionService).registerJobCompleted(shardingContexts);
        verify(failoverService, times(0)).updateFailoverComplete(shardingContexts.getShardingItemParameters().keySet());
    }
    
    @Test
    void assertRegisterJobCompletedWhenFailoverEnabled() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(true).monitorExecution(true).build());
        liteJobFacade.registerJobCompleted(shardingContexts);
        verify(executionService).registerJobCompleted(shardingContexts);
        verify(failoverService).updateFailoverComplete(shardingContexts.getShardingItemParameters().keySet());
    }
    
    @Test
    void assertGetShardingContextWhenIsFailoverEnableAndFailover() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(true).monitorExecution(true).build());
        when(failoverService.getLocalFailoverItems()).thenReturn(Collections.singletonList(1));
        when(executionContextService.getJobShardingContext(Collections.singletonList(1))).thenReturn(shardingContexts);
        assertThat(liteJobFacade.getShardingContexts(), is(shardingContexts));
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
        assertThat(liteJobFacade.getShardingContexts(), is(shardingContexts));
        verify(shardingService).shardingIfNecessary();
    }
    
    @Test
    void assertGetShardingContextWhenIsFailoverDisable() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(false).build());
        when(shardingService.getLocalShardingItems()).thenReturn(Arrays.asList(0, 1));
        when(executionContextService.getJobShardingContext(Arrays.asList(0, 1))).thenReturn(shardingContexts);
        assertThat(liteJobFacade.getShardingContexts(), is(shardingContexts));
        verify(shardingService).shardingIfNecessary();
    }
    
    @Test
    void assertGetShardingContextWhenHasDisabledItems() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(false).build());
        when(shardingService.getLocalShardingItems()).thenReturn(Lists.newArrayList(0, 1));
        when(executionService.getDisabledItems(Arrays.asList(0, 1))).thenReturn(Collections.singletonList(1));
        when(executionContextService.getJobShardingContext(Collections.singletonList(0))).thenReturn(shardingContexts);
        assertThat(liteJobFacade.getShardingContexts(), is(shardingContexts));
        verify(shardingService).shardingIfNecessary();
    }
    
    @Test
    void assertMisfireIfRunning() {
        when(executionService.misfireIfHasRunningItems(Arrays.asList(0, 1))).thenReturn(true);
        assertThat(liteJobFacade.misfireIfRunning(Arrays.asList(0, 1)), is(true));
    }
    
    @Test
    void assertClearMisfire() {
        liteJobFacade.clearMisfire(Arrays.asList(0, 1));
        verify(executionService).clearMisfire(Arrays.asList(0, 1));
    }
    
    @Test
    void assertIsNeedSharding() {
        when(shardingService.isNeedSharding()).thenReturn(true);
        assertThat(liteJobFacade.isNeedSharding(), is(true));
    }
    
    @Test
    void assertBeforeJobExecuted() {
        liteJobFacade.beforeJobExecuted(new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.emptyMap()));
        verify(caller, times(2)).before();
        assertThat(orderResult.toString(), is("l2l1"));
    }
    
    @Test
    void assertAfterJobExecuted() {
        liteJobFacade.afterJobExecuted(new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.emptyMap()));
        verify(caller, times(2)).after();
        assertThat(orderResult.toString(), is("l2l1"));
    }
    
    @Test
    void assertPostJobExecutionEvent() {
        liteJobFacade.postJobExecutionEvent(null);
        verify(jobTracingEventBus).post(null);
    }
}
