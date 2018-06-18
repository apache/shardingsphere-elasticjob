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

package io.elasticjob.lite.api.listener;

import com.google.common.collect.Sets;
import io.elasticjob.lite.api.listener.fixture.ElasticJobListenerCaller;
import io.elasticjob.lite.api.listener.fixture.TestDistributeOnceElasticJobListener;
import io.elasticjob.lite.exception.JobSystemException;
import io.elasticjob.lite.executor.ShardingContexts;
import io.elasticjob.lite.internal.guarantee.GuaranteeService;
import io.elasticjob.lite.util.env.TimeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    
    private ShardingContexts shardingContexts;
    
    private TestDistributeOnceElasticJobListener distributeOnceElasticJobListener;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        distributeOnceElasticJobListener = new TestDistributeOnceElasticJobListener(elasticJobListenerCaller);
        ReflectionUtils.setFieldValue(distributeOnceElasticJobListener, ReflectionUtils.getFieldWithName(AbstractDistributeOnceElasticJobListener.class, "guaranteeService", false), guaranteeService);
        ReflectionUtils.setFieldValue(distributeOnceElasticJobListener, ReflectionUtils.getFieldWithName(AbstractDistributeOnceElasticJobListener.class, "timeService", false), timeService);
        Map<Integer, String> map = new HashMap<>(2, 1);
        map.put(0, "");
        map.put(1, "");
        shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", map);
    }
    
    @Test
    public void assertBeforeJobExecutedWhenIsAllStarted() {
        when(guaranteeService.isAllStarted()).thenReturn(true);
        distributeOnceElasticJobListener.beforeJobExecuted(shardingContexts);
        verify(guaranteeService).registerStart(Sets.newHashSet(0, 1));
        verify(elasticJobListenerCaller).before();
        verify(guaranteeService).clearAllStartedInfo();
    }
    
    @Test
    public void assertBeforeJobExecutedWhenIsNotAllStartedAndNotTimeout() {
        when(guaranteeService.isAllStarted()).thenReturn(false);
        when(timeService.getCurrentMillis()).thenReturn(0L);
        distributeOnceElasticJobListener.beforeJobExecuted(shardingContexts);
        verify(guaranteeService).registerStart(Sets.newHashSet(0, 1));
        verify(guaranteeService, times(0)).clearAllStartedInfo();
    }
    
    @Test(expected = JobSystemException.class)
    public void assertBeforeJobExecutedWhenIsNotAllStartedAndTimeout() {
        when(guaranteeService.isAllStarted()).thenReturn(false);
        when(timeService.getCurrentMillis()).thenReturn(0L, 2L);
        distributeOnceElasticJobListener.beforeJobExecuted(shardingContexts);
        verify(guaranteeService).registerStart(Arrays.asList(0, 1));
        verify(guaranteeService, times(0)).clearAllStartedInfo();
    }
    
    @Test
    public void assertAfterJobExecutedWhenIsAllCompleted() {
        when(guaranteeService.isAllCompleted()).thenReturn(true);
        distributeOnceElasticJobListener.afterJobExecuted(shardingContexts);
        verify(guaranteeService).registerComplete(Sets.newHashSet(0, 1));
        verify(elasticJobListenerCaller).after();
        verify(guaranteeService).clearAllCompletedInfo();
    }
    
    @Test
    public void assertAfterJobExecutedWhenIsAllCompletedAndNotTimeout() {
        when(guaranteeService.isAllCompleted()).thenReturn(false);
        when(timeService.getCurrentMillis()).thenReturn(0L);
        distributeOnceElasticJobListener.afterJobExecuted(shardingContexts);
        verify(guaranteeService).registerComplete(Sets.newHashSet(0, 1));
        verify(guaranteeService, times(0)).clearAllCompletedInfo();
    }
    
    @Test(expected = JobSystemException.class)
    public void assertAfterJobExecutedWhenIsAllCompletedAndTimeout() {
        when(guaranteeService.isAllCompleted()).thenReturn(false);
        when(timeService.getCurrentMillis()).thenReturn(0L, 2L);
        distributeOnceElasticJobListener.afterJobExecuted(shardingContexts);
        verify(guaranteeService).registerComplete(Arrays.asList(0, 1));
        verify(guaranteeService, times(0)).clearAllCompletedInfo();
    }
}
