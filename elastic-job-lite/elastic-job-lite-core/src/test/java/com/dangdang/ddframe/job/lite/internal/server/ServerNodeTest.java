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
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.util.ReflectionUtils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ServerNodeTest {
    
    private static ServerNode serverNode;
    
    @BeforeClass
    public static void setUp() throws NoSuchFieldException {
        JobRegistry.getInstance().addJobInstanceId("test_job", "test_job_instance_id");
        serverNode = new ServerNode("test_job");
        ReflectionUtils.setFieldValue(serverNode, "ip", "host0");
    }
    
    @Test
    public void assertGetStatusNode() {
        assertThat(serverNode.getStatusNode(), is("servers/host0/instances/test_job_instance_id/status"));
    }
    
    @Test
    public void assertGetTriggerNode() {
        assertThat(serverNode.getTriggerNode("host0"), is("servers/host0/instances/test_job_instance_id/trigger"));
    }
    
    @Test
    public void assertIsLocalJobTriggerPath() {
        assertTrue(serverNode.isLocalJobTriggerPath("/test_job/servers/host0/instances/test_job_instance_id/trigger"));
    }
    
    @Test
    public void assertIsServerStatusPath() {
        assertTrue(serverNode.isServerStatusPath("/test_job/servers/host0/instances/status"));
        assertFalse(serverNode.isServerStatusPath("/otherJob/servers/host0/instances/status"));
        assertFalse(serverNode.isServerStatusPath("/test_job/servers/host0/operation/disabled"));
    }
}
