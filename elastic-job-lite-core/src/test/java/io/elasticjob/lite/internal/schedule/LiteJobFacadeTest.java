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

package io.elasticjob.lite.internal.schedule;

import com.google.common.collect.Lists;
import io.elasticjob.lite.api.listener.ElasticJobListener;
import io.elasticjob.lite.api.listener.fixture.ElasticJobListenerCaller;
import io.elasticjob.lite.api.listener.fixture.TestElasticJobListener;
import io.elasticjob.lite.config.JobCoreConfiguration;
import io.elasticjob.lite.config.LiteJobConfiguration;
import io.elasticjob.lite.config.dataflow.DataflowJobConfiguration;
import io.elasticjob.lite.config.simple.SimpleJobConfiguration;
import io.elasticjob.lite.event.JobEventBus;
import io.elasticjob.lite.exception.JobExecutionEnvironmentException;
import io.elasticjob.lite.executor.ShardingContexts;
import io.elasticjob.lite.fixture.TestDataflowJob;
import io.elasticjob.lite.fixture.TestSimpleJob;
import io.elasticjob.lite.internal.config.ConfigurationService;
import io.elasticjob.lite.internal.failover.FailoverService;
import io.elasticjob.lite.internal.sharding.ExecutionContextService;
import io.elasticjob.lite.internal.sharding.ExecutionService;
import io.elasticjob.lite.internal.sharding.ShardingService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LiteJobFacadeTest {
    
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
    private JobEventBus eventBus;
    
    @Mock
    private ElasticJobListenerCaller caller;
    
    private LiteJobFacade liteJobFacade;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        liteJobFacade = new LiteJobFacade(null, "test_job", Collections.<ElasticJobListener>singletonList(new TestElasticJobListener(caller)), eventBus);
        ReflectionUtils.setFieldValue(liteJobFacade, "configService", configService);
        ReflectionUtils.setFieldValue(liteJobFacade, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(liteJobFacade, "executionContextService", executionContextService);
        ReflectionUtils.setFieldValue(liteJobFacade, "executionService", executionService);
        ReflectionUtils.setFieldValue(liteJobFacade, "failoverService", failoverService);
    }
    
    @Test
    public void assertLoad() {
        LiteJobConfiguration expected = LiteJobConfiguration.newBuilder(null).build();
        when(configService.load(true)).thenReturn(expected);
        assertThat(liteJobFacade.loadJobRootConfiguration(true), is(expected));
    }
    
    @Test
    public void assertCheckMaxTimeDiffSecondsTolerable() throws JobExecutionEnvironmentException {
        liteJobFacade.checkJobExecutionEnvironment();
        verify(configService).checkMaxTimeDiffSecondsTolerable();
    }
    
    @Test
    public void assertFailoverIfUnnecessary() {
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).failover(false).build(), TestSimpleJob.class.getCanonicalName())).build());
        liteJobFacade.failoverIfNecessary();
        verify(failoverService, times(0)).failoverIfNecessary();
    }
    
    @Test
    public void assertFailoverIfNecessary() {
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).failover(true).build(), 
                TestSimpleJob.class.getCanonicalName())).monitorExecution(true).build());
        liteJobFacade.failoverIfNecessary();
        verify(failoverService).failoverIfNecessary();
    }
    
    @Test
    public void assertRegisterJobBegin() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.<Integer, String>emptyMap());
        liteJobFacade.registerJobBegin(shardingContexts);
        verify(executionService).registerJobBegin(shardingContexts);
    }
    
    @Test
    public void assertRegisterJobCompletedWhenFailoverDisabled() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.<Integer, String>emptyMap());
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).failover(false).build(), TestSimpleJob.class.getCanonicalName())).build());
        liteJobFacade.registerJobCompleted(shardingContexts);
        verify(executionService).registerJobCompleted(shardingContexts);
        verify(failoverService, times(0)).updateFailoverComplete(shardingContexts.getShardingItemParameters().keySet());
    }
    
    @Test
    public void assertRegisterJobCompletedWhenFailoverEnabled() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.<Integer, String>emptyMap());
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).failover(true).build(), 
                TestSimpleJob.class.getCanonicalName())).monitorExecution(true).build());
        liteJobFacade.registerJobCompleted(shardingContexts);
        verify(executionService).registerJobCompleted(shardingContexts);
        verify(failoverService).updateFailoverComplete(shardingContexts.getShardingItemParameters().keySet());
    }
    
    @Test
    public void assertGetShardingContextWhenIsFailoverEnableAndFailover() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.<Integer, String>emptyMap());
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).failover(true).build(), 
                TestSimpleJob.class.getCanonicalName())).monitorExecution(true).build());
        when(failoverService.getLocalFailoverItems()).thenReturn(Collections.singletonList(1));
        when(executionContextService.getJobShardingContext(Collections.singletonList(1))).thenReturn(shardingContexts);
        assertThat(liteJobFacade.getShardingContexts(), is(shardingContexts));
        verify(shardingService, times(0)).shardingIfNecessary();
    }
    
    @Test
    public void assertGetShardingContextWhenIsFailoverEnableAndNotFailover() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.<Integer, String>emptyMap());
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).failover(true).build(), 
                TestSimpleJob.class.getCanonicalName())).monitorExecution(true).build());
        when(failoverService.getLocalFailoverItems()).thenReturn(Collections.<Integer>emptyList());
        when(shardingService.getLocalShardingItems()).thenReturn(Lists.newArrayList(0, 1));
        when(failoverService.getLocalTakeOffItems()).thenReturn(Collections.singletonList(0));
        when(executionContextService.getJobShardingContext(Collections.singletonList(1))).thenReturn(shardingContexts);
        assertThat(liteJobFacade.getShardingContexts(), is(shardingContexts));
        verify(shardingService).shardingIfNecessary();
    }
    
    @Test
    public void assertGetShardingContextWhenIsFailoverDisable() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.<Integer, String>emptyMap());
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).failover(false).build(), TestSimpleJob.class.getCanonicalName())).build());
        when(shardingService.getLocalShardingItems()).thenReturn(Lists.newArrayList(0, 1));
        when(executionContextService.getJobShardingContext(Lists.newArrayList(0, 1))).thenReturn(shardingContexts);
        assertThat(liteJobFacade.getShardingContexts(), is(shardingContexts));
        verify(shardingService).shardingIfNecessary();
    }
    
    @Test
    public void assertGetShardingContextWhenHasDisabledItems() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.<Integer, String>emptyMap());
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).failover(false).build(), TestSimpleJob.class.getCanonicalName())).build());
        when(shardingService.getLocalShardingItems()).thenReturn(Lists.newArrayList(0, 1));
        when(executionService.getDisabledItems(Lists.newArrayList(0, 1))).thenReturn(Collections.singletonList(1));
        when(executionContextService.getJobShardingContext(Lists.newArrayList(0))).thenReturn(shardingContexts);
        assertThat(liteJobFacade.getShardingContexts(), is(shardingContexts));
        verify(shardingService).shardingIfNecessary();
    }
    
    @Test
    public void assertMisfireIfRunning() {
        when(executionService.misfireIfHasRunningItems(Arrays.asList(0, 1))).thenReturn(true);
        assertThat(liteJobFacade.misfireIfRunning(Arrays.asList(0, 1)), is(true));
    }
    
    @Test
    public void assertClearMisfire() {
        liteJobFacade.clearMisfire(Arrays.asList(0, 1));
        verify(executionService).clearMisfire(Arrays.asList(0, 1));
    }
    
    @Test
    public void assertIsNeedSharding() {
        when(shardingService.isNeedSharding()).thenReturn(true);
        assertThat(liteJobFacade.isNeedSharding(), is(true));
    }
    
    @Test
    public void assertBeforeJobExecuted() {
        liteJobFacade.beforeJobExecuted(new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.<Integer, String>emptyMap()));
        verify(caller).before();
    }
    
    @Test
    public void assertAfterJobExecuted() {
        liteJobFacade.afterJobExecuted(new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.<Integer, String>emptyMap()));
        verify(caller).after();
    }
    
    @Test
    public void assertNotEligibleForJobRunningWhenNeedSharding() {
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(new DataflowJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(),
                TestDataflowJob.class.getCanonicalName(), true)).build());
        when(shardingService.isNeedSharding()).thenReturn(true);
        assertThat(liteJobFacade.isEligibleForJobRunning(), is(false));
        verify(shardingService).isNeedSharding();
    }
    
    @Test
    public void assertNotEligibleForJobRunningWhenUnStreamingProcess() {
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(new DataflowJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(),
                TestDataflowJob.class.getCanonicalName(), false)).build());
        assertThat(liteJobFacade.isEligibleForJobRunning(), is(false));
        verify(configService).load(true);
    }
    
    @Test
    public void assertEligibleForJobRunningWhenNotNeedShardingAndStreamingProcess() {
        when(shardingService.isNeedSharding()).thenReturn(false);
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(new DataflowJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(),
                TestDataflowJob.class.getCanonicalName(), true)).build());
        assertThat(liteJobFacade.isEligibleForJobRunning(), is(true));
        verify(shardingService).isNeedSharding();
        verify(configService).load(true);
    }
    
    @Test
    public void assertPostJobExecutionEvent() {
        liteJobFacade.postJobExecutionEvent(null);
        verify(eventBus).post(null);
    }
}
