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

package com.dangdang.ddframe.job.lite.internal.server;

import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ServerServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    private ServerService serverService;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        serverService = new ServerService(null, "test_job");
        MockitoAnnotations.initMocks(this);
        ServerNode serverNode = new ServerNode("test_job");
        ReflectionUtils.setFieldValue(serverService, "serverNode", serverNode);
        ReflectionUtils.setFieldValue(serverService, "jobNodeStorage", jobNodeStorage);
    }
    
    @Test
    public void assertPersistOnlineForInstanceShutdown() {
        JobRegistry.getInstance().shutdown("test_job");
        serverService.persistOnline(false);
        verify(jobNodeStorage, times(0)).fillJobNode("servers/127.0.0.1", ServerStatus.DISABLED.name());
    }
    
    @Test
    public void assertPersistOnlineForDisabledServer() {
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController, regCenter);
        serverService.persistOnline(false);
        verify(jobNodeStorage).fillJobNode("servers/127.0.0.1", ServerStatus.DISABLED.name());
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertPersistOnlineForEnabledServer() {
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController, regCenter);
        serverService.persistOnline(true);
        verify(jobNodeStorage).fillJobNode("servers/127.0.0.1", "");
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertHasAvailableServers() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("127.0.0.1", "127.0.0.2", "127.0.0.3"));
        when(jobNodeStorage.getJobNodeData("servers/127.0.0.1")).thenReturn(ServerStatus.DISABLED.name());
        when(jobNodeStorage.getJobNodeChildrenKeys("instances")).thenReturn(Collections.singletonList("127.0.0.3@-@0"));
        assertTrue(serverService.hasAvailableServers());
    }
    
    @Test
    public void assertHasNotAvailableServers() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("127.0.0.1", "127.0.0.2"));
        when(jobNodeStorage.getJobNodeData("servers/127.0.0.2")).thenReturn(ServerStatus.DISABLED.name());
        when(jobNodeStorage.getJobNodeChildrenKeys("instances")).thenReturn(Arrays.asList("127.0.0.2@-@0", "127.0.0.2@-@1"));
        assertFalse(serverService.hasAvailableServers());
    }
    
    @Test
    public void assertIsNotAvailableServerWhenDisabled() {
        when(jobNodeStorage.getJobNodeData("servers/127.0.0.1")).thenReturn(ServerStatus.DISABLED.name());
        assertFalse(serverService.isAvailableServer("127.0.0.1"));
    }
    
    @Test
    public void assertIsNotAvailableServerWithoutOnlineInstances() {
        when(jobNodeStorage.getJobNodeChildrenKeys("instances")).thenReturn(Collections.singletonList("127.0.0.2@-@0"));
        assertFalse(serverService.isAvailableServer("127.0.0.1"));
    }
    
    @Test
    public void assertIsAvailableServer() {
        when(jobNodeStorage.getJobNodeChildrenKeys("instances")).thenReturn(Collections.singletonList("127.0.0.1@-@0"));
        assertTrue(serverService.isAvailableServer("127.0.0.1"));
    }
    
    @Test
    public void assertIsNotEnableServer() {
        when(jobNodeStorage.getJobNodeData("servers/127.0.0.1")).thenReturn(ServerStatus.DISABLED.name());
        assertFalse(serverService.isEnableServer("127.0.0.1"));
    }
    
    @Test
    public void assertIsEnableServer() {
        assertTrue(serverService.isEnableServer("127.0.0.1"));
    }
}
