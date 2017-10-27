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

package io.elasticjob;

import io.elasticjob.api.AllApiTests;
import io.elasticjob.config.AllConfigTests;
import io.elasticjob.context.AllContextTests;
import io.elasticjob.event.AllEventTests;
import io.elasticjob.exception.AllExceptionTests;
import io.elasticjob.executor.AllExecutorTests;
import io.elasticjob.reg.AllRegTests;
import io.elasticjob.statistics.AllStatisticsTests;
import io.elasticjob.util.AllUtilTests;
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
