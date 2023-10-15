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

package org.apache.shardingsphere.elasticjob.kernel.internal.config;

import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.kernel.fixture.LiteYamlConstants;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.kernel.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.kernel.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RescheduleListenerManagerTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    private final RescheduleListenerManager rescheduleListenerManager = new RescheduleListenerManager(null, "test_job");
    
    @BeforeEach
    void setUp() {
        ReflectionUtils.setSuperclassFieldValue(rescheduleListenerManager, "jobNodeStorage", jobNodeStorage);
    }
    
    @Test
    void assertStart() {
        rescheduleListenerManager.start();
        verify(jobNodeStorage).addDataListener(ArgumentMatchers.<RescheduleListenerManager.CronSettingAndJobEventChangedJobListener>any());
    }
    
    @Test
    void assertCronSettingChangedJobListenerWhenIsNotCronPath() {
        rescheduleListenerManager.new CronSettingAndJobEventChangedJobListener().onChange(new DataChangedEvent(DataChangedEvent.Type.ADDED, "/test_job/config/other", LiteYamlConstants.getJobYaml()));
        verify(jobScheduleController, times(0)).rescheduleJob(any(), any());
    }
    
    @Test
    void assertCronSettingChangedJobListenerWhenIsCronPathButNotUpdate() {
        rescheduleListenerManager.new CronSettingAndJobEventChangedJobListener().onChange(new DataChangedEvent(DataChangedEvent.Type.ADDED, "/test_job/config", LiteYamlConstants.getJobYaml()));
        verify(jobScheduleController, times(0)).rescheduleJob(any(), any());
    }
    
    @Test
    void assertCronSettingChangedJobListenerWhenIsCronPathAndUpdateButCannotFindJob() {
        rescheduleListenerManager.new CronSettingAndJobEventChangedJobListener().onChange(new DataChangedEvent(DataChangedEvent.Type.UPDATED, "/test_job/config", LiteYamlConstants.getJobYaml()));
        verify(jobScheduleController, times(0)).rescheduleJob(any(), any());
    }
    
    @Test
    void assertCronSettingChangedJobListenerWhenIsCronPathAndUpdateAndFindJob() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        rescheduleListenerManager.new CronSettingAndJobEventChangedJobListener().onChange(new DataChangedEvent(DataChangedEvent.Type.UPDATED, "/test_job/config", LiteYamlConstants.getJobYaml()));
        verify(jobScheduleController).rescheduleJob("0/1 * * * * ?", null);
        JobRegistry.getInstance().shutdown("test_job");
    }
}
