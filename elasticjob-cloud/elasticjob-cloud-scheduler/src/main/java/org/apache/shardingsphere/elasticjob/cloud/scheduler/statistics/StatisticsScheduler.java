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

import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.job.StatisticJob;
import org.apache.shardingsphere.elasticjob.infra.exception.JobStatisticException;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.plugins.management.ShutdownHookPlugin;
import org.quartz.simpl.SimpleThreadPool;

import java.util.Properties;

/**
 * Statistic scheduler.
 */
final class StatisticsScheduler {
    
    private final StdSchedulerFactory factory;
    
    private Scheduler scheduler;

    StatisticsScheduler() {
        factory = new StdSchedulerFactory();
        try {
            factory.initialize(getQuartzProperties());
        } catch (final SchedulerException ex) {
            throw new JobStatisticException(ex);
        }
    }
    
    private Properties getQuartzProperties() {
        Properties result = new Properties();
        result.put("org.quartz.threadPool.class", SimpleThreadPool.class.getName());
        result.put("org.quartz.threadPool.threadCount", Integer.toString(1));
        result.put("org.quartz.scheduler.instanceName", "ELASTIC_JOB_CLOUD_STATISTICS_SCHEDULER");
        result.put("org.quartz.plugin.shutdownhook.class", ShutdownHookPlugin.class.getName());
        result.put("org.quartz.plugin.shutdownhook.cleanShutdown", Boolean.TRUE.toString());
        return result;
    }
    
    /**
     * Start.
     */
    void start() {
        try {
            scheduler = factory.getScheduler();
            scheduler.start();
        } catch (final SchedulerException ex) {
            throw new JobStatisticException(ex);
        }
    }
    
    /**
     * Register statistic job.
     * 
     * @param statisticJob statistic job
     */
    void register(final StatisticJob statisticJob) {
        try {
            JobDetail jobDetail = statisticJob.buildJobDetail();
            jobDetail.getJobDataMap().putAll(statisticJob.getDataMap());
            scheduler.scheduleJob(jobDetail, statisticJob.buildTrigger());
        } catch (final SchedulerException ex) {
            throw new JobStatisticException(ex);
        }
    }
    
    /**
     * Shutdown.
     */
    void shutdown() {
        try {
            if (null != scheduler && !scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        } catch (final SchedulerException ex) {
            throw new JobStatisticException(ex);
        }
    }
}
