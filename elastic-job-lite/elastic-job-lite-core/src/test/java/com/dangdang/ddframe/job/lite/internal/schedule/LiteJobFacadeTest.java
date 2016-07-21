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

package com.dangdang.ddframe.job.lite.internal.schedule;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.lite.api.config.JobConfiguration;
import com.dangdang.ddframe.job.lite.api.config.JobConfigurationFactory;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.api.listener.fixture.ElasticJobListenerCaller;
import com.dangdang.ddframe.job.lite.api.listener.fixture.TestElasticJobListener;
import com.dangdang.ddframe.job.lite.fixture.TestJob;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.lite.internal.execution.ExecutionContextService;
import com.dangdang.ddframe.job.lite.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.lite.internal.failover.FailoverService;
import com.dangdang.ddframe.job.lite.internal.offset.OffsetService;
import com.dangdang.ddframe.job.lite.internal.server.ServerService;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingService;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LiteJobFacadeTest {
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private ServerService serverService;
    
    @Mock
    private ShardingService shardingService;
    
    @Mock
    private ExecutionContextService executionContextService;
    
    @Mock
    private ExecutionService executionService;
    
    @Mock
    private FailoverService failoverService;
    
    @Mock
    private OffsetService offsetService;
    
    @Mock
    private ElasticJobListenerCaller caller;
    
    private JobConfiguration jobConfig = JobConfigurationFactory.createSimpleJobConfigurationBuilder("testJob", TestJob.class, 3, "0/1 * * * * ?").build();
    
    private LiteJobFacade liteJobFacade;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        liteJobFacade = new LiteJobFacade(null, jobConfig, Collections.<ElasticJobListener>singletonList(new TestElasticJobListener(caller)));
        ReflectionUtils.setFieldValue(liteJobFacade, "configService", configService);
        ReflectionUtils.setFieldValue(liteJobFacade, "serverService", serverService);
        ReflectionUtils.setFieldValue(liteJobFacade, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(liteJobFacade, "executionContextService", executionContextService);
        ReflectionUtils.setFieldValue(liteJobFacade, "executionService", executionService);
        ReflectionUtils.setFieldValue(liteJobFacade, "failoverService", failoverService);
        ReflectionUtils.setFieldValue(liteJobFacade, "offsetService", offsetService);
    }
    
    @Test
    public void assertGetJobName() {
        when(configService.getJobName()).thenReturn("testJob");
        assertThat(liteJobFacade.getJobName(), is("testJob"));
    }
    
    @Test
    public void assertCheckMaxTimeDiffSecondsTolerable() {
        liteJobFacade.checkMaxTimeDiffSecondsTolerable();
        verify(configService).checkMaxTimeDiffSecondsTolerable();
    }
    
    @Test
    public void assertGetConcurrentDataProcessThreadCount() {
        when(configService.getConcurrentDataProcessThreadCount()).thenReturn(100);
        assertThat(liteJobFacade.getConcurrentDataProcessThreadCount(), is(100));
    }
    
    @Test
    public void assertIsStreamingProcess() {
        when(configService.isStreamingProcess()).thenReturn(false);
        assertFalse(liteJobFacade.isStreamingProcess());
    }
    
    @Test
    public void assertGetScriptCommandLine() {
        when(configService.getScriptCommandLine()).thenReturn("echo hello");
        assertThat(liteJobFacade.getScriptCommandLine(), is("echo hello"));
    }
    
    @Test
    public void assertFailoverIfUnnecessary() {
        when(configService.isFailover()).thenReturn(false);
        liteJobFacade.failoverIfNecessary();
        verify(failoverService, times(0)).failoverIfNecessary();
    }
    
    @Test
    public void assertFailoverIfNecessaryButIsPaused() {
        when(configService.isFailover()).thenReturn(true);
        when(serverService.isJobPausedManually()).thenReturn(true);
        liteJobFacade.failoverIfNecessary();
        verify(failoverService, times(0)).failoverIfNecessary();
    }
    
    @Test
    public void assertFailoverIfNecessary() {
        when(configService.isFailover()).thenReturn(true);
        when(serverService.isJobPausedManually()).thenReturn(false);
        liteJobFacade.failoverIfNecessary();
        verify(failoverService).failoverIfNecessary();
    }
    
    @Test
    public void assertRegisterJobBegin() {
        ShardingContext shardingContext = new ShardingContext("test_job", 10, "", 0, Collections.<ShardingContext.ShardingItem>emptyList());
        liteJobFacade.registerJobBegin(shardingContext);
        verify(executionService).registerJobBegin(shardingContext);
    }
    
    @Test
    public void assertRegisterJobCompletedWhenFailoverDisabled() {
        ShardingContext shardingContext = new ShardingContext("test_job", 10, "", 0, Collections.<ShardingContext.ShardingItem>emptyList());
        when(configService.isFailover()).thenReturn(false);
        liteJobFacade.registerJobCompleted(shardingContext);
        verify(executionService).registerJobCompleted(shardingContext);
        verify(failoverService, times(0)).updateFailoverComplete(shardingContext.getShardingItems().keySet());
    }
    
    @Test
    public void assertRegisterJobCompletedWhenFailoverEnabled() {
        ShardingContext shardingContext = new ShardingContext("test_job", 10, "", 0, Collections.<ShardingContext.ShardingItem>emptyList());
        when(configService.isFailover()).thenReturn(true);
        liteJobFacade.registerJobCompleted(shardingContext);
        verify(executionService).registerJobCompleted(shardingContext);
        verify(failoverService).updateFailoverComplete(shardingContext.getShardingItems().keySet());
    }
    
    @Test
    public void assertGetShardingContextWhenIsFailoverEnableAndFailover() {
        ShardingContext shardingContext = new ShardingContext("test_job", 10, "", 0, Collections.<ShardingContext.ShardingItem>emptyList());
        when(configService.isFailover()).thenReturn(true);
        when(failoverService.getLocalHostFailoverItems()).thenReturn(Collections.singletonList(1));
        when(executionContextService.getJobShardingContext(Collections.singletonList(1))).thenReturn(shardingContext);
        assertThat(liteJobFacade.getShardingContext(), is(shardingContext));
        verify(shardingService, times(0)).shardingIfNecessary();
    }
    
    @Test
    public void assertGetShardingContextWhenIsFailoverEnableAndNotFailover() {
        ShardingContext shardingContext = new ShardingContext("test_job", 10, "", 0, Collections.<ShardingContext.ShardingItem>emptyList());
        when(configService.isFailover()).thenReturn(true);
        when(failoverService.getLocalHostFailoverItems()).thenReturn(Collections.<Integer>emptyList());
        when(shardingService.getLocalHostShardingItems()).thenReturn(Lists.newArrayList(0, 1));
        when(failoverService.getLocalHostTakeOffItems()).thenReturn(Collections.singletonList(0));
        when(executionContextService.getJobShardingContext(Collections.singletonList(1))).thenReturn(shardingContext);
        assertThat(liteJobFacade.getShardingContext(), is(shardingContext));
        verify(shardingService).shardingIfNecessary();
    }
    
    @Test
    public void assertGetShardingContextWhenIsFailoverDisable() {
        ShardingContext shardingContext = new ShardingContext("test_job", 10, "", 0, Collections.<ShardingContext.ShardingItem>emptyList());
        when(configService.isFailover()).thenReturn(false);
        when(shardingService.getLocalHostShardingItems()).thenReturn(Lists.newArrayList(0, 1));
        when(executionContextService.getJobShardingContext(Lists.newArrayList(0, 1))).thenReturn(shardingContext);
        assertThat(liteJobFacade.getShardingContext(), is(shardingContext));
        verify(shardingService).shardingIfNecessary();
    }
    
    @Test
    public void assertMisfireIfNecessary() {
        when(executionService.misfireIfNecessary(Arrays.asList(0, 1))).thenReturn(true);
        assertThat(liteJobFacade.misfireIfNecessary(Arrays.asList(0, 1)), is(true));
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
    public void assertUpdateOffset() {
        liteJobFacade.updateOffset(0, "offset0");
        verify(offsetService).updateOffset(0, "offset0");
    }
    
    @Test
    public void assertCleanPreviousExecutionInfo() {
        liteJobFacade.cleanPreviousExecutionInfo();
        verify(executionService).cleanPreviousExecutionInfo();
    }
    
    @Test
    public void assertBeforeJobExecuted() {
        liteJobFacade.beforeJobExecuted(new ShardingContext("test_job", 10, "", 0, Collections.<ShardingContext.ShardingItem>emptyList()));
        verify(caller).before();
    }
    
    @Test
    public void assertAfterJobExecuted() {
        liteJobFacade.afterJobExecuted(new ShardingContext("test_job", 10, "", 0, Collections.<ShardingContext.ShardingItem>emptyList()));
        verify(caller).after();
    }
    
    @Test
    public void assertNotEligibleForJobRunningWhenJobPausedManually() {
        when(serverService.isJobPausedManually()).thenReturn(true);
        assertThat(liteJobFacade.isEligibleForJobRunning(), is(false));
        verify(serverService).isJobPausedManually();
    }
    
    @Test
    public void assertNotEligibleForJobRunningWhenNeedSharding() {
        when(shardingService.isNeedSharding()).thenReturn(true);
        assertThat(liteJobFacade.isEligibleForJobRunning(), is(false));
        verify(shardingService).isNeedSharding();
    }
    
    @Test
    public void assertNotEligibleForJobRunningWhenUnStreamingProcess() {
        when(configService.isStreamingProcess()).thenReturn(false);
        assertThat(liteJobFacade.isEligibleForJobRunning(), is(false));
        verify(configService).isStreamingProcess();
    }
    
    @Test
    public void assertEligibleForJobRunningWhenNotJobPausedManuallyAndNotNeedShardingAndStreamingProcess() {
        when(serverService.isJobPausedManually()).thenReturn(false);
        when(shardingService.isNeedSharding()).thenReturn(false);
        when(configService.isStreamingProcess()).thenReturn(true);
        assertThat(liteJobFacade.isEligibleForJobRunning(), is(true));
        verify(serverService).isJobPausedManually();
        verify(shardingService).isNeedSharding();
        verify(configService).isStreamingProcess();
    }
}
