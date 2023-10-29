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

package org.apache.shardingsphere.elasticjob.kernel.internal.sharding;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.kernel.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.kernel.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecutionServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ConfigurationService configService;
    
    private final ExecutionService executionService = new ExecutionService(null, "test_job");
    
    @BeforeEach
    void setUp() {
        ReflectionUtils.setFieldValue(executionService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(executionService, "configService", configService);
    }
    
    @AfterEach
    void tearDown() {
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    void assertRegisterJobBeginWithoutMonitorExecution() {
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").monitorExecution(false).build());
        executionService.registerJobBegin(getShardingContext());
        verify(jobNodeStorage, times(0)).fillEphemeralJobNode(any(), any());
        assertTrue(JobRegistry.getInstance().isJobRunning("test_job"));
    }
    
    @Test
    void assertRegisterJobBeginWithMonitorExecution() {
        String jobInstanceId = "127.0.0.1@-@1";
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance(jobInstanceId));
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").monitorExecution(true).build());
        executionService.registerJobBegin(getShardingContext());
        verify(jobNodeStorage).fillEphemeralJobNode("sharding/0/running", jobInstanceId);
        verify(jobNodeStorage).fillEphemeralJobNode("sharding/1/running", jobInstanceId);
        verify(jobNodeStorage).fillEphemeralJobNode("sharding/2/running", jobInstanceId);
        assertTrue(JobRegistry.getInstance().isJobRunning("test_job"));
    }
    
    @Test
    void assertRegisterJobBeginWithFailoverEnabled() {
        String jobInstanceId = "127.0.0.1@-@1";
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance(jobInstanceId));
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(true).build());
        executionService.registerJobBegin(getShardingContext());
        verify(jobNodeStorage).fillJobNode("sharding/0/running", jobInstanceId);
        verify(jobNodeStorage).fillJobNode("sharding/1/running", jobInstanceId);
        verify(jobNodeStorage).fillJobNode("sharding/2/running", jobInstanceId);
        assertTrue(JobRegistry.getInstance().isJobRunning("test_job"));
    }
    
    @Test
    void assertRegisterJobCompletedWithoutMonitorExecution() {
        JobRegistry.getInstance().setJobRunning("test_job", true);
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").monitorExecution(false).build());
        executionService.registerJobCompleted(new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.emptyMap()));
        verify(jobNodeStorage, times(0)).removeJobNodeIfExisted(any());
        verify(jobNodeStorage, times(0)).createJobNodeIfNeeded(any());
        assertFalse(JobRegistry.getInstance().isJobRunning("test_job"));
    }
    
    @Test
    void assertRegisterJobCompletedWithMonitorExecution() {
        JobRegistry.getInstance().setJobRunning("test_job", true);
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").monitorExecution(true).build());
        executionService.registerJobCompleted(getShardingContext());
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/0/running");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/1/running");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/2/running");
        assertFalse(JobRegistry.getInstance().isJobRunning("test_job"));
    }
    
    @Test
    void assertClearAllRunningInfo() {
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").monitorExecution(false).build());
        executionService.clearAllRunningInfo();
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/0/running");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/1/running");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/2/running");
    }
    
    @Test
    void assertClearRunningInfo() {
        executionService.clearRunningInfo(Arrays.asList(0, 1));
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/0/running");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/1/running");
    }
    
    @Test
    void assertNotHaveRunningItemsWithoutMonitorExecution() {
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").monitorExecution(false).build());
        assertFalse(executionService.hasRunningItems(Arrays.asList(0, 1, 2)));
    }
    
    @Test
    void assertHasRunningItemsWithMonitorExecution() {
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").monitorExecution(true).build());
        when(jobNodeStorage.isJobNodeExisted("sharding/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/running")).thenReturn(true);
        assertTrue(executionService.hasRunningItems(Arrays.asList(0, 1, 2)));
    }
    
    @Test
    void assertNotHaveRunningItems() {
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").monitorExecution(true).build());
        when(jobNodeStorage.isJobNodeExisted("sharding/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("sharding/2/running")).thenReturn(false);
        assertFalse(executionService.hasRunningItems(Arrays.asList(0, 1, 2)));
    }
    
    @Test
    void assertHasRunningItemsForAll() {
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build());
        when(jobNodeStorage.isJobNodeExisted("sharding/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/running")).thenReturn(true);
        assertTrue(executionService.hasRunningItems());
    }
    
    @Test
    void assertNotHaveRunningItemsForAll() {
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build());
        when(jobNodeStorage.isJobNodeExisted("sharding/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("sharding/2/running")).thenReturn(false);
        assertFalse(executionService.hasRunningItems());
    }
    
    @Test
    void assertGetAllRunningItems() {
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).build());
        String jobInstanceId = "127.0.0.1@-@1";
        when(jobNodeStorage.getJobNodeData("sharding/0/running")).thenReturn(jobInstanceId);
        lenient().when(jobNodeStorage.getJobNodeData("sharding/2/running")).thenReturn(jobInstanceId);
        Map<Integer, JobInstance> actual = executionService.getAllRunningItems();
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), is(new JobInstance(jobInstanceId)));
        assertThat(actual.get(2), is(new JobInstance(jobInstanceId)));
    }
    
    @Test
    void assertMisfireIfNotRunning() {
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").monitorExecution(true).build());
        when(jobNodeStorage.isJobNodeExisted("sharding/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("sharding/2/running")).thenReturn(false);
        assertFalse(executionService.misfireIfHasRunningItems(Arrays.asList(0, 1, 2)));
    }
    
    @Test
    void assertMisfireIfRunning() {
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").monitorExecution(true).build());
        when(jobNodeStorage.isJobNodeExisted("sharding/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/running")).thenReturn(true);
        assertTrue(executionService.misfireIfHasRunningItems(Arrays.asList(0, 1, 2)));
    }
    
    @Test
    void assertSetMisfire() {
        executionService.setMisfire(Arrays.asList(0, 1, 2));
        verify(jobNodeStorage).createJobNodeIfNeeded("sharding/0/misfire");
        verify(jobNodeStorage).createJobNodeIfNeeded("sharding/1/misfire");
        verify(jobNodeStorage).createJobNodeIfNeeded("sharding/2/misfire");
    }
    
    @Test
    void assertGetMisfiredJobItems() {
        when(jobNodeStorage.isJobNodeExisted("sharding/0/misfire")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/misfire")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("sharding/2/misfire")).thenReturn(false);
        assertThat(executionService.getMisfiredJobItems(Arrays.asList(0, 1, 2)), is(Arrays.asList(0, 1)));
    }
    
    @Test
    void assertClearMisfire() {
        executionService.clearMisfire(Arrays.asList(0, 1, 2));
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/0/misfire");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/1/misfire");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/2/misfire");
    }
    
    @Test
    void assertGetDisabledItems() {
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
