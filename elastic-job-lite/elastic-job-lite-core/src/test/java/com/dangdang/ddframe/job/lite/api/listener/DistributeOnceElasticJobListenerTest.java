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

package com.dangdang.ddframe.job.lite.api.listener;

import com.dangdang.ddframe.job.exception.JobTimeoutException;
import com.dangdang.ddframe.job.internal.env.TimeService;
import com.dangdang.ddframe.job.lite.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.lite.api.listener.fixture.ElasticJobListenerCaller;
import com.dangdang.ddframe.job.lite.api.listener.fixture.TestDistributeOnceElasticJobListener;
import com.dangdang.ddframe.job.lite.internal.guarantee.GuaranteeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class DistributeOnceElasticJobListenerTest {
    
    @Mock
    private GuaranteeService guaranteeService;

    @Mock
    private TimeService timeService;
    
    @Mock
    private ElasticJobListenerCaller elasticJobListenerCaller;
    
    private JobExecutionMultipleShardingContext shardingContext = new JobExecutionMultipleShardingContext();
    
    private TestDistributeOnceElasticJobListener distributeOnceElasticJobListener;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        distributeOnceElasticJobListener = new TestDistributeOnceElasticJobListener(elasticJobListenerCaller);
        ReflectionUtils.setFieldValue(distributeOnceElasticJobListener, ReflectionUtils.getFieldWithName(AbstractDistributeOnceElasticJobListener.class, "guaranteeService", false), guaranteeService);
        ReflectionUtils.setFieldValue(distributeOnceElasticJobListener, ReflectionUtils.getFieldWithName(AbstractDistributeOnceElasticJobListener.class, "timeService", false), timeService);
        shardingContext.setShardingItems(Arrays.asList(0, 1));
    }
    
    @Test
    public void testBeforeJobExecutedWhenIsAllStarted() {
        when(guaranteeService.isAllStarted()).thenReturn(true);
        distributeOnceElasticJobListener.beforeJobExecuted(shardingContext);
        verify(guaranteeService).registerStart(Arrays.asList(0, 1));
        verify(elasticJobListenerCaller).before();
        verify(guaranteeService).clearAllStartedInfo();
    }
    
    @Test
    public void testBeforeJobExecutedWhenIsNotAllStartedAndNotTimeout() {
        when(guaranteeService.isAllStarted()).thenReturn(false);
        when(timeService.getCurrentMillis()).thenReturn(0L);
        distributeOnceElasticJobListener.beforeJobExecuted(shardingContext);
        verify(guaranteeService).registerStart(Arrays.asList(0, 1));
        verify(guaranteeService, times(0)).clearAllStartedInfo();
    }
    
    @Test(expected = JobTimeoutException.class)
    public void testBeforeJobExecutedWhenIsNotAllStartedAndTimeout() {
        when(guaranteeService.isAllStarted()).thenReturn(false);
        when(timeService.getCurrentMillis()).thenReturn(0L, 2L);
        distributeOnceElasticJobListener.beforeJobExecuted(shardingContext);
        verify(guaranteeService).registerStart(Arrays.asList(0, 1));
        verify(guaranteeService, times(0)).clearAllStartedInfo();
    }
    
    @Test
    public void testAfterJobExecutedWhenIsAllCompleted() {
        when(guaranteeService.isAllCompleted()).thenReturn(true);
        distributeOnceElasticJobListener.afterJobExecuted(shardingContext);
        verify(guaranteeService).registerComplete(Arrays.asList(0, 1));
        verify(elasticJobListenerCaller).after();
        verify(guaranteeService).clearAllCompletedInfo();
    }
    
    @Test
    public void testAfterJobExecutedWhenIsAllCompletedAndNotTimeout() {
        when(guaranteeService.isAllCompleted()).thenReturn(false);
        when(timeService.getCurrentMillis()).thenReturn(0L);
        distributeOnceElasticJobListener.afterJobExecuted(shardingContext);
        verify(guaranteeService).registerComplete(Arrays.asList(0, 1));
        verify(guaranteeService, times(0)).clearAllCompletedInfo();
    }
    
    @Test(expected = JobTimeoutException.class)
    public void testAfterJobExecutedWhenIsAllCompletedAndTimeout() {
        when(guaranteeService.isAllCompleted()).thenReturn(false);
        when(timeService.getCurrentMillis()).thenReturn(0L, 2L);
        distributeOnceElasticJobListener.afterJobExecuted(shardingContext);
        verify(guaranteeService).registerComplete(Arrays.asList(0, 1));
        verify(guaranteeService, times(0)).clearAllCompletedInfo();
    }
}
