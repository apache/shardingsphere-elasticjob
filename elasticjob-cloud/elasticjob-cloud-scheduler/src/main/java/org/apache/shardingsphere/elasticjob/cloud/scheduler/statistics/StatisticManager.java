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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.config.pojo.CloudJobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.job.JobRunningStatisticJob;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.job.RegisteredJobStatisticJob;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.job.TaskResultStatisticJob;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.util.StatisticTimeUtils;
import org.apache.shardingsphere.elasticjob.cloud.statistics.StatisticInterval;
import org.apache.shardingsphere.elasticjob.cloud.statistics.rdb.StatisticRdbRepository;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobExecutionTypeStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobRegisterStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobRunningStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskResultStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskRunningStatistics;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Statistic manager.
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatisticManager {
    
    private static volatile StatisticManager instance;
    
    private final CoordinatorRegistryCenter registryCenter;
    
    private final CloudJobConfigurationService configurationService;
    
    private final TracingConfiguration tracingConfiguration;
    
    private final StatisticsScheduler scheduler;
    
    private final Map<StatisticInterval, TaskResultMetaData> statisticData;
    
    private StatisticRdbRepository rdbRepository;
    
    private StatisticManager(final CoordinatorRegistryCenter registryCenter, final TracingConfiguration tracingConfiguration,
                             final StatisticsScheduler scheduler, final Map<StatisticInterval, TaskResultMetaData> statisticData) {
        this.registryCenter = registryCenter;
        this.configurationService = new CloudJobConfigurationService(registryCenter);
        this.tracingConfiguration = tracingConfiguration;
        this.scheduler = scheduler;
        this.statisticData = statisticData;
    }
    
    /**
     * Get statistic manager.
     * 
     * @param regCenter registry center
     * @param tracingConfiguration tracing configuration
     * @return statistic manager
     */
    public static StatisticManager getInstance(final CoordinatorRegistryCenter regCenter, final TracingConfiguration tracingConfiguration) {
        if (null == instance) {
            synchronized (StatisticManager.class) {
                if (null == instance) {
                    Map<StatisticInterval, TaskResultMetaData> statisticData = new HashMap<>();
                    statisticData.put(StatisticInterval.MINUTE, new TaskResultMetaData());
                    statisticData.put(StatisticInterval.HOUR, new TaskResultMetaData());
                    statisticData.put(StatisticInterval.DAY, new TaskResultMetaData());
                    instance = new StatisticManager(regCenter, tracingConfiguration, new StatisticsScheduler(), statisticData);
                    init();
                }
            }
        }
        return instance;
    }
    
    private static void init() {
        if (null != instance.tracingConfiguration) {
            try {
                instance.rdbRepository = new StatisticRdbRepository((DataSource) instance.tracingConfiguration.getTracingStorageConfiguration().getStorage());
            } catch (final SQLException ex) {
                log.error("Init StatisticRdbRepository error:", ex);
            }
        }
    }
    
    /**
     * Startup.
     */
    public void startup() {
        if (null != rdbRepository) {
            scheduler.start();
            scheduler.register(new TaskResultStatisticJob(StatisticInterval.MINUTE, statisticData.get(StatisticInterval.MINUTE), rdbRepository));
            scheduler.register(new TaskResultStatisticJob(StatisticInterval.HOUR, statisticData.get(StatisticInterval.HOUR), rdbRepository));
            scheduler.register(new TaskResultStatisticJob(StatisticInterval.DAY, statisticData.get(StatisticInterval.DAY), rdbRepository));
            scheduler.register(new JobRunningStatisticJob(registryCenter, rdbRepository));
            scheduler.register(new RegisteredJobStatisticJob(configurationService, rdbRepository));
        }
    }
    
    /**
     * Shutdown.
     */
    public void shutdown() {
        scheduler.shutdown();
    }
    
    /**
     * Run task successfully.
     */
    public void taskRunSuccessfully() {
        statisticData.get(StatisticInterval.MINUTE).incrementAndGetSuccessCount();
        statisticData.get(StatisticInterval.HOUR).incrementAndGetSuccessCount();
        statisticData.get(StatisticInterval.DAY).incrementAndGetSuccessCount();
    }
    
    /**
     * Run task failed.
     */
    public void taskRunFailed() {
        statisticData.get(StatisticInterval.MINUTE).incrementAndGetFailedCount();
        statisticData.get(StatisticInterval.HOUR).incrementAndGetFailedCount();
        statisticData.get(StatisticInterval.DAY).incrementAndGetFailedCount();
    }
    
    private boolean isRdbConfigured() {
        return null != rdbRepository;
    }
    
    /**
     * Get statistic of the recent week.
     * @return task result statistic
     */
    public TaskResultStatistics getTaskResultStatisticsWeekly() {
        if (!isRdbConfigured()) {
            return new TaskResultStatistics(0, 0, StatisticInterval.DAY, new Date());
        }
        return rdbRepository.getSummedTaskResultStatistics(StatisticTimeUtils.getStatisticTime(StatisticInterval.DAY, -7), StatisticInterval.DAY);
    }
    
    /**
     * Get statistic since online.
     * 
     * @return task result statistic
     */
    public TaskResultStatistics getTaskResultStatisticsSinceOnline() {
        if (!isRdbConfigured()) {
            return new TaskResultStatistics(0, 0, StatisticInterval.DAY, new Date());
        }
        return rdbRepository.getSummedTaskResultStatistics(getOnlineDate(), StatisticInterval.DAY);
    }
    
    /**
     * Get the latest statistic of the specified interval.
     * @param statisticInterval statistic interval
     * @return task result statistic
     */
    public TaskResultStatistics findLatestTaskResultStatistics(final StatisticInterval statisticInterval) {
        if (isRdbConfigured()) {
            Optional<TaskResultStatistics> result = rdbRepository.findLatestTaskResultStatistics(statisticInterval);
            if (result.isPresent()) {
                return result.get();
            }
        }
        return new TaskResultStatistics(0, 0, statisticInterval, new Date());
    }
    
    /**
     * Get statistic of the recent day.
     * 
     * @return task result statistic
     */
    public List<TaskResultStatistics> findTaskResultStatisticsDaily() {
        if (!isRdbConfigured()) {
            return Collections.emptyList();
        }
        return rdbRepository.findTaskResultStatistics(StatisticTimeUtils.getStatisticTime(StatisticInterval.HOUR, -24), StatisticInterval.MINUTE);
    }
    
    /**
     * Get job execution type statistics.
     * 
     * @return Job execution type statistics data object
     */
    public JobExecutionTypeStatistics getJobExecutionTypeStatistics() {
        int transientJobCnt = 0;
        int daemonJobCnt = 0;
        for (CloudJobConfigurationPOJO each : configurationService.loadAll()) {
            if (CloudJobExecutionType.TRANSIENT.equals(each.getJobExecutionType())) {
                transientJobCnt++;
            } else if (CloudJobExecutionType.DAEMON.equals(each.getJobExecutionType())) {
                daemonJobCnt++;
            }
        }
        return new JobExecutionTypeStatistics(transientJobCnt, daemonJobCnt);
    }
    
    /**
     * Get the collection of task statistics in the most recent week.
     * 
     * @return Collection of running task statistics data objects
     */
    public List<TaskRunningStatistics> findTaskRunningStatisticsWeekly() {
        if (!isRdbConfigured()) {
            return Collections.emptyList();
        }
        return rdbRepository.findTaskRunningStatistics(StatisticTimeUtils.getStatisticTime(StatisticInterval.DAY, -7));
    }
    
    /**
     * Get the collection of job statistics in the most recent week.
     * 
     * @return collection of running task statistics data objects
     */
    public List<JobRunningStatistics> findJobRunningStatisticsWeekly() {
        if (!isRdbConfigured()) {
            return Collections.emptyList();
        }
        return rdbRepository.findJobRunningStatistics(StatisticTimeUtils.getStatisticTime(StatisticInterval.DAY, -7));
    }
    
    /**
     * Get running task statistics data collection since online.
     * 
     * @return collection of running task statistics data objects
     */
    public List<JobRegisterStatistics> findJobRegisterStatisticsSinceOnline() {
        if (!isRdbConfigured()) {
            return Collections.emptyList();
        }
        return rdbRepository.findJobRegisterStatistics(getOnlineDate());
    }
    
    private Date getOnlineDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return formatter.parse("2016-12-16");
        } catch (final ParseException ex) {
            return null;
        }
    }
}
