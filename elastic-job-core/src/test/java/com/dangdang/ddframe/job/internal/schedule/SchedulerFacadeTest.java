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

package com.dangdang.ddframe.job.internal.schedule;

import com.dangdang.ddframe.job.api.config.DataFlowJobConfiguration;
import com.dangdang.ddframe.job.api.config.JobConfiguration;
import com.dangdang.ddframe.job.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.fixture.TestDataFlowJob;
import com.dangdang.ddframe.job.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.internal.job.JobType;
import com.dangdang.ddframe.job.internal.listener.ListenerManager;
import com.dangdang.ddframe.job.internal.monitor.MonitorService;
import com.dangdang.ddframe.job.internal.server.ServerService;
import com.dangdang.ddframe.job.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.internal.statistics.StatisticsService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SchedulerFacadeTest {
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private LeaderElectionService leaderElectionService;
    
    @Mock
    private ServerService serverService;
    
    @Mock
    private ShardingService shardingService;
    
    @Mock
    private ExecutionService executionService;
    
    @Mock
    private StatisticsService statisticsService;
    
    @Mock
    private MonitorService monitorService;
    
    @Mock
    private ListenerManager listenerManager;
    
    private JobConfiguration jobConfig = new DataFlowJobConfiguration("testJob", TestDataFlowJob.class, 3, "0/1 * * * * ?");
    
    private SchedulerFacade schedulerFacade;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        schedulerFacade = new SchedulerFacade(null, jobConfig, Collections.<ElasticJobListener>emptyList());
        when(configService.getJobType()).thenReturn(JobType.DATA_FLOW);
        ReflectionUtils.setFieldValue(schedulerFacade, "configService", configService);
        ReflectionUtils.setFieldValue(schedulerFacade, "leaderElectionService", leaderElectionService);
        ReflectionUtils.setFieldValue(schedulerFacade, "serverService", serverService);
        ReflectionUtils.setFieldValue(schedulerFacade, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(schedulerFacade, "executionService", executionService);
        ReflectionUtils.setFieldValue(schedulerFacade, "statisticsService", statisticsService);
        ReflectionUtils.setFieldValue(schedulerFacade, "monitorService", monitorService);
        ReflectionUtils.setFieldValue(schedulerFacade, "listenerManager", listenerManager);
    }
    
    @Test
    public void testClearPreviousServerStatus() {
        schedulerFacade.clearPreviousServerStatus();
        verify(serverService).clearPreviousServerStatus();
    }
    
    @Test
    public void testRegisterStartUpInfo() {
        schedulerFacade.registerStartUpInfo();
        verify(listenerManager).startAllListeners();
        verify(leaderElectionService).leaderForceElection();
        verify(configService).persistJobConfiguration();
        verify(serverService).persistServerOnline();
        verify(statisticsService).startProcessCountJob();
        verify(serverService).clearJobPausedStatus();
        verify(shardingService).setReshardingFlag();
        verify(monitorService).listen();
    }
    
    @Test
    public void testReleaseJobResource() {
        schedulerFacade.releaseJobResource();
        verify(monitorService).close();
        verify(statisticsService).stopProcessCountJob();
        verify(serverService).removeServerStatus();
    }
    
    @Test
    public void testGetCron() {
        when(configService.getCron()).thenReturn("0 * * * * *");
        assertThat(schedulerFacade.getCron(), is("0 * * * * *"));
    }
    
    @Test
    public void testIsMisfire() {
        when(configService.isMisfire()).thenReturn(true);
        assertTrue(schedulerFacade.isMisfire());
    }
    
    @Test
    public void testNewJobTriggerListener() {
        assertThat(schedulerFacade.newJobTriggerListener(), instanceOf(JobTriggerListener.class));
    }
}
