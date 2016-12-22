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
import java.util.Set;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningService;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.Interval;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.util.StatisticTimeUtils;
import com.dangdang.ddframe.job.context.TaskContext;
import com.dangdang.ddframe.job.statistics.rdb.StatisticRdbRepository;
import com.dangdang.ddframe.job.statistics.type.JobRegisterStatistics;
import com.dangdang.ddframe.job.statistics.type.TaskRunningStatistics;
import com.google.common.base.Optional;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 运行中的任务统计作业.
 *
 * @author liguangyun
 */
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class TaskRunningStatisticJob extends AbstractStatisticJob implements Job {
    
    private RunningService runningService;
    
    private StatisticRdbRepository repository;
    
    /**
     * 构造函数.
     * @param rdbRepository 基于rdb的数据仓库对象
     */
    public TaskRunningStatisticJob(final StatisticRdbRepository rdbRepository) {
        runningService = new RunningService();
        this.repository = rdbRepository;
    }
    
    @Override
    public JobDetail buildJobDetail() {
        return JobBuilder.newJob(this.getClass()).withIdentity(getJobName()).build();
    }
    
    @Override
    public Trigger buildTrigger() {
        return TriggerBuilder.newTrigger()
                .withIdentity(getTriggerName())
                .withSchedule(CronScheduleBuilder.cronSchedule(Interval.MINUTE.getCron())
                .withMisfireHandlingInstructionDoNothing()).build();
    }
    
    @Override
    public Map<String, Object> getDataMap() {
        Map<String, Object> result = new HashMap<>(2);
        result.put("runningService", runningService);
        result.put("repository", repository);
        return result;
    }
    
    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        Optional<TaskRunningStatistics> latestOne = repository.findLatestTaskRunningStatistics();
        if (latestOne.isPresent()) {
            fillBlankIfNeeded(latestOne.get());
        }
        TaskRunningStatistics taskRunningStatistics = new TaskRunningStatistics(getRunningCnt(), StatisticTimeUtils.getCurrentStatisticTime(Interval.MINUTE));
        log.info("Add taskRunningStatistics, info is:{}", taskRunningStatistics);
        repository.add(taskRunningStatistics);
    }
    
    private int getRunningCnt() {
        int runningCnt = 0;
        Map<String, Set<TaskContext>> allRunnintTasks = runningService.getAllRunningTasks();
        for (String each : allRunnintTasks.keySet()) {
            runningCnt += allRunnintTasks.get(each).size();
        }
        return runningCnt;
    }
    
    private void fillBlankIfNeeded(final TaskRunningStatistics latestOne) {
        List<Date> blankDateRange = findBlankStatisticTimes(latestOne.getStatisticsTime(), Interval.HOUR);
        if (!blankDateRange.isEmpty()) {
            log.info("Fill blank range of taskRunningStatistics, info is:{}, range is:{}", latestOne, blankDateRange);
        }
        for (Date each : blankDateRange) {
            repository.add(new JobRegisterStatistics(latestOne.getRunningCount(), each));
        }
    }
}
