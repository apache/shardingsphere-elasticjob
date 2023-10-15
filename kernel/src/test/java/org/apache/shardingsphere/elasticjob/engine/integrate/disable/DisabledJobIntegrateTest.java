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

package org.apache.shardingsphere.elasticjob.engine.integrate.disable;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.env.IpUtils;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.engine.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.engine.fixture.job.DetailedFooJob;
import org.apache.shardingsphere.elasticjob.engine.integrate.BaseIntegrateTest;
import org.apache.shardingsphere.elasticjob.engine.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.engine.internal.server.ServerStatus;
import org.awaitility.Awaitility;
import org.hamcrest.core.IsNull;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class DisabledJobIntegrateTest extends BaseIntegrateTest {
    
    public DisabledJobIntegrateTest(final TestType type) {
        super(type, new DetailedFooJob());
    }
    
    protected final void assertDisabledRegCenterInfo() {
        Awaitility.await().atLeast(1L, TimeUnit.MILLISECONDS).atMost(1L, TimeUnit.MINUTES).untilAsserted(() -> {
            assertThat(JobRegistry.getInstance().getCurrentShardingTotalCount(getJobName()), is(3));
            assertThat(JobRegistry.getInstance().getJobInstance(getJobName()).getServerIp(), is(IpUtils.getIp()));
        });
        JobConfiguration jobConfig = YamlEngine.unmarshal(getREGISTRY_CENTER().get("/" + getJobName() + "/config"), JobConfigurationPOJO.class).toJobConfiguration();
        assertThat(jobConfig.getShardingTotalCount(), is(3));
        if (getJobBootstrap() instanceof ScheduleJobBootstrap) {
            assertThat(jobConfig.getCron(), is("0/1 * * * * ?"));
        } else {
            assertNull(jobConfig.getCron());
        }
        assertThat(jobConfig.getShardingItemParameters(), is("0=A,1=B,2=C"));
        assertThat(getREGISTRY_CENTER().get("/" + getJobName() + "/servers/" + JobRegistry.getInstance().getJobInstance(getJobName()).getServerIp()), is(ServerStatus.DISABLED.name()));
        Awaitility.await().atMost(1L, TimeUnit.MINUTES).untilAsserted(() -> assertThat(getREGISTRY_CENTER().get("/" + getJobName() + "/leader/election/instance"), is(IsNull.nullValue())));
    }
}
