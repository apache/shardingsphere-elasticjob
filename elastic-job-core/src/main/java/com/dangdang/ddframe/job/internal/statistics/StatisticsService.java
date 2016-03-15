/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.internal.statistics;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.internal.config.ConfigurationService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;

/**
 * 作业统计信息服务.
 * 
 * @author zhangliang
 */
public class StatisticsService {
    
    private final CoordinatorRegistryCenter coordinatorRegistryCenter;
    
    private final JobConfiguration jobConfiguration;
    
    private final ConfigurationService configService;
    
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    
    public StatisticsService(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration) {
        this.coordinatorRegistryCenter = coordinatorRegistryCenter;
        this.jobConfiguration = jobConfiguration;
        configService = new ConfigurationService(coordinatorRegistryCenter, jobConfiguration);
    }
    
    /**
     * 开启统计处理数据数量的作业.
     */
    public void startProcessCountJob() {
        int processCountIntervalSeconds = configService.getProcessCountIntervalSeconds();
        if (processCountIntervalSeconds > 0) {
            scheduledExecutorService.scheduleAtFixedRate(new ProcessCountJob(coordinatorRegistryCenter, jobConfiguration), processCountIntervalSeconds, processCountIntervalSeconds, TimeUnit.SECONDS);
        }
    }
    
    /**
     * 停止统计处理数据数量的作业.
     */
    public void stopProcessCountJob() {
        scheduledExecutorService.shutdown();
    }
}
