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

package io.elasticjob.cloud.scheduler.statistics.job;

import io.elasticjob.reg.base.CoordinatorRegistryCenter;
import io.elasticjob.cloud.scheduler.state.running.RunningService;
import io.elasticjob.cloud.scheduler.statistics.util.StatisticTimeUtils;
import io.elasticjob.context.TaskContext;
import io.elasticjob.statistics.StatisticInterval;
import io.elasticjob.statistics.rdb.StatisticRdbRepository;
import io.elasticjob.statistics.type.job.JobRunningStatistics;
import io.elasticjob.statistics.type.task.TaskRunningStatistics;
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
public final class JobRunningStatisticJob extends AbstractStatisticJob {
    
    private static final StatisticInterval EXECUTE_INTERVAL = StatisticInterval.MINUTE;
    
    private RunningService runningService;
    
    private StatisticRdbRepository repository;
    
    /**
     * 构造函数.
     * @param registryCenter 注册中心
     * @param rdbRepository 基于rdb的数据仓库对象
     */
    public JobRunningStatisticJob(final CoordinatorRegistryCenter registryCenter, final StatisticRdbRepository rdbRepository) {
        runningService = new RunningService(registryCenter);
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
                .withSchedule(CronScheduleBuilder.cronSchedule(EXECUTE_INTERVAL.getCron())
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
        Map<String, Set<TaskContext>> allRunningTasks = runningService.getAllRunningTasks();
        statisticJob(getJobRunningCount(allRunningTasks));
        statisticTask(getTaskRunningCount(allRunningTasks));
    }
    
    private void statisticJob(final int runningCount) {
        Optional<JobRunningStatistics> latestOne = repository.findLatestJobRunningStatistics();
        if (latestOne.isPresent()) {
            fillBlankIfNeeded(latestOne.get());
        }
        JobRunningStatistics jobRunningStatistics = new JobRunningStatistics(runningCount, StatisticTimeUtils.getCurrentStatisticTime(EXECUTE_INTERVAL));
        log.debug("Add jobRunningStatistics, runningCount is:{}", runningCount);
        repository.add(jobRunningStatistics);
    }
    
    private void statisticTask(final int runningCount) {
        Optional<TaskRunningStatistics> latestOne = repository.findLatestTaskRunningStatistics();
        if (latestOne.isPresent()) {
            fillBlankIfNeeded(latestOne.get());
        }
        TaskRunningStatistics taskRunningStatistics = new TaskRunningStatistics(runningCount, StatisticTimeUtils.getCurrentStatisticTime(EXECUTE_INTERVAL));
        log.debug("Add taskRunningStatistics, runningCount is:{}", runningCount);
        repository.add(taskRunningStatistics);
    }
    
    private int getJobRunningCount(final Map<String, Set<TaskContext>> allRunningTasks) {
        int result = 0;
        for (Map.Entry<String, Set<TaskContext>> entry : allRunningTasks.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                result++;
            }
        }
        return result;
    }
    
    private int getTaskRunningCount(final Map<String, Set<TaskContext>> allRunningTasks) {
        int result = 0;
        for (Map.Entry<String, Set<TaskContext>> entry : allRunningTasks.entrySet()) {
            result += entry.getValue().size();
        }
        return result;
    }
    
    private void fillBlankIfNeeded(final JobRunningStatistics latestOne) {
        List<Date> blankDateRange = findBlankStatisticTimes(latestOne.getStatisticsTime(), EXECUTE_INTERVAL);
        if (!blankDateRange.isEmpty()) {
            log.debug("Fill blank range of jobRunningStatistics, range is:{}", blankDateRange);
        }
        for (Date each : blankDateRange) {
            repository.add(new JobRunningStatistics(latestOne.getRunningCount(), each));
        }
    }
    
    private void fillBlankIfNeeded(final TaskRunningStatistics latestOne) {
        List<Date> blankDateRange = findBlankStatisticTimes(latestOne.getStatisticsTime(), EXECUTE_INTERVAL);
        if (!blankDateRange.isEmpty()) {
            log.debug("Fill blank range of taskRunningStatistics, range is:{}", blankDateRange);
        }
        for (Date each : blankDateRange) {
            repository.add(new TaskRunningStatistics(latestOne.getRunningCount(), each));
        }
    }
}
