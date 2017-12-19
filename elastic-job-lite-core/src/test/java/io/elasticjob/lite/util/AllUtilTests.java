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

package io.elasticjob.lite.util;

import io.elasticjob.lite.util.concurrent.ExecutorServiceObjectTest;
import io.elasticjob.lite.util.config.ShardingItemParametersTest;
import io.elasticjob.lite.util.config.ShardingItemsTest;
import io.elasticjob.lite.util.digest.EncryptionTest;
import io.elasticjob.lite.util.env.HostExceptionTest;
import io.elasticjob.lite.util.env.IpUtilsTest;
import io.elasticjob.lite.util.env.TimeServiceTest;
import io.elasticjob.lite.util.json.GsonFactoryTest;
import io.elasticjob.lite.util.json.JobConfigurationGsonTypeAdapterTest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ExecutorServiceObjectTest.class, 
        EncryptionTest.class, 
        TimeServiceTest.class, 
        IpUtilsTest.class, 
        HostExceptionTest.class, 
        GsonFactoryTest.class, 
        JobConfigurationGsonTypeAdapterTest.class, 
        ShardingItemsTest.class, 
        ShardingItemParametersTest.class
    })
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AllUtilTests {
}
