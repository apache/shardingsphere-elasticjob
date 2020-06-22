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

import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.shardingsphere.elasticjob.lite.fixture.LiteJsonConstants;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
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
        monitorExecutionListenerManager.new MonitorExecutionSettingsChangedJobListener().dataChanged("/test_job/other", TreeCacheEvent.Type.NODE_ADDED, LiteJsonConstants.getJobJson());
        verify(executionService, times(0)).clearAllRunningInfo();
    }
    
    @Test
    public void assertMonitorExecutionSettingsChangedJobListenerWhenIsFailoverPathButNotUpdate() {
        monitorExecutionListenerManager.new MonitorExecutionSettingsChangedJobListener().dataChanged("/test_job/config", TreeCacheEvent.Type.NODE_ADDED, "");
        verify(executionService, times(0)).clearAllRunningInfo();
    }
    
    @Test
    public void assertMonitorExecutionSettingsChangedJobListenerWhenIsFailoverPathAndUpdateButEnableFailover() {
        monitorExecutionListenerManager.new MonitorExecutionSettingsChangedJobListener().dataChanged("/test_job/config", TreeCacheEvent.Type.NODE_UPDATED, LiteJsonConstants.getJobJson());
        verify(executionService, times(0)).clearAllRunningInfo();
    }
    
    @Test
    public void assertMonitorExecutionSettingsChangedJobListenerWhenIsFailoverPathAndUpdateButDisableFailover() {
        monitorExecutionListenerManager.new MonitorExecutionSettingsChangedJobListener().dataChanged(
                "/test_job/config", TreeCacheEvent.Type.NODE_UPDATED, LiteJsonConstants.getJobJsonWithMonitorExecution(false));
        verify(executionService).clearAllRunningInfo();
    }
}
