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

package org.apache.shardingsphere.elasticjob.cloud;

import org.apache.shardingsphere.elasticjob.cloud.api.AllApiTests;
import org.apache.shardingsphere.elasticjob.cloud.executor.AllExecutorTests;
import org.apache.shardingsphere.elasticjob.cloud.statistics.AllStatisticsTests;
import org.apache.shardingsphere.elasticjob.cloud.config.AllConfigTests;
import org.apache.shardingsphere.elasticjob.cloud.context.AllContextTests;
import org.apache.shardingsphere.elasticjob.cloud.event.AllEventTests;
import org.apache.shardingsphere.elasticjob.cloud.exception.AllExceptionTests;
import org.apache.shardingsphere.elasticjob.cloud.reg.AllRegTests;
import org.apache.shardingsphere.elasticjob.cloud.util.AllUtilTests;
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
