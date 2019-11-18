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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
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
        when(jobNodeStorage.getNodeDataAndVersion("guarantee/started")).thenReturn(new ImmutablePair<String, Integer>("", 0));
        when(jobNodeStorage.setNodeDataAndVersion(anyString(), anyString(), anyInt())).thenReturn(true);
        int res = guaranteeService.registerStart(2, anyInt());
        verify(jobNodeStorage).setNodeDataAndVersion("guarantee/started", "2", 0);
        assertEquals(2, res);
    }
    
    @Test
    public void assertRegisterStartWhenGetDataFirstNull() {
        when(jobNodeStorage.getNodeDataAndVersion("guarantee/started")).thenReturn(null).thenReturn(new ImmutablePair<String, Integer>("1", 0));
        when(jobNodeStorage.setNodeDataAndVersion(anyString(), anyString(), anyInt())).thenReturn(true);
        int res = guaranteeService.registerStart(2, anyInt());
        verify(jobNodeStorage).setNodeDataAndVersion("guarantee/started", "3", 0);
        assertEquals(3, res);
    }
    
    @Test
    public void assertRegisterStartWhenSetDataFalse() {
        when(jobNodeStorage.getNodeDataAndVersion("guarantee/started")).thenReturn(new ImmutablePair<String, Integer>("", 0));
        when(jobNodeStorage.setNodeDataAndVersion(anyString(), anyString(), anyInt())).thenReturn(false);
        int res = guaranteeService.registerStart(2, anyInt());
        verify(jobNodeStorage, times(3)).setNodeDataAndVersion("guarantee/started", "2", 0);
        assertEquals(0, res);
    }
    
    @Test
    public void assertIsNotAllStartedWhenRootNodeIsNotExisted() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/started")).thenReturn(false);
        assertFalse(guaranteeService.isAllStarted(anyInt()));
    }
    
    @Test
    public void assertIsNotAllStarted() {
        when(configService.load(false)).thenReturn(LiteJobConfiguration.newBuilder(new DataflowJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 2).build(),
                TestDataflowJob.class.getCanonicalName(), true)).build());
        when(jobNodeStorage.isJobNodeExisted("guarantee/started")).thenReturn(true);
        assertFalse(guaranteeService.isAllStarted(1));
    }
    
    @Test
    public void assertIsAllStarted() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/started")).thenReturn(true);
        when(configService.load(false)).thenReturn(LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 2).build(),
                TestSimpleJob.class.getCanonicalName())).build());
        assertTrue(guaranteeService.isAllStarted(2));
    }
    
    @Test
    public void assertClearAllStartedInfo() {
        guaranteeService.clearAllStartedInfo();
        verify(jobNodeStorage).removeJobNodeIfExisted("guarantee/started");
    }
    
    @Test
    public void assertRegisterComplete() {
        when(jobNodeStorage.getNodeDataAndVersion("guarantee/completed")).thenReturn(new ImmutablePair<String, Integer>("", 0));
        when(jobNodeStorage.setNodeDataAndVersion(anyString(), anyString(), anyInt())).thenReturn(true);
        int res = guaranteeService.registerComplete(2, anyInt());
        verify(jobNodeStorage).setNodeDataAndVersion("guarantee/completed", "2", 0);
        assertEquals(2, res);
    }
    
    @Test
    public void assertRegisterCompleteWhenGetDataFirstNull() {
        when(jobNodeStorage.getNodeDataAndVersion("guarantee/completed")).thenReturn(null).thenReturn(new ImmutablePair<String, Integer>("1", 0));
        when(jobNodeStorage.setNodeDataAndVersion(anyString(), anyString(), anyInt())).thenReturn(true);
        int res = guaranteeService.registerComplete(2, anyInt());
        verify(jobNodeStorage).setNodeDataAndVersion("guarantee/completed", "3", 0);
        assertEquals(3, res);
    }
    
    @Test
    public void assertRegisterCompleteWhenSetDataFalse() {
        when(jobNodeStorage.getNodeDataAndVersion("guarantee/completed")).thenReturn(new ImmutablePair<String, Integer>("", 0));
        when(jobNodeStorage.setNodeDataAndVersion(anyString(), anyString(), anyInt())).thenReturn(false);
        int res = guaranteeService.registerComplete(2, anyInt());
        verify(jobNodeStorage, times(3)).setNodeDataAndVersion("guarantee/completed", "2", 0);
        assertEquals(0, res);
    }
    
    @Test
    public void assertIsNotAllCompletedWhenRootNodeIsNotExisted() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/completed")).thenReturn(false);
        assertFalse(guaranteeService.isAllCompleted(anyInt()));
    }
    
    @Test
    public void assertIsNotAllCompleted() {
        when(configService.load(false)).thenReturn(LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 2).build(),
                TestSimpleJob.class.getCanonicalName())).build());
        when(jobNodeStorage.isJobNodeExisted("guarantee/completed")).thenReturn(true);
        assertFalse(guaranteeService.isAllCompleted(1));
    }
    
    @Test
    public void assertIsAllCompleted() {
        when(jobNodeStorage.isJobNodeExisted("guarantee/completed")).thenReturn(true);
        when(configService.load(false)).thenReturn(LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 2).build(),
                TestSimpleJob.class.getCanonicalName())).build());
        assertTrue(guaranteeService.isAllCompleted(2));
    }
    
    @Test
    public void assertClearAllCompletedInfo() {
        guaranteeService.clearAllCompletedInfo();
        verify(jobNodeStorage).removeJobNodeIfExisted("guarantee/completed");
    }
}
