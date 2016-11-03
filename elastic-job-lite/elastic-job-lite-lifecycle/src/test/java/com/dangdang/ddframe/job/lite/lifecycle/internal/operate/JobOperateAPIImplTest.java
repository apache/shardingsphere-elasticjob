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
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
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
    public void assertTriggerWithJobNameAndServerIp() {
        jobOperateAPI.trigger(Optional.of("test_job"), Optional.of("localhost"));
        verify(regCenter).persist("/test_job/servers/localhost/trigger", "");
    }
    
    @Test
    public void assertTriggerWithJobName() {
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        jobOperateAPI.trigger(Optional.of("test_job"), Optional.<String>absent());
        verify(regCenter).getChildrenKeys("/test_job/servers");
        verify(regCenter).persist("/test_job/servers/ip1/trigger", "");
        verify(regCenter).persist("/test_job/servers/ip2/trigger", "");
    }
    
    @Test
    public void assertTriggerWithServerIp() {
        when(regCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("test_job1", "test_job2"));
        jobOperateAPI.trigger(Optional.<String>absent(), Optional.of("localhost"));
        verify(regCenter).getChildrenKeys("/");
        verify(regCenter).persist("/test_job1/servers/localhost/trigger", "");
        verify(regCenter).persist("/test_job2/servers/localhost/trigger", "");
    }
    
    @Test
    public void assertPauseWithJobNameAndServerIp() {
        jobOperateAPI.pause(Optional.of("test_job"), Optional.of("localhost"));
        verify(regCenter).persist("/test_job/servers/localhost/paused", "");
    }
    
    @Test
    public void assertPauseWithJobName() {
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        jobOperateAPI.pause(Optional.of("test_job"), Optional.<String>absent());
        verify(regCenter).getChildrenKeys("/test_job/servers");
        verify(regCenter).persist("/test_job/servers/ip1/paused", "");
        verify(regCenter).persist("/test_job/servers/ip2/paused", "");
    }
    
    @Test
    public void assertPauseWithServerIp() {
        when(regCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("test_job1", "test_job2"));
        jobOperateAPI.pause(Optional.<String>absent(), Optional.of("localhost"));
        verify(regCenter).getChildrenKeys("/");
        verify(regCenter).persist("/test_job1/servers/localhost/paused", "");
        verify(regCenter).persist("/test_job2/servers/localhost/paused", "");
    }
    
    @Test
    public void assertResumeWithJobNameAndServerIp() {
        jobOperateAPI.resume(Optional.of("test_job"), Optional.of("localhost"));
        verify(regCenter).remove("/test_job/servers/localhost/paused");
    }
    
    @Test
    public void assertResumeWithJobName() {
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        jobOperateAPI.resume(Optional.of("test_job"), Optional.<String>absent());
        verify(regCenter).getChildrenKeys("/test_job/servers");
        verify(regCenter).remove("/test_job/servers/ip1/paused");
        verify(regCenter).remove("/test_job/servers/ip2/paused");
    }
    
    @Test
    public void assertResumeWithServerIp() {
        when(regCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("test_job1", "test_job2"));
        jobOperateAPI.resume(Optional.<String>absent(), Optional.of("localhost"));
        verify(regCenter).getChildrenKeys("/");
        verify(regCenter).remove("/test_job1/servers/localhost/paused");
        verify(regCenter).remove("/test_job2/servers/localhost/paused");
    }
    
    @Test
    public void assertDisableWithJobNameAndServerIp() {
        jobOperateAPI.disable(Optional.of("test_job"), Optional.of("localhost"));
        verify(regCenter).persist("/test_job/servers/localhost/disabled", "");
    }
    
    @Test
    public void assertDisableWithJobName() {
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        jobOperateAPI.disable(Optional.of("test_job"), Optional.<String>absent());
        verify(regCenter).getChildrenKeys("/test_job/servers");
        verify(regCenter).persist("/test_job/servers/ip1/disabled", "");
        verify(regCenter).persist("/test_job/servers/ip2/disabled", "");
    }
    
    @Test
    public void assertDisableWithServerIp() {
        when(regCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("test_job1", "test_job2"));
        jobOperateAPI.disable(Optional.<String>absent(), Optional.of("localhost"));
        verify(regCenter).getChildrenKeys("/");
        verify(regCenter).persist("/test_job1/servers/localhost/disabled", "");
        verify(regCenter).persist("/test_job2/servers/localhost/disabled", "");
    }
    
    @Test
    public void assertEnableWithJobNameAndServerIp() {
        jobOperateAPI.enable(Optional.of("test_job"), Optional.of("localhost"));
        verify(regCenter).remove("/test_job/servers/localhost/disabled");
    }
    
    @Test
    public void assertEnableWithJobName() {
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        jobOperateAPI.enable(Optional.of("test_job"), Optional.<String>absent());
        verify(regCenter).getChildrenKeys("/test_job/servers");
        verify(regCenter).remove("/test_job/servers/ip1/disabled");
        verify(regCenter).remove("/test_job/servers/ip2/disabled");
    }
    
    @Test
    public void assertEnableWithServerIp() {
        when(regCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("test_job1", "test_job2"));
        jobOperateAPI.enable(Optional.<String>absent(), Optional.of("localhost"));
        verify(regCenter).getChildrenKeys("/");
        verify(regCenter).remove("/test_job1/servers/localhost/disabled");
        verify(regCenter).remove("/test_job2/servers/localhost/disabled");
    }
    
    @Test
    public void assertShutdownWithJobNameAndServerIp() {
        jobOperateAPI.shutdown(Optional.of("test_job"), Optional.of("localhost"));
        verify(regCenter).persist("/test_job/servers/localhost/shutdown", "");
    }
    
    @Test
    public void assertShutdownWithJobName() {
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        jobOperateAPI.shutdown(Optional.of("test_job"), Optional.<String>absent());
        verify(regCenter).getChildrenKeys("/test_job/servers");
        verify(regCenter).persist("/test_job/servers/ip1/shutdown", "");
        verify(regCenter).persist("/test_job/servers/ip2/shutdown", "");
    }
    
    @Test
    public void assertShutdownWithServerIp() {
        when(regCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("test_job1", "test_job2"));
        jobOperateAPI.shutdown(Optional.<String>absent(), Optional.of("localhost"));
        verify(regCenter).getChildrenKeys("/");
        verify(regCenter).persist("/test_job1/servers/localhost/shutdown", "");
        verify(regCenter).persist("/test_job2/servers/localhost/shutdown", "");
    }
    
    @Test
    public void assertRemoveWithJobNameAndServerIpWhenJobStillAlive() {
        when(regCenter.isExisted("/test_job/servers/localhost/status")).thenReturn(true);
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Collections.<String>emptyList());
        assertThat(jobOperateAPI.remove(Optional.of("test_job"), Optional.of("localhost")), Is.<Collection<String>>is(Collections.singletonList("localhost")));
        verify(regCenter).isExisted("/test_job/servers/localhost/status");
    }
    
    @Test
    public void assertRemoveWithJobNameAndServerIpWhenIsLastInstance() {
        when(regCenter.isExisted("/test_job/servers/localhost/status")).thenReturn(false);
        when(regCenter.getNumChildren("/test_job/servers")).thenReturn(0);
        assertThat(jobOperateAPI.remove(Optional.of("test_job"), Optional.of("localhost")), Is.<Collection<String>>is(Collections.<String>emptyList()));
        verify(regCenter).isExisted("/test_job/servers/localhost/status");
        verify(regCenter).getNumChildren("/test_job/servers");
        verify(regCenter).remove("/test_job/servers/localhost");
        verify(regCenter).remove("/test_job");
    }
    
    @Test
    public void assertRemoveWithJobNameAndServerIpWhenIsNotLastInstance() {
        when(regCenter.isExisted("/test_job/servers/localhost/status")).thenReturn(false);
        when(regCenter.getNumChildren("/test_job/servers")).thenReturn(1);
        assertThat(jobOperateAPI.remove(Optional.of("test_job"), Optional.of("localhost")), Is.<Collection<String>>is(Collections.<String>emptyList()));
        verify(regCenter).isExisted("/test_job/servers/localhost/status");
        verify(regCenter).getNumChildren("/test_job/servers");
        verify(regCenter).remove("/test_job/servers/localhost");
        verify(regCenter, times(0)).remove("/test_job");
    }
    
    @Test
    public void assertRemoveWithJobName() {
        when(regCenter.isExisted("/test_job/servers/ip1/status")).thenReturn(false);
        when(regCenter.isExisted("/test_job/servers/ip2/status")).thenReturn(true);
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        assertThat(jobOperateAPI.remove(Optional.of("test_job"), Optional.<String>absent()), Is.<Collection<String>>is(Collections.singletonList("ip2")));
        verify(regCenter).isExisted("/test_job/servers/ip1/status");
        verify(regCenter).isExisted("/test_job/servers/ip2/status");
        verify(regCenter).getChildrenKeys("/test_job/servers");
        verify(regCenter).remove("/test_job/servers/ip1");
        verify(regCenter).getNumChildren("/test_job/servers");
        verify(regCenter, times(0)).remove("/test_job/servers/ip2");
    }
    
    @Test
    public void assertRemoveWithServerIp() {
        when(regCenter.isExisted("/test_job1/servers/localhost/status")).thenReturn(false);
        when(regCenter.isExisted("/test_job2/servers/localhost/status")).thenReturn(true);
        when(regCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("test_job1", "test_job2"));
        assertThat(jobOperateAPI.remove(Optional.<String>absent(), Optional.of("localhost")), Is.<Collection<String>>is(Collections.singletonList("test_job2")));
        verify(regCenter).getChildrenKeys("/");
        verify(regCenter).isExisted("/test_job1/servers/localhost/status");
        verify(regCenter).isExisted("/test_job2/servers/localhost/status");
        verify(regCenter).remove("/test_job1/servers/localhost");
        verify(regCenter, times(0)).remove("/test_job2/servers/localhost");
    }
    
    @Test
    public void assertRemoveWithJobNameWhenLeaderHostStillExisted() {
        when(regCenter.isExisted("/test_job/leader/election/host")).thenReturn(true);
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Collections.<String>emptyList());
        assertThat(jobOperateAPI.remove(Optional.of("test_job"),  Optional.of("localhost")), Is.<Collection<String>>is(Collections.singletonList("localhost")));
        verify(regCenter).isExisted("/test_job/leader/election/host");
        verify(regCenter).isExisted("/test_job/servers/localhost/status");
    }
}
