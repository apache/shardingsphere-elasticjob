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

package com.dangdang.ddframe.job.cloud.scheduler.statistics.job;

import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.util.StatisticTimeUtils;
import com.dangdang.ddframe.job.statistics.StatisticInterval;
import com.dangdang.ddframe.job.statistics.rdb.StatisticRdbRepository;
import com.dangdang.ddframe.job.statistics.type.job.JobRegisterStatistics;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

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
    public void assertBuildTrigger() throws SchedulerException {
        Trigger trigger = registeredJobStatisticJob.buildTrigger();
        assertThat(trigger.getKey().getName(), is(RegisteredJobStatisticJob.class.getSimpleName() + "Trigger"));
    }
    
    @Test
    public void assertGetDataMap() throws SchedulerException {
        assertThat((CloudJobConfigurationService) registeredJobStatisticJob.getDataMap().get("configurationService"), is(configurationService));
        assertThat((StatisticRdbRepository) registeredJobStatisticJob.getDataMap().get("repository"), is(repository));
    }
    
    @Test
    public void assertExecuteWhenRepositoryIsEmpty() throws SchedulerException {
        Optional<JobRegisterStatistics> latestOne = Optional.absent();
        when(repository.findLatestJobRegisterStatistics()).thenReturn(latestOne);
        when(repository.add(any(JobRegisterStatistics.class))).thenReturn(true);
        when(configurationService.loadAll()).thenReturn(Lists.newArrayList(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        registeredJobStatisticJob.execute(null);
        verify(repository).findLatestJobRegisterStatistics();
        verify(repository).add(any(JobRegisterStatistics.class));
        verify(configurationService).loadAll();
    }
    
    @Test
    public void assertExecute() throws SchedulerException {
        Optional<JobRegisterStatistics> latestOne = Optional.of(new JobRegisterStatistics(0, StatisticTimeUtils.getStatisticTime(StatisticInterval.DAY, -3)));
        when(repository.findLatestJobRegisterStatistics()).thenReturn(latestOne);
        when(repository.add(any(JobRegisterStatistics.class))).thenReturn(true);
        when(configurationService.loadAll()).thenReturn(Lists.newArrayList(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        registeredJobStatisticJob.execute(null);
        verify(repository).findLatestJobRegisterStatistics();
        verify(repository, times(3)).add(any(JobRegisterStatistics.class));
        verify(configurationService).loadAll();
    }
}
