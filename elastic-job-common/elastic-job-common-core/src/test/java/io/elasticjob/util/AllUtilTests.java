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

package io.elasticjob.util;

import io.elasticjob.util.concurrent.ExecutorServiceObjectTest;
import io.elasticjob.util.config.ShardingItemParametersTest;
import io.elasticjob.util.config.ShardingItemsTest;
import io.elasticjob.util.digest.EncryptionTest;
import io.elasticjob.util.env.HostExceptionTest;
import io.elasticjob.util.env.IpUtilsTest;
import io.elasticjob.util.env.TimeServiceTest;
import io.elasticjob.util.json.GsonFactoryTest;
import io.elasticjob.util.json.JobConfigurationGsonTypeAdapterTest;
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
