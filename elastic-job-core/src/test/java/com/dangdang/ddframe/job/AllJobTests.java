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

package com.dangdang.ddframe.job;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dangdang.ddframe.job.api.AllApiTests;
import com.dangdang.ddframe.job.exception.AllExceptionTests;
import com.dangdang.ddframe.job.integrate.AllIntegrateTests;
import com.dangdang.ddframe.job.internal.AllInternalTests;
import com.dangdang.ddframe.job.plugin.AllPluginTests;

@RunWith(Suite.class)
@SuiteClasses({
    AllApiTests.class, 
    AllExceptionTests.class, 
    AllPluginTests.class, 
    AllInternalTests.class, 
    AllIntegrateTests.class
    })
public final class AllJobTests {
}
