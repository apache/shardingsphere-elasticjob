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

import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.util.env.LocalHostService;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ServerNodeTest {
    
    private LocalHostService localHostService = new LocalHostService();
    
    private ServerNode serverNode = new ServerNode("test_job");
    
    @BeforeClass
    public static void setUp() {
        JobRegistry.getInstance().addJobInstanceId("test_job", "test_job_instance_id");
    }
    
    @Test
    public void assertGetHostNameNode() {
        assertThat(ServerNode.getHostNameNode("host0"), is("servers/host0"));
    }
    
    @Test
    public void assertGetStatusNode() {
        assertThat(serverNode.getStatusNode("host0"), is("servers/host0/test_job_instance_id/status"));
    }
    
    @Test
    public void assertGetTriggerNode() {
        assertThat(serverNode.getTriggerNode("host0"), is("servers/host0/test_job_instance_id/trigger"));
    }
    
    @Test
    public void assertGetDisabledNode() {
        assertThat(serverNode.getDisabledNode("host0"), is("servers/host0/test_job_instance_id/disabled"));
    }
    
    @Test
    public void assertPausedNode() {
        assertThat(serverNode.getPausedNode("host0"), is("servers/host0/test_job_instance_id/paused"));
    }
    
    @Test
    public void assertShutdownNode() {
        assertThat(serverNode.getShutdownNode("host0"), is("servers/host0/test_job_instance_id/shutdown"));
    }
    
    @Test
    public void assertIsLocalJobTriggerPath() {
        assertTrue(serverNode.isLocalJobTriggerPath("/test_job/servers/" + localHostService.getIp() + "/test_job_instance_id/trigger"));
    }
    
    @Test
    public void assertIsLocalJobPausedPath() {
        assertTrue(serverNode.isLocalJobPausedPath("/test_job/servers/" + localHostService.getIp() + "/test_job_instance_id/paused"));
    }
    
    @Test
    public void assertIsLocalJobShutdownPath() {
        assertTrue(serverNode.isLocalJobShutdownPath("/test_job/servers/" + localHostService.getIp() + "/test_job_instance_id/shutdown"));
    }
    
    @Test
    public void assertIsLocalServerDisabledPath() {
        assertTrue(serverNode.isLocalServerDisabledPath("/test_job/servers/" + localHostService.getIp() + "/test_job_instance_id/disabled"));
    }
    
    @Test
    public void assertIsServerStatusPath() {
        assertTrue(serverNode.isServerStatusPath("/test_job/servers/host0/status"));
        assertFalse(serverNode.isServerStatusPath("/otherJob/servers/host0/status"));
        assertFalse(serverNode.isServerStatusPath("/test_job/servers/host0/disabled"));
    }
    
    @Test
    public void assertIsServerDisabledPath() {
        assertTrue(serverNode.isServerDisabledPath("/test_job/servers/host0/disabled"));
        assertFalse(serverNode.isServerDisabledPath("/otherJob/servers/host0/status"));
        assertFalse(serverNode.isServerDisabledPath("/test_job/servers/host0/status"));
    }
    
    @Test
    public void assertIsServerShutdownPath() {
        assertTrue(serverNode.isServerShutdownPath("/test_job/servers/host0/shutdown"));
        assertFalse(serverNode.isServerShutdownPath("/otherJob/servers/host0/status"));
        assertFalse(serverNode.isServerShutdownPath("/test_job/servers/host0/status"));
    }
}
