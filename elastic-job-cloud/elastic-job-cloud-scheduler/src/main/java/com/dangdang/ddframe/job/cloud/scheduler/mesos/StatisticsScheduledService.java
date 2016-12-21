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

package com.dangdang.ddframe.job.cloud.scheduler.mesos;

import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningService;
import com.google.common.util.concurrent.AbstractScheduledService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 任务统计信息处理器.
 * 
 * @author zhangliang
 * @author gaohongtao
 */
@Slf4j
public class StatisticsScheduledService extends AbstractScheduledService {
    
    private final RunningService runningService = new RunningService();
    
    @Override
    protected String serviceName() {
        return "statistics-processor";
    }
    
    @Override
    protected void runOneIteration() throws Exception {
        log.debug("Elastic job: All running tasks are: " + runningService.getAllRunningTasks());
    }
    
    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(1, 10, TimeUnit.SECONDS);
    }
    
    @Override
    protected void startUp() throws Exception {
        log.info("Elastic Job: Start {}", serviceName());
    }
    
    @Override
    protected void shutDown() throws Exception {
        log.info("Elastic Job: Stop {}", serviceName());
    }
}
