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

package io.elasticjob.lite;

import io.elasticjob.lite.api.AllApiTests;
import io.elasticjob.lite.config.AllConfigTests;
import io.elasticjob.lite.context.AllContextTests;
import io.elasticjob.lite.event.AllEventTests;
import io.elasticjob.lite.exception.AllExceptionTests;
import io.elasticjob.lite.executor.AllExecutorTests;
import io.elasticjob.lite.integrate.AllIntegrateTests;
import io.elasticjob.lite.internal.AllInternalTests;
import io.elasticjob.lite.reg.AllRegTests;
import io.elasticjob.lite.statistics.AllStatisticsTests;
import io.elasticjob.lite.util.AllUtilTests;
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
        AllUtilTests.class, 
        AllApiTests.class, 
        AllConfigTests.class,
        AllInternalTests.class, 
        AllIntegrateTests.class
    })
public final class AllLiteCoreTests {
}
