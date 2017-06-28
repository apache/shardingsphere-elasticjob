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

package com.dangdang.ddframe.job.cloud.scheduler.state;

import com.dangdang.ddframe.job.cloud.scheduler.state.disable.app.DisableAppNodeTest;
import com.dangdang.ddframe.job.cloud.scheduler.state.disable.app.DisableAppServiceTest;
import com.dangdang.ddframe.job.cloud.scheduler.state.disable.job.DisableJobNodeTest;
import com.dangdang.ddframe.job.cloud.scheduler.state.disable.job.DisableJobServiceTest;
import com.dangdang.ddframe.job.cloud.scheduler.state.failover.FailoverNodeTest;
import com.dangdang.ddframe.job.cloud.scheduler.state.failover.FailoverServiceTest;
import com.dangdang.ddframe.job.cloud.scheduler.state.ready.ReadyNodeTest;
import com.dangdang.ddframe.job.cloud.scheduler.state.ready.ReadyServiceTest;
import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningNodeTest;
import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ReadyNodeTest.class, 
        ReadyServiceTest.class, 
        FailoverNodeTest.class, 
        FailoverServiceTest.class,
        RunningNodeTest.class,
        RunningServiceTest.class,
        DisableAppNodeTest.class,
        DisableAppServiceTest.class,
        DisableJobNodeTest.class,
        DisableJobServiceTest.class
    })
public final class AllStateTests {
}
