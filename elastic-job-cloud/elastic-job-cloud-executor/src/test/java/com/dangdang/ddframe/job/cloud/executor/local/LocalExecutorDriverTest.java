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

import org.apache.mesos.Protos;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class LocalExecutorDriverTest {
    
    private CountDownLatch latch;
    
    private LocalExecutorDriver executorDriver;
    
    @Before
    public void assertSetUp() throws Exception {
        latch = new CountDownLatch(1);
        executorDriver = new LocalExecutorDriver(latch);
    }
    
    @Test
    public void assertNoBlock() throws Exception {
        assertThat(executorDriver.start(), is(Protos.Status.DRIVER_RUNNING));
        assertThat(executorDriver.stop(), is(Protos.Status.DRIVER_STOPPED));
    }
    
    @Test
    public void assertRun() throws Exception {
        assertThat(executorDriver.run(), is(Protos.Status.DRIVER_RUNNING));
        assertThat(executorDriver.stop(), is(Protos.Status.DRIVER_STOPPED));
    }
    
    @Test
    public void assertJoin() throws Exception {
        assertThat(executorDriver.start(), is(Protos.Status.DRIVER_RUNNING));
        assertThat(executorDriver.join(), is(Protos.Status.DRIVER_RUNNING));
        assertThat(executorDriver.abort(), is(Protos.Status.DRIVER_ABORTED));
    }
    
    @Test
    public void assertSendStatusUpdate() {
        assertThat(executorDriver.sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder()
                .setValue("1")).setState(Protos.TaskState.TASK_ERROR).build()), is(Protos.Status.DRIVER_NOT_STARTED));
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (final InterruptedException ignored) {
        }
        assertFalse(latch.getCount() > 0);
    }
    
    @Test
    public void assertSendFrameworkMessage() throws Exception {
        assertThat(executorDriver.start(), is(Protos.Status.DRIVER_RUNNING));
        assertThat(executorDriver.sendFrameworkMessage("test".getBytes()), is(Protos.Status.DRIVER_RUNNING));
    }
    
}
