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

package org.apache.shardingsphere.elasticjob.kernel.internal.guarantee;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.kernel.api.listener.AbstractDistributeOnceElasticJobListener;
import org.apache.shardingsphere.elasticjob.kernel.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.kernel.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuaranteeServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private AbstractDistributeOnceElasticJobListener listener;
    
    @Mock
    private ShardingContexts shardingContexts;
    
    private final GuaranteeService guaranteeService = new GuaranteeService(null, "test_job");
    
    @BeforeEach
    void setUp() {
        ReflectionUtils.setFieldValue(guaranteeService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(guaranteeService, "configService", configService);
    }
    
    @Test
    void assertRegisterStart() {
        guaranteeService.registerStart(Arrays.asList(0, 1));
        verify(jobNodeStorage).createJobNodeIfNeeded("guarantee/started/0");
        verify(jobNodeStorage).createJobNodeIfNeeded("guarantee/started/1");
    }
    
    @Test
    void assertIsNotRegisterStartSuccess() {
        assertFalse(guaranteeService.isRegisterStartSuccess(Arrays.asList(0, 1)));
    }
    
    @Test
    void assertIsRegisterStartSuccess() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/started/0")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("guarantee/started/1")).thenReturn(true);
        assertTrue(guaranteeService.isRegisterStartSuccess(Arrays.asList(0, 1)));
    }
    
    @Test
    void assertIsNotAllStartedWhenRootNodeIsNotExisted() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/started")).thenReturn(false);
        assertFalse(guaranteeService.isAllStarted());
    }
    
    @Test
    void assertIsNotAllStarted() {
        when(configService.load(false)).thenReturn(
                JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").setProperty("streaming.process", Boolean.TRUE.toString()).build());
        when(jobNodeStorage.isJobNodeExisted("guarantee/started")).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("guarantee/started")).thenReturn(Arrays.asList("0", "1"));
        assertFalse(guaranteeService.isAllStarted());
    }
    
    @Test
    void assertIsAllStarted() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/started")).thenReturn(true);
        when(configService.load(false)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build());
        when(jobNodeStorage.getJobNodeChildrenKeys("guarantee/started")).thenReturn(Arrays.asList("0", "1", "2"));
        assertTrue(guaranteeService.isAllStarted());
    }
    
    @Test
    void assertClearAllStartedInfo() {
        guaranteeService.clearAllStartedInfo();
        verify(jobNodeStorage).removeJobNodeIfExisted("guarantee/started");
    }
    
    @Test
    void assertRegisterComplete() {
        guaranteeService.registerComplete(Arrays.asList(0, 1));
        verify(jobNodeStorage).createJobNodeIfNeeded("guarantee/completed/0");
        verify(jobNodeStorage).createJobNodeIfNeeded("guarantee/completed/1");
    }
    
    @Test
    void assertIsNotRegisterCompleteSuccess() {
        assertFalse(guaranteeService.isRegisterCompleteSuccess(Arrays.asList(0, 1)));
    }
    
    @Test
    void assertIsRegisterCompleteSuccess() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/completed/0")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("guarantee/completed/1")).thenReturn(true);
        assertTrue(guaranteeService.isRegisterCompleteSuccess(Arrays.asList(0, 1)));
    }
    
    @Test
    void assertIsNotAllCompletedWhenRootNodeIsNotExisted() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/completed")).thenReturn(false);
        assertFalse(guaranteeService.isAllCompleted());
    }
    
    @Test
    void assertIsNotAllCompleted() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/completed")).thenReturn(false);
        assertFalse(guaranteeService.isAllCompleted());
    }
    
    @Test
    void assertIsAllCompleted() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/completed")).thenReturn(true);
        when(configService.load(false)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build());
        when(jobNodeStorage.getJobNodeChildrenKeys("guarantee/completed")).thenReturn(Arrays.asList("0", "1", "2"));
        assertTrue(guaranteeService.isAllCompleted());
    }
    
    @Test
    void assertClearAllCompletedInfo() {
        guaranteeService.clearAllCompletedInfo();
        verify(jobNodeStorage).removeJobNodeIfExisted("guarantee/completed");
    }
    
    @Test
    void assertExecuteInLeaderForLastCompleted() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/completed")).thenReturn(true);
        when(configService.load(false)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build());
        when(jobNodeStorage.getJobNodeChildrenKeys("guarantee/completed")).thenReturn(Arrays.asList("0", "1", "2"));
        guaranteeService.new LeaderExecutionCallbackForLastCompleted(listener, shardingContexts).execute();
        verify(listener).doAfterJobExecutedAtLastCompleted(shardingContexts);
    }
    
    @Test
    void assertExecuteInLeaderForNotLastCompleted() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/completed")).thenReturn(false);
        guaranteeService.new LeaderExecutionCallbackForLastCompleted(listener, shardingContexts).execute();
        verify(listener, never()).doAfterJobExecutedAtLastCompleted(shardingContexts);
    }
    
    @Test
    void assertExecuteInLeaderForLastStarted() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/started")).thenReturn(true);
        when(configService.load(false)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build());
        when(jobNodeStorage.getJobNodeChildrenKeys("guarantee/started")).thenReturn(Arrays.asList("0", "1", "2"));
        guaranteeService.new LeaderExecutionCallbackForLastStarted(listener, shardingContexts).execute();
        verify(listener).doBeforeJobExecutedAtLastStarted(shardingContexts);
    }
    
    @Test
    void assertExecuteInLeaderForNotLastStarted() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/started")).thenReturn(false);
        guaranteeService.new LeaderExecutionCallbackForLastStarted(listener, shardingContexts).execute();
        verify(listener, never()).doBeforeJobExecutedAtLastStarted(shardingContexts);
    }
}
