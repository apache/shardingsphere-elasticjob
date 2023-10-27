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

package org.apache.shardingsphere.elasticjob.kernel.api.listener;

import com.google.common.collect.Sets;
import org.apache.shardingsphere.elasticjob.kernel.internal.time.TimeService;
import org.apache.shardingsphere.elasticjob.infra.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.kernel.api.listener.fixture.ElasticJobListenerCaller;
import org.apache.shardingsphere.elasticjob.kernel.api.listener.fixture.TestDistributeOnceElasticJobListener;
import org.apache.shardingsphere.elasticjob.kernel.internal.guarantee.GuaranteeService;
import org.apache.shardingsphere.elasticjob.kernel.util.ReflectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DistributeOnceElasticJobListenerTest {
    
    @Mock
    private GuaranteeService guaranteeService;
    
    @Mock
    private TimeService timeService;
    
    @Mock
    private ElasticJobListenerCaller elasticJobListenerCaller;
    
    private ShardingContexts shardingContexts;
    
    private TestDistributeOnceElasticJobListener distributeOnceElasticJobListener;
    
    @BeforeEach
    void setUp() {
        distributeOnceElasticJobListener = new TestDistributeOnceElasticJobListener(elasticJobListenerCaller);
        distributeOnceElasticJobListener.setGuaranteeService(guaranteeService);
        ReflectionUtils.setSuperclassFieldValue(distributeOnceElasticJobListener, "timeService", timeService);
        Map<Integer, String> map = new HashMap<>(2, 1);
        map.put(0, "");
        map.put(1, "");
        shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", map);
    }
    
    @Test
    void assertBeforeJobExecutedWhenIsAllStarted() {
        when(guaranteeService.isRegisterStartSuccess(Sets.newHashSet(0, 1))).thenReturn(true);
        when(guaranteeService.isAllStarted()).thenReturn(true);
        distributeOnceElasticJobListener.beforeJobExecuted(shardingContexts);
        verify(guaranteeService).registerStart(Sets.newHashSet(0, 1));
        verify(guaranteeService).executeInLeaderForLastStarted(distributeOnceElasticJobListener, shardingContexts);
    }
    
    @Test
    void assertBeforeJobExecutedWhenIsNotAllStartedAndNotTimeout() {
        when(guaranteeService.isRegisterStartSuccess(Sets.newHashSet(0, 1))).thenReturn(true);
        when(guaranteeService.isAllStarted()).thenReturn(false);
        when(timeService.getCurrentMillis()).thenReturn(0L);
        distributeOnceElasticJobListener.beforeJobExecuted(shardingContexts);
        verify(guaranteeService).registerStart(Sets.newHashSet(0, 1));
        verify(guaranteeService, times(0)).clearAllStartedInfo();
    }
    
    @Test
    void assertBeforeJobExecutedWhenIsNotAllStartedAndTimeout() {
        assertThrows(JobSystemException.class, () -> {
            when(guaranteeService.isRegisterStartSuccess(Sets.newHashSet(0, 1))).thenReturn(true);
            when(guaranteeService.isAllStarted()).thenReturn(false);
            when(timeService.getCurrentMillis()).thenReturn(0L, 2L);
            distributeOnceElasticJobListener.beforeJobExecuted(shardingContexts);
            verify(guaranteeService).registerStart(Arrays.asList(0, 1));
            verify(guaranteeService, times(0)).clearAllStartedInfo();
        });
    }
    
    @Test
    void assertAfterJobExecutedWhenIsAllCompleted() {
        when(guaranteeService.isRegisterCompleteSuccess(Sets.newHashSet(0, 1))).thenReturn(true);
        when(guaranteeService.isAllCompleted()).thenReturn(true);
        distributeOnceElasticJobListener.afterJobExecuted(shardingContexts);
        verify(guaranteeService).registerComplete(Sets.newHashSet(0, 1));
        verify(guaranteeService).executeInLeaderForLastCompleted(distributeOnceElasticJobListener, shardingContexts);
    }
    
    @Test
    void assertAfterJobExecutedWhenIsAllCompletedAndNotTimeout() {
        when(guaranteeService.isRegisterCompleteSuccess(Sets.newHashSet(0, 1))).thenReturn(true);
        when(guaranteeService.isAllCompleted()).thenReturn(false);
        when(timeService.getCurrentMillis()).thenReturn(0L);
        distributeOnceElasticJobListener.afterJobExecuted(shardingContexts);
        verify(guaranteeService).registerComplete(Sets.newHashSet(0, 1));
        verify(guaranteeService, times(0)).clearAllCompletedInfo();
    }
    
    @Test
    void assertAfterJobExecutedWhenIsAllCompletedAndTimeout() {
        assertThrows(JobSystemException.class, () -> {
            when(guaranteeService.isRegisterCompleteSuccess(Sets.newHashSet(0, 1))).thenReturn(true);
            when(guaranteeService.isAllCompleted()).thenReturn(false);
            when(timeService.getCurrentMillis()).thenReturn(0L, 2L);
            distributeOnceElasticJobListener.afterJobExecuted(shardingContexts);
            verify(guaranteeService).registerComplete(Arrays.asList(0, 1));
            verify(guaranteeService, times(0)).clearAllCompletedInfo();
        });
    }
}
