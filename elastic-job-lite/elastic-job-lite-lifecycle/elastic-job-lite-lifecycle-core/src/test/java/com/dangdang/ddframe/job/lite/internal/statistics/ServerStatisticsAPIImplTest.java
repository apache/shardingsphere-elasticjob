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

package com.dangdang.ddframe.job.lite.internal.statistics;

import com.dangdang.ddframe.job.lite.api.ServerStatisticsAPI;
import com.dangdang.ddframe.job.lite.domain.ServerBriefInfo;
import com.dangdang.ddframe.job.lite.domain.ServerInfo;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public final class ServerStatisticsAPIImplTest {
    
    private ServerStatisticsAPI serverStatisticsAPI;
    
    @Mock
    private CoordinatorRegistryCenter registryCenter;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        serverStatisticsAPI = new ServerStatisticsAPIImpl(registryCenter);
    }
    
    @Test
    public void assertGetAllServersBriefInfo() {
        when(registryCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("testJob1", "testJob2"));
        when(registryCenter.getChildrenKeys("/testJob1/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        when(registryCenter.getChildrenKeys("/testJob2/servers")).thenReturn(Arrays.asList("ip3", "ip4"));
        when(registryCenter.get("/testJob1/servers/ip1/hostName")).thenReturn("host1");
        when(registryCenter.get("/testJob1/servers/ip2/hostName")).thenReturn("host2");
        when(registryCenter.get("/testJob2/servers/ip3/hostName")).thenReturn("host3");
        when(registryCenter.get("/testJob2/servers/ip4/hostName")).thenReturn("host4");
        when(registryCenter.isExisted("/testJob1/servers/ip1/shutdown")).thenReturn(false);
        when(registryCenter.isExisted("/testJob1/servers/ip1/status")).thenReturn(true);
        when(registryCenter.isExisted("/testJob1/servers/ip2/shutdown")).thenReturn(true);
        when(registryCenter.isExisted("/testJob2/servers/ip3/shutdown")).thenReturn(false);
        when(registryCenter.isExisted("/testJob2/servers/ip3/status")).thenReturn(false);
        when(registryCenter.isExisted("/testJob2/servers/ip4/shutdown")).thenReturn(false);
        when(registryCenter.isExisted("/testJob2/servers/ip4/status")).thenReturn(true);
        int i = 0;
        for (ServerBriefInfo each : serverStatisticsAPI.getAllServersBriefInfo()) {
            i++;
            assertThat(each.getServerIp(), is("ip" + i));
            assertThat(each.getServerHostName(), is("host" + i));
            switch (i) {
                case 1:
                    assertThat(each.getStatus(), is(ServerBriefInfo.ServerBriefStatus.OK));
                    break;
                case 2:
                    assertThat(each.getStatus(), is(ServerBriefInfo.ServerBriefStatus.ALL_CRASHED));
                    break;
                case 3:
                    assertThat(each.getStatus(), is(ServerBriefInfo.ServerBriefStatus.ALL_CRASHED));
                    break;
                case 4:
                    assertThat(each.getStatus(), is(ServerBriefInfo.ServerBriefStatus.OK));
                    break;
                default:
                    fail();
            }
        }
    }
    
    @Test
    public void assertGetJobs() {
        when(registryCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("testJob1", "testJob2", "testJob3"));
        when(registryCenter.isExisted("/testJob1/servers/localhost")).thenReturn(true);
        when(registryCenter.isExisted("/testJob2/servers/localhost")).thenReturn(true);
        when(registryCenter.isExisted("/testJob3/servers/localhost")).thenReturn(false);
        when(registryCenter.get("/testJob1/servers/localhost/hostName")).thenReturn("localhost");
        when(registryCenter.get("/testJob2/servers/localhost/hostName")).thenReturn("localhost");
        when(registryCenter.get("/testJob1/servers/localhost/processSuccessCount")).thenReturn("100");
        when(registryCenter.get("/testJob2/servers/localhost/processSuccessCount")).thenReturn(null);
        when(registryCenter.get("/testJob1/servers/localhost/processFailureCount")).thenReturn("10");
        when(registryCenter.get("/testJob2/servers/localhost/processFailureCount")).thenReturn(null);
        when(registryCenter.get("/testJob1/servers/localhost/sharding")).thenReturn("0,1");
        when(registryCenter.get("/testJob2/servers/localhost/sharding")).thenReturn("2");
        when(registryCenter.get("/testJob1/servers/localhost/status")).thenReturn("RUNNING");
        when(registryCenter.get("/testJob2/servers/localhost/status")).thenReturn("RUNNING");
        when(registryCenter.isExisted("/testJob1/servers/localhost/disabled")).thenReturn(false);
        when(registryCenter.isExisted("/testJob2/servers/localhost/disabled")).thenReturn(false);
        when(registryCenter.isExisted("/testJob1/servers/localhost/paused")).thenReturn(false);
        when(registryCenter.isExisted("/testJob2/servers/localhost/paused")).thenReturn(false);
        when(registryCenter.isExisted("/testJob1/servers/localhost/shutdown")).thenReturn(false);
        when(registryCenter.isExisted("/testJob2/servers/localhost/shutdown")).thenReturn(false);
        int i = 0;
        for (ServerInfo each : serverStatisticsAPI.getJobs("localhost")) {
            i++;
            assertThat(each.getJobName(), is("testJob" + i));
            assertThat(each.getIp(), is("localhost"));
            assertThat(each.getHostName(), is("localhost"));
            assertThat(each.getStatus(), is(ServerInfo.ServerStatus.RUNNING));
            switch (i) {
                case 1:
                    assertThat(each.getProcessSuccessCount(), is(100));
                    assertThat(each.getProcessFailureCount(), is(10));
                    assertThat(each.getSharding(), is("0,1"));
                    break;
                case 2:
                    assertThat(each.getProcessSuccessCount(), is(0));
                    assertThat(each.getProcessFailureCount(), is(0));
                    assertThat(each.getSharding(), is("2"));
                    break;
                default:
                    fail();
            }
        }
    }
}
