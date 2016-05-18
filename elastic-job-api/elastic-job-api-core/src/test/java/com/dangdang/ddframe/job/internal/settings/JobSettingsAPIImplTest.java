/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.internal.settings;

import com.dangdang.ddframe.job.api.JobSettingsAPI;
import com.dangdang.ddframe.job.domain.JobSettings;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JobSettingsAPIImplTest {
    
    private JobSettingsAPI jobSettingsAPI;
    
    @Mock
    private CoordinatorRegistryCenter registryCenter;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        jobSettingsAPI = new JobSettingsAPIImpl(registryCenter);
    }
    
    @Test
    public void assertGetJobSettingsWithMonitorPort() {
        createExpected();
        when(registryCenter.get("/testJob/config/monitorPort")).thenReturn("-1");
        JobSettings actual = jobSettingsAPI.getJobSettings("testJob");
        assertJobSettings(actual);
        assertThat(actual.getMonitorPort(), is(-1));
        verifyCall();
        verify(registryCenter).get("/testJob/config/monitorPort");
    }
    
    @Test
    public void assertGetJobSettingsWithoutMonitorPort() {
        createExpected();
        assertJobSettings(jobSettingsAPI.getJobSettings("testJob"));
        verifyCall();
    }
    
    private void createExpected() {
        when(registryCenter.get("/testJob/config/jobClass")).thenReturn("TestJob");
        when(registryCenter.get("/testJob/config/shardingTotalCount")).thenReturn("1");
        when(registryCenter.get("/testJob/config/cron")).thenReturn("0/30 * * * * *");
        when(registryCenter.get("/testJob/config/shardingItemParameters")).thenReturn("0=A");
        when(registryCenter.get("/testJob/config/jobParameter")).thenReturn("param");
        when(registryCenter.get("/testJob/config/monitorExecution")).thenReturn("true");
        when(registryCenter.get("/testJob/config/processCountIntervalSeconds")).thenReturn("300");
        when(registryCenter.get("/testJob/config/concurrentDataProcessThreadCount")).thenReturn("10");
        when(registryCenter.get("/testJob/config/fetchDataCount")).thenReturn("100");
        when(registryCenter.get("/testJob/config/maxTimeDiffSeconds")).thenReturn("60000");
        when(registryCenter.get("/testJob/config/failover")).thenReturn("true");
        when(registryCenter.get("/testJob/config/misfire")).thenReturn("true");
        when(registryCenter.get("/testJob/config/jobShardingStrategyClass")).thenReturn("MyJobShardingStrategy");
        when(registryCenter.get("/testJob/config/description")).thenReturn("description");
    }
    
    private void assertJobSettings(JobSettings jobSettings) {
        assertThat(jobSettings.getJobName(), is("testJob"));
        assertThat(jobSettings.getJobClass(), is("TestJob"));
        assertThat(jobSettings.getShardingTotalCount(), is(1));
        assertThat(jobSettings.getCron(), is("0/30 * * * * *"));
        assertThat(jobSettings.getShardingItemParameters(), is("0=A"));
        assertThat(jobSettings.getJobParameter(), is("param"));
        assertThat(jobSettings.isMonitorExecution(), is(true));
        assertThat(jobSettings.getProcessCountIntervalSeconds(), is(300));
        assertThat(jobSettings.getConcurrentDataProcessThreadCount(), is(10));
        assertThat(jobSettings.getFetchDataCount(), is(100));
        assertThat(jobSettings.getMaxTimeDiffSeconds(), is(60000));
        assertThat(jobSettings.isFailover(), is(true));
        assertThat(jobSettings.isMisfire(), is(true));
        assertThat(jobSettings.getJobShardingStrategyClass(), is("MyJobShardingStrategy"));
        assertThat(jobSettings.getDescription(), is("description"));
    }
    
    private void verifyCall() {
        verify(registryCenter).get("/testJob/config/jobClass");
        verify(registryCenter).get("/testJob/config/shardingTotalCount");
        verify(registryCenter).get("/testJob/config/cron");
        verify(registryCenter).get("/testJob/config/shardingItemParameters");
        verify(registryCenter).get("/testJob/config/jobParameter");
        verify(registryCenter).get("/testJob/config/monitorExecution");
        verify(registryCenter).get("/testJob/config/processCountIntervalSeconds");
        verify(registryCenter).get("/testJob/config/concurrentDataProcessThreadCount");
        verify(registryCenter).get("/testJob/config/fetchDataCount");
        verify(registryCenter).get("/testJob/config/maxTimeDiffSeconds");
        verify(registryCenter).get("/testJob/config/failover");
        verify(registryCenter).get("/testJob/config/misfire");
        verify(registryCenter).get("/testJob/config/jobShardingStrategyClass");
        verify(registryCenter).get("/testJob/config/description");
    }
    
    @Test
    public void assertUpdateJobSettings() {
        createExpected();
        JobSettings jobSettings = new JobSettings();
        jobSettings.setJobName("testJob");
        jobSettings.setJobClass("TestJob");
        jobSettings.setShardingTotalCount(10);
        jobSettings.setCron(null);
        jobSettings.setProcessCountIntervalSeconds(300);
        jobSettings.setConcurrentDataProcessThreadCount(10);
        jobSettings.setFetchDataCount(100);
        jobSettings.setMaxTimeDiffSeconds(60000);
        jobSettings.setMonitorExecution(true);
        jobSettings.setFailover(true);
        jobSettings.setMisfire(true);
        jobSettingsAPI.updateJobSettings(jobSettings);
        verify(registryCenter).update("/testJob/config/shardingTotalCount", "10");
        verify(registryCenter, times(0)).update("/testJob/config/cron", null);
    }
}
