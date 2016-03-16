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

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
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
    
    private JobConfiguration jobConfig = new JobConfiguration("testJob", TestJob.class, 3, "0/1 * * * * ?");
    
    private JobFacade jobFacade;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        jobFacade = new JobFacade(null, jobConfig, Collections.<ElasticJobListener> singletonList(new TestElasticJobListener(caller)));
        ReflectionUtils.setFieldValue(jobFacade, "configService", configService);
        ReflectionUtils.setFieldValue(jobFacade, "serverService", serverService);
        ReflectionUtils.setFieldValue(jobFacade, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(jobFacade, "executionContextService", executionContextService);
        ReflectionUtils.setFieldValue(jobFacade, "executionService", executionService);
        ReflectionUtils.setFieldValue(jobFacade, "failoverService", failoverService);
        ReflectionUtils.setFieldValue(jobFacade, "offsetService", offsetService);
    }
    
    @Test
    public void testGetJobName() {
        when(configService.getJobName()).thenReturn("testJob");
        assertThat(jobFacade.getJobName(), is("testJob"));
    }
    
    @Test
    public void testCheckMaxTimeDiffSecondsTolerable() {
        jobFacade.checkMaxTimeDiffSecondsTolerable();
        verify(configService).checkMaxTimeDiffSecondsTolerable();
    }
    
    @Test
    public void testFailoverIfUnnecessary() {
        when(configService.isFailover()).thenReturn(false);
        jobFacade.failoverIfNecessary();
        verify(failoverService, times(0)).failoverIfNecessary();
    }
    
    @Test
    public void testFailoverIfNecessaryButIsStopped() {
        when(configService.isFailover()).thenReturn(true);
        when(serverService.isJobStoppedManually()).thenReturn(true);
        jobFacade.failoverIfNecessary();
        verify(failoverService, times(0)).failoverIfNecessary();
    }
    
    @Test
    public void testFailoverIfNecessary() {
        when(configService.isFailover()).thenReturn(true);
        when(serverService.isJobStoppedManually()).thenReturn(false);
        jobFacade.failoverIfNecessary();
        verify(failoverService).failoverIfNecessary();
    }
    
    @Test
    public void testRegisterJobBegin() {
        JobExecutionMultipleShardingContext shardingContext = new JobExecutionMultipleShardingContext();
        jobFacade.registerJobBegin(shardingContext);
        verify(executionService).registerJobBegin(shardingContext);
    }
    
    @Test
    public void testRegisterJobCompletedWhenFailoverDisabled() {
        JobExecutionMultipleShardingContext shardingContext = new JobExecutionMultipleShardingContext();
        when(configService.isFailover()).thenReturn(false);
        jobFacade.registerJobCompleted(shardingContext);
        verify(executionService).registerJobCompleted(shardingContext);
        verify(failoverService, times(0)).updateFailoverComplete(shardingContext.getShardingItems());
    }
    
    @Test
    public void testRegisterJobCompletedWhenFailoverEnabled() {
        JobExecutionMultipleShardingContext shardingContext = new JobExecutionMultipleShardingContext();
        when(configService.isFailover()).thenReturn(true);
        jobFacade.registerJobCompleted(shardingContext);
        verify(executionService).registerJobCompleted(shardingContext);
        verify(failoverService).updateFailoverComplete(shardingContext.getShardingItems());
    }
    
    @Test
    public void testGetShardingContext() {
        JobExecutionMultipleShardingContext shardingContext = new JobExecutionMultipleShardingContext();
        when(executionContextService.getJobExecutionShardingContext()).thenReturn(shardingContext);
        assertThat(jobFacade.getShardingContext(), is(shardingContext));
        verify(shardingService).shardingIfNecessary();
    }
    
    @Test
    public void testMisfireIfNecessary() {
        when(executionService.misfireIfNecessary(Arrays.asList(0, 1))).thenReturn(true);
        assertThat(jobFacade.misfireIfNecessary(Arrays.asList(0, 1)), is(true));
    }
    
    @Test
    public void testClearMisfire() {
        jobFacade.clearMisfire(Arrays.asList(0, 1));
        verify(executionService).clearMisfire(Arrays.asList(0, 1));
    }
    
    @Test
    public void testIsNeedSharding() {
        when(shardingService.isNeedSharding()).thenReturn(true);
        assertThat(jobFacade.isNeedSharding(), is(true));
    }
    
    @Test
    public void testUpdateOffset() {
        jobFacade.updateOffset(0, "offset0");
        verify(offsetService).updateOffset(0, "offset0");
    }
    
    @Test
    public void testBeforeJobExecuted() {
        jobFacade.beforeJobExecuted(new JobExecutionMultipleShardingContext());
        verify(caller).before();
    }
    
    @Test
    public void testAfterJobExecuted() {
        jobFacade.afterJobExecuted(new JobExecutionMultipleShardingContext());
        verify(caller).after();
    }
}
