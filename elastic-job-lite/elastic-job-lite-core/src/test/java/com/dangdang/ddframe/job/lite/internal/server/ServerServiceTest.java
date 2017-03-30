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
import com.dangdang.ddframe.job.lite.internal.instance.InstanceNode;
import com.dangdang.ddframe.job.lite.internal.instance.InstanceStatus;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ServerServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    private ServerService serverService;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        serverService = new ServerService(null, "test_job");
        MockitoAnnotations.initMocks(this);
        InstanceNode instanceNode = new InstanceNode("test_job");
        ServerNode serverNode = new ServerNode("test_job");
        ReflectionUtils.setFieldValue(serverNode, "ip", "127.0.0.1");
        ReflectionUtils.setFieldValue(serverService, "instanceNode", instanceNode);
        ReflectionUtils.setFieldValue(serverService, "serverNode", serverNode);
        ReflectionUtils.setFieldValue(serverService, "jobNodeStorage", jobNodeStorage);
    }
    
    @Test
    public void assertPersistServerOnlineForDisabledServerWithLeaderElecting() {
        serverService.persistServerOnline(false);
        verify(jobNodeStorage).fillJobNode("servers/127.0.0.1", ServerStatus.DISABLED.name());
    }
    
    @Test
    public void assertPersistServerOnlineForEnabledServer() {
        serverService.persistServerOnline(true);
        verify(jobNodeStorage).fillJobNode("servers/127.0.0.1", "");
    }
    
    @Test
    public void assertGetAvailableServers() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("host0", "host2", "host1", "host3"));
        when(jobNodeStorage.getJobNodeChildrenKeys("instances")).thenReturn(Arrays.asList("host0@-@0", "host2@-@0", "host3@-@0"));
        when(jobNodeStorage.getJobNodeData("servers/host2")).thenReturn(ServerStatus.DISABLED.name());
        assertThat(serverService.getAvailableServers(), is(Arrays.asList("host0", "host3")));
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerCrashed() {
        when(jobNodeStorage.isJobNodeExisted("servers/127.0.0.1/operation/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/127.0.0.1/instances/127.0.0.1@-@0")).thenReturn(false);
        assertFalse(serverService.isLocalhostServerReady());
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerDisabled() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/127.0.0.1/instances")).thenReturn(Collections.singletonList("127.0.0.1@-@0"));
        when(jobNodeStorage.isJobNodeExisted("servers/127.0.0.1/instances/127.0.0.1@-@0")).thenReturn(true);
        when(jobNodeStorage.getJobNodeData("servers/127.0.0.1")).thenReturn(ServerStatus.DISABLED.name());
        assertFalse(serverService.isLocalhostServerReady());
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerShutdown() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/127.0.0.1/instances")).thenReturn(Collections.singletonList("127.0.0.1@-@0"));
        when(jobNodeStorage.isJobNodeExisted("servers/127.0.0.1/instances/127.0.0.1@-@0")).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly("servers/127.0.0.1/instances/127.0.0.1@-@0")).thenReturn(InstanceStatus.RUNNING.name());
        when(jobNodeStorage.getJobNodeData("servers/127.0.0.1")).thenReturn("");
        assertFalse(serverService.isLocalhostServerReady());
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerRunning() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/127.0.0.1/instances")).thenReturn(Collections.singletonList("127.0.0.1@-@0"));
        when(jobNodeStorage.isJobNodeExisted("servers/127.0.0.1/instances/127.0.0.1@-@0")).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly("servers/127.0.0.1/instances/127.0.0.1@-@0")).thenReturn(InstanceStatus.RUNNING.name());
        when(jobNodeStorage.isJobNodeExisted("servers/127.0.0.1/operation/disabled")).thenReturn(false);
        when(jobNodeStorage.getJobNodeData("servers/127.0.0.1/127.0.0.1@-@0/status")).thenReturn(InstanceStatus.RUNNING.name());
        assertFalse(serverService.isLocalhostServerReady());
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerReady() {
        when(jobNodeStorage.getJobNodeChildrenKeys("instances")).thenReturn(Collections.singletonList("127.0.0.1@-@0"));
        when(jobNodeStorage.isJobNodeExisted("instances/127.0.0.1@-@0")).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly("instances/127.0.0.1@-@0")).thenReturn(InstanceStatus.READY.name());
        when(jobNodeStorage.getJobNodeData("servers/127.0.0.1")).thenReturn("");
        assertTrue(serverService.isLocalhostServerReady());
    }
    
    @Test
    public void assertIsServerEnabled() {
        when(jobNodeStorage.getJobNodeData("servers/127.0.0.1")).thenReturn("");
        assertTrue(serverService.isServerEnabled("127.0.0.1"));
    }
}
