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

package com.dangdang.ddframe.job.internal.server;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dangdang.ddframe.job.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.internal.sharding.ShardingService;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.state.ConnectionState;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.api.JobScheduler;
import com.dangdang.ddframe.job.fixture.TestJob;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.internal.server.JobOperationListenerManager.ConnectionLostListener;
import com.dangdang.ddframe.job.internal.server.JobOperationListenerManager.JobStoppedStatusJobListener;
import com.dangdang.ddframe.job.internal.storage.JobNodeStorage;

import java.util.Arrays;

public final class JobOperationListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private LeaderElectionService leaderElectionService;
    
    @Mock
    private ServerService serverService;
    
    @Mock
    private ShardingService shardingService;
    
    @Mock
    private ExecutionService executionService;
    
    @Mock
    private JobScheduler jobScheduler;
    
    private String ip = new LocalHostService().getIp();
    
    private final JobOperationListenerManager jobOperationListenerManager = new JobOperationListenerManager(null, new JobConfiguration("testJob", TestJob.class, 3, "0/1 * * * * ?"));
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(jobOperationListenerManager, "leaderElectionService", leaderElectionService);
        ReflectionUtils.setFieldValue(jobOperationListenerManager, "serverService", serverService);
        ReflectionUtils.setFieldValue(jobOperationListenerManager, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(jobOperationListenerManager, "executionService", executionService);
        ReflectionUtils.setFieldValue(jobOperationListenerManager, jobOperationListenerManager.getClass().getSuperclass().getDeclaredField("jobNodeStorage"), jobNodeStorage);
    }
    
    @Test
    public void assertStart() {
        jobOperationListenerManager.start();
        verify(jobNodeStorage).addConnectionStateListener(Matchers.<ConnectionLostListener>any());
        verify(jobNodeStorage, times(2)).addDataListener(Matchers.<JobStoppedStatusJobListener>any());
    }
    
    @Test
    public void assertConnectionLostListenerWhenConnectionStateIsLost() {
        JobRegistry.getInstance().addJobScheduler("testJob", jobScheduler);
        jobOperationListenerManager.new ConnectionLostListener().stateChanged(null, ConnectionState.LOST);
        verify(jobScheduler).stopJob();
    }
    
    @Test
    public void assertConnectionLostListenerWhenConnectionStateIsReconnectedAndIsNotStoppedManually() {
        JobRegistry.getInstance().addJobScheduler("testJob", jobScheduler);
        when(shardingService.getLocalHostShardingItems()).thenReturn(Arrays.asList(0, 1));
        when(serverService.isJobStoppedManually()).thenReturn(false);
        jobOperationListenerManager.new ConnectionLostListener().stateChanged(null, ConnectionState.RECONNECTED);
        verify(serverService).persistServerOnline();
        verify(executionService).clearRunningInfo(Arrays.asList(0, 1));
        verify(jobScheduler).resumeJob();
    }
    
    @Test
    public void assertConnectionLostListenerWhenConnectionStateIsReconnectedAndIsStoppedManually() {
        JobRegistry.getInstance().addJobScheduler("testJob", jobScheduler);
        when(shardingService.getLocalHostShardingItems()).thenReturn(Arrays.asList(0, 1));
        when(serverService.isJobStoppedManually()).thenReturn(true);
        jobOperationListenerManager.new ConnectionLostListener().stateChanged(null, ConnectionState.RECONNECTED);
        verify(serverService).persistServerOnline();
        verify(executionService).clearRunningInfo(Arrays.asList(0, 1));
        verify(jobScheduler, times(0)).resumeJob();
    }
    
    @Test
    public void assertConnectionLostListenerWhenConnectionStateIsOther() {
        JobRegistry.getInstance().addJobScheduler("testJob", jobScheduler);
        jobOperationListenerManager.new ConnectionLostListener().stateChanged(null, ConnectionState.CONNECTED);
        verify(jobScheduler, times(0)).stopJob();
        verify(jobScheduler, times(0)).resumeJob();
    }
    
    @Test
    public void assertJobStoppedStatusJobListenerWhenIsNotJobStoppedPath() {
        jobOperationListenerManager.new JobStoppedStatusJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/testJob/servers/" + ip + "/other", null, "".getBytes())), "/testJob/servers/" + ip + "/other");
        verify(jobScheduler, times(0)).stopJob();
        verify(jobScheduler, times(0)).resumeJob();
    }
    
    @Test
    public void assertJobStoppedStatusJobListenerWhenIsJobStoppedPathButJobIsNotExisted() {
        jobOperationListenerManager.new JobStoppedStatusJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/testJob/servers/" + ip + "/stoped", null, "".getBytes())), "/testJob/servers/" + ip + "/stoped");
        verify(jobScheduler, times(0)).stopJob();
        verify(jobScheduler, times(0)).resumeJob();
    }
    
    @Test
    public void assertJobStoppedStatusJobListenerWhenIsJobStoppedPathAndUpdate() {
        JobRegistry.getInstance().addJobScheduler("testJob", jobScheduler);
        jobOperationListenerManager.new JobStoppedStatusJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/testJob/servers/" + ip + "/stoped", null, "".getBytes())), "/testJob/servers/" + ip + "/stoped");
        verify(jobScheduler, times(0)).stopJob();
        verify(jobScheduler, times(0)).resumeJob();
    }
    
    @Test
    public void assertJobStoppedStatusJobListenerWhenIsJobStoppedPathAndAdd() {
        JobRegistry.getInstance().addJobScheduler("testJob", jobScheduler);
        jobOperationListenerManager.new JobStoppedStatusJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/testJob/servers/" + ip + "/stoped", null, "".getBytes())), "/testJob/servers/" + ip + "/stoped");
        verify(jobScheduler).stopJob();
        verify(jobScheduler, times(0)).resumeJob();
    }
    
    @Test
    public void assertJobStoppedStatusJobListenerWhenIsJobStoppedPathAndRemove() {
        JobRegistry.getInstance().addJobScheduler("testJob", jobScheduler);
        jobOperationListenerManager.new JobStoppedStatusJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/testJob/servers/" + ip + "/stoped", null, "".getBytes())), "/testJob/servers/" + ip + "/stoped");
        verify(jobScheduler, times(0)).stopJob();
        verify(jobScheduler).resumeJob();
        verify(serverService).clearJobStoppedStatus();
    }
    
    @Test
    public void assertJobShutdownStatusJobListenerWhenIsNotJobShutdownPath() {
        jobOperationListenerManager.new JobShutdownStatusJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/testJob/servers/" + ip + "/other", null, "".getBytes())), "/testJob/servers/" + ip + "/other");
        verify(jobScheduler, times(0)).shutdown();
    }
    
    @Test
    public void assertJobShutdownStatusJobListenerWhenIsJobShutdownPathButJobIsNotExisted() {
        jobOperationListenerManager.new JobShutdownStatusJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/testJob/servers/" + ip + "/shutdown", null, "".getBytes())), "/testJob/servers/" + ip + "/shutdown");
        verify(jobScheduler, times(0)).shutdown();
    }
    
    @Test
    public void assertJobShutdownStatusJobListenerWhenIsJobShutdownPathAndUpdate() {
        JobRegistry.getInstance().addJobScheduler("testJob", jobScheduler);
        jobOperationListenerManager.new JobShutdownStatusJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/testJob/servers/" + ip + "/shutdown", null, "".getBytes())), "/testJob/servers/" + ip + "/shutdown");
        verify(jobScheduler, times(0)).shutdown();
    }
    
    @Test
    public void assertJobShutdownStatusJobListenerWhenIsJobShutdownPathAndAdd() {
        JobRegistry.getInstance().addJobScheduler("testJob", jobScheduler);
        jobOperationListenerManager.new JobShutdownStatusJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/testJob/servers/" + ip + "/shutdown", null, "".getBytes())), "/testJob/servers/" + ip + "/shutdown");
        verify(jobScheduler).shutdown();
        verify(serverService).processServerShutdown();
    }
}
