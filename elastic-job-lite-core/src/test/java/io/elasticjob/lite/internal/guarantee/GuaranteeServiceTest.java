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

package io.elasticjob.lite.internal.guarantee;

import io.elasticjob.lite.config.JobCoreConfiguration;
import io.elasticjob.lite.config.LiteJobConfiguration;
import io.elasticjob.lite.config.dataflow.DataflowJobConfiguration;
import io.elasticjob.lite.config.simple.SimpleJobConfiguration;
import io.elasticjob.lite.fixture.TestDataflowJob;
import io.elasticjob.lite.fixture.TestSimpleJob;
import io.elasticjob.lite.internal.config.ConfigurationService;
import io.elasticjob.lite.internal.storage.JobNodeStorage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class GuaranteeServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ConfigurationService configService;
    
    private final GuaranteeService guaranteeService = new GuaranteeService(null, "test_job");
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
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
        when(configService.load(false)).thenReturn(LiteJobConfiguration.newBuilder(new DataflowJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(),
                TestDataflowJob.class.getCanonicalName(), true)).build());
        when(jobNodeStorage.isJobNodeExisted("guarantee/started")).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("guarantee/started")).thenReturn(Arrays.asList("0", "1"));
        assertFalse(guaranteeService.isAllStarted());
    }
    
    @Test
    public void assertIsAllStarted() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/started")).thenReturn(true);
        when(configService.load(false)).thenReturn(LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(),
                TestSimpleJob.class.getCanonicalName())).build());
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
        when(configService.load(false)).thenReturn(LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 10).build(),
                TestSimpleJob.class.getCanonicalName())).build());
        when(jobNodeStorage.isJobNodeExisted("guarantee/completed")).thenReturn(false);
        when(jobNodeStorage.getJobNodeChildrenKeys("guarantee/completed")).thenReturn(Arrays.asList("0", "1"));
        assertFalse(guaranteeService.isAllCompleted());
    }
    
    @Test
    public void assertIsAllCompleted() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/completed")).thenReturn(true);
        when(configService.load(false)).thenReturn(LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(),
                TestSimpleJob.class.getCanonicalName())).build());
        when(jobNodeStorage.getJobNodeChildrenKeys("guarantee/completed")).thenReturn(Arrays.asList("0", "1", "2"));
        assertTrue(guaranteeService.isAllCompleted());
    }
    
    @Test
    public void assertClearAllCompletedInfo() {
        guaranteeService.clearAllCompletedInfo();
        verify(jobNodeStorage).removeJobNodeIfExisted("guarantee/completed");
    }
}
