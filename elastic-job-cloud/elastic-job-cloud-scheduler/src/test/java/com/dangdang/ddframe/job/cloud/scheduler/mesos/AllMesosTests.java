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

package com.dangdang.ddframe.job.cloud.scheduler.mesos;

import com.dangdang.ddframe.job.cloud.scheduler.ha.FrameworkIDServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SupportedExtractionTypeTest.class, 
        LeasesQueueTest.class, 
        JobTaskRequestTest.class, 
        TaskInfoDataTest.class, 
        FacadeServiceTest.class, 
        SchedulerEngineTest.class,
        TaskLaunchScheduledServiceTest.class,
        SchedulerServiceTest.class,
        LaunchingTasksTest.class,
        FrameworkIDServiceTest.class,
        MesosStateServiceTest.class,
        ReconcileServiceTest.class
    })
public final class AllMesosTests {
}
