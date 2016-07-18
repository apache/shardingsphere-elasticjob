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

package com.dangdang.ddframe.job.cloud.internal.statistics;

import com.dangdang.ddframe.job.cloud.api.config.JobConfiguration;
import com.dangdang.ddframe.job.cloud.api.config.JobConfigurationFactory;
import com.dangdang.ddframe.job.cloud.fixture.TestJob;
import com.dangdang.ddframe.job.cloud.internal.config.ConfigurationService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class StatisticsServiceTest {
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private ScheduledExecutorService scheduledExecutorService;
    
    private final JobConfiguration jobConfig = JobConfigurationFactory.createSimpleJobConfigurationBuilder("testJob", TestJob.class, 3, "0/1 * * * * ?").build();
    
    private final StatisticsService statisticsService = new StatisticsService(null, jobConfig);
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(statisticsService, "configService", configService);
        ReflectionUtils.setFieldValue(statisticsService, "scheduledExecutorService", scheduledExecutorService);
    }
    
    @Test
    public void assertStartProcessCountJobWhenNegativeProcessCountIntervalSeconds() {
        when(configService.getProcessCountIntervalSeconds()).thenReturn(-1);
        statisticsService.startProcessCountJob();
        verify(configService).getProcessCountIntervalSeconds();
        verify(scheduledExecutorService, times(0)).scheduleAtFixedRate(Matchers.<ProcessCountJob>any(), eq(-1L), eq(-1L), eq(TimeUnit.SECONDS));
    }
    
    @Test
    public void assertStartProcessCountJobWhenPositiveProcessCountIntervalSeconds() {
        when(configService.getProcessCountIntervalSeconds()).thenReturn(10);
        statisticsService.startProcessCountJob();
        verify(configService).getProcessCountIntervalSeconds();
        verify(scheduledExecutorService).scheduleAtFixedRate(Matchers.<ProcessCountJob>any(), eq(10L), eq(10L), eq(TimeUnit.SECONDS));
    }
    
    @Test
    public void assertStopProcessCountJob() {
        statisticsService.stopProcessCountJob();
        verify(scheduledExecutorService).shutdown();
    }
}
