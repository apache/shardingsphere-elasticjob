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

package com.dangdang.ddframe.job.internal.execution;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.unitils.util.ReflectionUtils;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.fixture.TestJob;
import com.dangdang.ddframe.job.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.failover.FailoverService;
import com.dangdang.ddframe.job.internal.offset.OffsetService;
import com.dangdang.ddframe.job.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.internal.storage.JobNodeStorage;
import com.google.common.collect.Lists;

public final class ExecutionContextServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private LocalHostService localHostService;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private OffsetService offsetService;
    
    private final JobConfiguration jobConfig = new JobConfiguration("testJob", TestJob.class, 3, "0/1 * * * * ?");
    
    private final ExecutionContextService executionContextService = new ExecutionContextService(null, jobConfig);
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(executionContextService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(executionContextService, "configService", configService);
        ReflectionUtils.setFieldValue(executionContextService, "offsetService", offsetService);
        when(localHostService.getIp()).thenReturn("mockedIP");
        when(localHostService.getHostName()).thenReturn("mockedHostName");
        when(jobNodeStorage.getJobConfiguration()).thenReturn(jobConfig);
    }
    
    @Test
    public void assertGetJobExecutionShardingContextWhenNotAssignShardingItem() {
        when(configService.getShardingTotalCount()).thenReturn(3);
        when(configService.isFailover()).thenReturn(false);
        when(configService.isMonitorExecution()).thenReturn(false);
        when(configService.getFetchDataCount()).thenReturn(10);
        JobExecutionMultipleShardingContext expected = new JobExecutionMultipleShardingContext();
        expected.setJobName("testJob");
        expected.setShardingTotalCount(3);
        expected.setFetchDataCount(10);
        assertThat(executionContextService.getJobExecutionShardingContext(Collections.<Integer>emptyList()), new ReflectionEquals(expected));
        verify(configService).getShardingTotalCount();
        verify(configService).isMonitorExecution();
        verify(configService).getFetchDataCount();
    }
    
    @Test
    public void assertGetJobExecutionShardingContextWhenAssignShardingItems() {
        when(configService.getShardingTotalCount()).thenReturn(3);
        when(configService.isFailover()).thenReturn(false);
        when(configService.isMonitorExecution()).thenReturn(false);
        when(configService.getFetchDataCount()).thenReturn(10);
        Map<Integer, String> shardingItemParameters = new HashMap<>(3);
        shardingItemParameters.put(0, "A");
        shardingItemParameters.put(1, "B");
        shardingItemParameters.put(2, "C");
        when(configService.getShardingItemParameters()).thenReturn(shardingItemParameters);
        Map<Integer, String> offsets = new HashMap<>(2);
        offsets.put(0, "offset0");
        offsets.put(1, "offset1");
        when(offsetService.getOffsets(Arrays.asList(0, 1))).thenReturn(offsets);
        JobExecutionMultipleShardingContext expected = new JobExecutionMultipleShardingContext();
        expected.setJobName("testJob");
        expected.setShardingTotalCount(3);
        expected.setFetchDataCount(10);
        expected.setShardingItems(Arrays.asList(0, 1));
        expected.getShardingItemParameters().put(0, "A");
        expected.getShardingItemParameters().put(1, "B");
        expected.setOffsets(offsets);
        assertThat(executionContextService.getJobExecutionShardingContext(Arrays.asList(0, 1)), new ReflectionEquals(expected));
        verify(configService).getShardingTotalCount();
        verify(configService).isMonitorExecution();
        verify(configService).getFetchDataCount();
        verify(configService).getShardingItemParameters();
        verify(offsetService).getOffsets(Arrays.asList(0, 1));
    }
    
    @Test
    public void assertGetJobExecutionShardingContextWhenHasRunningItems() {
        when(configService.getShardingTotalCount()).thenReturn(3);
        when(configService.isFailover()).thenReturn(true);
        when(configService.isMonitorExecution()).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("execution/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("execution/1/running")).thenReturn(true);
        when(configService.getFetchDataCount()).thenReturn(10);
        Map<Integer, String> shardingItemParameters = new HashMap<>(3);
        shardingItemParameters.put(0, "A");
        shardingItemParameters.put(1, "B");
        shardingItemParameters.put(2, "C");
        when(configService.getShardingItemParameters()).thenReturn(shardingItemParameters);
        Map<Integer, String> offsets = new HashMap<>(1);
        offsets.put(0, "offset0");
        when(offsetService.getOffsets(Collections.singletonList(0))).thenReturn(offsets);
        JobExecutionMultipleShardingContext expected = new JobExecutionMultipleShardingContext();
        expected.setJobName("testJob");
        expected.setShardingTotalCount(3);
        expected.setFetchDataCount(10);
        expected.setShardingItems(Collections.singletonList(0));
        expected.getShardingItemParameters().put(0, "A");
        expected.setMonitorExecution(true);
        expected.setOffsets(offsets);
        assertThat(executionContextService.getJobExecutionShardingContext(Lists.newArrayList(0, 1)), new ReflectionEquals(expected));
        verify(configService).getShardingTotalCount();
        verify(configService).isMonitorExecution();
        verify(jobNodeStorage).isJobNodeExisted("execution/0/running");
        verify(jobNodeStorage).isJobNodeExisted("execution/1/running");
        verify(configService).getFetchDataCount();
        verify(configService).getShardingItemParameters();
        verify(offsetService).getOffsets(Collections.singletonList(0));
    }
}
