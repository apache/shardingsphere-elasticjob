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

package com.dangdang.ddframe.job.lite.lifecycle.internal.statistics;

import com.dangdang.ddframe.job.lite.lifecycle.api.ServerStatisticsAPI;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ServerBriefInfo;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ServerInfo;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
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
    private CoordinatorRegistryCenter regCenter;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        serverStatisticsAPI = new ServerStatisticsAPIImpl(regCenter);
    }
    
    @Test
    public void assertGetAllServersBriefInfo() {
        when(regCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("test_job1", "test_job2"));
        when(regCenter.getChildrenKeys("/test_job1/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        when(regCenter.getChildrenKeys("/test_job2/servers")).thenReturn(Arrays.asList("ip3", "ip4"));
        when(regCenter.get("/test_job1/servers/ip1/hostName")).thenReturn("host1");
        when(regCenter.get("/test_job1/servers/ip2/hostName")).thenReturn("host2");
        when(regCenter.get("/test_job2/servers/ip3/hostName")).thenReturn("host3");
        when(regCenter.get("/test_job2/servers/ip4/hostName")).thenReturn("host4");
        when(regCenter.isExisted("/test_job1/servers/ip1/shutdown")).thenReturn(false);
        when(regCenter.isExisted("/test_job1/servers/ip1/status")).thenReturn(true);
        when(regCenter.isExisted("/test_job1/servers/ip2/shutdown")).thenReturn(true);
        when(regCenter.isExisted("/test_job2/servers/ip3/shutdown")).thenReturn(false);
        when(regCenter.isExisted("/test_job2/servers/ip3/status")).thenReturn(false);
        when(regCenter.isExisted("/test_job2/servers/ip4/shutdown")).thenReturn(false);
        when(regCenter.isExisted("/test_job2/servers/ip4/status")).thenReturn(true);
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
        when(regCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("test_job1", "test_job2", "test_job3"));
        when(regCenter.isExisted("/test_job1/servers/localhost")).thenReturn(true);
        when(regCenter.isExisted("/test_job2/servers/localhost")).thenReturn(true);
        when(regCenter.isExisted("/test_job3/servers/localhost")).thenReturn(false);
        when(regCenter.get("/test_job1/servers/localhost/hostName")).thenReturn("localhost");
        when(regCenter.get("/test_job2/servers/localhost/hostName")).thenReturn("localhost");
        when(regCenter.get("/test_job1/servers/localhost/sharding")).thenReturn("0,1");
        when(regCenter.get("/test_job2/servers/localhost/sharding")).thenReturn("2");
        when(regCenter.get("/test_job1/servers/localhost/status")).thenReturn("RUNNING");
        when(regCenter.get("/test_job2/servers/localhost/status")).thenReturn("RUNNING");
        when(regCenter.isExisted("/test_job1/servers/localhost/disabled")).thenReturn(false);
        when(regCenter.isExisted("/test_job2/servers/localhost/disabled")).thenReturn(false);
        when(regCenter.isExisted("/test_job1/servers/localhost/paused")).thenReturn(false);
        when(regCenter.isExisted("/test_job2/servers/localhost/paused")).thenReturn(false);
        when(regCenter.isExisted("/test_job1/servers/localhost/shutdown")).thenReturn(false);
        when(regCenter.isExisted("/test_job2/servers/localhost/shutdown")).thenReturn(false);
        int i = 0;
        for (ServerInfo each : serverStatisticsAPI.getJobs("localhost")) {
            i++;
            assertThat(each.getJobName(), is("test_job" + i));
            assertThat(each.getIp(), is("localhost"));
            assertThat(each.getHostName(), is("localhost"));
            assertThat(each.getStatus(), is(ServerInfo.ServerStatus.RUNNING));
            switch (i) {
                case 1:
                    assertThat(each.getSharding(), is("0,1"));
                    break;
                case 2:
                    assertThat(each.getSharding(), is("2"));
                    break;
                default:
                    fail();
            }
        }
    }
}
