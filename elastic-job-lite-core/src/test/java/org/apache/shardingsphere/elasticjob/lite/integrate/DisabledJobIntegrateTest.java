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

package org.apache.shardingsphere.elasticjob.lite.integrate;

import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.job.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.executor.fixture.job.DetailedFooJob;
import org.apache.shardingsphere.elasticjob.lite.internal.config.yaml.YamlJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerStatus;
import org.apache.shardingsphere.elasticjob.lite.util.concurrent.BlockUtils;
import org.apache.shardingsphere.elasticjob.lite.util.env.IpUtils;
import org.apache.shardingsphere.elasticjob.lite.util.yaml.YamlEngine;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class DisabledJobIntegrateTest extends BaseIntegrateTest {
    
    public DisabledJobIntegrateTest(final TestType type) {
        super(type, new DetailedFooJob());
    }
    
    @Test
    public final void assertJobRunning() {
        BlockUtils.waitingShortTime();
        assertDisabledRegCenterInfo();
        setJobEnable();
        assertEnabledRegCenterInfo();
    }
    
    private void assertDisabledRegCenterInfo() {
        assertThat(JobRegistry.getInstance().getCurrentShardingTotalCount(getJobName()), is(3));
        assertThat(JobRegistry.getInstance().getJobInstance(getJobName()).getIp(), is(IpUtils.getIp()));
        JobConfiguration jobConfig = YamlEngine.unmarshal(getRegCenter().get("/" + getJobName() + "/config"), YamlJobConfiguration.class).toJobConfiguration();
        assertThat(jobConfig.getShardingTotalCount(), is(3));
        if (getJobBootstrap() instanceof ScheduleJobBootstrap) {
            assertThat(jobConfig.getCron(), is("0/1 * * * * ?"));
        } else {
            assertNull(jobConfig.getCron());
        }
        assertThat(jobConfig.getShardingItemParameters(), is("0=A,1=B,2=C"));
        assertThat(getRegCenter().get("/" + getJobName() + "/servers/" + JobRegistry.getInstance().getJobInstance(getJobName()).getIp()), is(ServerStatus.DISABLED.name()));
        while (null != getRegCenter().get("/" + getJobName() + "/leader/election/instance")) {
            BlockUtils.waitingShortTime();
        }
    }
    
    private void setJobEnable() {
        getRegCenter().persist("/" + getJobName() + "/servers/" + JobRegistry.getInstance().getJobInstance(getJobName()).getIp(), ServerStatus.ENABLED.name());
    }
    
    private void assertEnabledRegCenterInfo() {
        assertTrue(getRegCenter().isExisted("/" + getJobName() + "/instances/" + JobRegistry.getInstance().getJobInstance(getJobName()).getJobInstanceId()));
        getRegCenter().remove("/" + getJobName() + "/leader/election");
    }
}
