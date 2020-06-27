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

package org.apache.shardingsphere.elasticjob.lite.internal.guarantee;

import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.JobCoreConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.dataflow.DataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.simple.SimpleJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.executor.type.impl.DataflowJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class GuaranteeServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ConfigurationService configService;
    
    private final GuaranteeService guaranteeService = new GuaranteeService(null, "test_job");
    
    @Before
    public void setUp() {
        ReflectionUtils.setFieldValue(guaranteeService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(guaranteeService, "configService", configService);
    }
    
    @Test
    public void assertRegisterStart() {
        guaranteeService.registerStart(Arrays.asList(0, 1));
        verify(jobNodeStorage).createJobNodeIfNeeded("guarantee/started/0");
        verify(jobNodeStorage).createJobNodeIfNeeded("guarantee/started/1");
    }
    
    @Test
    public void assertIsNotAllStartedWhenRootNodeIsNotExisted() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/started")).thenReturn(false);
        assertFalse(guaranteeService.isAllStarted());
    }
    
    @Test
    public void assertIsNotAllStarted() {
        Properties props = new Properties();
        props.setProperty(DataflowJobExecutor.STREAM_PROCESS_KEY, Boolean.TRUE.toString());
        when(configService.load(false)).thenReturn(
                JobConfiguration.newBuilder(new DataflowJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), props)).build());
        when(jobNodeStorage.isJobNodeExisted("guarantee/started")).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("guarantee/started")).thenReturn(Arrays.asList("0", "1"));
        assertFalse(guaranteeService.isAllStarted());
    }
    
    @Test
    public void assertIsAllStarted() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/started")).thenReturn(true);
        when(configService.load(false)).thenReturn(JobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build())).build());
        when(jobNodeStorage.getJobNodeChildrenKeys("guarantee/started")).thenReturn(Arrays.asList("0", "1", "2"));
        assertTrue(guaranteeService.isAllStarted());
    }
    
    @Test
    public void assertClearAllStartedInfo() {
        guaranteeService.clearAllStartedInfo();
        verify(jobNodeStorage).removeJobNodeIfExisted("guarantee/started");
    }
    
    @Test
    public void assertRegisterComplete() {
        guaranteeService.registerComplete(Arrays.asList(0, 1));
        verify(jobNodeStorage).createJobNodeIfNeeded("guarantee/completed/0");
        verify(jobNodeStorage).createJobNodeIfNeeded("guarantee/completed/1");
    }
    
    @Test
    public void assertIsNotAllCompletedWhenRootNodeIsNotExisted() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/completed")).thenReturn(false);
        assertFalse(guaranteeService.isAllCompleted());
    }
    
    @Test
    public void assertIsNotAllCompleted() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/completed")).thenReturn(false);
        assertFalse(guaranteeService.isAllCompleted());
    }
    
    @Test
    public void assertIsAllCompleted() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/completed")).thenReturn(true);
        when(configService.load(false)).thenReturn(JobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build())).build());
        when(jobNodeStorage.getJobNodeChildrenKeys("guarantee/completed")).thenReturn(Arrays.asList("0", "1", "2"));
        assertTrue(guaranteeService.isAllCompleted());
    }
    
    @Test
    public void assertClearAllCompletedInfo() {
        guaranteeService.clearAllCompletedInfo();
        verify(jobNodeStorage).removeJobNodeIfExisted("guarantee/completed");
    }
}
