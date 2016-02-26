/**
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

package com.dangdang.ddframe.job.plugin.job.type.simple;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.exception.TimeDiffIntolerableException;
import com.dangdang.ddframe.job.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.internal.execution.ExecutionContextService;
import com.dangdang.ddframe.job.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.internal.failover.FailoverService;
import com.dangdang.ddframe.job.internal.job.AbstractElasticJob;
import com.dangdang.ddframe.job.internal.offset.OffsetService;
import com.dangdang.ddframe.job.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.plugin.job.type.ElasticJobAssert;
import com.dangdang.ddframe.job.plugin.job.type.fixture.FooSimpleElasticJob;
import com.dangdang.ddframe.job.plugin.job.type.fixture.JobCaller;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobExecutionException;
import org.unitils.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class SimpleElasticJobTest {
    
    @Mock
    private JobCaller jobCaller;
    
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
    private OffsetService offsetService;
    
    private FooSimpleElasticJob simpleElasticJob;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        when(configService.getJobName()).thenReturn("testJob");
        simpleElasticJob = new FooSimpleElasticJob(jobCaller);
        simpleElasticJob.setConfigService(configService);
        simpleElasticJob.setShardingService(shardingService);
        simpleElasticJob.setExecutionContextService(executionContextService);
        simpleElasticJob.setExecutionService(executionService);
        simpleElasticJob.setFailoverService(failoverService);
        simpleElasticJob.setOffsetService(offsetService);
    }
    
    @Test(expected = TimeDiffIntolerableException.class)
    public void assertExecuteWhenCheckMaxTimeDiffSecondsUntolerable() throws JobExecutionException {
        doThrow(TimeDiffIntolerableException.class).when(configService).checkMaxTimeDiffSecondsTolerable();
        try {
            simpleElasticJob.execute(null);
        } finally {
            verify(configService).checkMaxTimeDiffSecondsTolerable();
            verify(shardingService, times(0)).shardingIfNecessary();
            verify(jobCaller, times(0)).process();
        }
    }
    
    @Test
    public void assertExecuteWhenPreviousJobStillRunning() throws JobExecutionException {
        JobExecutionMultipleShardingContext shardingContext = new JobExecutionMultipleShardingContext();
        when(executionContextService.getJobExecutionShardingContext()).thenReturn(shardingContext);
        when(executionService.misfireIfNecessary(shardingContext.getShardingItems())).thenReturn(true);
        simpleElasticJob.execute(null);
        verify(configService).checkMaxTimeDiffSecondsTolerable();
        verify(shardingService).shardingIfNecessary();
        verify(executionContextService).getJobExecutionShardingContext();
        verify(executionService).misfireIfNecessary(shardingContext.getShardingItems());
        verify(executionService, times(0)).cleanPreviousExecutionInfo();
        verify(jobCaller, times(0)).process();
    }
    
    @Test
    public void assertExecuteWhenShardingItemsIsEmpty() throws JobExecutionException {
        JobExecutionMultipleShardingContext shardingContext = new JobExecutionMultipleShardingContext();
        ElasticJobAssert.prepareForIsNotMisfireAndIsNotFailover(configService, executionContextService, executionService, shardingContext);
        simpleElasticJob.execute(null);
        verify(configService).checkMaxTimeDiffSecondsTolerable();
        verify(shardingService).shardingIfNecessary();
        verify(executionContextService).getJobExecutionShardingContext();
        verify(executionService).misfireIfNecessary(shardingContext.getShardingItems());
        verify(executionService).cleanPreviousExecutionInfo();
        verify(executionService, times(0)).registerJobBegin(shardingContext);
        verify(jobCaller, times(0)).process();
    }
    
    @Test(expected = JobExecutionException.class)
    public void assertExecuteWhenRunOnceAndThrowException() throws JobExecutionException {
        JobExecutionMultipleShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        ElasticJobAssert.prepareForIsNotMisfireAndIsNotFailover(configService, executionContextService, executionService, shardingContext);
        doThrow(RuntimeException.class).when(jobCaller).process();
        try {
            simpleElasticJob.execute(null);
        } finally {
            verify(configService).checkMaxTimeDiffSecondsTolerable();
            verify(shardingService).shardingIfNecessary();
            verify(executionContextService).getJobExecutionShardingContext();
            verify(executionService).misfireIfNecessary(shardingContext.getShardingItems());
            verify(executionService).cleanPreviousExecutionInfo();
            verify(executionService).registerJobBegin(shardingContext);
            verify(jobCaller).process();
            verify(executionService).registerJobCompleted(shardingContext);
            verify(configService, times(1)).isFailover();
            verify(failoverService, times(0)).updateFailoverComplete(shardingContext.getShardingItems());
        }
    }
    
    @Test
    public void assertExecuteWhenRunOnceSuccess() throws JobExecutionException {
        JobExecutionMultipleShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        ElasticJobAssert.prepareForIsNotMisfireAndIsNotFailover(configService, executionContextService, executionService, shardingContext);
        simpleElasticJob.execute(null);
        ElasticJobAssert.verifyForIsNotMisfireAndIsNotFailover(configService, shardingService, executionContextService, executionService, failoverService, shardingContext);
        verify(jobCaller).process();
    }
    
    @Test
    public void assertExecuteWhenRunOnceWithMisfireIsEmpty() throws JobExecutionException {
        JobExecutionMultipleShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        when(executionContextService.getJobExecutionShardingContext()).thenReturn(shardingContext);
        when(executionService.misfireIfNecessary(shardingContext.getShardingItems())).thenReturn(false);
        when(configService.isMisfire()).thenReturn(true);
        when(executionService.getMisfiredJobItems(shardingContext.getShardingItems())).thenReturn(Collections.<Integer>emptyList());
        when(configService.isFailover()).thenReturn(false);
        simpleElasticJob.execute(null);
        ElasticJobAssert.verifyForIsNotMisfireAndIsNotFailover(configService, shardingService, executionContextService, executionService, failoverService, shardingContext);
        verify(jobCaller).process();
    }
    
    @Test
    public void assertExecuteWhenRunOnceWithMisfireIsNotEmptyButIsStoped() throws JobExecutionException {
        JobExecutionMultipleShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        when(executionContextService.getJobExecutionShardingContext()).thenReturn(shardingContext);
        when(executionService.misfireIfNecessary(shardingContext.getShardingItems())).thenReturn(false);
        when(configService.isMisfire()).thenReturn(true);
        when(executionService.getMisfiredJobItems(shardingContext.getShardingItems())).thenReturn(Collections.singletonList(0));
        when(configService.isFailover()).thenReturn(false);
        simpleElasticJob.stop();
        simpleElasticJob.execute(null);
        ElasticJobAssert.verifyForIsNotMisfireAndIsNotFailover(configService, shardingService, executionContextService, executionService, failoverService, shardingContext);
        verify(jobCaller).process();
        verify(executionService).getMisfiredJobItems(shardingContext.getShardingItems());
        verify(executionService, times(0)).clearMisfire(shardingContext.getShardingItems());
    }
    
    @Test
    public void assertExecuteWhenRunOnceWithMisfireIsNotEmptyButIsNeedSharding() throws JobExecutionException {
        JobExecutionMultipleShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        when(executionContextService.getJobExecutionShardingContext()).thenReturn(shardingContext);
        when(executionService.misfireIfNecessary(shardingContext.getShardingItems())).thenReturn(false);
        when(configService.isMisfire()).thenReturn(true);
        when(executionService.getMisfiredJobItems(shardingContext.getShardingItems())).thenReturn(Collections.singletonList(0));
        when(shardingService.isNeedSharding()).thenReturn(true);
        when(configService.isFailover()).thenReturn(false);
        simpleElasticJob.execute(null);
        ElasticJobAssert.verifyForIsNotMisfireAndIsNotFailover(configService, shardingService, executionContextService, executionService, failoverService, shardingContext);
        verify(jobCaller).process();
        verify(executionService).getMisfiredJobItems(shardingContext.getShardingItems());
        verify(shardingService).isNeedSharding();
        verify(executionService, times(0)).clearMisfire(shardingContext.getShardingItems());
    }
    
    @Test
    public void assertExecuteWhenRunOnceWithMisfire() throws JobExecutionException {
        JobExecutionMultipleShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        when(executionContextService.getJobExecutionShardingContext()).thenReturn(shardingContext);
        when(executionService.misfireIfNecessary(shardingContext.getShardingItems())).thenReturn(false);
        when(configService.isMisfire()).thenReturn(true, false);
        when(executionService.getMisfiredJobItems(shardingContext.getShardingItems())).thenReturn(Collections.singletonList(0));
        when(shardingService.isNeedSharding()).thenReturn(false);
        when(configService.isFailover()).thenReturn(false);
        simpleElasticJob.execute(null);
        verify(configService).checkMaxTimeDiffSecondsTolerable();
        verify(shardingService).shardingIfNecessary();
        verify(executionContextService).getJobExecutionShardingContext();
        verify(executionService).misfireIfNecessary(shardingContext.getShardingItems());
        verify(executionService).cleanPreviousExecutionInfo();
        verify(executionService, times(2)).registerJobBegin(shardingContext);
        verify(jobCaller, times(2)).process();
        verify(executionService, times(2)).registerJobCompleted(shardingContext);
        verify(configService, times(2)).isMisfire();
        verify(executionService).getMisfiredJobItems(shardingContext.getShardingItems());
        verify(shardingService).isNeedSharding();
        verify(executionService).clearMisfire(shardingContext.getShardingItems());
        verify(configService, times(3)).isFailover();
        verify(failoverService, times(0)).updateFailoverComplete(shardingContext.getShardingItems());
        verify(failoverService, times(0)).failoverIfNecessary();
    }
    
    @Test
    public void assertExecuteWhenRunOnceWhenIsFailoverButStoped() throws JobExecutionException {
        JobExecutionMultipleShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        when(executionContextService.getJobExecutionShardingContext()).thenReturn(shardingContext);
        when(executionService.misfireIfNecessary(shardingContext.getShardingItems())).thenReturn(false);
        when(configService.isMisfire()).thenReturn(false);
        when(configService.isFailover()).thenReturn(true);
        simpleElasticJob.stop();
        simpleElasticJob.execute(null);
        verify(configService).checkMaxTimeDiffSecondsTolerable();
        verify(shardingService).shardingIfNecessary();
        verify(executionContextService).getJobExecutionShardingContext();
        verify(executionService).misfireIfNecessary(shardingContext.getShardingItems());
        verify(executionService).cleanPreviousExecutionInfo();
        verify(executionService).registerJobBegin(shardingContext);
        verify(jobCaller).process();
        verify(executionService).registerJobCompleted(shardingContext);
        verify(configService).isMisfire();
        verify(configService, times(2)).isFailover();
        verify(failoverService).updateFailoverComplete(shardingContext.getShardingItems());
        verify(failoverService, times(0)).failoverIfNecessary();
    }
    
    @Test
    public void assertExecuteWhenRunOnceWhenIsFailover() throws JobExecutionException {
        JobExecutionMultipleShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        when(executionContextService.getJobExecutionShardingContext()).thenReturn(shardingContext);
        when(executionService.misfireIfNecessary(shardingContext.getShardingItems())).thenReturn(false);
        when(configService.isMisfire()).thenReturn(false);
        when(configService.isFailover()).thenReturn(true);
        simpleElasticJob.execute(null);
        verify(configService).checkMaxTimeDiffSecondsTolerable();
        verify(shardingService).shardingIfNecessary();
        verify(executionContextService).getJobExecutionShardingContext();
        verify(executionService).misfireIfNecessary(shardingContext.getShardingItems());
        verify(executionService).cleanPreviousExecutionInfo();
        verify(executionService).registerJobBegin(shardingContext);
        verify(jobCaller).process();
        verify(executionService).registerJobCompleted(shardingContext);
        verify(configService).isMisfire();
        verify(configService, times(2)).isFailover();
        verify(failoverService).updateFailoverComplete(shardingContext.getShardingItems());
        verify(failoverService).failoverIfNecessary();
    }
    
    @Test
    public void assertStop() throws JobExecutionException, InvocationTargetException, NoSuchMethodException {
        simpleElasticJob.stop();
        assertTrue((Boolean) ReflectionUtils.invokeMethod(simpleElasticJob, AbstractElasticJob.class.getDeclaredMethod("isStoped")));
    }
    
    @Test
    public void assertResume() throws JobExecutionException, InvocationTargetException, NoSuchMethodException {
        simpleElasticJob.stop();
        simpleElasticJob.resume();
        assertFalse((Boolean) ReflectionUtils.invokeMethod(simpleElasticJob, AbstractElasticJob.class.getDeclaredMethod("isStoped")));
    }
}
