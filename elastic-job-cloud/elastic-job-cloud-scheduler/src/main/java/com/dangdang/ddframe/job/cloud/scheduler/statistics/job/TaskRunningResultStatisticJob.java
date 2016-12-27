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

package com.dangdang.ddframe.job.cloud.scheduler.statistics.job;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import com.dangdang.ddframe.job.cloud.scheduler.statistics.Interval;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.TaskRunningResultMetaData;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.util.StatisticTimeUtils;
import com.dangdang.ddframe.job.statistics.rdb.StatisticRdbRepository;
import com.dangdang.ddframe.job.statistics.type.TaskRunningResultStatistics;
import com.dangdang.ddframe.job.statistics.type.TaskRunningResultStatistics.StatisticUnit;
import com.google.common.base.Optional;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务运行结果统计作业.
 *
 * @author liguangyun
 */
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class TaskRunningResultStatisticJob extends AbstractStatisticJob {
    
    private StatisticUnit statisticUnit;
    
    private TaskRunningResultMetaData sharedData;
    
    private StatisticRdbRepository repository;
    
    @Override
    public JobDetail buildJobDetail() {
        JobDetail result = JobBuilder.newJob(this.getClass()).withIdentity(getJobName() + "_" + statisticUnit).build();
        result.getJobDataMap().put("statisticUnit", statisticUnit);
        return result;
    }
    
    @Override
    public Trigger buildTrigger() {
        return TriggerBuilder.newTrigger()
                .withIdentity(getTriggerName() + "_" + statisticUnit)
                .withSchedule(CronScheduleBuilder.cronSchedule(Interval.valueOf(statisticUnit.name()).getCron())
                .withMisfireHandlingInstructionDoNothing()).build();
    }
    
    @Override
    public Map<String, Object> getDataMap() {
        Map<String, Object> result = new HashMap<>(3);
        result.put("statisticUnit", statisticUnit);
        result.put("sharedData", sharedData);
        result.put("repository", repository);
        return result;
    }
    
    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        Optional<TaskRunningResultStatistics> latestOne = repository.findLatestTaskRunningResultStatistics(statisticUnit);
        if (latestOne.isPresent()) {
            fillBlankIfNeeded(latestOne.get());
        }
        TaskRunningResultStatistics taskRunningResultStatistics = new TaskRunningResultStatistics(
                sharedData.getSuccessCount(), sharedData.getFailedCount(), statisticUnit,
                StatisticTimeUtils.getCurrentStatisticTime(Interval.valueOf(statisticUnit.name())));
        log.info("Add taskRunningResultStatistics, info is:{}", taskRunningResultStatistics);
        repository.add(taskRunningResultStatistics);
        sharedData.reset();
    }
    
    private void fillBlankIfNeeded(final TaskRunningResultStatistics latestOne) {
        List<Date> blankDateRange = findBlankStatisticTimes(latestOne.getStatisticsTime(), Interval.valueOf(statisticUnit.name()));
        if (!blankDateRange.isEmpty()) {
            log.info("Fill blank range of taskRunningResultStatistics, info is:{}, range is:{}", latestOne, blankDateRange);
        }
        for (Date each : blankDateRange) {
            repository.add(new TaskRunningResultStatistics(latestOne.getSuccessCount(), latestOne.getFailedCount(), statisticUnit, each));
        }
    }
}
