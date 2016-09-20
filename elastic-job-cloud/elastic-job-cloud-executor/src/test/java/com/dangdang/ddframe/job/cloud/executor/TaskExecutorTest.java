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

package com.dangdang.ddframe.job.cloud.executor;

import com.dangdang.ddframe.job.api.JobType;
import com.dangdang.ddframe.job.cloud.executor.fixture.TestJob;
import com.dangdang.ddframe.job.exception.JobSystemException;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.google.protobuf.ByteString;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.ExecutorInfo;
import org.apache.mesos.Protos.FrameworkInfo;
import org.apache.mesos.Protos.SlaveInfo;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.Protos.TaskState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class TaskExecutorTest {
    
    @Mock
    private ExecutorDriver executorDriver;
    
    private ExecutorInfo executorInfo = ExecutorInfo.getDefaultInstance();
    
    private SlaveInfo slaveInfo = SlaveInfo.getDefaultInstance();
    
    private FrameworkInfo frameworkInfo = FrameworkInfo.getDefaultInstance();
    
    private TaskExecutor taskExecutor;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        executorDriver = mock(ExecutorDriver.class);
        taskExecutor = new TaskExecutor();
    }
    
    @Test
    public void assertKillTask() {
        TaskID taskID = Protos.TaskID.newBuilder().setValue("task_id").build();
        taskExecutor.killTask(executorDriver, taskID);
        verify(executorDriver).sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskID).setState(Protos.TaskState.TASK_KILLED).build());
        verify(executorDriver).stop();
    }
    
    @Test
    public void assertLaunchTaskWithDaemonTaskAndJavaSimpleJob() {
        TaskInfo taskInfo = buildTransientTaskInfo();
        taskExecutor.launchTask(executorDriver, taskInfo);
        verify(executorDriver).sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId()).setState(TaskState.TASK_RUNNING).build());
    }
    
    @Test
    public void assertLaunchTaskWithTransientTaskAndSpringSimpleJob() {
        TaskInfo taskInfo = buildDaemonTaskInfo();
        taskExecutor.launchTask(executorDriver, taskInfo);
        verify(executorDriver).sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId()).setState(TaskState.TASK_RUNNING).build());
    }
    
    @Test
    public void assertLaunchTaskWithTransientTaskAndJavaScriptJob() {
        TaskInfo taskInfo = buildScriptDaemonTaskInfo();
        taskExecutor.launchTask(executorDriver, taskInfo);
        verify(executorDriver).sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId()).setState(TaskState.TASK_RUNNING).build());
    }
    
    @Test(expected = JobSystemException.class)
    public void assertLaunchTaskWithWrongClass() throws NoSuchFieldException {
        taskExecutor.launchTask(executorDriver, buildWrongTaskInfo());
    }
    
    @Test(expected = JobSystemException.class)
    public void assertLaunchTaskWithNotElasticJobClass() throws NoSuchFieldException {
        taskExecutor.launchTask(executorDriver, buildNotElasticJobTaskInfo());
    }
    
    @Test
    public void assertOtherOperations() throws NoSuchFieldException {
        taskExecutor.registered(executorDriver, executorInfo, frameworkInfo, slaveInfo);
        taskExecutor.reregistered(executorDriver, slaveInfo);
        taskExecutor.disconnected(executorDriver);
        taskExecutor.frameworkMessage(executorDriver, null);
        taskExecutor.shutdown(executorDriver);
        taskExecutor.error(executorDriver, "");
    }
    
    private TaskInfo buildTransientTaskInfo() {
        return buildTaskInfo(buildSpringJobConfigurationContextMap()).build();
    }
    
    private TaskInfo buildDaemonTaskInfo() {
        return buildTaskInfo(buildBaseJobConfigurationContextMapWithJobClassAndCron(TestJob.class.getCanonicalName(), "ignoredCron")).build();
    }
    
    private TaskInfo buildScriptDaemonTaskInfo() {
        return buildTaskInfo(buildBaseJobConfigurationContextMap(TestJob.class.getCanonicalName(), "ignoredCron", JobType.SCRIPT)).build();
    }
    
    private TaskInfo buildWrongTaskInfo() {
        return buildTaskInfo(buildBaseJobConfigurationContextMapWithJobClass("WrongJobClass")).build();
    }
    
    private TaskInfo buildNotElasticJobTaskInfo() {
        return buildTaskInfo(buildBaseJobConfigurationContextMapWithJobClass(Object.class.getCanonicalName())).build();
    }
    
    private TaskInfo.Builder buildTaskInfo(Map<String, String> jobConfigurationContext) {
        return TaskInfo.newBuilder().setData(ByteString.copyFrom(serialize(jobConfigurationContext)))
                .setName("test_job").setTaskId(Protos.TaskID.newBuilder().setValue("task_id")).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0"));
    }
    
    private byte[] serialize(final Map<String, String> jobConfigurationContext) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>(2, 1);
        ShardingContexts shardingContexts = new ShardingContexts("test_job", 1, "", Collections.singletonMap(1, "a"));
        result.put("shardingContext", shardingContexts);
        result.put("jobConfigContext", jobConfigurationContext);
        return SerializationUtils.serialize(result);
    }
    
    private Map<String, String> buildSpringJobConfigurationContextMap() {
        Map<String, String> context = buildBaseJobConfigurationContextMapWithJobClass(TestJob.class.getCanonicalName());
        context.put("beanName", "testJob");
        context.put("applicationContext", "applicationContext.xml");
        return context;
    }
    
    private Map<String, String> buildBaseJobConfigurationContextMapWithJobClass(String jobClass) {
        return buildBaseJobConfigurationContextMapWithJobClassAndCron(jobClass, "0/1 * * * * ?");
    }
    
    private Map<String, String> buildBaseJobConfigurationContextMapWithJobClassAndCron(String jobClass, String cron) {
        return buildBaseJobConfigurationContextMap(jobClass, cron, JobType.SIMPLE);
    }
    
    private Map<String, String> buildBaseJobConfigurationContextMap(String jobClass, String cron, JobType jobType) {
        Map<String, String> result = new HashMap<>();
        result.put("jobName", "test_job");
        result.put("cron", cron);
        result.put("jobClass", jobClass);
        result.put("jobType", jobType.name());
        result.put("scriptCommandLine", "echo \"\"");
        return result;
    }
}
