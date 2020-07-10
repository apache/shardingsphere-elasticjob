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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos;

import com.google.common.collect.Sets;
import org.apache.shardingsphere.elasticjob.cloud.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.context.TaskContext;
import org.apache.shardingsphere.elasticjob.cloud.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.CloudAppConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.CloudAppConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.context.JobContext;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudAppConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.TaskNode;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.app.DisableAppService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.job.DisableJobService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.failover.FailoverService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.ready.ReadyService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running.RunningService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;

@RunWith(MockitoJUnitRunner.class)
public final class FacadeServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private CloudAppConfigurationService appConfigService;
    
    @Mock
    private CloudJobConfigurationService jobConfigService;
    
    @Mock
    private ReadyService readyService;
    
    @Mock
    private RunningService runningService;
    
    @Mock
    private FailoverService failoverService;
    
    @Mock
    private DisableAppService disableAppService;
    
    @Mock
    private DisableJobService disableJobService;
    
    @Mock
    private MesosStateService mesosStateService;
    
    private FacadeService facadeService;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        facadeService = new FacadeService(regCenter);
        ReflectionUtils.setFieldValue(facadeService, "jobConfigService", jobConfigService);
        ReflectionUtils.setFieldValue(facadeService, "appConfigService", appConfigService);
        ReflectionUtils.setFieldValue(facadeService, "readyService", readyService);
        ReflectionUtils.setFieldValue(facadeService, "runningService", runningService);
        ReflectionUtils.setFieldValue(facadeService, "failoverService", failoverService);
        ReflectionUtils.setFieldValue(facadeService, "disableAppService", disableAppService);
        ReflectionUtils.setFieldValue(facadeService, "disableJobService", disableJobService);
        ReflectionUtils.setFieldValue(facadeService, "mesosStateService", mesosStateService);
    }
    
    @Test
    public void assertStart() {
        facadeService.start();
        Mockito.verify(runningService).start();
    }
    
    @Test
    public void assertGetEligibleJobContext() {
        Collection<JobContext> failoverJobContexts = Collections.singletonList(JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("failover_job"), ExecutionType.FAILOVER));
        Collection<JobContext> readyJobContexts = Collections.singletonList(JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("ready_job"), ExecutionType.READY));
        Mockito.when(failoverService.getAllEligibleJobContexts()).thenReturn(failoverJobContexts);
        Mockito.when(readyService.getAllEligibleJobContexts(failoverJobContexts)).thenReturn(readyJobContexts);
        Collection<JobContext> actual = facadeService.getEligibleJobContext();
        Assert.assertThat(actual.size(), is(2));
        int i = 0;
        for (JobContext each : actual) {
            switch (i) {
                case 0:
                    Assert.assertThat(each.getJobConfig().getJobName(), is("failover_job"));
                    break;
                case 1:
                    Assert.assertThat(each.getJobConfig().getJobName(), is("ready_job"));
                    break;
                default:
                    break;
            }
            i++;
        }
    }
    
    @Test
    public void assertRemoveLaunchTasksFromQueue() {
        facadeService.removeLaunchTasksFromQueue(Arrays.asList(
                TaskContext.from(TaskNode.builder().type(ExecutionType.FAILOVER).build().getTaskNodeValue()),
                TaskContext.from(TaskNode.builder().build().getTaskNodeValue())));
        Mockito.verify(failoverService).remove(Collections.singletonList(TaskContext.MetaInfo.from(TaskNode.builder().build().getTaskNodePath())));
        Mockito.verify(readyService).remove(Sets.newHashSet("test_job"));
    }
    
    @Test
    public void assertAddRunning() {
        TaskContext taskContext = TaskContext.from(TaskNode.builder().build().getTaskNodeValue());
        facadeService.addRunning(taskContext);
        Mockito.verify(runningService).add(taskContext);
    }
    
    @Test
    public void assertUpdateDaemonStatus() {
        TaskContext taskContext = TaskContext.from(TaskNode.builder().build().getTaskNodeValue());
        facadeService.updateDaemonStatus(taskContext, true);
        Mockito.verify(runningService).updateIdle(taskContext, true);
    }
    
    @Test
    public void assertRemoveRunning() {
        String taskNodeValue = TaskNode.builder().build().getTaskNodeValue();
        TaskContext taskContext = TaskContext.from(taskNodeValue);
        facadeService.removeRunning(taskContext);
        Mockito.verify(runningService).remove(taskContext);
    }
    
    @Test
    public void assertRecordFailoverTaskWhenJobConfigNotExisted() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        Mockito.when(jobConfigService.load("test_job")).thenReturn(Optional.empty());
        facadeService.recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(failoverService, Mockito.times(0)).add(TaskContext.from(taskNode.getTaskNodeValue()));
    }
    
    @Test
    public void assertRecordFailoverTaskWhenIsFailoverDisabled() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        Mockito.when(jobConfigService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createOtherCloudJobConfiguration("test_job")));
        facadeService.recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(failoverService, Mockito.times(0)).add(TaskContext.from(taskNode.getTaskNodeValue()));
    }
    
    @Test
    public void assertRecordFailoverTaskWhenIsFailoverDisabledAndIsDaemonJob() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        Mockito.when(jobConfigService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job", CloudJobExecutionType.DAEMON)));
        facadeService.recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(failoverService).add(TaskContext.from(taskNode.getTaskNodeValue()));
    }
    
    @Test
    public void assertRecordFailoverTaskWhenIsFailoverEnabled() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        Mockito.when(jobConfigService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        TaskContext taskContext = TaskContext.from(taskNode.getTaskNodeValue());
        facadeService.recordFailoverTask(taskContext);
        Mockito.verify(failoverService).add(taskContext);
    }
    
    @Test
    public void assertLoadAppConfig() {
        Optional<CloudAppConfiguration> appConfigOptional = Optional.of(CloudAppConfigurationBuilder.createCloudAppConfiguration("test_app"));
        Mockito.when(appConfigService.load("test_app")).thenReturn(appConfigOptional);
        Assert.assertThat(facadeService.loadAppConfig("test_app"), is(appConfigOptional));
    }
    
    @Test
    public void assertLoadJobConfig() {
        Optional<CloudJobConfiguration> jobConfigOptional = Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job"));
        Mockito.when(jobConfigService.load("test_job")).thenReturn(jobConfigOptional);
        Assert.assertThat(facadeService.load("test_job"), is(jobConfigOptional));
    }
    
    @Test
    public void assertLoadAppConfigWhenAbsent() {
        Mockito.when(appConfigService.load("test_app")).thenReturn(Optional.empty());
        Assert.assertThat(facadeService.loadAppConfig("test_app"), is(Optional.<CloudAppConfiguration>empty()));
    }
    
    @Test
    public void assertLoadJobConfigWhenAbsent() {
        Mockito.when(jobConfigService.load("test_job")).thenReturn(Optional.empty());
        Assert.assertThat(facadeService.load("test_job"), is(Optional.<CloudJobConfiguration>empty()));
    }
    
    @Test
    public void assertAddDaemonJobToReadyQueue() {
        Mockito.when(jobConfigService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        facadeService.addDaemonJobToReadyQueue("test_job");
        Mockito.verify(readyService).addDaemon("test_job");
    }
    
    @Test
    public void assertIsRunningForReadyJobAndNotRunning() {
        Mockito.when(runningService.getRunningTasks("test_job")).thenReturn(Collections.emptyList());
        Assert.assertFalse(facadeService.isRunning(TaskContext.from(TaskNode.builder().type(ExecutionType.READY).build().getTaskNodeValue())));
    }
    
    @Test
    public void assertIsRunningForFailoverJobAndNotRunning() {
        Mockito.when(runningService.isTaskRunning(TaskContext.from(TaskNode.builder().type(ExecutionType.FAILOVER).build().getTaskNodeValue()).getMetaInfo())).thenReturn(false);
        Assert.assertFalse(facadeService.isRunning(TaskContext.from(TaskNode.builder().type(ExecutionType.FAILOVER).build().getTaskNodeValue())));
    }
    
    @Test
    public void assertIsRunningForFailoverJobAndRunning() {
        Mockito.when(runningService.isTaskRunning(TaskContext.from(TaskNode.builder().type(ExecutionType.FAILOVER).build().getTaskNodeValue()).getMetaInfo())).thenReturn(true);
        Assert.assertTrue(facadeService.isRunning(TaskContext.from(TaskNode.builder().type(ExecutionType.FAILOVER).build().getTaskNodeValue())));
    }
    
    @Test
    public void assertAddMapping() {
        facadeService.addMapping("taskId", "localhost");
        Mockito.verify(runningService).addMapping("taskId", "localhost");
    }
    
    @Test
    public void assertPopMapping() {
        facadeService.popMapping("taskId");
        Mockito.verify(runningService).popMapping("taskId");
    }
    
    @Test
    public void assertStop() {
        facadeService.stop();
        Mockito.verify(runningService).clear();
    }
    
    @Test
    public void assertGetFailoverTaskId() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        Mockito.when(jobConfigService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        TaskContext taskContext = TaskContext.from(taskNode.getTaskNodeValue());
        facadeService.recordFailoverTask(taskContext);
        Mockito.verify(failoverService).add(taskContext);
        facadeService.getFailoverTaskId(taskContext.getMetaInfo());
        Mockito.verify(failoverService).getTaskId(taskContext.getMetaInfo());
    }
    
    @Test
    public void assertJobDisabledWhenAppEnabled() {
        Mockito.when(jobConfigService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        Mockito.when(disableAppService.isDisabled("test_app")).thenReturn(false);
        Mockito.when(disableJobService.isDisabled("test_job")).thenReturn(true);
        Assert.assertTrue(facadeService.isJobDisabled("test_job"));
    }
    
    @Test
    public void assertIsJobEnabled() {
        Mockito.when(jobConfigService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        Assert.assertFalse(facadeService.isJobDisabled("test_job"));
    }
    
    @Test
    public void assertIsJobDisabledWhenAppDisabled() {
        Mockito.when(jobConfigService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        Mockito.when(disableAppService.isDisabled("test_app")).thenReturn(true);
        Assert.assertTrue(facadeService.isJobDisabled("test_job"));
    }
    
    @Test
    public void assertIsJobDisabledWhenAppEnabled() {
        Mockito.when(jobConfigService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        Mockito.when(disableAppService.isDisabled("test_app")).thenReturn(false);
        Mockito.when(disableJobService.isDisabled("test_job")).thenReturn(true);
        Assert.assertTrue(facadeService.isJobDisabled("test_job"));
    }
    
    @Test
    public void assertEnableJob() {
        facadeService.enableJob("test_job");
        Mockito.verify(disableJobService).remove("test_job");
    }
    
    @Test
    public void assertDisableJob() {
        facadeService.disableJob("test_job");
        Mockito.verify(disableJobService).add("test_job");
    }
    
    @Test
    public void assertLoadExecutor() throws Exception {
        facadeService.loadExecutorInfo();
        Mockito.verify(mesosStateService).executors();
    }
}
