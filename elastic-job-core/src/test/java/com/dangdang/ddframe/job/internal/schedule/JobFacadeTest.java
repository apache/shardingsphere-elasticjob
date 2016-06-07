/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.internal.schedule;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.api.config.JobConfiguration;
import com.dangdang.ddframe.job.api.config.JobConfigurationFactory;
import com.dangdang.ddframe.job.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.api.listener.fixture.ElasticJobListenerCaller;
import com.dangdang.ddframe.job.api.listener.fixture.TestElasticJobListener;
import com.dangdang.ddframe.job.fixture.TestJob;
import com.dangdang.ddframe.job.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.internal.execution.ExecutionContextService;
import com.dangdang.ddframe.job.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.internal.failover.FailoverService;
import com.dangdang.ddframe.job.internal.offset.OffsetService;
import com.dangdang.ddframe.job.internal.server.ServerService;
import com.dangdang.ddframe.job.internal.sharding.ShardingService;
import com.google.common.collect.Lists;
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

public class JobFacadeTest {
    
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
    
    private JobFacade jobFacade;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        jobFacade = new JobFacade(null, jobConfig, Collections.<ElasticJobListener>singletonList(new TestElasticJobListener(caller)));
        ReflectionUtils.setFieldValue(jobFacade, "configService", configService);
        ReflectionUtils.setFieldValue(jobFacade, "serverService", serverService);
        ReflectionUtils.setFieldValue(jobFacade, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(jobFacade, "executionContextService", executionContextService);
        ReflectionUtils.setFieldValue(jobFacade, "executionService", executionService);
        ReflectionUtils.setFieldValue(jobFacade, "failoverService", failoverService);
        ReflectionUtils.setFieldValue(jobFacade, "offsetService", offsetService);
    }
    
    @Test
    public void assertGetJobName() {
        when(configService.getJobName()).thenReturn("testJob");
        assertThat(jobFacade.getJobName(), is("testJob"));
    }
    
    @Test
    public void assertCheckMaxTimeDiffSecondsTolerable() {
        jobFacade.checkMaxTimeDiffSecondsTolerable();
        verify(configService).checkMaxTimeDiffSecondsTolerable();
    }
    
    @Test
    public void assertIsStreamingProcess() {
        when(configService.isStreamingProcess()).thenReturn(false);
        assertThat(jobFacade.isStreamingProcess(), is(false));
    }
    
    @Test
    public void assertFailoverIfUnnecessary() {
        when(configService.isFailover()).thenReturn(false);
        jobFacade.failoverIfNecessary();
        verify(failoverService, times(0)).failoverIfNecessary();
    }
    
    @Test
    public void assertFailoverIfNecessaryButIsPaused() {
        when(configService.isFailover()).thenReturn(true);
        when(serverService.isJobPausedManually()).thenReturn(true);
        jobFacade.failoverIfNecessary();
        verify(failoverService, times(0)).failoverIfNecessary();
    }
    
    @Test
    public void assertFailoverIfNecessary() {
        when(configService.isFailover()).thenReturn(true);
        when(serverService.isJobPausedManually()).thenReturn(false);
        jobFacade.failoverIfNecessary();
        verify(failoverService).failoverIfNecessary();
    }
    
    @Test
    public void assertRegisterJobBegin() {
        JobExecutionMultipleShardingContext shardingContext = new JobExecutionMultipleShardingContext();
        jobFacade.registerJobBegin(shardingContext);
        verify(executionService).registerJobBegin(shardingContext);
    }
    
    @Test
    public void assertRegisterJobCompletedWhenFailoverDisabled() {
        JobExecutionMultipleShardingContext shardingContext = new JobExecutionMultipleShardingContext();
        when(configService.isFailover()).thenReturn(false);
        jobFacade.registerJobCompleted(shardingContext);
        verify(executionService).registerJobCompleted(shardingContext);
        verify(failoverService, times(0)).updateFailoverComplete(shardingContext.getShardingItems());
    }
    
    @Test
    public void assertRegisterJobCompletedWhenFailoverEnabled() {
        JobExecutionMultipleShardingContext shardingContext = new JobExecutionMultipleShardingContext();
        when(configService.isFailover()).thenReturn(true);
        jobFacade.registerJobCompleted(shardingContext);
        verify(executionService).registerJobCompleted(shardingContext);
        verify(failoverService).updateFailoverComplete(shardingContext.getShardingItems());
    }
    
    @Test
    public void assertGetShardingContextWhenIsFailoverEnableAndFailover() {
        JobExecutionMultipleShardingContext shardingContext = new JobExecutionMultipleShardingContext();
        when(configService.isFailover()).thenReturn(true);
        when(failoverService.getLocalHostFailoverItems()).thenReturn(Collections.singletonList(1));
        when(executionContextService.getJobExecutionShardingContext(Collections.singletonList(1))).thenReturn(shardingContext);
        assertThat(jobFacade.getShardingContext(), is(shardingContext));
        verify(shardingService, times(0)).shardingIfNecessary();
    }
    
    @Test
    public void assertGetShardingContextWhenIsFailoverEnableAndNotFailover() {
        JobExecutionMultipleShardingContext shardingContext = new JobExecutionMultipleShardingContext();
        when(configService.isFailover()).thenReturn(true);
        when(failoverService.getLocalHostFailoverItems()).thenReturn(Collections.<Integer>emptyList());
        when(shardingService.getLocalHostShardingItems()).thenReturn(Lists.newArrayList(0, 1));
        when(failoverService.getLocalHostTakeOffItems()).thenReturn(Collections.singletonList(0));
        when(executionContextService.getJobExecutionShardingContext(Collections.singletonList(1))).thenReturn(shardingContext);
        assertThat(jobFacade.getShardingContext(), is(shardingContext));
        verify(shardingService).shardingIfNecessary();
    }
    
    @Test
    public void assertGetShardingContextWhenIsFailoverDisable() {
        JobExecutionMultipleShardingContext shardingContext = new JobExecutionMultipleShardingContext();
        when(configService.isFailover()).thenReturn(false);
        when(shardingService.getLocalHostShardingItems()).thenReturn(Lists.newArrayList(0, 1));
        when(executionContextService.getJobExecutionShardingContext(Lists.newArrayList(0, 1))).thenReturn(shardingContext);
        assertThat(jobFacade.getShardingContext(), is(shardingContext));
        verify(shardingService).shardingIfNecessary();
    }
    
    @Test
    public void assertMisfireIfNecessary() {
        when(executionService.misfireIfNecessary(Arrays.asList(0, 1))).thenReturn(true);
        assertThat(jobFacade.misfireIfNecessary(Arrays.asList(0, 1)), is(true));
    }
    
    @Test
    public void assertClearMisfire() {
        jobFacade.clearMisfire(Arrays.asList(0, 1));
        verify(executionService).clearMisfire(Arrays.asList(0, 1));
    }
    
    @Test
    public void assertIsNeedSharding() {
        when(shardingService.isNeedSharding()).thenReturn(true);
        assertThat(jobFacade.isNeedSharding(), is(true));
    }
    
    @Test
    public void assertUpdateOffset() {
        jobFacade.updateOffset(0, "offset0");
        verify(offsetService).updateOffset(0, "offset0");
    }
    
    @Test
    public void assertCleanPreviousExecutionInfo() {
        jobFacade.cleanPreviousExecutionInfo();
        verify(executionService).cleanPreviousExecutionInfo();
    }
    
    @Test
    public void assertBeforeJobExecuted() {
        jobFacade.beforeJobExecuted(new JobExecutionMultipleShardingContext());
        verify(caller).before();
    }
    
    @Test
    public void assertAfterJobExecuted() {
        jobFacade.afterJobExecuted(new JobExecutionMultipleShardingContext());
        verify(caller).after();
    }
    
    @Test
    public void assertNotEligibleForJobRunningWhenJobPausedManually() {
        when(serverService.isJobPausedManually()).thenReturn(true);
        assertThat(jobFacade.isEligibleForJobRunning(), is(false));
        verify(serverService).isJobPausedManually();
    }
    
    @Test
    public void assertNotEligibleForJobRunningWhenNeedSharding() {
        when(shardingService.isNeedSharding()).thenReturn(true);
        assertThat(jobFacade.isEligibleForJobRunning(), is(false));
        verify(shardingService).isNeedSharding();
    }
    
    @Test
    public void assertNotEligibleForJobRunningWhenUnStreamingProcess() {
        when(configService.isStreamingProcess()).thenReturn(false);
        assertThat(jobFacade.isEligibleForJobRunning(), is(false));
        verify(configService).isStreamingProcess();
    }
    
    @Test
    public void assertEligibleForJobRunningWhenNotJobPausedManuallyAndNotNeedShardingAndStreamingProcess() {
        when(serverService.isJobPausedManually()).thenReturn(false);
        when(shardingService.isNeedSharding()).thenReturn(false);
        when(configService.isStreamingProcess()).thenReturn(true);
        assertThat(jobFacade.isEligibleForJobRunning(), is(true));
        verify(serverService).isJobPausedManually();
        verify(shardingService).isNeedSharding();
        verify(configService).isStreamingProcess();
    }
}
