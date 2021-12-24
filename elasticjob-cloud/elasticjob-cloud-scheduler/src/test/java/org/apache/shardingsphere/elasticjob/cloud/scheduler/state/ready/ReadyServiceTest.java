/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.cloud.scheduler.state.ready;

import org.apache.shardingsphere.elasticjob.cloud.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.context.JobContext;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.BootstrapEnvironment;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running.RunningService;
import org.apache.shardingsphere.elasticjob.infra.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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
    
    private ReadyService readyService;
        
    @Before
    public void setUp() {
        readyService = new ReadyService(regCenter);
        ReflectionUtils.setFieldValue(readyService, "configService", configService);
        ReflectionUtils.setFieldValue(readyService, "runningService", runningService);
    }
    
    @Test
    public void assertAddTransientWithJobConfigIsNotPresent() {
        when(configService.load("test_job")).thenReturn(Optional.empty());
        readyService.addTransient("test_job");
        verify(regCenter, times(0)).isExisted("/state/ready");
        verify(regCenter, times(0)).persist(ArgumentMatchers.any(), ArgumentMatchers.eq(""));
    }
    
    @Test
    public void assertAddTransientWithJobConfigIsNotTransient() {
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job", CloudJobExecutionType.DAEMON)));
        readyService.addTransient("test_job");
        verify(regCenter, times(0)).isExisted("/state/ready");
        verify(regCenter, times(0)).persist(ArgumentMatchers.any(), ArgumentMatchers.eq(""));
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
        when(regCenter.getNumChildren(ReadyNode.ROOT)).thenReturn(BootstrapEnvironment.getINSTANCE().getFrameworkConfiguration().getJobStateQueueSize() + 1);
        readyService.addTransient("test_job");
        verify(regCenter, times(0)).persist("/state/ready/test_job", "1");
    }
    
    @Test
    public void assertAddDaemonWithOverJobQueueSize() {
        when(regCenter.getNumChildren(ReadyNode.ROOT)).thenReturn(BootstrapEnvironment.getINSTANCE().getFrameworkConfiguration().getJobStateQueueSize() + 1);
        readyService.addDaemon("test_job");
        verify(regCenter, times(0)).persist("/state/ready/test_job", "1");
    }
    
    @Test
    public void assertAddDaemonWithJobConfigIsNotPresent() {
        when(configService.load("test_job")).thenReturn(Optional.empty());
        readyService.addDaemon("test_job");
        verify(regCenter, times(0)).isExisted("/state/ready");
        verify(regCenter, times(0)).persist(ArgumentMatchers.any(), ArgumentMatchers.eq("1"));
    }
    
    @Test
    public void assertAddDaemonWithJobConfigIsNotDaemon() {
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        readyService.addDaemon("test_job");
        verify(regCenter, times(0)).isExisted("/state/ready");
        verify(regCenter, times(0)).persist(ArgumentMatchers.any(), ArgumentMatchers.eq("1"));
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
        verify(regCenter).persist(ArgumentMatchers.any(), ArgumentMatchers.eq("1"));
    }
    
    @Test
    public void assertAddRunningDaemon() {
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job", CloudJobExecutionType.DAEMON)));
        when(runningService.isJobRunning("test_job")).thenReturn(true);
        readyService.addDaemon("test_job");
        verify(regCenter, never()).persist(ArgumentMatchers.any(), ArgumentMatchers.eq("1"));
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
        assertTrue(readyService.getAllEligibleJobContexts(Collections.emptyList()).isEmpty());
        verify(regCenter).isExisted("/state/ready");
    }
    
    @Test
    public void assertSetMisfireDisabledWhenJobIsNotExisted() {
        when(configService.load("test_job")).thenReturn(Optional.empty());
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
        when(configService.load("not_existed_job")).thenReturn(Optional.empty());
        when(configService.load("running_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("running_job")));
        when(configService.load("eligible_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("eligible_job")));
        when(runningService.isJobRunning("running_job")).thenReturn(true);
        when(runningService.isJobRunning("eligible_job")).thenReturn(false);
        assertThat(readyService.getAllEligibleJobContexts(Collections.singletonList(
                JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("ineligible_job").toCloudJobConfiguration(), ExecutionType.READY))).size(), is(1));
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
        when(configService.load("not_existed_job")).thenReturn(Optional.empty());
        when(configService.load("running_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("running_job", CloudJobExecutionType.DAEMON)));
        when(runningService.isJobRunning("running_job")).thenReturn(true);
        assertThat(readyService.getAllEligibleJobContexts(Collections.emptyList()).size(), is(0));
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
        verify(regCenter, times(0)).getChildrenKeys(ArgumentMatchers.any());
        verify(regCenter, times(0)).get(ArgumentMatchers.any());
    }
    
    @Test
    public void assertGetAllTasksWhenRootNodeHasNoChild() {
        when(regCenter.isExisted(ReadyNode.ROOT)).thenReturn(true);
        when(regCenter.getChildrenKeys(ReadyNode.ROOT)).thenReturn(Collections.emptyList());
        assertTrue(readyService.getAllReadyTasks().isEmpty());
        verify(regCenter).isExisted(ReadyNode.ROOT);
        verify(regCenter).getChildrenKeys(ReadyNode.ROOT);
        verify(regCenter, times(0)).get(ArgumentMatchers.any());
    }
    
    @Test
    public void assertGetAllTasksWhenNodeIsEmpty() {
        when(regCenter.isExisted(ReadyNode.ROOT)).thenReturn(true);
        when(regCenter.getChildrenKeys(ReadyNode.ROOT)).thenReturn(Collections.singletonList("test_job"));
        when(regCenter.get(ReadyNode.getReadyJobNodePath("test_job"))).thenReturn("");
        assertTrue(readyService.getAllReadyTasks().isEmpty());
        verify(regCenter).isExisted(ReadyNode.ROOT);
        verify(regCenter).getChildrenKeys(ReadyNode.ROOT);
        verify(regCenter).get(ReadyNode.getReadyJobNodePath("test_job"));
    }
    
    @Test
    public void assertGetAllTasksWithRootNode() {
        when(regCenter.isExisted(ReadyNode.ROOT)).thenReturn(true);
        when(regCenter.getChildrenKeys(ReadyNode.ROOT)).thenReturn(Arrays.asList("test_job_1", "test_job_2"));
        when(regCenter.get(ReadyNode.getReadyJobNodePath("test_job_1"))).thenReturn("1");
        when(regCenter.get(ReadyNode.getReadyJobNodePath("test_job_2"))).thenReturn("5");
        Map<String, Integer> result = readyService.getAllReadyTasks();
        assertThat(result.size(), is(2));
        assertThat(result.get("test_job_1"), is(1));
        assertThat(result.get("test_job_2"), is(5));
        verify(regCenter).isExisted(ReadyNode.ROOT);
        verify(regCenter).getChildrenKeys(ReadyNode.ROOT);
        verify(regCenter, times(2)).get(ArgumentMatchers.any());
    }
}
