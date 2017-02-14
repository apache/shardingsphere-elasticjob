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
import com.dangdang.ddframe.job.cloud.executor.TaskExecutor.TaskThread;
import com.dangdang.ddframe.job.cloud.executor.fixture.TestJob;
import com.dangdang.ddframe.job.context.ExecutionType;
import com.dangdang.ddframe.job.exception.JobSystemException;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.google.protobuf.ByteString;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.Protos.TaskState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class TaskExecutorThreadTest {
    
    @Mock
    private ExecutorDriver executorDriver;
    
    private final String taskId = String.format("%s@-@0@-@%s@-@fake_slave_id@-@0", "test_job", ExecutionType.READY);
    
    @Test
    public void assertLaunchTaskWithDaemonTaskAndJavaSimpleJob() {
        TaskInfo taskInfo = buildJavaTransientTaskInfo();
        TaskThread taskThread = new TaskExecutor().new TaskThread(executorDriver, taskInfo);
        taskThread.run();
        verify(executorDriver).sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId()).setState(TaskState.TASK_RUNNING).build());
        verify(executorDriver).sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId()).setState(Protos.TaskState.TASK_FINISHED).build());
    }
    
    @Test
    public void assertLaunchTaskWithTransientTaskAndSpringSimpleJob() {
        TaskInfo taskInfo = buildSpringDaemonTaskInfo();
        TaskThread taskThread = new TaskExecutor().new TaskThread(executorDriver, taskInfo);
        taskThread.run();
        verify(executorDriver).sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId()).setState(TaskState.TASK_RUNNING).build());
    }
    
    @Test
    public void assertLaunchTaskWithDaemonTaskAndJavaScriptJob() {
        TaskInfo taskInfo = buildSpringScriptTransientTaskInfo();
        TaskThread taskThread = new TaskExecutor().new TaskThread(executorDriver, taskInfo);
        taskThread.run();
        verify(executorDriver).sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId()).setState(TaskState.TASK_RUNNING).build());
        verify(executorDriver).sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId()).setState(Protos.TaskState.TASK_FINISHED).build());
    }
    
    @Test
    public void assertLaunchTaskWithWrongElasticJobClass() {
        TaskInfo taskInfo = buildWrongElasticJobClass();
        TaskThread taskThread = new TaskExecutor().new TaskThread(executorDriver, taskInfo);
        try {
            taskThread.run();
        } catch (final JobSystemException ex) {
            assertTrue(ex.getMessage().startsWith("Elastic-Job: Class 'com.dangdang.ddframe.job.cloud.executor.TaskExecutorThreadTest' must implements ElasticJob interface."));
        }
    }
    
    @Test
    public void assertLaunchTaskWithWrongClass() {
        TaskInfo taskInfo = buildWrongClass();
        TaskThread taskThread = new TaskExecutor().new TaskThread(executorDriver, taskInfo);
        try {
            taskThread.run();    
        } catch (final JobSystemException ex) {
            assertTrue(ex.getMessage().startsWith("Elastic-Job: Class 'WrongClass' initialize failure, the error message is 'WrongClass'."));
        }
    }
    
    private TaskInfo buildWrongClass() {
        return buildTaskInfo(buildBaseJobConfigurationContextMapWithJobClassAndCron("WrongClass", "ignoredCron")).build();
    }
    
    private TaskInfo buildWrongElasticJobClass() {
        return buildTaskInfo(buildBaseJobConfigurationContextMapWithJobClassAndCron(TaskExecutorThreadTest.class.getCanonicalName(), "ignoredCron")).build();
    }
    
    private TaskInfo buildSpringDaemonTaskInfo() {
        return buildTaskInfo(buildSpringJobConfigurationContextMap()).build();
    }
    
    private TaskInfo buildJavaTransientTaskInfo() {
        return buildTaskInfo(buildBaseJobConfigurationContextMapWithJobClassAndCron(TestJob.class.getCanonicalName(), "ignoredCron")).build();
    }
    
    private TaskInfo buildSpringScriptTransientTaskInfo() {
        return buildTaskInfo(buildBaseJobConfigurationContextMap(TestJob.class.getCanonicalName(), "ignoredCron", JobType.SCRIPT)).build();
    }
    
    private TaskInfo.Builder buildTaskInfo(final Map<String, String> jobConfigurationContext) {
        return TaskInfo.newBuilder().setData(ByteString.copyFrom(serialize(jobConfigurationContext)))
                .setName("test_job").setTaskId(TaskID.newBuilder().setValue(taskId)).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0"));
    }
    
    private byte[] serialize(final Map<String, String> jobConfigurationContext) {
        // CHECKSTYLE:OFF
        LinkedHashMap<String, Object> result = new LinkedHashMap<>(2, 1);
        // CHECKSTYLE:ON
        ShardingContexts shardingContexts = new ShardingContexts(taskId, "test_job", 1, "", Collections.singletonMap(1, "a"));
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
    
    private Map<String, String> buildBaseJobConfigurationContextMapWithJobClass(final String jobClass) {
        return buildBaseJobConfigurationContextMapWithJobClassAndCron(jobClass, "0/1 * * * * ?");
    }
    
    private Map<String, String> buildBaseJobConfigurationContextMapWithJobClassAndCron(final String jobClass, final String cron) {
        return buildBaseJobConfigurationContextMap(jobClass, cron, JobType.SIMPLE);
    }
    
    private Map<String, String> buildBaseJobConfigurationContextMap(final String jobClass, final String cron, final JobType jobType) {
        Map<String, String> result = new HashMap<>();
        result.put("jobName", "test_job");
        result.put("cron", cron);
        result.put("jobClass", jobClass);
        result.put("jobType", jobType.name());
        result.put("scriptCommandLine", "echo \"\"");
        return result;
    }
}
