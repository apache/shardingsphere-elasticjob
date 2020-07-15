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

import org.apache.shardingsphere.elasticjob.cloud.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.job.StatisticJob;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.job.TestStatisticJob;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsSchedulerTest {
    
    private StatisticsScheduler statisticsScheduler;
    
    @Mock
    private Scheduler scheduler;
    
    @Before
    public void setUp() {
        statisticsScheduler = new StatisticsScheduler();
        ReflectionUtils.setFieldValue(statisticsScheduler, "scheduler", scheduler);
    }
    
    @Test
    public void assertRegister() throws SchedulerException {
        StatisticJob job = new TestStatisticJob();
        statisticsScheduler.register(job);
        verify(scheduler).scheduleJob(job.buildJobDetail(), job.buildTrigger());
    }
    
    @Test
    public void assertShutdown() throws SchedulerException {
        when(scheduler.isShutdown()).thenReturn(false);
        statisticsScheduler.shutdown();
        when(scheduler.isShutdown()).thenReturn(true);
        statisticsScheduler.shutdown();
        verify(scheduler).shutdown();
    }
}
