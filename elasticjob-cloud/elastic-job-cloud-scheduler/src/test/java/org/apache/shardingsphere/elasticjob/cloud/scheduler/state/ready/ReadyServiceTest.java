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

import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.context.JobContext;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.BootstrapEnvironment;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running.RunningService;
import org.apache.shardingsphere.elasticjob.cloud.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        Mockito.when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        readyService.addTransient("test_job");
        Mockito.verify(regCenter, Mockito.times(0)).isExisted("/state/ready");
        Mockito.verify(regCenter, Mockito.times(0)).persist((String) ArgumentMatchers.any(), ArgumentMatchers.eq(""));
    }
    
    @Test
    public void assertAddTransientWithJobConfigIsNotTransient() {
        Mockito.when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job", CloudJobExecutionType.DAEMON)));
        readyService.addTransient("test_job");
        Mockito.verify(regCenter, Mockito.times(0)).isExisted("/state/ready");
        Mockito.verify(regCenter, Mockito.times(0)).persist((String) ArgumentMatchers.any(), ArgumentMatchers.eq(""));
    }
    
    @Test
    public void assertAddTransientWhenJobExistedAndEnableMisfired() {
        Mockito.when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        Mockito.when(regCenter.getDirectly("/state/ready/test_job")).thenReturn("1");
        readyService.addTransient("test_job");
        Mockito.verify(regCenter).persist("/state/ready/test_job", "2");
    }
    
    @Test
    public void assertAddTransientWhenJobExistedAndDisableMisfired() {
        Mockito.when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job", false)));
        Mockito.when(regCenter.getDirectly("/state/ready/test_job")).thenReturn("1");
        readyService.addTransient("test_job");
        Mockito.verify(regCenter).persist("/state/ready/test_job", "1");
    }
    
    @Test
    public void assertAddTransientWhenJobNotExisted() {
        Mockito.when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        readyService.addTransient("test_job");
        Mockito.verify(regCenter).persist("/state/ready/test_job", "1");
    }
    
    @Test
    public void assertAddTransientWithOverJobQueueSize() {
        Mockito.when(regCenter.getNumChildren(ReadyNode.ROOT)).thenReturn(BootstrapEnvironment.getInstance().getFrameworkConfiguration().getJobStateQueueSize() + 1);
        readyService.addTransient("test_job");
        Mockito.verify(regCenter, Mockito.times(0)).persist("/state/ready/test_job", "1");
    }
    
    @Test
    public void assertAddDaemonWithOverJobQueueSize() {
        Mockito.when(regCenter.getNumChildren(ReadyNode.ROOT)).thenReturn(BootstrapEnvironment.getInstance().getFrameworkConfiguration().getJobStateQueueSize() + 1);
        readyService.addDaemon("test_job");
        Mockito.verify(regCenter, Mockito.times(0)).persist("/state/ready/test_job", "1");
    }
    
    @Test
    public void assertAddDaemonWithJobConfigIsNotPresent() {
        Mockito.when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        readyService.addDaemon("test_job");
        Mockito.verify(regCenter, Mockito.times(0)).isExisted("/state/ready");
        Mockito.verify(regCenter, Mockito.times(0)).persist((String) ArgumentMatchers.any(), ArgumentMatchers.eq("1"));
    }
    
    @Test
    public void assertAddDaemonWithJobConfigIsNotDaemon() {
        Mockito.when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        readyService.addDaemon("test_job");
        Mockito.verify(regCenter, Mockito.times(0)).isExisted("/state/ready");
        Mockito.verify(regCenter, Mockito.times(0)).persist((String) ArgumentMatchers.any(), ArgumentMatchers.eq("1"));
    }
    
    @Test
    public void assertAddDaemonWithoutRootNode() {
        Mockito.when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job", CloudJobExecutionType.DAEMON)));
        readyService.addDaemon("test_job");
        Mockito.verify(regCenter).persist("/state/ready/test_job", "1");
    }
    
    @Test
    public void assertAddDaemonWithSameJobName() {
        Mockito.when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job", CloudJobExecutionType.DAEMON)));
        readyService.addDaemon("test_job");
        Mockito.verify(regCenter).persist((String) ArgumentMatchers.any(), ArgumentMatchers.eq("1"));
    }
    
    @Test
    public void assertAddRunningDaemon() {
        Mockito.when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job", CloudJobExecutionType.DAEMON)));
        Mockito.when(runningService.isJobRunning("test_job")).thenReturn(true);
        readyService.addDaemon("test_job");
        Mockito.verify(regCenter, Mockito.never()).persist((String) ArgumentMatchers.any(), ArgumentMatchers.eq("1"));
    }
    
    @Test
    public void assertAddDaemonWithoutSameJobName() {
        Mockito.when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job", CloudJobExecutionType.DAEMON)));
        readyService.addDaemon("test_job");
        Mockito.verify(regCenter).persist("/state/ready/test_job", "1");
    }
    
    @Test
    public void assertGetAllEligibleJobContextsWithoutRootNode() {
        Mockito.when(regCenter.isExisted("/state/ready")).thenReturn(false);
        Assert.assertTrue(readyService.getAllEligibleJobContexts(Collections.<JobContext>emptyList()).isEmpty());
        Mockito.verify(regCenter).isExisted("/state/ready");
    }
    
    @Test
    public void assertSetMisfireDisabledWhenJobIsNotExisted() {
        Mockito.when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        readyService.setMisfireDisabled("test_job");
        Mockito.verify(regCenter, Mockito.times(0)).persist("/state/ready/test_job", "1");
    }
    
    @Test
    public void assertSetMisfireDisabledWhenReadyNodeNotExisted() {
        Mockito.when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        readyService.setMisfireDisabled("test_job");
        Mockito.verify(regCenter, Mockito.times(0)).persist("/state/ready/test_job", "1");
    }
    
    @Test
    public void assertSetMisfireDisabledWhenReadyNodeExisted() {
        Mockito.when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        Mockito.when(regCenter.getDirectly("/state/ready/test_job")).thenReturn("100");
        readyService.setMisfireDisabled("test_job");
        Mockito.verify(regCenter).persist("/state/ready/test_job", "1");
    }
    
    @Test
    public void assertGetAllEligibleJobContextsWithRootNode() {
        Mockito.when(regCenter.isExisted("/state/ready")).thenReturn(true);
        Mockito.when(regCenter.getChildrenKeys("/state/ready")).thenReturn(Arrays.asList("not_existed_job", "running_job", "ineligible_job", "eligible_job"));
        Mockito.when(configService.load("not_existed_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        Mockito.when(configService.load("running_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("running_job")));
        Mockito.when(configService.load("eligible_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("eligible_job")));
        Mockito.when(runningService.isJobRunning("running_job")).thenReturn(true);
        Mockito.when(runningService.isJobRunning("eligible_job")).thenReturn(false);
        Assert.assertThat(readyService.getAllEligibleJobContexts(Collections.singletonList(
                JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("ineligible_job"), ExecutionType.READY))).size(), Is.is(1));
        Mockito.verify(regCenter).isExisted("/state/ready");
        Mockito.verify(regCenter, Mockito.times(1)).getChildrenKeys("/state/ready");
        Mockito.verify(configService).load("not_existed_job");
        Mockito.verify(configService).load("running_job");
        Mockito.verify(configService).load("eligible_job");
        Mockito.verify(regCenter).remove("/state/ready/not_existed_job");
    }
    
    @Test
    public void assertGetAllEligibleJobContextsWithRootNodeAndDaemonJob() {
        Mockito.when(regCenter.isExisted("/state/ready")).thenReturn(true);
        Mockito.when(regCenter.getChildrenKeys("/state/ready")).thenReturn(Arrays.asList("not_existed_job", "running_job"));
        Mockito.when(configService.load("not_existed_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        Mockito.when(configService.load("running_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("running_job", CloudJobExecutionType.DAEMON)));
        Mockito.when(runningService.isJobRunning("running_job")).thenReturn(true);
        Assert.assertThat(readyService.getAllEligibleJobContexts(Collections.<JobContext>emptyList()).size(), Is.is(0));
        Mockito.verify(regCenter).isExisted("/state/ready");
        Mockito.verify(regCenter, Mockito.times(1)).getChildrenKeys("/state/ready");
        Mockito.verify(configService).load("not_existed_job");
        Mockito.verify(configService).load("running_job");
    }
    
    @Test
    public void assertRemove() {
        Mockito.when(regCenter.getDirectly("/state/ready/test_job_1")).thenReturn("1");
        Mockito.when(regCenter.getDirectly("/state/ready/test_job_2")).thenReturn("2");
        readyService.remove(Arrays.asList("test_job_1", "test_job_2"));
        Mockito.verify(regCenter).persist("/state/ready/test_job_2", "1");
        Mockito.verify(regCenter).remove("/state/ready/test_job_1");
        Mockito.verify(regCenter, Mockito.times(0)).persist("/state/ready/test_job_1", "0");
        Mockito.verify(regCenter, Mockito.times(0)).remove("/state/ready/test_job_2");
    }
    
    @Test
    public void assertGetAllTasksWithoutRootNode() {
        Mockito.when(regCenter.isExisted(ReadyNode.ROOT)).thenReturn(false);
        Assert.assertTrue(readyService.getAllReadyTasks().isEmpty());
        Mockito.verify(regCenter).isExisted(ReadyNode.ROOT);
        Mockito.verify(regCenter, Mockito.times(0)).getChildrenKeys((String) ArgumentMatchers.any());
        Mockito.verify(regCenter, Mockito.times(0)).get((String) ArgumentMatchers.any());
    }
    
    @Test
    public void assertGetAllTasksWhenRootNodeHasNoChild() {
        Mockito.when(regCenter.isExisted(ReadyNode.ROOT)).thenReturn(true);
        Mockito.when(regCenter.getChildrenKeys(ReadyNode.ROOT)).thenReturn(Collections.<String>emptyList());
        Assert.assertTrue(readyService.getAllReadyTasks().isEmpty());
        Mockito.verify(regCenter).isExisted(ReadyNode.ROOT);
        Mockito.verify(regCenter).getChildrenKeys(ReadyNode.ROOT);
        Mockito.verify(regCenter, Mockito.times(0)).get((String) ArgumentMatchers.any());
    }
    
    @Test
    public void assertGetAllTasksWhenNodeIsEmpty() {
        Mockito.when(regCenter.isExisted(ReadyNode.ROOT)).thenReturn(true);
        Mockito.when(regCenter.getChildrenKeys(ReadyNode.ROOT)).thenReturn(Lists.newArrayList("test_job"));
        Mockito.when(regCenter.get(ReadyNode.getReadyJobNodePath("test_job"))).thenReturn("");
        Assert.assertTrue(readyService.getAllReadyTasks().isEmpty());
        Mockito.verify(regCenter).isExisted(ReadyNode.ROOT);
        Mockito.verify(regCenter).getChildrenKeys(ReadyNode.ROOT);
        Mockito.verify(regCenter).get(ReadyNode.getReadyJobNodePath("test_job"));
    }
    
    @Test
    public void assertGetAllTasksWithRootNode() {
        Mockito.when(regCenter.isExisted(ReadyNode.ROOT)).thenReturn(true);
        Mockito.when(regCenter.getChildrenKeys(ReadyNode.ROOT)).thenReturn(Lists.newArrayList("test_job_1", "test_job_2"));
        Mockito.when(regCenter.get(ReadyNode.getReadyJobNodePath("test_job_1"))).thenReturn("1");
        Mockito.when(regCenter.get(ReadyNode.getReadyJobNodePath("test_job_2"))).thenReturn("5");
        Map<String, Integer> result = readyService.getAllReadyTasks();
        Assert.assertThat(result.size(), Is.is(2));
        Assert.assertThat(result.get("test_job_1"), Is.is(1));
        Assert.assertThat(result.get("test_job_2"), Is.is(5));
        Mockito.verify(regCenter).isExisted(ReadyNode.ROOT);
        Mockito.verify(regCenter).getChildrenKeys(ReadyNode.ROOT);
        Mockito.verify(regCenter, Mockito.times(2)).get((String) ArgumentMatchers.any());
    }
}
