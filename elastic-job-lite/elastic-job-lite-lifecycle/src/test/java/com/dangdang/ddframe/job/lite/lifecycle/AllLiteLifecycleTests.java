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

package com.dangdang.ddframe.job.lite.lifecycle;

import com.dangdang.ddframe.job.lite.lifecycle.api.JobAPIFactoryTest;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ExecutionStatusTest;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobStatusTest;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ServerBriefStatusTest;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ServerStatusTest;
import com.dangdang.ddframe.job.lite.lifecycle.internal.operate.JobOperateAPIImplTest;
import com.dangdang.ddframe.job.lite.lifecycle.internal.reg.RegistryCenterFactoryTest;
import com.dangdang.ddframe.job.lite.lifecycle.internal.statistics.JobStatisticsAPIImplTest;
import com.dangdang.ddframe.job.lite.lifecycle.internal.statistics.ServerStatisticsAPIImplTest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        JobAPIFactoryTest.class, 
        JobStatusTest.class,
        ServerStatusTest.class, 
        ServerBriefStatusTest.class, 
        ExecutionStatusTest.class, 
        RegistryCenterFactoryTest.class, 
        JobOperateAPIImplTest.class,
        JobStatisticsAPIImplTest.class,
        ServerStatisticsAPIImplTest.class
    })
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AllLiteLifecycleTests {
}
