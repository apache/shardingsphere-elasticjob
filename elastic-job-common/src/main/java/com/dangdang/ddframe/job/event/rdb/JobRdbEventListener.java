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

package com.dangdang.ddframe.job.event.rdb;

import com.dangdang.ddframe.job.event.JobEventListener;
import com.dangdang.ddframe.job.event.JobExecutionEvent;
import com.dangdang.ddframe.job.event.JobTraceEvent;

/**
 * 运行痕迹事件数据库监听器.
 *
 * @author caohao
 */
public final class JobRdbEventListener implements JobEventListener {
    
    private final JobRdbEventStorage repository;
    
    public JobRdbEventListener(final JobRdbEventConfiguration config) {
        repository = new JobRdbEventStorage(config.getDriverClassName(), config.getUrl(), config.getUsername(), config.getPassword(), config.getLogLevel());
    }
    
    @Override
    public String getName() {
        return "rdb";
    }
    
    @Override
    public void listen(final JobTraceEvent traceEvent) {
        repository.addJobTraceEvent(traceEvent);
    }
    
    @Override
    public void listen(final JobExecutionEvent executionEvent) {
        repository.addJobExecutionEvent(executionEvent);
    }
}
