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

package com.dangdang.ddframe;

import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dangdang.ddframe.job.AllJobTests;
import com.dangdang.ddframe.reg.AbstractNestedZookeeperBaseTest;
import com.dangdang.ddframe.reg.AllRegTests;
import com.dangdang.ddframe.reg.zookeeper.NestedZookeeperServers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@RunWith(Suite.class)
@SuiteClasses({
    AllRegTests.class, 
    AllJobTests.class
    })
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AllTests {
    
    @AfterClass
    public static void clear() {
        NestedZookeeperServers.getInstance().closeServer(AbstractNestedZookeeperBaseTest.PORT);
    }
}
