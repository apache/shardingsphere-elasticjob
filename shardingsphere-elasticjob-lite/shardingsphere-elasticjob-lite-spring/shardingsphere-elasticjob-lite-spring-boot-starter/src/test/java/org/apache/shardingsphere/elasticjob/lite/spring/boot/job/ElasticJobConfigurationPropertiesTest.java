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

package org.apache.shardingsphere.elasticjob.lite.spring.boot.job;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.junit.Test;

public final class ElasticJobConfigurationPropertiesTest {

    @Test
    public void assertToJobConfiguration() {
        ElasticJobConfigurationProperties properties = new ElasticJobConfigurationProperties();
        properties.setElasticJobClass(ElasticJob.class);
        properties.setElasticJobType("jobType");
        properties.setCron("cron");
        properties.setJobBootstrapBeanName("beanName");
        properties.setShardingTotalCount(3);
        properties.setShardingItemParameters("a=1,b=2");
        properties.setJobParameter("testParam");
        properties.setMonitorExecution(true);
        properties.setFailover(true);
        properties.setMisfire(true);
        properties.setMaxTimeDiffSeconds(1);
        properties.setReconcileIntervalMinutes(2);
        properties.setJobShardingStrategyType("strategyType");
        properties.setJobExecutorServiceHandlerType("executorType");
        properties.setJobErrorHandlerType("errorHandlerType");
        properties.setJobListenerTypes(Collections.singleton("listenerType"));
        properties.setDescription("test desc");
        properties.setDisabled(true);
        properties.setOverwrite(true);
        properties.getProps().setProperty("test", "test");
        JobConfiguration actual = properties.toJobConfiguration("testJob");
        assertThat(actual.getJobName(), is("testJob"));
        assertThat(actual.getShardingTotalCount(), is(properties.getShardingTotalCount()));
        assertThat(actual.getCron(), is(properties.getCron()));
        assertThat(actual.getShardingItemParameters(), is(properties.getShardingItemParameters()));
        assertThat(actual.getJobParameter(), is(properties.getJobParameter()));
        assertThat(actual.getMaxTimeDiffSeconds(), is(properties.getMaxTimeDiffSeconds()));
        assertThat(actual.getReconcileIntervalMinutes(), is(properties.getReconcileIntervalMinutes()));
        assertThat(actual.getJobShardingStrategyType(), is(properties.getJobShardingStrategyType()));
        assertThat(actual.getJobExecutorServiceHandlerType(), is(properties.getJobExecutorServiceHandlerType()));
        assertThat(actual.getJobErrorHandlerType(), is(properties.getJobErrorHandlerType()));
        assertThat(actual.getJobListenerTypes().toArray(), is(properties.getJobListenerTypes().toArray()));
        assertThat(actual.getDescription(), is(properties.getDescription()));
        assertThat(actual.isDisabled(), is(properties.isDisabled()));
        assertThat(actual.isOverwrite(), is(properties.isOverwrite()));
        assertThat(actual.isMisfire(), is(properties.isMisfire()));
        assertThat(actual.isFailover(), is(properties.isFailover()));
        assertThat(actual.isMonitorExecution(), is(properties.isMonitorExecution()));
        assertThat(actual.getProps().size(), is(properties.getProps().size()));
        assertThat(actual.getProps().getProperty("test"), is(properties.getProps().getProperty("test")));
    }
}
