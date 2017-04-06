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

package com.dangdang.ddframe.job.lite.lifecycle.internal.operate;

import com.dangdang.ddframe.job.lite.lifecycle.api.JobOperateAPI;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class JobOperateAPIImplTest {
    
    private JobOperateAPI jobOperateAPI;
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        jobOperateAPI = new JobOperateAPIImpl(regCenter);
    }
    
    @Test
    public void assertTriggerWithJobName() {
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        when(regCenter.getChildrenKeys("/test_job/instances")).thenReturn(Arrays.asList("ip1@-@defaultInstance", "ip2@-@defaultInstance"));
        jobOperateAPI.trigger(Optional.of("test_job"), Optional.<String>absent());
        verify(regCenter).getChildrenKeys("/test_job/instances");
        verify(regCenter).persist("/test_job/instances/ip1@-@defaultInstance", "TRIGGER");
        verify(regCenter).persist("/test_job/instances/ip2@-@defaultInstance", "TRIGGER");
    }
    
    @Test
    public void assertDisableWithJobNameAndServerIp() {
        jobOperateAPI.disable(Optional.of("test_job"), Optional.of("localhost"));
        verify(regCenter).persist("/test_job/servers/localhost", "DISABLED");
    }

    @Test
    public void assertDisableWithJobName() {
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        jobOperateAPI.disable(Optional.of("test_job"), Optional.<String>absent());
        verify(regCenter).getChildrenKeys("/test_job/servers");
        verify(regCenter).persist("/test_job/servers/ip1", "DISABLED");
        verify(regCenter).persist("/test_job/servers/ip2", "DISABLED");
    }

    @Test
    public void assertDisableWithServerIp() {
        when(regCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("test_job1", "test_job2"));
        when(regCenter.isExisted("/test_job1/servers/localhost")).thenReturn(true);
        when(regCenter.isExisted("/test_job2/servers/localhost")).thenReturn(true);
        jobOperateAPI.disable(Optional.<String>absent(), Optional.of("localhost"));
        verify(regCenter).getChildrenKeys("/");
        verify(regCenter).persist("/test_job1/servers/localhost", "DISABLED");
        verify(regCenter).persist("/test_job2/servers/localhost", "DISABLED");
    }

    @Test
    public void assertEnableWithJobNameAndServerIp() {
        jobOperateAPI.enable(Optional.of("test_job"), Optional.of("localhost"));
        verify(regCenter).persist("/test_job/servers/localhost", "");
    }
    
    @Test
    public void assertEnableWithJobName() {
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        jobOperateAPI.enable(Optional.of("test_job"), Optional.<String>absent());
        verify(regCenter).getChildrenKeys("/test_job/servers");
        verify(regCenter).persist("/test_job/servers/ip1", "");
        verify(regCenter).persist("/test_job/servers/ip2", "");
    }

    @Test
    public void assertEnableWithServerIp() {
        when(regCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("test_job1", "test_job2"));
        when(regCenter.isExisted("/test_job1/servers/localhost")).thenReturn(true);
        when(regCenter.isExisted("/test_job2/servers/localhost")).thenReturn(true);
        jobOperateAPI.enable(Optional.<String>absent(), Optional.of("localhost"));
        verify(regCenter).getChildrenKeys("/");
        verify(regCenter).persist("/test_job1/servers/localhost", "");
        verify(regCenter).persist("/test_job2/servers/localhost", "");
    }

    @Test
    public void assertShutdownWithJobNameAndServerIp() {
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("localhost"));
        when(regCenter.getChildrenKeys("/test_job/instances")).thenReturn(Arrays.asList("localhost@-@defaultInstance"));
        jobOperateAPI.shutdown(Optional.of("test_job"), Optional.of("localhost"));
        verify(regCenter).remove("/test_job/instances/localhost@-@defaultInstance");
    }

    @Test
    public void assertShutdownWithJobName() {
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        when(regCenter.getChildrenKeys("/test_job/instances")).thenReturn(Arrays.asList("ip1@-@defaultInstance", "ip2@-@defaultInstance"));
        jobOperateAPI.shutdown(Optional.of("test_job"), Optional.<String>absent());
        verify(regCenter).getChildrenKeys("/test_job/instances");
        verify(regCenter).remove("/test_job/instances/ip1@-@defaultInstance");
    }

    @Test
    public void assertShutdownWithServerIp() {
        when(regCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("test_job1", "test_job2"));
        when(regCenter.getChildrenKeys("/test_job1/instances")).thenReturn(Arrays.asList("localhost@-@defaultInstance"));
        when(regCenter.getChildrenKeys("/test_job2/instances")).thenReturn(Arrays.asList("localhost@-@defaultInstance"));
        jobOperateAPI.shutdown(Optional.<String>absent(), Optional.of("localhost"));
        verify(regCenter).getChildrenKeys("/");
        verify(regCenter).remove("/test_job1/instances/localhost@-@defaultInstance");
        verify(regCenter).remove("/test_job2/instances/localhost@-@defaultInstance");
    }

    @Test
    public void assertRemoveWithJobNameAndServerIp() {
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        jobOperateAPI.remove(Optional.of("test_job"), Optional.of("ip1"));
        verify(regCenter).remove("/test_job/servers/ip1");
        assertFalse(regCenter.isExisted("/test_job/servers/ip1"));
    }

    @Test
    public void assertRemoveWithJobName() {
        when(regCenter.isExisted("/test_job")).thenReturn(true);
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        jobOperateAPI.remove(Optional.of("test_job"), Optional.<String>absent());
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
        when(regCenter.getChildrenKeys("/test_job1/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        when(regCenter.getChildrenKeys("/test_job2/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        jobOperateAPI.remove(Optional.<String>absent(), Optional.of("ip1"));
        assertFalse(regCenter.isExisted("/test_job1/servers/ip1"));
        assertFalse(regCenter.isExisted("/test_job2/servers/ip1"));
    }
}
