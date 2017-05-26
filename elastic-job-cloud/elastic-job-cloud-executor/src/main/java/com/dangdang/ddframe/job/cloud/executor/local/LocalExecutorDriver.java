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

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.Protos;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * 本地模式运行时ExecutorDriver的仿造对象.
 * 
 * @author gaohongtao
 */
@Slf4j
@RequiredArgsConstructor
public final class LocalExecutorDriver implements ExecutorDriver {
    
    private static final Set<Protos.TaskState> TERMINAL_STATE = Sets.newHashSet(Protos.TaskState.TASK_ERROR, Protos.TaskState.TASK_FINISHED, Protos.TaskState.TASK_KILLED);
    
    private volatile Protos.Status driverStatus = Protos.Status.DRIVER_NOT_STARTED;
    
    private final CountDownLatch latch;
    
    @Override
    public Protos.Status start() {
        log.info("Driver is starting");
        return driverStatus = Protos.Status.DRIVER_RUNNING;
    }
    
    @Override
    public Protos.Status stop() {
        log.info("Driver is stopped");
        return driverStatus = Protos.Status.DRIVER_STOPPED;
    }
    
    @Override
    public Protos.Status abort() {
        log.info("Driver is aborted");
        return driverStatus = Protos.Status.DRIVER_ABORTED;
    }
    
    @Override
    public Protos.Status join() {
        log.info("Waiting for driver to be aborted");
        return driverStatus;
    }
    
    @Override
    public Protos.Status run() {
        log.info("Driver is running");
        start();
        return join();
    }
    
    @Override
    public Protos.Status sendStatusUpdate(final Protos.TaskStatus status) {
        log.info("Task driverStatus is {}", status);
        if (TERMINAL_STATE.contains(status.getState())) {
            latch.countDown();
        }
        return driverStatus;
    }
    
    @Override
    public Protos.Status sendFrameworkMessage(final byte[] data) {
        log.info("The message of send to framework is {}", data);
        return driverStatus;
    }
}
