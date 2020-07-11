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

package org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.operate;

import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobOperateAPI;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class JobOperateAPIImplTest {
    
    private JobOperateAPI jobOperateAPI;
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Before
    public void setUp() {
        jobOperateAPI = new JobOperateAPIImpl(regCenter);
    }
    
    @Test
    public void assertTriggerWithJobName() {
        when(regCenter.getChildrenKeys("/test_job/instances")).thenReturn(Arrays.asList("ip1@-@defaultInstance", "ip2@-@defaultInstance"));
        jobOperateAPI.trigger("test_job");
        verify(regCenter).getChildrenKeys("/test_job/instances");
        verify(regCenter).persist("/test_job/instances/ip1@-@defaultInstance", "TRIGGER");
        verify(regCenter).persist("/test_job/instances/ip2@-@defaultInstance", "TRIGGER");
    }
    
    @Test
    public void assertDisableWithJobNameAndServerIp() {
        jobOperateAPI.disable("test_job", "localhost");
        verify(regCenter).persist("/test_job/servers/localhost", "DISABLED");
    }
    
    @Test
    public void assertDisableWithJobName() {
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        jobOperateAPI.disable("test_job", null);
        verify(regCenter).getChildrenKeys("/test_job/servers");
        verify(regCenter).persist("/test_job/servers/ip1", "DISABLED");
        verify(regCenter).persist("/test_job/servers/ip2", "DISABLED");
    }
    
    @Test
    public void assertDisableWithServerIp() {
        when(regCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("test_job1", "test_job2"));
        when(regCenter.isExisted("/test_job1/servers/localhost")).thenReturn(true);
        when(regCenter.isExisted("/test_job2/servers/localhost")).thenReturn(true);
        jobOperateAPI.disable(null, "localhost");
        verify(regCenter).getChildrenKeys("/");
        verify(regCenter).persist("/test_job1/servers/localhost", "DISABLED");
        verify(regCenter).persist("/test_job2/servers/localhost", "DISABLED");
    }
    
    @Test
    public void assertEnableWithJobNameAndServerIp() {
        jobOperateAPI.enable("test_job", "localhost");
        verify(regCenter).persist("/test_job/servers/localhost", "ENABLED");
    }
    
    @Test
    public void assertEnableWithJobName() {
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        jobOperateAPI.enable("test_job", null);
        verify(regCenter).getChildrenKeys("/test_job/servers");
        verify(regCenter).persist("/test_job/servers/ip1", "ENABLED");
        verify(regCenter).persist("/test_job/servers/ip2", "ENABLED");
    }
    
    @Test
    public void assertEnableWithServerIp() {
        when(regCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("test_job1", "test_job2"));
        when(regCenter.isExisted("/test_job1/servers/localhost")).thenReturn(true);
        when(regCenter.isExisted("/test_job2/servers/localhost")).thenReturn(true);
        jobOperateAPI.enable(null, "localhost");
        verify(regCenter).getChildrenKeys("/");
        verify(regCenter).persist("/test_job1/servers/localhost", "ENABLED");
        verify(regCenter).persist("/test_job2/servers/localhost", "ENABLED");
    }
    
    @Test
    public void assertShutdownWithJobNameAndServerIp() {
        when(regCenter.getChildrenKeys("/test_job/instances")).thenReturn(Collections.singletonList("localhost@-@defaultInstance"));
        jobOperateAPI.shutdown("test_job", "localhost");
        verify(regCenter).remove("/test_job/instances/localhost@-@defaultInstance");
    }
    
    @Test
    public void assertShutdownWithJobName() {
        when(regCenter.getChildrenKeys("/test_job/instances")).thenReturn(Arrays.asList("ip1@-@defaultInstance", "ip2@-@defaultInstance"));
        jobOperateAPI.shutdown("test_job", null);
        verify(regCenter).getChildrenKeys("/test_job/instances");
        verify(regCenter).remove("/test_job/instances/ip1@-@defaultInstance");
    }
    
    @Test
    public void assertShutdownWithServerIp() {
        when(regCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("test_job1", "test_job2"));
        when(regCenter.getChildrenKeys("/test_job1/instances")).thenReturn(Collections.singletonList("localhost@-@defaultInstance"));
        when(regCenter.getChildrenKeys("/test_job2/instances")).thenReturn(Collections.singletonList("localhost@-@defaultInstance"));
        jobOperateAPI.shutdown(null, "localhost");
        verify(regCenter).getChildrenKeys("/");
        verify(regCenter).remove("/test_job1/instances/localhost@-@defaultInstance");
        verify(regCenter).remove("/test_job2/instances/localhost@-@defaultInstance");
    }
    
    @Test
    public void assertRemoveWithJobNameAndServerIp() {
        jobOperateAPI.remove("test_job", "ip1");
        verify(regCenter).remove("/test_job/servers/ip1");
        assertFalse(regCenter.isExisted("/test_job/servers/ip1"));
    }
    
    @Test
    public void assertRemoveWithJobName() {
        when(regCenter.isExisted("/test_job")).thenReturn(true);
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        jobOperateAPI.remove("test_job", null);
        verify(regCenter).getChildrenKeys("/test_job/servers");
        verify(regCenter).remove("/test_job/servers/ip1");
        verify(regCenter).remove("/test_job/servers/ip2");
        assertFalse(regCenter.isExisted("/test_job/servers/ip1"));
        assertFalse(regCenter.isExisted("/test_job/servers/ip2"));
        assertTrue(regCenter.isExisted("/test_job"));
    }
    
    @Test
    public void assertRemoveWithServerIp() {
        when(regCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("test_job1", "test_job2"));
        jobOperateAPI.remove(null, "ip1");
        assertFalse(regCenter.isExisted("/test_job1/servers/ip1"));
        assertFalse(regCenter.isExisted("/test_job2/servers/ip1"));
    }
}
