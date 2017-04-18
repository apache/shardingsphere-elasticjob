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

package com.dangdang.ddframe.job;

import com.dangdang.ddframe.job.api.AllApiTests;
import com.dangdang.ddframe.job.config.AllConfigTests;
import com.dangdang.ddframe.job.context.AllContextTests;
import com.dangdang.ddframe.job.event.AllEventTests;
import com.dangdang.ddframe.job.exception.AllExceptionTests;
import com.dangdang.ddframe.job.executor.AllExecutorTests;
import com.dangdang.ddframe.job.reg.AllRegTests;
import com.dangdang.ddframe.job.statistics.AllStatisticsTests;
import com.dangdang.ddframe.job.util.AllUtilTests;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        AllRegTests.class,
        AllContextTests.class,
        AllApiTests.class, 
        AllConfigTests.class, 
        AllExecutorTests.class, 
        AllEventTests.class, 
        AllExceptionTests.class,
        AllStatisticsTests.class,
        AllUtilTests.class
    })
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AllCoreTests {
}
