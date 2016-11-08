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
import com.dangdang.ddframe.job.util.concurrent.BlockUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务统计信息处理器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Slf4j
public final class StatisticsProcessor implements Runnable {
    
    private static volatile boolean shutdown;
    
    private final RunningService runningService = new RunningService();
    
    /**
     * 线程关闭.
     */
    public static void shutdown() {
        shutdown = true;
    }
    
    @Override
    public void run() {
        while (!shutdown) {
            log.debug("All running tasks are: " + runningService.getAllRunningTasks());
            BlockUtils.sleep(10000L);
        }
    }
}
