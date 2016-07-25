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

package com.dangdang.ddframe.job.lite.internal.statistics;

import com.dangdang.ddframe.job.api.dataflow.ProcessCountStatistics;
import com.dangdang.ddframe.job.lite.api.config.JobConfiguration;
import com.dangdang.ddframe.job.lite.internal.server.ServerService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;

/**
 * 统计处理数据数量的作业.
 * 
 * @author zhangliang
 */
public final class ProcessCountJob implements Runnable {
    
    private final JobConfiguration jobConfiguration;
    
    private final ServerService serverService;
    
    public ProcessCountJob(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration) {
        this.jobConfiguration = jobConfiguration;
        serverService = new ServerService(coordinatorRegistryCenter, jobConfiguration);
    }
    
    @Override
    public void run() {
        String jobName = jobConfiguration.getJobName();
        serverService.persistProcessSuccessCount(ProcessCountStatistics.getProcessSuccessCount(jobName));
        serverService.persistProcessFailureCount(ProcessCountStatistics.getProcessFailureCount(jobName));
        ProcessCountStatistics.reset(jobName);
    }
}
