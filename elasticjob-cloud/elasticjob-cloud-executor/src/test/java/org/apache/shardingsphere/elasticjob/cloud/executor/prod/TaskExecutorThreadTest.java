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

package org.apache.shardingsphere.elasticjob.cloud.executor.prod;

import com.google.protobuf.ByteString;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.Protos.TaskState;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.executor.fixture.TestSimpleJob;
import org.apache.shardingsphere.elasticjob.cloud.executor.prod.TaskExecutor.TaskThread;
import org.apache.shardingsphere.elasticjob.infra.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.script.props.ScriptJobProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.LinkedHashMap;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class TaskExecutorThreadTest {
    
    @Mock
    private ExecutorDriver executorDriver;
    
    private final String taskId = String.format("%s@-@0@-@%s@-@fake_slave_id@-@0", "test_job", ExecutionType.READY);
    
    @Test
    public void assertLaunchTaskWithDaemonTaskAndJavaSimpleJob() {
        TaskInfo taskInfo = buildJavaTransientTaskInfo();
        TaskThread taskThread = new TaskExecutor(new TestSimpleJob()).new TaskThread(executorDriver, taskInfo);
        taskThread.run();
        verify(executorDriver).sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId()).setState(TaskState.TASK_RUNNING).build());
        verify(executorDriver).sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId()).setState(TaskState.TASK_FINISHED).build());
    }
    
    @Test
    public void assertLaunchTaskWithDaemonTaskAndJavaScriptJob() {
        TaskInfo taskInfo = buildSpringScriptTransientTaskInfo();
        TaskThread taskThread = new TaskExecutor(new TestSimpleJob()).new TaskThread(executorDriver, taskInfo);
        taskThread.run();
        verify(executorDriver).sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId()).setState(TaskState.TASK_RUNNING).build());
        verify(executorDriver).sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId()).setState(TaskState.TASK_FINISHED).build());
    }
    
    private TaskInfo buildJavaTransientTaskInfo() {
        return buildTaskInfo(buildJobConfigurationYaml()).build();
    }
    
    private TaskInfo buildSpringScriptTransientTaskInfo() {
        return buildTaskInfo(buildJobConfigurationYaml()).build();
    }
    
    private TaskInfo.Builder buildTaskInfo(final String jobConfigurationYaml) {
        return TaskInfo.newBuilder().setData(ByteString.copyFrom(serialize(jobConfigurationYaml)))
                .setName("test_job").setTaskId(TaskID.newBuilder().setValue(taskId)).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0"));
    }
    
    private byte[] serialize(final String jobConfigurationYaml) {
        // CHECKSTYLE:OFF
        LinkedHashMap<String, Object> result = new LinkedHashMap<>(2, 1);
        // CHECKSTYLE:ON
        ShardingContexts shardingContexts = new ShardingContexts(taskId, "test_job", 1, "", Collections.singletonMap(1, "a"));
        result.put("shardingContext", shardingContexts);
        result.put("jobConfigContext", jobConfigurationYaml);
        return SerializationUtils.serialize(result);
    }
    
    private String buildJobConfigurationYaml() {
        return YamlEngine.marshal(JobConfigurationPOJO.fromJobConfiguration(JobConfiguration.newBuilder("test_job", 1).setProperty(ScriptJobProperties.SCRIPT_KEY, "echo test").build()));
    }
}
