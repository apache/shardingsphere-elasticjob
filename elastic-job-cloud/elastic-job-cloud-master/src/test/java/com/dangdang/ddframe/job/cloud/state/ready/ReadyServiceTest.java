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

package com.dangdang.ddframe.job.cloud.state.ready;

import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.context.ExecutionType;
import com.dangdang.ddframe.job.cloud.context.JobContext;
import com.dangdang.ddframe.job.cloud.state.fixture.CloudJobConfigurationBuilder;
import com.dangdang.ddframe.job.cloud.state.misfired.MisfiredService;
import com.dangdang.ddframe.job.cloud.state.running.RunningService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ReadyServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private RunningService runningService;
    
    @Mock
    private MisfiredService misfiredService;
    
    private ReadyService readyService;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        readyService = new ReadyService(regCenter);
        ReflectionUtils.setFieldValue(readyService, "configService", configService);
        ReflectionUtils.setFieldValue(readyService, "runningService", runningService);
        ReflectionUtils.setFieldValue(readyService, "misfiredService", misfiredService);
    }
    
    @Test
    public void assertAdd() {
        when(regCenter.isExisted("/state/ready")).thenReturn(false);
        readyService.add("test_job");
        verify(regCenter).persist((String) any(), eq(""));
    }
    
    @Test
    public void assertAddUniqueWithoutRootNode() {
        when(regCenter.isExisted("/state/ready")).thenReturn(false);
        readyService.addUnique("test_job");
        verify(regCenter, times(0)).persist((String) any(), eq(""));
    }
    
    @Test
    public void assertAddUniqueWithSameJobName() {
        when(regCenter.isExisted("/state/ready")).thenReturn(true);
        when(regCenter.getChildrenKeys("/state/ready")).thenReturn(Arrays.asList("other_job@-@111", "test_job@-@111"));
        readyService.addUnique("test_job");
        verify(regCenter, times(0)).persist((String) any(), eq(""));
    }
    
    @Test
    public void assertAddUniqueWithoutSameJobName() {
        when(regCenter.isExisted("/state/ready")).thenReturn(true);
        when(regCenter.getChildrenKeys("/state/ready")).thenReturn(Arrays.asList("other_job@-@111", "other_job@-@222"));
        readyService.addUnique("test_job");
        verify(regCenter).persist((String) any(), eq(""));
    }
    
    @Test
    public void assertGetAllEligibleJobContextsWithoutRootNode() {
        when(regCenter.isExisted("/state/ready")).thenReturn(false);
        assertTrue(readyService.getAllEligibleJobContexts(Collections.<JobContext>emptyList()).isEmpty());
        verify(regCenter).isExisted("/state/ready");
    }
    
    @Test
    public void assertGetAllEligibleJobContextsWithRootNode() {
        when(regCenter.isExisted("/state/ready")).thenReturn(true);
        when(regCenter.getChildrenKeys("/state/ready")).thenReturn(Arrays.asList("not_existed_job@-@0", "running_job@-@0", "ineligible_job@-@0", "eligible_job@-@0", "eligible_job@-@1"));
        when(configService.load("not_existed_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        when(configService.load("running_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("running_job")));
        when(configService.load("ineligible_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("ineligible_job")));
        when(configService.load("eligible_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("eligible_job")));
        when(runningService.isJobRunning("running_job")).thenReturn(true);
        when(runningService.isJobRunning("ineligible_job")).thenReturn(false);
        when(runningService.isJobRunning("eligible_job")).thenReturn(false);
        assertThat(readyService.getAllEligibleJobContexts(Collections.singletonList(
                JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("ineligible_job"), ExecutionType.READY))).size(), is(1));
        verify(regCenter).isExisted("/state/ready");
        verify(regCenter).getChildrenKeys("/state/ready");
        verify(configService).load("not_existed_job");
        verify(configService).load("running_job");
        verify(configService).load("eligible_job");
        verify(regCenter).remove("/state/ready/not_existed_job@-@0");
        verify(misfiredService).add("running_job");
    }
    
    @Test
    public void assertRemove() {
        when(regCenter.getChildrenKeys("/state/ready")).thenReturn(Arrays.asList("test_job_0@-@00", "test_job_1@-@00"));
        readyService.remove(Arrays.asList("test_job_1", "test_job_2"));
        verify(regCenter).remove("/state/ready/test_job_1@-@00");
        verify(regCenter, times(0)).remove("/state/ready/test_job_0@-@00");
        verify(regCenter, times(0)).remove("/state/ready/test_job_2");
    }
}
