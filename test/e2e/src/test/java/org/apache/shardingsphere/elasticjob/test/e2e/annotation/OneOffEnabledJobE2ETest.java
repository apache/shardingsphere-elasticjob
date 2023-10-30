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

package org.apache.shardingsphere.elasticjob.test.e2e.annotation;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.env.IpUtils;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.kernel.internal.config.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.kernel.internal.server.ServerStatus;
import org.apache.shardingsphere.elasticjob.test.e2e.annotation.fixture.AnnotationUnShardingJob;
import org.awaitility.Awaitility;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OneOffEnabledJobE2ETest extends BaseAnnotationE2ETest {
    
    OneOffEnabledJobE2ETest() {
        super(TestType.ONE_OFF, new AnnotationUnShardingJob());
    }
    
    @BeforeEach
    void assertEnabledRegCenterInfo() {
        assertThat(JobRegistry.getInstance().getCurrentShardingTotalCount(getJobName()), is(1));
        assertThat(JobRegistry.getInstance().getJobInstance(getJobName()).getServerIp(), is(IpUtils.getIp()));
        JobConfiguration jobConfig = YamlEngine.unmarshal(getREGISTRY_CENTER().get("/" + getJobName() + "/config"), JobConfigurationPOJO.class).toJobConfiguration();
        assertThat(jobConfig.getShardingTotalCount(), is(1));
        assertNull(jobConfig.getCron());
        assertThat(getREGISTRY_CENTER().get("/" + getJobName() + "/servers/" + JobRegistry.getInstance().getJobInstance(getJobName()).getServerIp()), is(ServerStatus.ENABLED.name()));
        assertThat(getREGISTRY_CENTER().get("/" + getJobName() + "/leader/election/instance"), is(JobRegistry.getInstance().getJobInstance(getJobName()).getJobInstanceId()));
        assertTrue(getREGISTRY_CENTER().isExisted("/" + getJobName() + "/instances/" + JobRegistry.getInstance().getJobInstance(getJobName()).getJobInstanceId()));
        getREGISTRY_CENTER().remove("/" + getJobName() + "/leader/election");
        assertTrue(getLeaderService().isLeaderUntilBlock());
    }
    
    @Test
    void assertJobInit() {
        Awaitility.await().atMost(1L, TimeUnit.MINUTES).untilAsserted(() -> MatcherAssert.assertThat(((AnnotationUnShardingJob) getElasticJob()).isCompleted(), is(true)));
        assertTrue(getREGISTRY_CENTER().isExisted("/" + getJobName() + "/sharding"));
    }
    
}
