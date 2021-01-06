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
import org.apache.shardingsphere.elasticjob.cloud.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.config.pojo.CloudJobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.CloudAppConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.CloudAppConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.pojo.CloudAppConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.context.JobContext;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudAppConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.TaskNode;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.app.DisableAppService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.job.DisableJobService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.failover.FailoverService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.ready.ReadyService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running.RunningService;
import org.apache.shardingsphere.elasticjob.infra.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext.MetaInfo;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void setUp() {
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
        verify(runningService).start();
    }
    
    @Test
    public void assertGetEligibleJobContext() {
        Collection<JobContext> failoverJobContexts = Collections.singletonList(JobContext.from(CloudJobConfigurationBuilder
                .createCloudJobConfiguration("failover_job").toCloudJobConfiguration(), ExecutionType.FAILOVER));
        Collection<JobContext> readyJobContexts = Collections.singletonList(JobContext.from(CloudJobConfigurationBuilder
                .createCloudJobConfiguration("ready_job").toCloudJobConfiguration(), ExecutionType.READY));
        when(failoverService.getAllEligibleJobContexts()).thenReturn(failoverJobContexts);
        when(readyService.getAllEligibleJobContexts(failoverJobContexts)).thenReturn(readyJobContexts);
        Collection<JobContext> actual = facadeService.getEligibleJobContext();
        assertThat(actual.size(), is(2));
        int i = 0;
        for (JobContext each : actual) {
            switch (i) {
                case 0:
                    assertThat(each.getCloudJobConfig().getJobConfig().getJobName(), is("failover_job"));
                    break;
                case 1:
                    assertThat(each.getCloudJobConfig().getJobConfig().getJobName(), is("ready_job"));
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
        verify(failoverService).remove(Collections.singletonList(MetaInfo.from(TaskNode.builder().build().getTaskNodePath())));
        verify(readyService).remove(Sets.newHashSet("test_job"));
    }
    
    @Test
    public void assertAddRunning() {
        TaskContext taskContext = TaskContext.from(TaskNode.builder().build().getTaskNodeValue());
        facadeService.addRunning(taskContext);
        verify(runningService).add(taskContext);
    }
    
    @Test
    public void assertUpdateDaemonStatus() {
        TaskContext taskContext = TaskContext.from(TaskNode.builder().build().getTaskNodeValue());
        facadeService.updateDaemonStatus(taskContext, true);
        verify(runningService).updateIdle(taskContext, true);
    }
    
    @Test
    public void assertRemoveRunning() {
        String taskNodeValue = TaskNode.builder().build().getTaskNodeValue();
        TaskContext taskContext = TaskContext.from(taskNodeValue);
        facadeService.removeRunning(taskContext);
        verify(runningService).remove(taskContext);
    }
    
    @Test
    public void assertRecordFailoverTaskWhenJobConfigNotExisted() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        when(jobConfigService.load("test_job")).thenReturn(Optional.empty());
        facadeService.recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(failoverService, times(0)).add(TaskContext.from(taskNode.getTaskNodeValue()));
    }
    
    @Test
    public void assertRecordFailoverTaskWhenIsFailoverDisabled() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        when(jobConfigService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createOtherCloudJobConfiguration("test_job")));
        facadeService.recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(failoverService, times(0)).add(TaskContext.from(taskNode.getTaskNodeValue()));
    }
    
    @Test
    public void assertRecordFailoverTaskWhenIsFailoverDisabledAndIsDaemonJob() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        when(jobConfigService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job", CloudJobExecutionType.DAEMON)));
        facadeService.recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(failoverService).add(TaskContext.from(taskNode.getTaskNodeValue()));
    }
    
    @Test
    public void assertRecordFailoverTaskWhenIsFailoverEnabled() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        when(jobConfigService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        TaskContext taskContext = TaskContext.from(taskNode.getTaskNodeValue());
        facadeService.recordFailoverTask(taskContext);
        verify(failoverService).add(taskContext);
    }
    
    @Test
    public void assertLoadAppConfig() {
        Optional<CloudAppConfigurationPOJO> appConfigOptional = Optional.of(CloudAppConfigurationBuilder.createCloudAppConfiguration("test_app"));
        when(appConfigService.load("test_app")).thenReturn(appConfigOptional);
        assertThat(facadeService.loadAppConfig("test_app"), is(appConfigOptional));
    }
    
    @Test
    public void assertLoadJobConfig() {
        Optional<CloudJobConfigurationPOJO> cloudJobConfig = Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job"));
        when(jobConfigService.load("test_job")).thenReturn(cloudJobConfig);
        assertThat(facadeService.load("test_job"), is(cloudJobConfig));
    }
    
    @Test
    public void assertLoadAppConfigWhenAbsent() {
        when(appConfigService.load("test_app")).thenReturn(Optional.empty());
        assertThat(facadeService.loadAppConfig("test_app"), is(Optional.<CloudAppConfiguration>empty()));
    }
    
    @Test
    public void assertLoadJobConfigWhenAbsent() {
        when(jobConfigService.load("test_job")).thenReturn(Optional.empty());
        assertThat(facadeService.load("test_job"), is(Optional.<CloudJobConfiguration>empty()));
    }
    
    @Test
    public void assertAddDaemonJobToReadyQueue() {
        when(jobConfigService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        facadeService.addDaemonJobToReadyQueue("test_job");
        verify(readyService).addDaemon("test_job");
    }
    
    @Test
    public void assertIsRunningForReadyJobAndNotRunning() {
        when(runningService.getRunningTasks("test_job")).thenReturn(Collections.emptyList());
        assertFalse(facadeService.isRunning(TaskContext.from(TaskNode.builder().type(ExecutionType.READY).build().getTaskNodeValue())));
    }
    
    @Test
    public void assertIsRunningForFailoverJobAndNotRunning() {
        when(runningService.isTaskRunning(TaskContext.from(TaskNode.builder().type(ExecutionType.FAILOVER).build().getTaskNodeValue()).getMetaInfo())).thenReturn(false);
        assertFalse(facadeService.isRunning(TaskContext.from(TaskNode.builder().type(ExecutionType.FAILOVER).build().getTaskNodeValue())));
    }
    
    @Test
    public void assertIsRunningForFailoverJobAndRunning() {
        when(runningService.isTaskRunning(TaskContext.from(TaskNode.builder().type(ExecutionType.FAILOVER).build().getTaskNodeValue()).getMetaInfo())).thenReturn(true);
        assertTrue(facadeService.isRunning(TaskContext.from(TaskNode.builder().type(ExecutionType.FAILOVER).build().getTaskNodeValue())));
    }
    
    @Test
    public void assertAddMapping() {
        facadeService.addMapping("taskId", "localhost");
        verify(runningService).addMapping("taskId", "localhost");
    }
    
    @Test
    public void assertPopMapping() {
        facadeService.popMapping("taskId");
        verify(runningService).popMapping("taskId");
    }
    
    @Test
    public void assertStop() {
        facadeService.stop();
        verify(runningService).clear();
    }
    
    @Test
    public void assertGetFailoverTaskId() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        when(jobConfigService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        TaskContext taskContext = TaskContext.from(taskNode.getTaskNodeValue());
        facadeService.recordFailoverTask(taskContext);
        verify(failoverService).add(taskContext);
        facadeService.getFailoverTaskId(taskContext.getMetaInfo());
        verify(failoverService).getTaskId(taskContext.getMetaInfo());
    }
    
    @Test
    public void assertJobDisabledWhenAppEnabled() {
        when(jobConfigService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        when(disableAppService.isDisabled("test_app")).thenReturn(false);
        when(disableJobService.isDisabled("test_job")).thenReturn(true);
        assertTrue(facadeService.isJobDisabled("test_job"));
    }
    
    @Test
    public void assertIsJobEnabled() {
        when(jobConfigService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        assertFalse(facadeService.isJobDisabled("test_job"));
    }
    
    @Test
    public void assertIsJobDisabledWhenAppDisabled() {
        when(jobConfigService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        when(disableAppService.isDisabled("test_app")).thenReturn(true);
        assertTrue(facadeService.isJobDisabled("test_job"));
    }
    
    @Test
    public void assertIsJobDisabledWhenAppEnabled() {
        when(jobConfigService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        when(disableAppService.isDisabled("test_app")).thenReturn(false);
        when(disableJobService.isDisabled("test_job")).thenReturn(true);
        assertTrue(facadeService.isJobDisabled("test_job"));
    }
    
    @Test
    public void assertEnableJob() {
        facadeService.enableJob("test_job");
        verify(disableJobService).remove("test_job");
    }
    
    @Test
    public void assertDisableJob() {
        facadeService.disableJob("test_job");
        verify(disableJobService).add("test_job");
    }
    
    @Test
    public void assertLoadExecutor() {
        facadeService.loadExecutorInfo();
        verify(mesosStateService).executors();
    }
}
