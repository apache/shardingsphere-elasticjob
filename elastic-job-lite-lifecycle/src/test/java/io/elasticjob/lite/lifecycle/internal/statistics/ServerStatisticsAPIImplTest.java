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

package io.elasticjob.lite.lifecycle.internal.statistics;

import io.elasticjob.lite.lifecycle.api.ServerStatisticsAPI;
import io.elasticjob.lite.lifecycle.domain.ServerBriefInfo;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;

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
    public void assertGetJobsTotalCount() {
        when(regCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("test_job_1", "test_job_2"));
        when(regCenter.getChildrenKeys("/test_job_1/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        when(regCenter.getChildrenKeys("/test_job_2/servers")).thenReturn(Arrays.asList("ip2", "ip3"));
        assertThat(serverStatisticsAPI.getServersTotalCount(), is(3));
    }
    
    @Test
    public void assertGetAllServersBriefInfo() {
        when(regCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("test_job1", "test_job2"));
        when(regCenter.getChildrenKeys("/test_job1/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        when(regCenter.getChildrenKeys("/test_job2/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        when(regCenter.get("/test_job1/servers/ip1")).thenReturn("DISABLED");
        when(regCenter.get("/test_job1/servers/ip2")).thenReturn("");
        when(regCenter.getChildrenKeys("/test_job1/instances")).thenReturn(Collections.singletonList("ip1@-@defaultInstance"));
        
        when(regCenter.get("/test_job2/servers/ip1")).thenReturn("DISABLED");
        when(regCenter.get("/test_job2/servers/ip2")).thenReturn("DISABLED");
        when(regCenter.getChildrenKeys("/test_job2/instances")).thenReturn(Arrays.asList("ip1@-@defaultInstance", "ip2@-@defaultInstance2"));
        
        int i = 0;
        for (ServerBriefInfo each : serverStatisticsAPI.getAllServersBriefInfo()) {
            i++;
            assertThat(each.getServerIp(), is("ip" + i));
            switch (i) {
                case 1:
                    assertThat(each.getDisabledJobsNum().intValue(), is(2));
                    assertThat(each.getJobsNum(), is(2));
                    assertThat(each.getInstancesNum(), is(1));
                    break;
                case 2:
                    assertThat(each.getDisabledJobsNum().intValue(), is(1));
                    assertThat(each.getJobsNum(), is(2));
                    assertThat(each.getInstancesNum(), is(1));
                    break;
                default:
                    fail();
            }
        }
    }
}
