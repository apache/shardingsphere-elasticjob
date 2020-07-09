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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.state;

import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.job.DisableJobNodeTest;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.job.DisableJobServiceTest;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.failover.FailoverNodeTest;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.failover.FailoverServiceTest;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.ready.ReadyServiceTest;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running.RunningServiceTest;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.app.DisableAppNodeTest;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.app.DisableAppServiceTest;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.ready.ReadyNodeTest;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running.RunningNodeTest;
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
