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

package com.dangdang.ddframe.job.cloud.scheduler.state.ready;

import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.context.JobContext;
import com.dangdang.ddframe.job.cloud.scheduler.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningService;
import com.dangdang.ddframe.job.context.ExecutionType;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ReadyServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private CloudJobConfigurationService configService;
    
    @Mock
    private RunningService runningService;
    
    @Mock
    private List<String> mockedReadyQueue;
    
    private ReadyService readyService;
        
    @Before
    public void setUp() throws NoSuchFieldException {
        readyService = new ReadyService(regCenter);
        ReflectionUtils.setFieldValue(readyService, "configService", configService);
        ReflectionUtils.setFieldValue(readyService, "runningService", runningService);
    }
    
    @Test
    public void assertAddTransientWithJobConfigIsNotPresent() {
        when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        readyService.addTransient("test_job");
        verify(regCenter, times(0)).isExisted("/state/ready");
        verify(regCenter, times(0)).persist((String) any(), eq(""));
    }
    
    @Test
    public void assertAddTransientWithJobConfigIsNotTransient() {
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job", CloudJobExecutionType.DAEMON)));
        readyService.addTransient("test_job");
        verify(regCenter, times(0)).isExisted("/state/ready");
        verify(regCenter, times(0)).persist((String) any(), eq(""));
    }
    
    @Test
    public void assertAddTransientWhenJobExistedAndEnableMisfired() {
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        when(regCenter.getDirectly("/state/ready/test_job")).thenReturn("1");
        readyService.addTransient("test_job");
        verify(regCenter).persist("/state/ready/test_job", "2");
    }
    
    @Test
    public void assertAddTransientWhenJobExistedAndDisableMisfired() {
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job", false)));
        when(regCenter.getDirectly("/state/ready/test_job")).thenReturn("1");
        readyService.addTransient("test_job");
        verify(regCenter).persist("/state/ready/test_job", "1");
    }
    
    @Test
    public void assertAddTransientWhenJobNotExisted() {
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        readyService.addTransient("test_job");
        verify(regCenter).persist("/state/ready/test_job", "1");
    }
    
    @Test
    public void assertAddTransientWithOverJobQueueSize() {
        when(regCenter.getNumChildren(ReadyNode.ROOT)).thenReturn(BootstrapEnvironment.getInstance().getFrameworkConfiguration().getJobStateQueueSize() + 1);
        readyService.addTransient("test_job");
        verify(regCenter, times(0)).persist("/state/ready/test_job", "1");
    }
    
    @Test
    public void assertAddDaemonWithOverJobQueueSize() {
        when(regCenter.getNumChildren(ReadyNode.ROOT)).thenReturn(BootstrapEnvironment.getInstance().getFrameworkConfiguration().getJobStateQueueSize() + 1);
        readyService.addDaemon("test_job");
        verify(regCenter, times(0)).persist("/state/ready/test_job", "1");
    }
    
    @Test
    public void assertAddDaemonWithJobConfigIsNotPresent() {
        when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        readyService.addDaemon("test_job");
        verify(regCenter, times(0)).isExisted("/state/ready");
        verify(regCenter, times(0)).persist((String) any(), eq("1"));
    }
    
    @Test
    public void assertAddDaemonWithJobConfigIsNotDaemon() {
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        readyService.addDaemon("test_job");
        verify(regCenter, times(0)).isExisted("/state/ready");
        verify(regCenter, times(0)).persist((String) any(), eq("1"));
    }
    
    @Test
    public void assertAddDaemonWithoutRootNode() {
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job", CloudJobExecutionType.DAEMON)));
        readyService.addDaemon("test_job");
        verify(regCenter).persist("/state/ready/test_job", "1");
    }
    
    @Test
    public void assertAddDaemonWithSameJobName() {
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job", CloudJobExecutionType.DAEMON)));
        readyService.addDaemon("test_job");
        verify(regCenter).persist((String) any(), eq("1"));
    }
    
    @Test
    public void assertAddRunningDaemon() {
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job", CloudJobExecutionType.DAEMON)));
        when(runningService.isJobRunning("test_job")).thenReturn(true);
        readyService.addDaemon("test_job");
        verify(regCenter, never()).persist((String) any(), eq("1"));
    }
    
    @Test
    public void assertAddDaemonWithoutSameJobName() {
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job", CloudJobExecutionType.DAEMON)));
        readyService.addDaemon("test_job");
        verify(regCenter).persist("/state/ready/test_job", "1");
    }
    
    @Test
    public void assertGetAllEligibleJobContextsWithoutRootNode() {
        when(regCenter.isExisted("/state/ready")).thenReturn(false);
        assertTrue(readyService.getAllEligibleJobContexts(Collections.<JobContext>emptyList()).isEmpty());
        verify(regCenter).isExisted("/state/ready");
    }
    
    @Test
    public void assertSetMisfireDisabledWhenJobIsNotExisted() {
        when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        readyService.setMisfireDisabled("test_job");
        verify(regCenter, times(0)).persist("/state/ready/test_job", "1");
    }
    
    @Test
    public void assertSetMisfireDisabledWhenReadyNodeNotExisted() {
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        readyService.setMisfireDisabled("test_job");
        verify(regCenter, times(0)).persist("/state/ready/test_job", "1");
    }
    
    @Test
    public void assertSetMisfireDisabledWhenReadyNodeExisted() {
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        when(regCenter.getDirectly("/state/ready/test_job")).thenReturn("100");
        readyService.setMisfireDisabled("test_job");
        verify(regCenter).persist("/state/ready/test_job", "1");
    }
    
    @Test
    public void assertGetAllEligibleJobContextsWithRootNode() {
        when(regCenter.isExisted("/state/ready")).thenReturn(true);
        when(regCenter.getChildrenKeys("/state/ready")).thenReturn(Arrays.asList("not_existed_job", "running_job", "ineligible_job", "eligible_job"));
        when(configService.load("not_existed_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        when(configService.load("running_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("running_job")));
        when(configService.load("eligible_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("eligible_job")));
        when(runningService.isJobRunning("running_job")).thenReturn(true);
        when(runningService.isJobRunning("eligible_job")).thenReturn(false);
        assertThat(readyService.getAllEligibleJobContexts(Collections.singletonList(
                JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("ineligible_job"), ExecutionType.READY))).size(), is(1));
        verify(regCenter).isExisted("/state/ready");
        verify(regCenter, times(1)).getChildrenKeys("/state/ready");
        verify(configService).load("not_existed_job");
        verify(configService).load("running_job");
        verify(configService).load("eligible_job");
        verify(regCenter).remove("/state/ready/not_existed_job");
    }
    
    @Test
    public void assertGetAllEligibleJobContextsWithRootNodeAndDaemonJob() {
        when(regCenter.isExisted("/state/ready")).thenReturn(true);
        when(regCenter.getChildrenKeys("/state/ready")).thenReturn(Arrays.asList("not_existed_job", "running_job"));
        when(configService.load("not_existed_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        when(configService.load("running_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("running_job", CloudJobExecutionType.DAEMON)));
        when(runningService.isJobRunning("running_job")).thenReturn(true);
        assertThat(readyService.getAllEligibleJobContexts(Collections.<JobContext>emptyList()).size(), is(0));
        verify(regCenter).isExisted("/state/ready");
        verify(regCenter, times(1)).getChildrenKeys("/state/ready");
        verify(configService).load("not_existed_job");
        verify(configService).load("running_job");
    }
    
    @Test
    public void assertRemove() {
        when(regCenter.getDirectly("/state/ready/test_job_1")).thenReturn("1");
        when(regCenter.getDirectly("/state/ready/test_job_2")).thenReturn("2");
        readyService.remove(Arrays.asList("test_job_1", "test_job_2"));
        verify(regCenter).persist("/state/ready/test_job_2", "1");
        verify(regCenter).remove("/state/ready/test_job_1");
        verify(regCenter, times(0)).persist("/state/ready/test_job_1", "0");
        verify(regCenter, times(0)).remove("/state/ready/test_job_2");
    }
    
    @Test
    public void assertGetAllTasksWithoutRootNode() {
        when(regCenter.isExisted(ReadyNode.ROOT)).thenReturn(false);
        assertTrue(readyService.getAllReadyTasks().isEmpty());
        verify(regCenter).isExisted(ReadyNode.ROOT);
        verify(regCenter, times(0)).getChildrenKeys((String) any());
        verify(regCenter, times(0)).get((String) any());
    }
    
    @Test
    public void assertGetAllTasksWhenRootNodeHasNoChild() {
        when(regCenter.isExisted(ReadyNode.ROOT)).thenReturn(true);
        when(regCenter.getChildrenKeys(ReadyNode.ROOT)).thenReturn(Collections.<String>emptyList());
        assertTrue(readyService.getAllReadyTasks().isEmpty());
        verify(regCenter).isExisted(ReadyNode.ROOT);
        verify(regCenter).getChildrenKeys(ReadyNode.ROOT);
        verify(regCenter, times(0)).get((String) any());
    }
    
    @Test
    public void assertGetAllTasksWhenNodeIsEmpty() {
        when(regCenter.isExisted(ReadyNode.ROOT)).thenReturn(true);
        when(regCenter.getChildrenKeys(ReadyNode.ROOT)).thenReturn(Lists.newArrayList("test_job"));
        when(regCenter.get(ReadyNode.getReadyJobNodePath("test_job"))).thenReturn("");
        assertTrue(readyService.getAllReadyTasks().isEmpty());
        verify(regCenter).isExisted(ReadyNode.ROOT);
        verify(regCenter).getChildrenKeys(ReadyNode.ROOT);
        verify(regCenter).get(ReadyNode.getReadyJobNodePath("test_job"));
    }
    
    @Test
    public void assertGetAllTasksWithRootNode() {
        when(regCenter.isExisted(ReadyNode.ROOT)).thenReturn(true);
        when(regCenter.getChildrenKeys(ReadyNode.ROOT)).thenReturn(Lists.newArrayList("test_job_1", "test_job_2"));
        when(regCenter.get(ReadyNode.getReadyJobNodePath("test_job_1"))).thenReturn("1");
        when(regCenter.get(ReadyNode.getReadyJobNodePath("test_job_2"))).thenReturn("5");
        Map<String, Integer> result = readyService.getAllReadyTasks();
        assertThat(result.size(), is(2));
        assertThat(result.get("test_job_1"), is(1));
        assertThat(result.get("test_job_2"), is(5));
        verify(regCenter).isExisted(ReadyNode.ROOT);
        verify(regCenter).getChildrenKeys(ReadyNode.ROOT);
        verify(regCenter, times(2)).get((String) any());
    }
}
