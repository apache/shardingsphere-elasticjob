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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.dangdang.ddframe.job.cloud.scheduler.statistics.job.BaseStatisticJobTest;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.job.JobRunningStatisticJobTest;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.job.RegisteredJobStatisticJobTest;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.job.TaskResultStatisticJobTest;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.util.StatisticTimeUtilsTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        StatisticManagerTest.class, 
        StatisticsSchedulerTest.class, 
        BaseStatisticJobTest.class, 
        TaskResultMetaDataTest.class, 
        StatisticTimeUtilsTest.class, 
        RegisteredJobStatisticJobTest.class, 
        TaskResultStatisticJobTest.class, 
        JobRunningStatisticJobTest.class
    })
public final class AllStatisticTests {
}
