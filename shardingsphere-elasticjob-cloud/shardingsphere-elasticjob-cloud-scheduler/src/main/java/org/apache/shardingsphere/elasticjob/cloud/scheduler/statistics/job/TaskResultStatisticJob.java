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
import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.TaskResultMetaData;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.util.StatisticTimeUtils;
import org.apache.shardingsphere.elasticjob.cloud.statistics.StatisticInterval;
import org.apache.shardingsphere.elasticjob.cloud.statistics.rdb.StatisticRdbRepository;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskResultStatistics;
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
 * Task result statistic.
 */
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public final class TaskResultStatisticJob extends AbstractStatisticJob {
    
    private StatisticInterval statisticInterval;
    
    private TaskResultMetaData sharedData;
    
    private StatisticRdbRepository repository;
    
    @Override
    public JobDetail buildJobDetail() {
        JobDetail result = JobBuilder.newJob(this.getClass()).withIdentity(getJobName() + "_" + statisticInterval).build();
        result.getJobDataMap().put("statisticUnit", statisticInterval);
        return result;
    }
    
    @Override
    public Trigger buildTrigger() {
        return TriggerBuilder.newTrigger()
                .withIdentity(getTriggerName() + "_" + statisticInterval)
                .withSchedule(CronScheduleBuilder.cronSchedule(statisticInterval.getCron())
                .withMisfireHandlingInstructionDoNothing()).build();
    }
    
    @Override
    public Map<String, Object> getDataMap() {
        Map<String, Object> result = new HashMap<>(3);
        result.put("statisticInterval", statisticInterval);
        result.put("sharedData", sharedData);
        result.put("repository", repository);
        return result;
    }
    
    @Override
    public void execute(final JobExecutionContext context) {
        Optional<TaskResultStatistics> latestOne = repository.findLatestTaskResultStatistics(statisticInterval);
        latestOne.ifPresent(this::fillBlankIfNeeded);
        TaskResultStatistics taskResultStatistics = new TaskResultStatistics(
                sharedData.getSuccessCount(), sharedData.getFailedCount(), statisticInterval,
                StatisticTimeUtils.getCurrentStatisticTime(statisticInterval));
        log.debug("Add taskResultStatistics, statisticInterval is:{}, successCount is:{}, failedCount is:{}", 
                statisticInterval, sharedData.getSuccessCount(), sharedData.getFailedCount());
        repository.add(taskResultStatistics);
        sharedData.reset();
    }
    
    private void fillBlankIfNeeded(final TaskResultStatistics latestOne) {
        List<Date> blankDateRange = findBlankStatisticTimes(latestOne.getStatisticsTime(), statisticInterval);
        if (!blankDateRange.isEmpty()) {
            log.debug("Fill blank range of taskResultStatistics, range is:{}", blankDateRange);
        }
        for (Date each : blankDateRange) {
            repository.add(new TaskResultStatistics(latestOne.getSuccessCount(), latestOne.getFailedCount(), statisticInterval, each));
        }
    }
}
