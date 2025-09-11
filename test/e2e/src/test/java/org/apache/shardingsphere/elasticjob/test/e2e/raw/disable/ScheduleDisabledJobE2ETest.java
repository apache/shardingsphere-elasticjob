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

package org.apache.shardingsphere.elasticjob.test.e2e.raw.disable;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.kernel.internal.server.ServerStatus;
import org.apache.shardingsphere.elasticjob.test.e2e.raw.fixture.job.E2EFixtureJobImpl;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ScheduleDisabledJobE2ETest extends DisabledJobE2ETest {
    
    ScheduleDisabledJobE2ETest() {
        super(TestType.SCHEDULE);
    }
    
    @Override
    protected JobConfiguration getJobConfiguration(final String jobName) {
        return JobConfiguration.newBuilder(jobName, 3).cron("0/1 * * * * ?").shardingItemParameters("0=A,1=B,2=C")
                .jobListenerTypes("INTEGRATE-TEST", "INTEGRATE-DISTRIBUTE").disabled(true).overwrite(true).build();
    }
    
    @Test
    void assertJobRunning() {
        assertDisabledRegCenterInfo();
        setJobEnable();
        Awaitility.await().atMost(30L, TimeUnit.SECONDS).until(() -> ((E2EFixtureJobImpl) getElasticJob()).isCompleted());
        assertEnabledRegCenterInfo();
    }
    
    private void setJobEnable() {
        getRegistryCenter().persist("/" + getJobName() + "/servers/" + JobRegistry.getInstance().getJobInstance(getJobName()).getServerIp(), ServerStatus.ENABLED.name());
    }
    
    private void assertEnabledRegCenterInfo() {
        assertTrue(getRegistryCenter().isExisted("/" + getJobName() + "/instances/" + JobRegistry.getInstance().getJobInstance(getJobName()).getJobInstanceId()));
        getRegistryCenter().remove("/" + getJobName() + "/leader/election");
        assertTrue(getRegistryCenter().isExisted("/" + getJobName() + "/sharding"));
    }
}
