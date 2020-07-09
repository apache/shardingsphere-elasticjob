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

import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.BootstrapEnvironment;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.FrameworkConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.context.TaskContext;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.util.concurrent.AbstractScheduledService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Reconcile service.
 */
@RequiredArgsConstructor
@Slf4j
public class ReconcileService extends AbstractScheduledService {
    
    private final SchedulerDriver schedulerDriver;
    
    private final FacadeService facadeService;
    
    private final ReentrantLock lock = new ReentrantLock();
    
    @Override
    protected void runOneIteration() throws Exception {
        lock.lock();
        try {
            explicitReconcile();
            implicitReconcile();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Explicit reconcile service.
     */
    public void explicitReconcile() {
        lock.lock();
        try {
            Set<TaskContext> runningTask = new HashSet<>();
            for (Set<TaskContext> each : facadeService.getAllRunningTasks().values()) {
                runningTask.addAll(each);
            }
            if (runningTask.isEmpty()) {
                return;
            }
            log.info("Requesting {} tasks reconciliation with the Mesos master", runningTask.size());
            schedulerDriver.reconcileTasks(Collections2.transform(runningTask, new Function<TaskContext, Protos.TaskStatus>() {
                @Override
                public Protos.TaskStatus apply(final TaskContext input) {
                    return Protos.TaskStatus.newBuilder()
                            .setTaskId(Protos.TaskID.newBuilder().setValue(input.getId()).build())
                            .setSlaveId(Protos.SlaveID.newBuilder().setValue(input.getSlaveId()).build())
                            .setState(Protos.TaskState.TASK_RUNNING).build();
                }
            }));
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Implicit reconcile service.
     */
    public void implicitReconcile() {
        lock.lock();
        try {
            schedulerDriver.reconcileTasks(Collections.<Protos.TaskStatus>emptyList());
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    protected Scheduler scheduler() {
        FrameworkConfiguration configuration = BootstrapEnvironment.getInstance().getFrameworkConfiguration();
        return Scheduler.newFixedDelaySchedule(configuration.getReconcileIntervalMinutes(), configuration.getReconcileIntervalMinutes(), TimeUnit.MINUTES);
    }
}
