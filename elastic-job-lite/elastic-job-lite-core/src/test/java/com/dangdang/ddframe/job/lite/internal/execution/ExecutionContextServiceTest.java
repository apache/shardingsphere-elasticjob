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

package com.dangdang.ddframe.job.lite.internal.execution;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.lite.api.config.JobConfiguration;
import com.dangdang.ddframe.job.lite.api.config.JobConfigurationFactory;
import com.dangdang.ddframe.job.lite.api.config.impl.JobType;
import com.dangdang.ddframe.job.lite.fixture.TestJob;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.util.env.LocalHostService;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ExecutionContextServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private LocalHostService localHostService;
    
    @Mock
    private ConfigurationService configService;
    
    private final JobConfiguration jobConfig = JobConfigurationFactory.createSimpleJobConfigurationBuilder("testJob", TestJob.class, 3, "0/1 * * * * ?").build();
    
    private final ExecutionContextService executionContextService = new ExecutionContextService(null, jobConfig);
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(executionContextService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(executionContextService, "configService", configService);
        when(localHostService.getIp()).thenReturn("mockedIP");
        when(localHostService.getHostName()).thenReturn("mockedHostName");
        when(jobNodeStorage.getJobConfiguration()).thenReturn(jobConfig);
    }
    
    @Test
    public void assertGetShardingContextWhenNotAssignShardingItem() {
        when(configService.getShardingTotalCount()).thenReturn(3);
        when(configService.isFailover()).thenReturn(false);
        when(configService.isMonitorExecution()).thenReturn(false);
        when(configService.getJobType()).thenReturn(JobType.DATAFLOW);
        ShardingContext expected = new ShardingContext("testJob", 3, null, Collections.<ShardingContext.ShardingItem>emptyList());
        assertThat(executionContextService.getJobShardingContext(Collections.<Integer>emptyList()), new ReflectionEquals(expected));
        verify(configService).getShardingTotalCount();
        verify(configService).isMonitorExecution();
    }
    
    @Test
    public void assertGetShardingContextWhenAssignShardingItems() {
        when(configService.getShardingTotalCount()).thenReturn(3);
        when(configService.isFailover()).thenReturn(false);
        when(configService.isMonitorExecution()).thenReturn(false);
        when(configService.getJobType()).thenReturn(JobType.DATAFLOW);
        Map<Integer, String> shardingItemParameters = new HashMap<>(3);
        shardingItemParameters.put(0, "A");
        shardingItemParameters.put(1, "B");
        shardingItemParameters.put(2, "C");
        when(configService.getShardingItemParameters()).thenReturn(shardingItemParameters);
        ShardingContext expected = new ShardingContext("testJob", 3, null, Arrays.asList(new ShardingContext.ShardingItem(0, "A"), new ShardingContext.ShardingItem(1, "B")));
        assertShardingContext(executionContextService.getJobShardingContext(Arrays.asList(0, 1)), expected);
        verify(configService).getShardingTotalCount();
        verify(configService).isMonitorExecution();
        verify(configService).getShardingItemParameters();
    }
    
    @Test
    public void assertGetShardingContextWhenHasRunningItems() {
        when(configService.getShardingTotalCount()).thenReturn(3);
        when(configService.isFailover()).thenReturn(true);
        when(configService.isMonitorExecution()).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("execution/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("execution/1/running")).thenReturn(true);
        when(configService.getJobType()).thenReturn(JobType.DATAFLOW);
        Map<Integer, String> shardingItemParameters = new HashMap<>(3);
        shardingItemParameters.put(0, "A");
        shardingItemParameters.put(1, "B");
        shardingItemParameters.put(2, "C");
        when(configService.getShardingItemParameters()).thenReturn(shardingItemParameters);
        ShardingContext expected = new ShardingContext("testJob", 3, null, Collections.singletonList(new ShardingContext.ShardingItem(0, "A")));
        assertShardingContext(executionContextService.getJobShardingContext(Lists.newArrayList(0, 1)), expected);
        verify(configService).getShardingTotalCount();
        verify(configService).isMonitorExecution();
        verify(jobNodeStorage).isJobNodeExisted("execution/0/running");
        verify(jobNodeStorage).isJobNodeExisted("execution/1/running");
        verify(configService).getShardingItemParameters();
    }
    
    private void assertShardingContext(final ShardingContext actual, final ShardingContext expected) {
        assertThat(actual.getJobName(), is(expected.getJobName()));
        assertThat(actual.getShardingTotalCount(), is(expected.getShardingTotalCount()));
        assertThat(actual.getJobParameter(), is(expected.getJobParameter()));
        assertThat(actual.getShardingItems().size(), is(expected.getShardingItems().size()));
        for (int i = 0; i < expected.getShardingItems().size(); i++) {
            assertThat(actual.getShardingItems().get(i).getItem(), is(expected.getShardingItems().get(i).getItem()));
            assertThat(actual.getShardingItems().get(i).getParameter(), is(expected.getShardingItems().get(i).getParameter()));
        }
    }
}
