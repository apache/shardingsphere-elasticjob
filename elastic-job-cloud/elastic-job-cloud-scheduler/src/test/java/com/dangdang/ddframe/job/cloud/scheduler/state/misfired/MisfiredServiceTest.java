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

package com.dangdang.ddframe.job.cloud.scheduler.state.misfired;

import com.dangdang.ddframe.job.cloud.scheduler.boot.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.cloud.scheduler.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.config.JobExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.context.ExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.context.JobContext;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningService;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MisfiredServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private RunningService runningService;
    
    @Mock
    private List<String> mockedMisfiredQueue;
    
    private MisfiredService misfiredService;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        misfiredService = new MisfiredService(regCenter);
        ReflectionUtils.setFieldValue(misfiredService, "configService", configService);
        ReflectionUtils.setFieldValue(misfiredService, "runningService", runningService);
    }
    
    @Test
    public void assertAddWithOverJobQueueSize() {
        when(regCenter.getNumChildren(MisfiredNode.ROOT)).thenReturn(BootstrapEnvironment.getInstance().getFrameworkConfiguration().getJobStateQueueSize() + 1);
        misfiredService.add("test_job");
        verify(regCenter, times(0)).persist("/state/misfired/test_job", "");
    }
    
    @Test
    public void assertAddWhenJobIsNotPresent() {
        when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        misfiredService.add("test_job");
        verify(regCenter, times(0)).persist("/state/misfired/test_job", "");
    }
    
    @Test
    public void assertAddWhenIsDaemonJob() {
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job", JobExecutionType.DAEMON)));
        misfiredService.add("test_job");
        verify(regCenter, times(0)).persist("/state/misfired/test_job", "");
    }
    
    @Test
    public void assertAddWhenExisted() {
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        when(regCenter.isExisted("/state/misfired/test_job")).thenReturn(true);
        misfiredService.add("test_job");
        verify(regCenter).isExisted("/state/misfired/test_job");
        verify(regCenter, times(0)).persist("/state/misfired/test_job", "");
    }
    
    @Test
    public void assertAddWhenNotExisted() {
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        when(regCenter.isExisted("/state/misfired/test_job")).thenReturn(false);
        misfiredService.add("test_job");
        verify(regCenter).isExisted("/state/misfired/test_job");
        verify(regCenter).persist("/state/misfired/test_job", "");
    }
    
    @Test
    public void assertGetAllEligibleJobContextsWithoutRootNode() {
        when(regCenter.isExisted("/state/misfired")).thenReturn(false);
        assertTrue(misfiredService.getAllEligibleJobContexts(Collections.<JobContext>emptyList()).isEmpty());
        verify(regCenter).isExisted("/state/misfired");
    }
    
    @Test
    public void assertGetAllEligibleJobContextsWithRootNode() {
        when(regCenter.isExisted("/state/misfired")).thenReturn(true);
        when(regCenter.getChildrenKeys("/state/misfired")).thenReturn(Arrays.asList("not_existed_job", "running_job", "ineligible_job", "eligible_job"));
        when(configService.load("not_existed_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        when(configService.load("running_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("running_job")));
        when(configService.load("ineligible_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("ineligible_job")));
        when(configService.load("eligible_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("eligible_job")));
        when(runningService.isJobRunning("running_job")).thenReturn(true);
        when(runningService.isJobRunning("ineligible_job")).thenReturn(false);
        when(runningService.isJobRunning("eligible_job")).thenReturn(false);
        assertThat(misfiredService.getAllEligibleJobContexts(Collections.singletonList(
                JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("eligible_job"), ExecutionType.MISFIRED))).size(), is(1));
        verify(regCenter).isExisted("/state/misfired");
        verify(regCenter).remove("/state/misfired/not_existed_job");
    }
    
    @Test
    public void assertRemove() {
        misfiredService.remove(Arrays.asList("test_job_1", "test_job_2"));
        verify(regCenter).remove("/state/misfired/test_job_1");
        verify(regCenter).remove("/state/misfired/test_job_2");
    }
}
