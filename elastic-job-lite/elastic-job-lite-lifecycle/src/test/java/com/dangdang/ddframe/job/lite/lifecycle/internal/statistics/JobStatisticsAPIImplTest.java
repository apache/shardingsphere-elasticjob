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

import com.dangdang.ddframe.job.lite.lifecycle.api.JobStatisticsAPI;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobBriefInfo;
import com.dangdang.ddframe.job.lite.lifecycle.fixture.LifecycleJsonConstants;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public final class JobStatisticsAPIImplTest {
    
    private JobStatisticsAPI jobStatisticsAPI;
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        jobStatisticsAPI = new JobStatisticsAPIImpl(regCenter);
    }
    
    @Test
    public void assertGetAllJobsBriefInfoWithoutNamespace() {
        when(regCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("test_job_1", "test_job_2"));
        assertThat(jobStatisticsAPI.getAllJobsBriefInfo().size(), is(0));
    }
    
    @Test
    public void assertGetJobBriefInfo() {
        when(regCenter.getChildrenKeys("/")).thenReturn(Lists.newArrayList("test_job"));
        when(regCenter.get("/test_job/config")).thenReturn(LifecycleJsonConstants.getSimpleJobJson("test_job", "desc"));
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        when(regCenter.getChildrenKeys("/test_job/instances")).thenReturn(Arrays.asList("ip1@-@defaultInstance", "ip2@-@defaultInstance"));
        when(regCenter.getChildrenKeys("/test_job/sharding")).thenReturn(Arrays.asList("0", "1", "2"));
        when(regCenter.get("/test_job/sharding/0/instance")).thenReturn("ip1@-@defaultInstance");
        when(regCenter.get("/test_job/sharding/1/instance")).thenReturn("ip1@-@defaultInstance");
        when(regCenter.get("/test_job/sharding/2/instance")).thenReturn("ip2@-@defaultInstance");
        when(regCenter.get("/test_job/servers/instances/ip1@-@defaultInstance")).thenReturn("RUNNING");
        when(regCenter.get("/test_job/servers/instances/ip2@-@defaultInstance")).thenReturn("READY");
        JobBriefInfo jobBrief = jobStatisticsAPI.getJobBriefInfo("test_job");
        assertThat(jobBrief.getJobName(), is("test_job"));
        assertThat(jobBrief.getDescription(), is("desc"));
        assertThat(jobBrief.getCron(), is("0/1 * * * * ?"));
        assertThat(jobBrief.getJobType(), is("SIMPLE"));
        assertThat(jobBrief.getShardingItems(), is("0,1,2"));
        assertThat(jobBrief.getShardingTotalCount(), is(3));
    }
    
    @Test
    public void assertGetAllJobsBriefInfo() {
        when(regCenter.getChildrenKeys("/")).thenReturn(Arrays.asList("test_job_1", "test_job_2"));
        when(regCenter.get("/test_job_1/config")).thenReturn(LifecycleJsonConstants.getSimpleJobJson("test_job_1", "desc1"));
        when(regCenter.get("/test_job_2/config")).thenReturn(LifecycleJsonConstants.getSimpleJobJson("test_job_2", "desc2"));
        when(regCenter.getChildrenKeys("/test_job_1/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        when(regCenter.getChildrenKeys("/test_job_2/servers")).thenReturn(Arrays.asList("ip3", "ip4"));
        when(regCenter.getChildrenKeys("/test_job_1/sharding")).thenReturn(Arrays.asList("1"));
        when(regCenter.getChildrenKeys("/test_job_2/sharding")).thenReturn(Arrays.asList("1", "2"));
        int i = 0;
        List<String> shardingItems = new ArrayList<>();
        for (JobBriefInfo each : jobStatisticsAPI.getAllJobsBriefInfo()) {
            i++;
            assertThat(each.getJobName(), is("test_job_" + i));
            assertThat(each.getDescription(), is("desc" + i));
            assertThat(each.getCron(), is("0/1 * * * * ?"));
            assertThat(each.getJobType(), is("SIMPLE"));
            shardingItems.add(String.valueOf(i));
            assertThat(each.getShardingItems(), is(Joiner.on(",").join(shardingItems)));
            assertThat(each.getShardingTotalCount(), is(3));
        }
    }
    
}
