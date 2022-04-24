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

package org.apache.shardingsphere.elasticjob.lite.internal.trigger;

import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class TriggerListenerManagerTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private TriggerService triggerService;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    private TriggerListenerManager triggerListenerManager;
    
    @Before
    public void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        triggerListenerManager = new TriggerListenerManager(null, "test_job");
        ReflectionUtils.setFieldValue(triggerListenerManager, "triggerService", triggerService);
        ReflectionUtils.setSuperclassFieldValue(triggerListenerManager, "jobNodeStorage", jobNodeStorage);
    }
    
    @Test
    public void assertStart() {
        triggerListenerManager.start();
        verify(jobNodeStorage).addDataListener(ArgumentMatchers.any());
    }
    
    @Test
    public void assertNotTriggerWhenIsNotLocalInstancePath() {
        triggerListenerManager.new JobTriggerStatusJobListener().onChange(new DataChangedEvent(DataChangedEvent.Type.ADDED, "/test_job/trigger/127.0.0.2@-@0", ""));
        verify(triggerService, times(0)).removeTriggerFlag();
    }
    
    @Test
    public void assertNotTriggerWhenIsNotCreate() {
        triggerListenerManager.new JobTriggerStatusJobListener().onChange(new DataChangedEvent(DataChangedEvent.Type.UPDATED, "/test_job/trigger/127.0.0.1@-@0", ""));
        verify(triggerService, times(0)).removeTriggerFlag();
    }
    
    @Test
    public void assertTriggerWhenJobScheduleControllerIsNull() {
        triggerListenerManager.new JobTriggerStatusJobListener().onChange(new DataChangedEvent(DataChangedEvent.Type.ADDED, "/test_job/trigger/127.0.0.1@-@0", ""));
        verify(triggerService).removeTriggerFlag();
        verify(jobScheduleController, times(0)).triggerJob();
    }
    
    @Test
    public void assertTriggerWhenJobIsRunning() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        JobRegistry.getInstance().setJobRunning("test_job", true);
        triggerListenerManager.new JobTriggerStatusJobListener().onChange(new DataChangedEvent(DataChangedEvent.Type.ADDED, "/test_job/trigger/127.0.0.1@-@0", ""));
        verify(triggerService).removeTriggerFlag();
        verify(jobScheduleController, times(0)).triggerJob();
        JobRegistry.getInstance().setJobRunning("test_job", false);
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertTriggerWhenJobIsNotRunning() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        triggerListenerManager.new JobTriggerStatusJobListener().onChange(new DataChangedEvent(DataChangedEvent.Type.ADDED, "/test_job/trigger/127.0.0.1@-@0", ""));
        verify(triggerService).removeTriggerFlag();
        verify(jobScheduleController).triggerJob();
        JobRegistry.getInstance().shutdown("test_job");
    }
}
