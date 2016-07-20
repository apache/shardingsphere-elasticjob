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

package com.dangdang.ddframe.job.lite.internal.operate;

import com.dangdang.ddframe.job.lite.api.JobOperateAPI;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
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
    private CoordinatorRegistryCenter registryCenter;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        jobOperateAPI = new JobOperateAPIImpl(registryCenter);
    }
    
    @Test
    public void assertTriggerWithJobNameAndServerIp() {
        jobOperateAPI.trigger(Optional.of("testJob"), Optional.of("localhost"));
        verify(registryCenter).persist("/testJob/servers/localhost/trigger", "");
    }
    
    @Test
    public void assertTriggerWithJobName() {
        when(registryCenter.getChildrenKeys("/testJob/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        jobOperateAPI.trigger(Optional.of("testJob"), Optional.<String>absent());
        verify(registryCenter).getChildrenKeys("/testJob/servers");
        verify(registryCenter).persist("/testJob/servers/ip1/trigger", "");
        verify(registryCenter).persist("/testJob/servers/ip2/trigger", "");
    }
    
    @Test
    public void assertTriggerWithServerIp() {
        when(registryCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("testJob1", "testJob2"));
        jobOperateAPI.trigger(Optional.<String>absent(), Optional.of("localhost"));
        verify(registryCenter).getChildrenKeys("/");
        verify(registryCenter).persist("/testJob1/servers/localhost/trigger", "");
        verify(registryCenter).persist("/testJob2/servers/localhost/trigger", "");
    }
    
    @Test
    public void assertPauseWithJobNameAndServerIp() {
        jobOperateAPI.pause(Optional.of("testJob"), Optional.of("localhost"));
        verify(registryCenter).persist("/testJob/servers/localhost/paused", "");
    }
    
    @Test
    public void assertPauseWithJobName() {
        when(registryCenter.getChildrenKeys("/testJob/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        jobOperateAPI.pause(Optional.of("testJob"), Optional.<String>absent());
        verify(registryCenter).getChildrenKeys("/testJob/servers");
        verify(registryCenter).persist("/testJob/servers/ip1/paused", "");
        verify(registryCenter).persist("/testJob/servers/ip2/paused", "");
    }
    
    @Test
    public void assertPauseWithServerIp() {
        when(registryCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("testJob1", "testJob2"));
        jobOperateAPI.pause(Optional.<String>absent(), Optional.of("localhost"));
        verify(registryCenter).getChildrenKeys("/");
        verify(registryCenter).persist("/testJob1/servers/localhost/paused", "");
        verify(registryCenter).persist("/testJob2/servers/localhost/paused", "");
    }
    
    @Test
    public void assertResumeWithJobNameAndServerIp() {
        jobOperateAPI.resume(Optional.of("testJob"), Optional.of("localhost"));
        verify(registryCenter).remove("/testJob/servers/localhost/paused");
    }
    
    @Test
    public void assertResumeWithJobName() {
        when(registryCenter.getChildrenKeys("/testJob/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        jobOperateAPI.resume(Optional.of("testJob"), Optional.<String>absent());
        verify(registryCenter).getChildrenKeys("/testJob/servers");
        verify(registryCenter).remove("/testJob/servers/ip1/paused");
        verify(registryCenter).remove("/testJob/servers/ip2/paused");
    }
    
    @Test
    public void assertResumeWithServerIp() {
        when(registryCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("testJob1", "testJob2"));
        jobOperateAPI.resume(Optional.<String>absent(), Optional.of("localhost"));
        verify(registryCenter).getChildrenKeys("/");
        verify(registryCenter).remove("/testJob1/servers/localhost/paused");
        verify(registryCenter).remove("/testJob2/servers/localhost/paused");
    }
    
    @Test
    public void assertDisableWithJobNameAndServerIp() {
        jobOperateAPI.disable(Optional.of("testJob"), Optional.of("localhost"));
        verify(registryCenter).persist("/testJob/servers/localhost/disabled", "");
    }
    
    @Test
    public void assertDisableWithJobName() {
        when(registryCenter.getChildrenKeys("/testJob/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        jobOperateAPI.disable(Optional.of("testJob"), Optional.<String>absent());
        verify(registryCenter).getChildrenKeys("/testJob/servers");
        verify(registryCenter).persist("/testJob/servers/ip1/disabled", "");
        verify(registryCenter).persist("/testJob/servers/ip2/disabled", "");
    }
    
    @Test
    public void assertDisableWithServerIp() {
        when(registryCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("testJob1", "testJob2"));
        jobOperateAPI.disable(Optional.<String>absent(), Optional.of("localhost"));
        verify(registryCenter).getChildrenKeys("/");
        verify(registryCenter).persist("/testJob1/servers/localhost/disabled", "");
        verify(registryCenter).persist("/testJob2/servers/localhost/disabled", "");
    }
    
    @Test
    public void assertEnableWithJobNameAndServerIp() {
        jobOperateAPI.enable(Optional.of("testJob"), Optional.of("localhost"));
        verify(registryCenter).remove("/testJob/servers/localhost/disabled");
    }
    
    @Test
    public void assertEnableWithJobName() {
        when(registryCenter.getChildrenKeys("/testJob/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        jobOperateAPI.enable(Optional.of("testJob"), Optional.<String>absent());
        verify(registryCenter).getChildrenKeys("/testJob/servers");
        verify(registryCenter).remove("/testJob/servers/ip1/disabled");
        verify(registryCenter).remove("/testJob/servers/ip2/disabled");
    }
    
    @Test
    public void assertEnableWithServerIp() {
        when(registryCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("testJob1", "testJob2"));
        jobOperateAPI.enable(Optional.<String>absent(), Optional.of("localhost"));
        verify(registryCenter).getChildrenKeys("/");
        verify(registryCenter).remove("/testJob1/servers/localhost/disabled");
        verify(registryCenter).remove("/testJob2/servers/localhost/disabled");
    }
    
    @Test
    public void assertShutdownWithJobNameAndServerIp() {
        jobOperateAPI.shutdown(Optional.of("testJob"), Optional.of("localhost"));
        verify(registryCenter).persist("/testJob/servers/localhost/shutdown", "");
    }
    
    @Test
    public void assertShutdownWithJobName() {
        when(registryCenter.getChildrenKeys("/testJob/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        jobOperateAPI.shutdown(Optional.of("testJob"), Optional.<String>absent());
        verify(registryCenter).getChildrenKeys("/testJob/servers");
        verify(registryCenter).persist("/testJob/servers/ip1/shutdown", "");
        verify(registryCenter).persist("/testJob/servers/ip2/shutdown", "");
    }
    
    @Test
    public void assertShutdownWithServerIp() {
        when(registryCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("testJob1", "testJob2"));
        jobOperateAPI.shutdown(Optional.<String>absent(), Optional.of("localhost"));
        verify(registryCenter).getChildrenKeys("/");
        verify(registryCenter).persist("/testJob1/servers/localhost/shutdown", "");
        verify(registryCenter).persist("/testJob2/servers/localhost/shutdown", "");
    }
    
    @Test
    public void assertRemoveWithJobNameAndServerIpWhenJobStillAlive() {
        when(registryCenter.isExisted("/testJob/servers/localhost/status")).thenReturn(true);
        when(registryCenter.getChildrenKeys("/testJob/servers")).thenReturn(Collections.<String>emptyList());
        assertThat(jobOperateAPI.remove(Optional.of("testJob"), Optional.of("localhost")), Is.<Collection<String>>is(Collections.singletonList("localhost")));
        verify(registryCenter).isExisted("/testJob/servers/localhost/status");
    }
    
    @Test
    public void assertRemoveWithJobNameAndServerIpWhenIsLastInstance() {
        when(registryCenter.isExisted("/testJob/servers/localhost/status")).thenReturn(false);
        when(registryCenter.getChildrenKeys("/testJob/servers")).thenReturn(Collections.<String>emptyList());
        assertThat(jobOperateAPI.remove(Optional.of("testJob"), Optional.of("localhost")), Is.<Collection<String>>is(Collections.<String>emptyList()));
        verify(registryCenter).isExisted("/testJob/servers/localhost/status");
        verify(registryCenter).getChildrenKeys("/testJob/servers");
        verify(registryCenter).remove("/testJob/servers/localhost");
        verify(registryCenter).remove("/testJob");
    }
    
    @Test
    public void assertRemoveWithJobNameAndServerIpWhenIsNotLastInstance() {
        when(registryCenter.isExisted("/testJob/servers/localhost/status")).thenReturn(false);
        when(registryCenter.getChildrenKeys("/testJob/servers")).thenReturn(Collections.singletonList("other_host"));
        assertThat(jobOperateAPI.remove(Optional.of("testJob"), Optional.of("localhost")), Is.<Collection<String>>is(Collections.<String>emptyList()));
        verify(registryCenter).isExisted("/testJob/servers/localhost/status");
        verify(registryCenter).getChildrenKeys("/testJob/servers");
        verify(registryCenter).remove("/testJob/servers/localhost");
        verify(registryCenter, times(0)).remove("/testJob");
    }
    
    @Test
    public void assertRemoveWithJobName() {
        when(registryCenter.isExisted("/testJob/servers/ip1/status")).thenReturn(false);
        when(registryCenter.isExisted("/testJob/servers/ip2/status")).thenReturn(true);
        when(registryCenter.getChildrenKeys("/testJob/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        assertThat(jobOperateAPI.remove(Optional.of("testJob"), Optional.<String>absent()), Is.<Collection<String>>is(Collections.singletonList("ip2")));
        verify(registryCenter).isExisted("/testJob/servers/ip1/status");
        verify(registryCenter).isExisted("/testJob/servers/ip2/status");
        verify(registryCenter, times(2)).getChildrenKeys("/testJob/servers");
        verify(registryCenter).remove("/testJob/servers/ip1");
        verify(registryCenter, times(0)).remove("/testJob/servers/ip2");
    }
    
    @Test
    public void assertRemoveWithServerIp() {
        when(registryCenter.isExisted("/testJob1/servers/localhost/status")).thenReturn(false);
        when(registryCenter.isExisted("/testJob2/servers/localhost/status")).thenReturn(true);
        when(registryCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("testJob1", "testJob2"));
        assertThat(jobOperateAPI.remove(Optional.<String>absent(), Optional.of("localhost")), Is.<Collection<String>>is(Collections.singletonList("testJob2")));
        verify(registryCenter).getChildrenKeys("/");
        verify(registryCenter).isExisted("/testJob1/servers/localhost/status");
        verify(registryCenter).isExisted("/testJob2/servers/localhost/status");
        verify(registryCenter).remove("/testJob1/servers/localhost");
        verify(registryCenter, times(0)).remove("/testJob2/servers/localhost");
    }
}
