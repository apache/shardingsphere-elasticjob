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

package com.dangdang.ddframe.job.lite.internal.sharding;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.fixture.TestSimpleJob;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ExecutionServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    private final ExecutionService executionService = new ExecutionService(null, "test_job");
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(executionService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(executionService, "configService", configService);
    }
    
    @Test
    public void assertRegisterJobBeginWithoutMonitorExecution() {
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), TestSimpleJob.class.getCanonicalName())).monitorExecution(false).build());
        executionService.registerJobBegin(getShardingContext());
        verify(jobNodeStorage, times(0)).fillEphemeralJobNode((String) any(), any());
        assertTrue(JobRegistry.getInstance().isJobRunning("test_job"));
    }
    
    @Test
    public void assertRegisterJobBeginWithMonitorExecution() {
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), TestSimpleJob.class.getCanonicalName())).monitorExecution(true).build());
        executionService.registerJobBegin(getShardingContext());
        verify(jobNodeStorage).fillEphemeralJobNode("sharding/0/running", "");
        verify(jobNodeStorage).fillEphemeralJobNode("sharding/1/running", "");
        verify(jobNodeStorage).fillEphemeralJobNode("sharding/2/running", "");
        assertTrue(JobRegistry.getInstance().isJobRunning("test_job"));
    }
    
    @Test
    public void assertRegisterJobCompletedWithoutMonitorExecution() {
        JobRegistry.getInstance().setJobRunning("test_job", true);
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), TestSimpleJob.class.getCanonicalName())).monitorExecution(false).build());
        executionService.registerJobCompleted(new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.<Integer, String>emptyMap()));
        verify(jobNodeStorage, times(0)).removeJobNodeIfExisted((String) any());
        verify(jobNodeStorage, times(0)).createJobNodeIfNeeded((String) any());
        assertFalse(JobRegistry.getInstance().isJobRunning("test_job"));
    }
    
    @Test
    public void assertRegisterJobCompletedWithMonitorExecution() {
        JobRegistry.getInstance().setJobRunning("test_job", true);
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), TestSimpleJob.class.getCanonicalName())).monitorExecution(true).build());
        executionService.registerJobCompleted(getShardingContext());
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/0/running");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/1/running");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/2/running");
        assertFalse(JobRegistry.getInstance().isJobRunning("test_job"));
    }
    
    
    @Test
    public void assertClearAllRunningInfo() {
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), TestSimpleJob.class.getCanonicalName())).monitorExecution(false).build());
        executionService.clearAllRunningInfo();
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/0/running");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/1/running");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/2/running");
    }
    
    @Test
    public void assertClearRunningInfo() {
        executionService.clearRunningInfo(Arrays.asList(0, 1));
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/0/running");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/1/running");
    }
    
    @Test
    public void assertNotHaveRunningItemsWithoutMonitorExecution() {
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), TestSimpleJob.class.getCanonicalName())).monitorExecution(false).build());
        assertFalse(executionService.hasRunningItems(Arrays.asList(0, 1, 2)));
    }
    
    @Test
    public void assertHasRunningItemsWithMonitorExecution() {
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), TestSimpleJob.class.getCanonicalName())).monitorExecution(true).build());
        when(jobNodeStorage.isJobNodeExisted("sharding/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/running")).thenReturn(true);
        assertTrue(executionService.hasRunningItems(Arrays.asList(0, 1, 2)));
    }
    
    @Test
    public void assertNotHaveRunningItems() {
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), TestSimpleJob.class.getCanonicalName())).monitorExecution(true).build());
        when(jobNodeStorage.isJobNodeExisted("sharding/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("sharding/2/running")).thenReturn(false);
        assertFalse(executionService.hasRunningItems(Arrays.asList(0, 1, 2)));
    }
    
    @Test
    public void assertHasRunningItemsForAll() {
        when(configService.load(true)).thenReturn(
                LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), TestSimpleJob.class.getCanonicalName())).build());
        when(jobNodeStorage.getJobNodeChildrenKeys("sharding")).thenReturn(Arrays.asList("0", "1", "2"));
        when(jobNodeStorage.isJobNodeExisted("sharding/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/running")).thenReturn(true);
        assertTrue(executionService.hasRunningItems());
    }
    
    @Test
    public void assertNotHaveRunningItemsForAll() {
        when(configService.load(true)).thenReturn(
                LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), TestSimpleJob.class.getCanonicalName())).build());
        when(jobNodeStorage.isJobNodeExisted("sharding/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("sharding/2/running")).thenReturn(false);
        assertFalse(executionService.hasRunningItems());
    }
    
    @Test
    public void assertMisfireIfNotRunning() {
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), TestSimpleJob.class.getCanonicalName())).monitorExecution(true).build());
        when(jobNodeStorage.isJobNodeExisted("sharding/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("sharding/2/running")).thenReturn(false);
        assertFalse(executionService.misfireIfHasRunningItems(Arrays.asList(0, 1, 2)));
    }
    
    @Test
    public void assertMisfireIfRunning() {
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(),
                TestSimpleJob.class.getCanonicalName())).monitorExecution(true).build());
        when(jobNodeStorage.isJobNodeExisted("sharding/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/running")).thenReturn(true);
        assertTrue(executionService.misfireIfHasRunningItems(Arrays.asList(0, 1, 2)));
    }
    
    @Test
    public void assertSetMisfire() {
        executionService.setMisfire(Arrays.asList(0, 1, 2));
        verify(jobNodeStorage).createJobNodeIfNeeded("sharding/0/misfire");
        verify(jobNodeStorage).createJobNodeIfNeeded("sharding/1/misfire");
        verify(jobNodeStorage).createJobNodeIfNeeded("sharding/2/misfire");
    }
    
    @Test
    public void assertGetMisfiredJobItems() {
        when(jobNodeStorage.isJobNodeExisted("sharding/0/misfire")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/misfire")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("sharding/2/misfire")).thenReturn(false);
        assertThat(executionService.getMisfiredJobItems(Arrays.asList(0, 1, 2)), is(Arrays.asList(0, 1)));
    }
    
    @Test
    public void assertClearMisfire() {
        executionService.clearMisfire(Arrays.asList(0, 1, 2));
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/0/misfire");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/1/misfire");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/2/misfire");
    }
    
    @Test
    public void assertGetDisabledItems() {
        when(jobNodeStorage.isJobNodeExisted("sharding/0/disabled")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/disabled")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("sharding/2/disabled")).thenReturn(false);
        assertThat(executionService.getDisabledItems(Arrays.asList(0, 1, 2)), is(Arrays.asList(0, 1)));
    }
    
    private ShardingContexts getShardingContext() {
        Map<Integer, String> map = new HashMap<>(3, 1);
        map.put(0, "");
        map.put(1, "");
        map.put(2, "");
        return new ShardingContexts("fake_task_id", "test_job", 10, "", map);
    }
}
