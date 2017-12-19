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

package io.elasticjob.lite.reg;

import io.elasticjob.lite.reg.exception.RegExceptionHandlerTest;
import io.elasticjob.lite.reg.zookeeper.ZookeeperConfigurationTest;
import io.elasticjob.lite.reg.zookeeper.ZookeeperElectionServiceTest;
import io.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenterForAuthTest;
import io.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenterInitFailureTest;
import io.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenterMiscellaneousTest;
import io.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenterModifyTest;
import io.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenterQueryWithCacheTest;
import io.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenterQueryWithoutCacheTest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ZookeeperConfigurationTest.class, 
        ZookeeperRegistryCenterForAuthTest.class, 
        ZookeeperRegistryCenterQueryWithCacheTest.class, 
        ZookeeperRegistryCenterQueryWithoutCacheTest.class, 
        ZookeeperRegistryCenterModifyTest.class, 
        ZookeeperRegistryCenterMiscellaneousTest.class,
        ZookeeperElectionServiceTest.class,
        RegExceptionHandlerTest.class, 
        ZookeeperRegistryCenterInitFailureTest.class
    })
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AllRegTests {
}
