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

package com.dangdang.ddframe.job.cloud.executor.local;

import lombok.AllArgsConstructor;
import org.apache.mesos.Protos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
@AllArgsConstructor
public class TerminalTaskStateTest {
    
    private Protos.TaskState terminalTaskState;
    
    @Parameterized.Parameters
    public static Collection data() {
        return Arrays.asList(new Object[][] {{Protos.TaskState.TASK_ERROR}, {Protos.TaskState.TASK_KILLED}, {Protos.TaskState.TASK_FINISHED}});
    }
    
    @Test
    public void assertSendStatusUpdate() {
        CountDownLatch latch = new CountDownLatch(1);
        LocalExecutorDriver executorDriver = new LocalExecutorDriver(latch);
        assertThat(executorDriver.sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder()
                .setValue("1")).setState(terminalTaskState).build()), is(Protos.Status.DRIVER_NOT_STARTED));
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (final InterruptedException ignored) {
        }
        assertFalse(latch.getCount() > 0);
    }
    
}
