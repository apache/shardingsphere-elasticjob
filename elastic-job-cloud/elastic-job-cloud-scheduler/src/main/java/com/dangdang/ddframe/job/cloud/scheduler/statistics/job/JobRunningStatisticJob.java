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

import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningService;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.util.StatisticTimeUtils;
import com.dangdang.ddframe.job.context.TaskContext;
import com.dangdang.ddframe.job.statistics.StatisticInterval;
import com.dangdang.ddframe.job.statistics.rdb.StatisticRdbRepository;
import com.dangdang.ddframe.job.statistics.type.job.JobRunningStatistics;
import com.dangdang.ddframe.job.statistics.type.task.TaskRunningStatistics;
import com.google.common.base.Optional;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 运行中的任务统计作业.
 *
 * @author liguangyun
 */
@Setter
@NoArgsConstructor
@Slf4j
public class JobRunningStatisticJob extends AbstractStatisticJob {
    
    private RunningService runningService;
    
    private StatisticRdbRepository repository;
    
    private final StatisticInterval execInterval = StatisticInterval.MINUTE;
    
    /**
     * 构造函数.
     * @param rdbRepository 基于rdb的数据仓库对象
     */
    public JobRunningStatisticJob(final StatisticRdbRepository rdbRepository) {
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
                .withSchedule(CronScheduleBuilder.cronSchedule(execInterval.getCron())
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
        Map<String, Set<TaskContext>> allRunnintTasks = runningService.getAllRunningTasks();
        statisticJob(allRunnintTasks);
        statisticTask(allRunnintTasks);
    }
    
    private void statisticJob(final Map<String, Set<TaskContext>> allRunnintTasks) {
        Optional<JobRunningStatistics> latestOne = repository.findLatestJobRunningStatistics();
        if (latestOne.isPresent()) {
            fillBlankIfNeeded(latestOne.get());
        }
        JobRunningStatistics jobRunningStatistics = new JobRunningStatistics(getJobRunningCount(allRunnintTasks), StatisticTimeUtils.getCurrentStatisticTime(execInterval));
        log.debug("Add jobRunningStatistics, runningCount is:{}", getJobRunningCount(allRunnintTasks));
        repository.add(jobRunningStatistics);
    }
    
    private void statisticTask(final Map<String, Set<TaskContext>> allRunnintTasks) {
        Optional<TaskRunningStatistics> latestOne = repository.findLatestTaskRunningStatistics();
        if (latestOne.isPresent()) {
            fillBlankIfNeeded(latestOne.get());
        }
        TaskRunningStatistics taskRunningStatistics = new TaskRunningStatistics(getTaskRunningCount(allRunnintTasks), StatisticTimeUtils.getCurrentStatisticTime(execInterval));
        log.debug("Add taskRunningStatistics, runningCount is:{}", getTaskRunningCount(allRunnintTasks));
        repository.add(taskRunningStatistics);
    }
    
    private int getJobRunningCount(final Map<String, Set<TaskContext>> allRunnintTasks) {
        return allRunnintTasks.keySet().size();
    }
    
    private int getTaskRunningCount(final Map<String, Set<TaskContext>> allRunnintTasks) {
        int runningCount = 0;
        for (String each : allRunnintTasks.keySet()) {
            runningCount += allRunnintTasks.get(each).size();
        }
        return runningCount;
    }
    
    private void fillBlankIfNeeded(final JobRunningStatistics latestOne) {
        List<Date> blankDateRange = findBlankStatisticTimes(latestOne.getStatisticsTime(), execInterval);
        if (!blankDateRange.isEmpty()) {
            log.debug("Fill blank range of jobRunningStatistics, range is:{}", blankDateRange);
        }
        for (Date each : blankDateRange) {
            repository.add(new JobRunningStatistics(latestOne.getRunningCount(), each));
        }
    }
    
    private void fillBlankIfNeeded(final TaskRunningStatistics latestOne) {
        List<Date> blankDateRange = findBlankStatisticTimes(latestOne.getStatisticsTime(), execInterval);
        if (!blankDateRange.isEmpty()) {
            log.debug("Fill blank range of taskRunningStatistics, range is:{}", blankDateRange);
        }
        for (Date each : blankDateRange) {
            repository.add(new TaskRunningStatistics(latestOne.getRunningCount(), each));
        }
    }
}
