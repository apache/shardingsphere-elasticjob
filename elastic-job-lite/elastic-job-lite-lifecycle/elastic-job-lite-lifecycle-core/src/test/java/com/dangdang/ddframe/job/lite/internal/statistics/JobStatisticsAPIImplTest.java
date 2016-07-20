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

import com.dangdang.ddframe.job.lite.api.JobStatisticsAPI;
import com.dangdang.ddframe.job.lite.domain.ExecutionInfo;
import com.dangdang.ddframe.job.lite.domain.JobBriefInfo;
import com.dangdang.ddframe.job.lite.domain.ServerInfo;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public final class JobStatisticsAPIImplTest {
    
    private JobStatisticsAPI jobStatisticsAPI;
    
    @Mock
    private CoordinatorRegistryCenter registryCenter;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        jobStatisticsAPI = new JobStatisticsAPIImpl(registryCenter);
    }
    
    @Test
    public void assertGetAllJobsBriefInfo() {
        when(registryCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("testJob1", "testJob2"));
        when(registryCenter.get("/testJob1/config/description")).thenReturn("description1");
        when(registryCenter.get("/testJob2/config/description")).thenReturn("description2");
        when(registryCenter.get("/testJob1/config/cron")).thenReturn("0/1 * * * * *");
        when(registryCenter.get("/testJob2/config/cron")).thenReturn("0/2 * * * * *");
        when(registryCenter.getChildrenKeys("/testJob1/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        when(registryCenter.getChildrenKeys("/testJob2/servers")).thenReturn(Arrays.asList("ip3", "ip4"));
        when(registryCenter.get("/testJob1/servers/ip1/status")).thenReturn("RUNNING");
        when(registryCenter.get("/testJob1/servers/ip2/status")).thenReturn("READY");
        when(registryCenter.isExisted("/testJob1/servers/ip2/disabled")).thenReturn(true);
        when(registryCenter.isExisted("/testJob2/servers/ip3/paused")).thenReturn(true);
        when(registryCenter.isExisted("/testJob2/servers/ip4/shutdown")).thenReturn(true);
        int i = 0;
        for (JobBriefInfo each : jobStatisticsAPI.getAllJobsBriefInfo()) {
            i++;
            assertThat(each.getJobName(), is("testJob" + i));
            assertThat(each.getDescription(), is("description" + i));
            switch (i) {
                case 1:
                    assertThat(each.getCron(), is("0/1 * * * * *"));
                    assertThat(each.getStatus(), is(JobBriefInfo.JobStatus.DISABLED));
                    break;
                case 2:
                    assertThat(each.getCron(), is("0/2 * * * * *"));
                    assertThat(each.getStatus(), is(JobBriefInfo.JobStatus.ALL_CRASHED));
                    break;
                default:
                    fail();
            }
        }
    }
    
    @Test
    public void assertGetServers() {
        when(registryCenter.getChildrenKeys("/testJob/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        when(registryCenter.get("/testJob/servers/ip1/hostName")).thenReturn("host1");
        when(registryCenter.get("/testJob/servers/ip2/hostName")).thenReturn("host2");
        when(registryCenter.get("/testJob/servers/ip1/processSuccessCount")).thenReturn("101");
        when(registryCenter.get("/testJob/servers/ip2/processSuccessCount")).thenReturn("102");
        when(registryCenter.get("/testJob/servers/ip1/processFailureCount")).thenReturn("11");
        when(registryCenter.get("/testJob/servers/ip2/processFailureCount")).thenReturn("12");
        when(registryCenter.get("/testJob/servers/ip1/sharding")).thenReturn("0,1");
        when(registryCenter.get("/testJob/servers/ip2/sharding")).thenReturn("2,3");
        when(registryCenter.get("/testJob/servers/ip1/status")).thenReturn("RUNNING");
        when(registryCenter.get("/testJob/servers/ip2/status")).thenReturn("READY");
        int i = 0;
        for (ServerInfo each : jobStatisticsAPI.getServers("testJob")) {
            i++;
            assertThat(each.getJobName(), is("testJob"));
            assertThat(each.getIp(), is("ip" + i));
            assertThat(each.getHostName(), is("host" + i));
            switch (i) {
                case 1:
                    assertThat(each.getProcessSuccessCount(), is(101));
                    assertThat(each.getProcessFailureCount(), is(11));
                    assertThat(each.getStatus(), is(ServerInfo.ServerStatus.RUNNING));
                    break;
                case 2:
                    assertThat(each.getProcessSuccessCount(), is(102));
                    assertThat(each.getProcessFailureCount(), is(12));
                    assertThat(each.getStatus(), is(ServerInfo.ServerStatus.READY));
                    break;
                default:
                    fail();
            }
        }
    }
    
    @Test
    public void assertGetExecutionInfoWithoutMonitorExecution() {
        when(registryCenter.isExisted("/testJob/execution")).thenReturn(false);
        assertTrue(jobStatisticsAPI.getExecutionInfo("testJob").isEmpty());
    }
    
    @Test
    public void assertGetExecutionInfoWithMonitorExecution() {
        when(registryCenter.isExisted("/testJob/execution")).thenReturn(true);
        when(registryCenter.getChildrenKeys("/testJob/execution")).thenReturn(Arrays.asList("0", "1", "2"));
        when(registryCenter.isExisted("/testJob/execution/0/running")).thenReturn(true);
        when(registryCenter.isExisted("/testJob/execution/1/running")).thenReturn(false);
        when(registryCenter.isExisted("/testJob/execution/1/completed")).thenReturn(true);
        when(registryCenter.isExisted("/testJob/execution/2/running")).thenReturn(false);
        when(registryCenter.isExisted("/testJob/execution/2/completed")).thenReturn(false);
        when(registryCenter.isExisted("/testJob/execution/0/failover")).thenReturn(false);
        when(registryCenter.isExisted("/testJob/execution/1/failover")).thenReturn(false);
        when(registryCenter.isExisted("/testJob/execution/2/failover")).thenReturn(true);
        when(registryCenter.get("/testJob/execution/2/failover")).thenReturn("ip0");
        when(registryCenter.get("/testJob/execution/0/lastBeginTime")).thenReturn("0");
        when(registryCenter.get("/testJob/execution/1/lastBeginTime")).thenReturn("0");
        when(registryCenter.get("/testJob/execution/2/lastBeginTime")).thenReturn(null);
        when(registryCenter.get("/testJob/execution/0/nextFireTime")).thenReturn("0");
        when(registryCenter.get("/testJob/execution/1/nextFireTime")).thenReturn("0");
        when(registryCenter.get("/testJob/execution/2/nextFireTime")).thenReturn(null);
        when(registryCenter.get("/testJob/execution/0/lastCompleteTime")).thenReturn("0");
        when(registryCenter.get("/testJob/execution/1/lastCompleteTime")).thenReturn("0");
        when(registryCenter.get("/testJob/execution/2/lastCompleteTime")).thenReturn(null);
        int i = 0;
        for (ExecutionInfo each : jobStatisticsAPI.getExecutionInfo("testJob")) {
            i++;
            assertThat(each.getItem(), is(i - 1));
            switch (i) {
                case 1:
                    assertNull(each.getFailoverIp());
                    assertThat(each.getLastBeginTime(), is(new Date(0L)));
                    assertThat(each.getNextFireTime(), is(new Date(0L)));
                    assertThat(each.getLastCompleteTime(), is(new Date(0L)));
                    assertThat(each.getStatus(), is(ExecutionInfo.ExecutionStatus.RUNNING));
                    break;
                case 2:
                    assertNull(each.getFailoverIp());
                    assertThat(each.getLastBeginTime(), is(new Date(0L)));
                    assertThat(each.getNextFireTime(), is(new Date(0L)));
                    assertThat(each.getLastCompleteTime(), is(new Date(0L)));
                    assertThat(each.getStatus(), is(ExecutionInfo.ExecutionStatus.COMPLETED));
                    break;
                case 3:
                    assertThat(each.getFailoverIp(), is("ip0"));
                    assertNull(each.getLastBeginTime());
                    assertNull(each.getNextFireTime());
                    assertNull(each.getLastCompleteTime());
                    assertThat(each.getStatus(), is(ExecutionInfo.ExecutionStatus.PENDING));
                    break;
                default:
                    fail();
            }
        }
    }
}
