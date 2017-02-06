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

import com.google.protobuf.ByteString;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.ExecutorInfo;
import org.apache.mesos.Protos.FrameworkInfo;
import org.apache.mesos.Protos.SlaveInfo;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class TaskExecutorTest {
    
    @Mock
    private ExecutorDriver executorDriver;
    
    @Mock
    private ExecutorService executorService;
    
    private ExecutorInfo executorInfo;
    
    private SlaveInfo slaveInfo = SlaveInfo.getDefaultInstance();
    
    private FrameworkInfo frameworkInfo = FrameworkInfo.getDefaultInstance();
    
    private TaskExecutor taskExecutor;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        taskExecutor = new TaskExecutor();
        ReflectionUtils.setFieldValue(taskExecutor, "executorService", executorService);
        executorInfo = ExecutorInfo.getDefaultInstance();
    }
    
    @Test
    public void assertKillTask() {
        TaskID taskID = Protos.TaskID.newBuilder().setValue("task_id").build();
        taskExecutor.killTask(executorDriver, taskID);
        verify(executorDriver).sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskID).setState(Protos.TaskState.TASK_KILLED).build());
    }
    
    @Test
    public void assertRegisteredWithoutData() {
        // CHECKSTYLE:OFF
        HashMap<String, String> data = new HashMap<>(4, 1);
        // CHECKSTYLE:ON
        data.put("event_trace_rdb_driver", "org.h2.Driver");
        data.put("event_trace_rdb_url", "jdbc:h2:mem:test_executor");
        data.put("event_trace_rdb_username", "sa");
        data.put("event_trace_rdb_password", "");
        ExecutorInfo executorInfo = ExecutorInfo.newBuilder().setExecutorId(Protos.ExecutorID.newBuilder().setValue("test_executor")).setCommand(Protos.CommandInfo.getDefaultInstance())
                .setData(ByteString.copyFrom(SerializationUtils.serialize(data))).build();
        taskExecutor.registered(executorDriver, executorInfo, frameworkInfo, slaveInfo);
    }
    
    @Test
    public void assertRegisteredWithData() {
        taskExecutor.registered(executorDriver, executorInfo, frameworkInfo, slaveInfo);
    }
    
    @Test
    public void assertLaunchTask() {
        taskExecutor.launchTask(executorDriver, TaskInfo.newBuilder().setName("test_job")
                .setTaskId(TaskID.newBuilder().setValue("fake_task_id")).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
    }
    
    @Test
    public void assertReregistered() {
        taskExecutor.reregistered(executorDriver, slaveInfo);
    }
    
    @Test
    public void assertDisconnected() {
        taskExecutor.disconnected(executorDriver);
    }
    
    @Test
    public void assertFrameworkMessage() {
        taskExecutor.frameworkMessage(executorDriver, null);
    }
    
    @Test
    public void assertShutdown() {
        taskExecutor.shutdown(executorDriver);
    }
    
    @Test
    public void assertError() {
        taskExecutor.error(executorDriver, "");
    }
}
