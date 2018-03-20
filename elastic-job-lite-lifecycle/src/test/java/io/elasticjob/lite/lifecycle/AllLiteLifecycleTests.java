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

package io.elasticjob.lite.lifecycle;

import io.elasticjob.lite.lifecycle.api.JobAPIFactoryTest;
import io.elasticjob.lite.lifecycle.domain.ShardingStatusTest;
import io.elasticjob.lite.lifecycle.internal.operate.JobOperateAPIImplTest;
import io.elasticjob.lite.lifecycle.internal.operate.ShardingOperateAPIImplTest;
import io.elasticjob.lite.lifecycle.internal.reg.RegistryCenterFactoryTest;
import io.elasticjob.lite.lifecycle.internal.settings.JobSettingsAPIImplTest;
import io.elasticjob.lite.lifecycle.internal.statistics.JobStatisticsAPIImplTest;
import io.elasticjob.lite.lifecycle.internal.statistics.ServerStatisticsAPIImplTest;
import io.elasticjob.lite.lifecycle.internal.statistics.ShardingStatisticsAPIImplTest;
import io.elasticjob.lite.lifecycle.restful.AllRestfulTests;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        JobAPIFactoryTest.class,
        JobSettingsAPIImplTest.class,
        ShardingStatusTest.class, 
        RegistryCenterFactoryTest.class, 
        JobOperateAPIImplTest.class,
        ShardingOperateAPIImplTest.class,
        JobStatisticsAPIImplTest.class,
        ServerStatisticsAPIImplTest.class,
        ShardingStatisticsAPIImplTest.class,
        AllRestfulTests.class
    })
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AllLiteLifecycleTests {
}
