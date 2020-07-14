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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.job;

import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.util.StatisticTimeUtils;
import org.apache.shardingsphere.elasticjob.cloud.statistics.StatisticInterval;
import org.apache.shardingsphere.elasticjob.cloud.statistics.rdb.StatisticRdbRepository;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobRegisterStatistics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.Trigger;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RegisteredJobStatisticJobTest {
    
    @Mock
    private CloudJobConfigurationService configurationService;
    
    @Mock
    private StatisticRdbRepository repository;
    
    private RegisteredJobStatisticJob registeredJobStatisticJob;
    
    @Before
    public void setUp() {
        registeredJobStatisticJob = new RegisteredJobStatisticJob();
        registeredJobStatisticJob.setConfigurationService(configurationService);
        registeredJobStatisticJob.setRepository(repository);
    }
    
    @Test
    public void assertBuildJobDetail() {
        assertThat(registeredJobStatisticJob.buildJobDetail().getKey().getName(), is(RegisteredJobStatisticJob.class.getSimpleName()));
    }
    
    @Test
    public void assertBuildTrigger() {
        Trigger trigger = registeredJobStatisticJob.buildTrigger();
        assertThat(trigger.getKey().getName(), is(RegisteredJobStatisticJob.class.getSimpleName() + "Trigger"));
    }
    
    @Test
    public void assertGetDataMap() {
        assertThat(registeredJobStatisticJob.getDataMap().get("configurationService"), is(configurationService));
        assertThat(registeredJobStatisticJob.getDataMap().get("repository"), is(repository));
    }
    
    @Test
    public void assertExecuteWhenRepositoryIsEmpty() {
        Optional<JobRegisterStatistics> latestOne = Optional.empty();
        when(repository.findLatestJobRegisterStatistics()).thenReturn(latestOne);
        when(repository.add(any(JobRegisterStatistics.class))).thenReturn(true);
        when(configurationService.loadAll()).thenReturn(Collections.singletonList(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        registeredJobStatisticJob.execute(null);
        verify(repository).findLatestJobRegisterStatistics();
        verify(repository).add(any(JobRegisterStatistics.class));
        verify(configurationService).loadAll();
    }
    
    @Test
    public void assertExecute() {
        Optional<JobRegisterStatistics> latestOne = Optional.of(new JobRegisterStatistics(0, StatisticTimeUtils.getStatisticTime(StatisticInterval.DAY, -3)));
        when(repository.findLatestJobRegisterStatistics()).thenReturn(latestOne);
        when(repository.add(any(JobRegisterStatistics.class))).thenReturn(true);
        when(configurationService.loadAll()).thenReturn(Collections.singletonList(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        registeredJobStatisticJob.execute(null);
        verify(repository).findLatestJobRegisterStatistics();
        verify(repository, times(3)).add(any(JobRegisterStatistics.class));
        verify(configurationService).loadAll();
    }
}
