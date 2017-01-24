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

package com.dangdang.ddframe.job.cloud.scheduler.ha;

import com.dangdang.ddframe.job.cloud.scheduler.mesos.FacadeService;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.StatisticManager;
import com.dangdang.ddframe.job.context.TaskContext;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.netflix.fenzo.TaskScheduler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 协调一致性处理器.
 * 
 * @author gaohongtao
 */
@Slf4j
@RequiredArgsConstructor
public final class ReconcileScheduledService extends AbstractScheduledService {
    
    private static final double DEFAULT_MULTIPLIER = 1.5;
    
    @Getter(AccessLevel.PACKAGE)
    private final Set<TaskContext> remainingTasks = new HashSet<>();
    
    private final FacadeService facadeService;
    
    private final SchedulerDriver scheduler;
    
    private final TaskScheduler taskScheduler;
    
    private final StatisticManager statisticManager;
    
    private long reconcileInterval = 30 * 60 * 1000;
    
    private long retryIntervalUnit = 5 * 60 * 1000;
    
    private long maxPostTimes = 3;
    
    private long latestFetchRemainingMilliSeconds;
    
    private long latestReconcileMilliSeconds;
    
    private int postTimes;
    
    private double currentRePostInterval;
    
    @Override
    protected String serviceName() {
        return "reconcile-processor";
    }
    
    @Override
    protected void runOneIteration() throws Exception {
        if (remainingTasks.isEmpty()) {
            fetchRemaining();
        } else {
            processRemaining();
        }
    }
    
    void fetchRemaining() {
        long now = System.currentTimeMillis();
        if ((latestFetchRemainingMilliSeconds + reconcileInterval) > now) {
            return;
        }
        remainingTasks.addAll(filterRunningTask());
        latestFetchRemainingMilliSeconds = now;
        if (remainingTasks.isEmpty()) {
            return;
        }
        currentRePostInterval = -1;
        postTimes = 0;
        postReconcile();
    }
    
    private Set<TaskContext> filterRunningTask() {
        return Sets.filter(facadeService.getAllRunningDaemonTask(), new Predicate<TaskContext>() {
            @Override
            public boolean apply(final TaskContext input) {
                return input.getUpdatedTime() < latestFetchRemainingMilliSeconds;
            }
        });
    }
    
    private void postReconcile() {
        log.info("Elastic Job - Reconcile: Posting {} tasks", remainingTasks.size());
        scheduler.reconcileTasks(Collections2.transform(remainingTasks, new Function<TaskContext, Protos.TaskStatus>() {
            @Override
            public Protos.TaskStatus apply(final TaskContext input) {
                return Protos.TaskStatus.newBuilder()
                        .setTaskId(Protos.TaskID.newBuilder().setValue(input.getId()).build())
                        .setSlaveId(Protos.SlaveID.newBuilder().setValue(input.getSlaveId()).build())
                        .setState(Protos.TaskState.TASK_RUNNING).build();
            }
        }));
        latestReconcileMilliSeconds = System.currentTimeMillis();
        currentRePostInterval = currentRePostInterval < 0 ? retryIntervalUnit : currentRePostInterval * DEFAULT_MULTIPLIER;
        postTimes++;
        log.info("Elastic Job - Reconcile: Posted {} times, post time : {}, next trigger interval is {}", postTimes, latestReconcileMilliSeconds, currentRePostInterval);
    }
    
    private void processRemaining() {
        Set<TaskContext> runningTasks = Sets.intersection(remainingTasks, filterRunningTask()).immutableCopy();
        remainingTasks.clear();
        if (runningTasks.isEmpty()) {
            log.info("Elastic Job - Reconcile: All tasks have been reconciled");
            return;
        }
        remainingTasks.addAll(runningTasks);
        long nextTriggerReconcileMilliSeconds = latestReconcileMilliSeconds + (long) currentRePostInterval;
        if (System.currentTimeMillis() < nextTriggerReconcileMilliSeconds) {
            log.debug("Elastic Job - Reconcile: Next trigger time : {}", new Date(nextTriggerReconcileMilliSeconds));
            return;
        }
        if (postTimes < maxPostTimes) {
            postReconcile();
            return;
        }
        log.warn("Elastic Job - Reconcile: Reconcile retrying reaches max times, clear task {}", remainingTasks.size());
        for (TaskContext taskContext : remainingTasks) {
            facadeService.removeRunning(taskContext);
            facadeService.recordFailoverTask(taskContext);
            String hostname = facadeService.popMapping(taskContext.getId());
            if (!Strings.isNullOrEmpty(hostname)) {
                taskScheduler.getTaskUnAssigner().call(TaskContext.getIdForUnassignedSlave(taskContext.getId()), hostname);
            }
            statisticManager.taskRunFailed();
        }
        remainingTasks.clear();
    }
    
    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(10, 20, TimeUnit.SECONDS);
    }
    
    @Override
    protected void startUp() throws Exception {
        log.info("Elastic Job - Reconcile: Start {}", serviceName());
        latestFetchRemainingMilliSeconds = System.currentTimeMillis() - reconcileInterval;
    }
    
    @Override
    protected void shutDown() throws Exception {
        log.info("Elastic Job - Reconcile: Stop {}", serviceName());
    }
}
