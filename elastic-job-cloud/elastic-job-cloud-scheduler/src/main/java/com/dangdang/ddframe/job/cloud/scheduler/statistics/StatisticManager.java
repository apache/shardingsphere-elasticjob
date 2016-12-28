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

package com.dangdang.ddframe.job.cloud.scheduler.statistics;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dangdang.ddframe.job.api.JobType;
import com.dangdang.ddframe.job.cloud.scheduler.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.config.JobExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.job.JobRunningStatisticJob;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.job.RegisteredJobStatisticJob;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.job.TaskRunningResultStatisticJob;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.util.StatisticTimeUtils;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.statistics.StatisticInterval;
import com.dangdang.ddframe.job.statistics.rdb.StatisticRdbRepository;
import com.dangdang.ddframe.job.statistics.type.job.JobExecutionTypeStatistics;
import com.dangdang.ddframe.job.statistics.type.job.JobRegisterStatistics;
import com.dangdang.ddframe.job.statistics.type.job.JobRunningStatistics;
import com.dangdang.ddframe.job.statistics.type.job.JobTypeStatistics;
import com.dangdang.ddframe.job.statistics.type.task.TaskRunningResultStatistics;
import com.dangdang.ddframe.job.statistics.type.task.TaskRunningStatistics;
import com.google.common.base.Optional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 统计作业调度管理器.
 *
 * @author liguangyun
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class StatisticManager {
    
    private static volatile StatisticManager instance; 
    
    private final ConfigurationService configurationService;
    
    private final Optional<JobEventRdbConfiguration> jobEventRdbConfiguration;
    
    private final StatisticsScheduler scheduler;
    
    private final Map<StatisticInterval, TaskRunningResultMetaData> statisticDatas;
    
    private StatisticRdbRepository rdbRepository;
    
    /**
     * 获取统计作业调度管理器.
     *
     * @return 调度管理器对象
     */
    public static StatisticManager getInstance(final CoordinatorRegistryCenter regCenter, final Optional<JobEventRdbConfiguration> jobEventRdbConfiguration) {
        if (null == instance) {
            synchronized (StatisticManager.class) {
                if (null == instance) {
                    Map<StatisticInterval, TaskRunningResultMetaData> statisticDatas = new HashMap<>();
                    statisticDatas.put(StatisticInterval.MINUTE, new TaskRunningResultMetaData());
                    statisticDatas.put(StatisticInterval.HOUR, new TaskRunningResultMetaData());
                    statisticDatas.put(StatisticInterval.DAY, new TaskRunningResultMetaData());
                    instance = new StatisticManager(new ConfigurationService(regCenter), jobEventRdbConfiguration, new StatisticsScheduler(), statisticDatas);
                    init();
                }
            }
        }
        return instance;
    }
    
    private static void init() {
        if (instance.jobEventRdbConfiguration.isPresent()) {
            try {
                instance.rdbRepository = new StatisticRdbRepository(instance.jobEventRdbConfiguration.get().getDataSource());
            } catch (final SQLException ex) {
                log.error("Init StatisticRdbRepository error:", ex);
            }
        }
    }
    
    /**
     * 启动统计作业调度.
     */
    public void startup() {
        if (null != rdbRepository) {
            scheduler.register(new TaskRunningResultStatisticJob(StatisticInterval.MINUTE, statisticDatas.get(StatisticInterval.MINUTE), rdbRepository));
            scheduler.register(new TaskRunningResultStatisticJob(StatisticInterval.HOUR, statisticDatas.get(StatisticInterval.HOUR), rdbRepository));
            scheduler.register(new TaskRunningResultStatisticJob(StatisticInterval.DAY, statisticDatas.get(StatisticInterval.DAY), rdbRepository));
            scheduler.register(new JobRunningStatisticJob(rdbRepository));
            scheduler.register(new RegisteredJobStatisticJob(configurationService, rdbRepository));
        }
    }
    
    /**
     * 停止统计作业调度.
     */
    public void shutdown() {
        scheduler.shutdown();
    }
    
    /**
     * 任务运行成功.
     */
    public void taskRunSuccessfully() {
        statisticDatas.get(StatisticInterval.MINUTE).incrementAndGetSuccessCount();
        statisticDatas.get(StatisticInterval.HOUR).incrementAndGetSuccessCount();
        statisticDatas.get(StatisticInterval.DAY).incrementAndGetSuccessCount();
    }
    
    /**
     * 作业运行失败.
     */
    public void taskRunFailed() {
        statisticDatas.get(StatisticInterval.MINUTE).incrementAndGetFailedCount();
        statisticDatas.get(StatisticInterval.HOUR).incrementAndGetFailedCount();
        statisticDatas.get(StatisticInterval.DAY).incrementAndGetFailedCount();
    }
    
    /**
     * 获取最近一周的任务运行结果统计数据.
     * 
     * @return 任务运行结果统计数据对象
     */
    public TaskRunningResultStatistics getTaskRunningResultStatisticsWeekly() {
        if (null == rdbRepository) {
            return new TaskRunningResultStatistics(0, 0, StatisticInterval.DAY, new Date());
        }
        return rdbRepository.getSummedTaskRunningResultStatistics(StatisticTimeUtils.getStatisticTime(StatisticInterval.DAY, -7), StatisticInterval.DAY);
    }
    
    /**
     * 获取自上线以来的任务运行结果统计数据.
     * 
     * @return 任务运行结果统计数据对象
     */
    public TaskRunningResultStatistics getTaskRunningResultStatisticsSinceOnline() {
        if (null == rdbRepository) {
            return new TaskRunningResultStatistics(0, 0, StatisticInterval.DAY, new Date());
        }
        return rdbRepository.getSummedTaskRunningResultStatistics(getOnlineDate(), StatisticInterval.DAY);
    }
    
    /**
     * 获取作业类型统计数据.
     * 
     * @return 作业类型统计数据对象
     */
    public JobTypeStatistics getJobTypeStatistics() {
        int scriptJobCnt = 0;
        int simpleJobCnt = 0;
        int dataflowJobCnt = 0;
        for (CloudJobConfiguration each : configurationService.loadAll()) {
            if (JobType.SCRIPT.equals(each.getTypeConfig().getJobType())) {
                scriptJobCnt++;
            } else if (JobType.SIMPLE.equals(each.getTypeConfig().getJobType())) {
                simpleJobCnt++;
            } else if (JobType.DATAFLOW.equals(each.getTypeConfig().getJobType())) {
                dataflowJobCnt++;
            }
        }
        return new JobTypeStatistics(scriptJobCnt, simpleJobCnt, dataflowJobCnt);
    }
    
    /**
     * 获取作业执行类型统计数据.
     * 
     * @return 作业执行类型统计数据对象
     */
    public JobExecutionTypeStatistics getJobExecutionTypeStatistics() {
        int transientJobCnt = 0;
        int daemonJobCnt = 0;
        for (CloudJobConfiguration each : configurationService.loadAll()) {
            if (JobExecutionType.TRANSIENT.equals(each.getJobExecutionType())) {
                transientJobCnt++;
            } else if (JobExecutionType.DAEMON.equals(each.getJobExecutionType())) {
                daemonJobCnt++;
            }
        }
        return new JobExecutionTypeStatistics(transientJobCnt, daemonJobCnt);
    }
    
    /**
     * 获取最近一周的运行中的任务统计数据集合.
     * 
     * @return 运行中的任务统计数据对象集合
     */
    public List<TaskRunningStatistics> findTaskRunningStatisticsWeekly() {
        if (null == rdbRepository) {
            return Collections.emptyList();
        }
        return rdbRepository.findTaskRunningStatistics(StatisticTimeUtils.getStatisticTime(StatisticInterval.DAY, -7));
    }
    
    /**
     * 获取最近一周的运行中的作业统计数据集合.
     * 
     * @return 运行中的任务统计数据对象集合
     */
    public List<JobRunningStatistics> findJobRunningStatisticsWeekly() {
        if (null == rdbRepository) {
            return Collections.emptyList();
        }
        return rdbRepository.findJobRunningStatistics(StatisticTimeUtils.getStatisticTime(StatisticInterval.DAY, -7));
    }
    
    /**
     * 获取自上线以来的运行中的任务统计数据集合.
     * 
     * @return 运行中的任务统计数据对象集合
     */
    public List<JobRegisterStatistics> findJobRegisterStatisticsSinceOnline() {
        if (null == rdbRepository) {
            return Collections.emptyList();
        }
        return rdbRepository.findJobRegisterStatistics(getOnlineDate());
    }
    
    private Date getOnlineDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date onlineDate = null;
        try {
            onlineDate = formatter.parse("2016-12-16");
        } catch (final ParseException ex) {
        }
        return onlineDate;
    }
}
