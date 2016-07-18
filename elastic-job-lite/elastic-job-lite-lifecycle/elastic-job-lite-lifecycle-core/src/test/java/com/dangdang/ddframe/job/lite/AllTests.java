/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.lite;

import com.dangdang.ddframe.job.lite.api.JobAPIFactoryTest;
import com.dangdang.ddframe.job.lite.domain.ExecutionStatusTest;
import com.dangdang.ddframe.job.lite.domain.JobStatusTest;
import com.dangdang.ddframe.job.lite.domain.ServerBriefStatusTest;
import com.dangdang.ddframe.job.lite.domain.ServerStatusTest;
import com.dangdang.ddframe.job.lite.internal.operate.JobOperateAPIImplTest;
import com.dangdang.ddframe.job.lite.internal.reg.RegistryCenterFactoryTest;
import com.dangdang.ddframe.job.lite.internal.settings.JobSettingsAPIImplTest;
import com.dangdang.ddframe.job.lite.internal.statistics.JobStatisticsAPIImplTest;
import com.dangdang.ddframe.job.lite.internal.statistics.ServerStatisticsAPIImplTest;
import com.dangdang.ddframe.reg.zookeeper.NestedZookeeperServers;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.AfterClass;
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
    JobSettingsAPIImplTest.class,
    JobOperateAPIImplTest.class,
    JobStatisticsAPIImplTest.class,
    ServerStatisticsAPIImplTest.class
})
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AllTests {
    
    @AfterClass
    public static void clear() {
        NestedZookeeperServers.getInstance().closeServer(AbstractNestedZookeeperBaseTest.PORT);
    }
}
