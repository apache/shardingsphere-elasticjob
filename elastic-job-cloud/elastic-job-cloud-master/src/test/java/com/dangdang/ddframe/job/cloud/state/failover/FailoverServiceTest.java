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

package com.dangdang.ddframe.job.cloud.state.failover;

import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.context.JobContext;
import com.dangdang.ddframe.job.cloud.context.TaskContext;
import com.dangdang.ddframe.job.cloud.state.fixture.CloudJobConfigurationBuilder;
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
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class FailoverServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private RunningService runningService;
    
    private FailoverService failoverService;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        failoverService = new FailoverService(regCenter);
        ReflectionUtils.setFieldValue(failoverService, "configService", configService);
        ReflectionUtils.setFieldValue(failoverService, "runningService", runningService);
    }
    
    @Test
    public void assertAddWhenExisted() {
        when(regCenter.isExisted("/state/failover/test_job/test_job@-@0@-@111")).thenReturn(true);
        failoverService.add(TaskContext.from("test_job@-@0@-@111"));
        verify(regCenter).isExisted("/state/failover/test_job/test_job@-@0@-@111");
        verify(regCenter, times(0)).persist("/state/failover/test_job/test_job@-@0@-@111", "");
    }
    
    @Test
    public void assertAddWhenNotExistedAndTaskIsRunning() {
        when(regCenter.isExisted("/state/failover/test_job/test_job@-@0@-@111")).thenReturn(false);
        when(runningService.isTaskRunning(TaskContext.from("test_job@-@0@-@111"))).thenReturn(true);
        failoverService.add(TaskContext.from("test_job@-@0@-@111"));
        verify(regCenter).isExisted("/state/failover/test_job/test_job@-@0@-@111");
        verify(runningService).isTaskRunning(TaskContext.from("test_job@-@0@-@111"));
        verify(regCenter, times(0)).persist("/state/failover/test_job/test_job@-@0@-@111", "");
    }
    
    @Test
    public void assertAddWhenNotExistedAndTaskIsNotRunning() {
        when(regCenter.isExisted("/state/failover/test_job/test_job@-@0@-@111")).thenReturn(false);
        when(runningService.isTaskRunning(TaskContext.from("test_job@-@0@-@111"))).thenReturn(false);
        failoverService.add(TaskContext.from("test_job@-@0@-@111"));
        verify(regCenter).isExisted("/state/failover/test_job/test_job@-@0@-@111");
        verify(runningService).isTaskRunning(TaskContext.from("test_job@-@0@-@111"));
        verify(regCenter).persist("/state/failover/test_job/test_job@-@0@-@111", "");
    }
    
    @Test
    public void assertGetAllEligibleJobContextsWithoutRootNode() {
        when(regCenter.isExisted("/state/failover")).thenReturn(false);
        assertTrue(failoverService.getAllEligibleJobContexts().isEmpty());
        verify(regCenter).isExisted("/state/failover");
    }
    
    @Test
    public void assertGetAllEligibleJobContextsWithRootNode() {
        when(regCenter.isExisted("/state/failover")).thenReturn(true);
        when(regCenter.getChildrenKeys("/state/failover")).thenReturn(Arrays.asList("task_empty_job", "not_existed_job", "eligible_job"));
        when(regCenter.getChildrenKeys("/state/failover/task_empty_job")).thenReturn(Collections.<String>emptyList());
        when(regCenter.getChildrenKeys("/state/failover/not_existed_job")).thenReturn(Arrays.asList("not_existed_job@-@0@-@11", "not_existed_job@-@1@-@11"));
        when(regCenter.getChildrenKeys("/state/failover/eligible_job")).thenReturn(Arrays.asList("eligible_job@-@0@-@11", "eligible_job@-@1@-@11", "eligible_job@-@1@-@22"));
        when(configService.load("not_existed_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        when(configService.load("eligible_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("eligible_job")));
        when(runningService.isTaskRunning(TaskContext.from("eligible_job@-@0@-@11"))).thenReturn(true);
        when(runningService.isTaskRunning(TaskContext.from("eligible_job@-@1@-@11"))).thenReturn(false);
        when(runningService.isTaskRunning(TaskContext.from("eligible_job@-@1@-@22"))).thenReturn(false);
        Collection<JobContext> actual = failoverService.getAllEligibleJobContexts();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getAssignedShardingItems().size(), is(1));
        assertThat(actual.iterator().next().getAssignedShardingItems().get(0), is(1));
        verify(regCenter).isExisted("/state/failover");
        verify(regCenter).remove("/state/failover/task_empty_job");
        verify(regCenter).remove("/state/failover/not_existed_job");
    }
    
    @Test
    public void assertRemove() {
        failoverService.remove(Arrays.asList(TaskContext.from("test_job@-@0@-@111"), TaskContext.from("test_job@-@1@-@111")));
        verify(regCenter).remove("/state/failover/test_job/test_job@-@0@-@111");
        verify(regCenter).remove("/state/failover/test_job/test_job@-@1@-@111");
    }
}