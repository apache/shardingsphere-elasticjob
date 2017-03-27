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

import com.dangdang.ddframe.job.util.env.LocalHostService;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ServerOperationNodeTest {
    
    private LocalHostService localHostService = new LocalHostService();
    
    private ServerOperationNode serverOperationNode = new ServerOperationNode("test_job");
    
    @Test
    public void assertGetDisabledNode() {
        assertThat(serverOperationNode.getDisabledNode("host0"), is("servers/host0/operation/disabled"));
    }
    
    @Test
    public void assertShutdownNode() {
        assertThat(serverOperationNode.getShutdownNode("host0"), is("servers/host0/operation/shutdown"));
    }
    
    @Test
    public void assertIsLocalJobShutdownPath() {
        assertTrue(serverOperationNode.isLocalJobShutdownPath("/test_job/servers/" + localHostService.getIp() + "/operation/shutdown"));
    }
    
    @Test
    public void assertIsLocalServerDisabledPath() {
        assertTrue(serverOperationNode.isLocalServerDisabledPath("/test_job/servers/" + localHostService.getIp() + "/operation/disabled"));
    }
    
    @Test
    public void assertIsServerDisabledPath() {
        assertTrue(serverOperationNode.isServerDisabledPath("/test_job/servers/host0/operation/disabled"));
        assertFalse(serverOperationNode.isServerDisabledPath("/otherJob/servers/host0/operation/status"));
        assertFalse(serverOperationNode.isServerDisabledPath("/test_job/servers/host0/operation/status"));
    }
    
    @Test
    public void assertIsServerShutdownPath() {
        assertTrue(serverOperationNode.isServerShutdownPath("/test_job/servers/host0/operation/shutdown"));
        assertFalse(serverOperationNode.isServerShutdownPath("/otherJob/servers/host0/operation/status"));
        assertFalse(serverOperationNode.isServerShutdownPath("/test_job/servers/host0/operation/status"));
    }
}
