/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.internal.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import org.junit.Test;

import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.env.RealLocalHostService;

public final class ServerNodeTest {
    
    private LocalHostService localHostService = new RealLocalHostService();
    
    private ServerNode serverNode = new ServerNode("testJob");
    
    @Test
    public void assertGetHostNameNode() {
        assertThat(ServerNode.getHostNameNode("host0"), is("servers/host0/hostName"));
    }
    
    @Test
    public void assertGetStatusNode() {
        assertThat(ServerNode.getStatusNode("host0"), is("servers/host0/status"));
    }
    
    @Test
    public void assertGetDisabledNode() {
        assertThat(ServerNode.getDisabledNode("host0"), is("servers/host0/disabled"));
    }
    
    @Test
    public void assertProcessSuccessCountNode() {
        assertThat(ServerNode.getProcessSuccessCountNode("host0"), is("servers/host0/processSuccessCount"));
    }
    
    @Test
    public void assertProcessFailureCountNode() {
        assertThat(ServerNode.getProcessFailureCountNode("host0"), is("servers/host0/processFailureCount"));
    }
    
    @Test
    public void assertStopedNode() {
        assertThat(ServerNode.getStopedNode("host0"), is("servers/host0/stoped"));
    }
    
    @Test
    public void assertIsServerStatusPath() {
        assertTrue(serverNode.isServerStatusPath("/testJob/servers/host0/status"));
        assertFalse(serverNode.isServerStatusPath("/otherJob/servers/host0/status"));
        assertFalse(serverNode.isServerStatusPath("/testJob/servers/host0/disabled"));
    }
    
    @Test
    public void assertIsServerDisabledPath() {
        assertTrue(serverNode.isServerDisabledPath("/testJob/servers/host0/disabled"));
        assertFalse(serverNode.isServerDisabledPath("/otherJob/servers/host0/status"));
        assertFalse(serverNode.isServerDisabledPath("/testJob/servers/host0/status"));
    }
    
    @Test
    public void assertIsJobStopedPath() {
        assertTrue(serverNode.isJobStopedPath("/testJob/servers/" + localHostService.getIp() + "/stoped"));
    }
}
