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

import org.apache.shardingsphere.elasticjob.kernel.fixture.YamlConstants;
import org.apache.shardingsphere.elasticjob.kernel.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MonitorExecutionListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ExecutionService executionService;
    
    private final MonitorExecutionListenerManager monitorExecutionListenerManager = new MonitorExecutionListenerManager(null, "test_job");
    
    @BeforeEach
    void setUp() {
        ReflectionUtils.setSuperclassFieldValue(monitorExecutionListenerManager, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(monitorExecutionListenerManager, "executionService", executionService);
    }
    
    @Test
    void assertMonitorExecutionSettingsChangedJobListenerWhenIsNotFailoverPath() {
        monitorExecutionListenerManager.new MonitorExecutionSettingsChangedJobListener().onChange(new DataChangedEvent(DataChangedEvent.Type.ADDED, "/test_job/other", YamlConstants.getJobYaml()));
        verify(executionService, times(0)).clearAllRunningInfo();
    }
    
    @Test
    void assertMonitorExecutionSettingsChangedJobListenerWhenIsFailoverPathButNotUpdate() {
        monitorExecutionListenerManager.new MonitorExecutionSettingsChangedJobListener().onChange(new DataChangedEvent(Type.ADDED, "/test_job/config", ""));
        verify(executionService, times(0)).clearAllRunningInfo();
    }
    
    @Test
    void assertMonitorExecutionSettingsChangedJobListenerWhenIsFailoverPathAndUpdateButEnableFailover() {
        monitorExecutionListenerManager.new MonitorExecutionSettingsChangedJobListener().onChange(new DataChangedEvent(Type.UPDATED, "/test_job/config", YamlConstants.getJobYaml()));
        verify(executionService, times(0)).clearAllRunningInfo();
    }
    
    @Test
    void assertMonitorExecutionSettingsChangedJobListenerWhenIsFailoverPathAndUpdateButDisableFailover() {
        DataChangedEvent event = new DataChangedEvent(Type.UPDATED, "/test_job/config", YamlConstants.getJobYamlWithMonitorExecution(false));
        monitorExecutionListenerManager.new MonitorExecutionSettingsChangedJobListener().onChange(event);
        verify(executionService).clearAllRunningInfo();
    }
}
