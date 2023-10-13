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

package org.apache.shardingsphere.elasticjob.lite.internal.instance;

import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.SchedulerFacade;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent.Type;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public final class ShutdownListenerManagerTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private InstanceService instanceService;
    
    @Mock
    private SchedulerFacade schedulerFacade;
    
    private ShutdownListenerManager shutdownListenerManager;
    
    @BeforeEach
    public void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        shutdownListenerManager = new ShutdownListenerManager(null, "test_job");
        ReflectionUtils.setFieldValue(shutdownListenerManager, "instanceService", instanceService);
        ReflectionUtils.setFieldValue(shutdownListenerManager, "schedulerFacade", schedulerFacade);
        ReflectionUtils.setSuperclassFieldValue(shutdownListenerManager, "jobNodeStorage", jobNodeStorage);
    }
    
    @AfterEach
    public void tearDown() {
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertStart() {
        shutdownListenerManager.start();
        verify(jobNodeStorage).addDataListener(ArgumentMatchers.any());
    }
    
    @Test
    public void assertIsShutdownAlready() {
        shutdownListenerManager.new InstanceShutdownStatusJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/instances/127.0.0.1@-@0", ""));
        verify(schedulerFacade, times(0)).shutdownInstance();
    }
    
    @Test
    public void assertIsNotLocalInstancePath() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        shutdownListenerManager.new InstanceShutdownStatusJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/instances/127.0.0.2@-@0", ""));
        verify(schedulerFacade, times(0)).shutdownInstance();
    }
    
    @Test
    public void assertUpdateLocalInstancePath() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        shutdownListenerManager.new InstanceShutdownStatusJobListener().onChange(new DataChangedEvent(Type.UPDATED, "/test_job/instances/127.0.0.1@-@0", ""));
        verify(schedulerFacade, times(0)).shutdownInstance();
    }
    
    @Test
    public void assertRemoveLocalInstancePathForPausedJob() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(jobScheduleController.isPaused()).thenReturn(true);
        shutdownListenerManager.new InstanceShutdownStatusJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/instances/127.0.0.1@-@0", ""));
        verify(schedulerFacade, times(0)).shutdownInstance();
    }
    
    @Test
    public void assertRemoveLocalInstancePathForReconnectedRegistryCenter() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(instanceService.isLocalJobInstanceExisted()).thenReturn(true);
        shutdownListenerManager.new InstanceShutdownStatusJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/instances/127.0.0.1@-@0", ""));
        verify(schedulerFacade, times(0)).shutdownInstance();
    }
    
    @Test
    public void assertRemoveLocalInstancePath() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        shutdownListenerManager.new InstanceShutdownStatusJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/instances/127.0.0.1@-@0", ""));
        verify(schedulerFacade).shutdownInstance();
    }
}
