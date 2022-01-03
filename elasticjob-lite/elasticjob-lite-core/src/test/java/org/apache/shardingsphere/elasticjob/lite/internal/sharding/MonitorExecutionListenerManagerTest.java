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

package org.apache.shardingsphere.elasticjob.lite.internal.sharding;

import org.apache.shardingsphere.elasticjob.lite.fixture.LiteYamlConstants;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent.Type;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class MonitorExecutionListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ExecutionService executionService;
    
    private final MonitorExecutionListenerManager monitorExecutionListenerManager = new MonitorExecutionListenerManager(null, "test_job");
    
    @Before
    public void setUp() {
        ReflectionUtils.setSuperclassFieldValue(monitorExecutionListenerManager, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(monitorExecutionListenerManager, "executionService", executionService);
    }
    
    @Test
    public void assertMonitorExecutionSettingsChangedJobListenerWhenIsNotFailoverPath() {
        monitorExecutionListenerManager.new MonitorExecutionSettingsChangedJobListener().onChange(new DataChangedEvent(DataChangedEvent.Type.ADDED, "/test_job/other", LiteYamlConstants.getJobYaml()));
        verify(executionService, times(0)).clearAllRunningInfo();
    }
    
    @Test
    public void assertMonitorExecutionSettingsChangedJobListenerWhenIsFailoverPathButNotUpdate() {
        monitorExecutionListenerManager.new MonitorExecutionSettingsChangedJobListener().onChange(new DataChangedEvent(Type.ADDED, "/test_job/config", ""));
        verify(executionService, times(0)).clearAllRunningInfo();
    }
    
    @Test
    public void assertMonitorExecutionSettingsChangedJobListenerWhenIsFailoverPathAndUpdateButEnableFailover() {
        monitorExecutionListenerManager.new MonitorExecutionSettingsChangedJobListener().onChange(new DataChangedEvent(Type.UPDATED, "/test_job/config", LiteYamlConstants.getJobYaml()));
        verify(executionService, times(0)).clearAllRunningInfo();
    }
    
    @Test
    public void assertMonitorExecutionSettingsChangedJobListenerWhenIsFailoverPathAndUpdateButDisableFailover() {
        DataChangedEvent event = new DataChangedEvent(Type.UPDATED, "/test_job/config", LiteYamlConstants.getJobYamlWithMonitorExecution(false));
        monitorExecutionListenerManager.new MonitorExecutionSettingsChangedJobListener().onChange(event);
        verify(executionService).clearAllRunningInfo();
    }
}
