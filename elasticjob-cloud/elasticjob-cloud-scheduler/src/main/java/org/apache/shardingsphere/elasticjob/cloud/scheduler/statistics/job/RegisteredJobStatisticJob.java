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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.job;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.util.StatisticTimeUtils;
import org.apache.shardingsphere.elasticjob.cloud.statistics.StatisticInterval;
import org.apache.shardingsphere.elasticjob.cloud.statistics.rdb.StatisticRdbRepository;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobRegisterStatistics;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registered job statistic.
 */
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public final class RegisteredJobStatisticJob extends AbstractStatisticJob {
    
    private CloudJobConfigurationService configurationService;
    
    private StatisticRdbRepository repository;
    
    private final StatisticInterval execInterval = StatisticInterval.DAY;
    
    @Override
    public JobDetail buildJobDetail() {
        return JobBuilder.newJob(this.getClass()).withIdentity(getJobName()).build();
    }
    
    @Override
    public Trigger buildTrigger() {
        return TriggerBuilder.newTrigger()
                .withIdentity(getTriggerName())
                .withSchedule(CronScheduleBuilder.cronSchedule(execInterval.getCron())
                .withMisfireHandlingInstructionDoNothing()).build();
    }
    
    @Override
    public Map<String, Object> getDataMap() {
        Map<String, Object> result = new HashMap<>(2);
        result.put("configurationService", configurationService);
        result.put("repository", repository);
        return result;
    }
    
    @Override
    public void execute(final JobExecutionContext context) {
        Optional<JobRegisterStatistics> latestOne = repository.findLatestJobRegisterStatistics();
        latestOne.ifPresent(this::fillBlankIfNeeded);
        int registeredCount = configurationService.loadAll().size();
        JobRegisterStatistics jobRegisterStatistics = new JobRegisterStatistics(registeredCount, StatisticTimeUtils.getCurrentStatisticTime(execInterval));
        log.debug("Add jobRegisterStatistics, registeredCount is:{}", registeredCount);
        repository.add(jobRegisterStatistics);
    }
    
    private void fillBlankIfNeeded(final JobRegisterStatistics latestOne) {
        List<Date> blankDateRange = findBlankStatisticTimes(latestOne.getStatisticsTime(), execInterval);
        if (!blankDateRange.isEmpty()) {
            log.debug("Fill blank range of jobRegisterStatistics, range is:{}", blankDateRange);
        }
        for (Date each : blankDateRange) {
            repository.add(new JobRegisterStatistics(latestOne.getRegisteredCount(), each));
        }
    }
}
