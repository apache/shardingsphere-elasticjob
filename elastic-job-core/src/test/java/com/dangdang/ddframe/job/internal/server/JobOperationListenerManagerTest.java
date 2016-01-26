/**
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
import com.dangdang.ddframe.job.integrate.AbstractBaseStdJobTest.TestJob;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.internal.server.JobOperationListenerManager.ConnectionLostListener;
import com.dangdang.ddframe.job.internal.server.JobOperationListenerManager.JobStopedStatusJobListener;
import com.dangdang.ddframe.job.internal.storage.JobNodeStorage;

public final class JobOperationListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private JobScheduler jobScheduler;
    
    private String ip = new LocalHostService().getIp();
    
    private final JobOperationListenerManager jobOperationListenerManager = new JobOperationListenerManager(null, new JobConfiguration("testJob", TestJob.class, 3, "0/1 * * * * ?"));
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(jobOperationListenerManager, jobOperationListenerManager.getClass().getSuperclass().getDeclaredField("jobNodeStorage"), jobNodeStorage);
    }
    
    @Test
    public void assertStart() {
        jobOperationListenerManager.start();
        verify(jobNodeStorage).addConnectionStateListener(Matchers.<ConnectionLostListener>any());
        verify(jobNodeStorage).addDataListener(Matchers.<JobStopedStatusJobListener>any());
    }
    
    @Test
    public void assertConnectionLostListenerWhenConnectionStateIsLost() {
        JobRegistry.getInstance().addJob("testJob", jobScheduler);
        jobOperationListenerManager.new ConnectionLostListener().stateChanged(null, ConnectionState.LOST);
        verify(jobScheduler).stopJob();
    }
    
    @Test
    public void assertConnectionLostListenerWhenConnectionStateIsReconnected() {
        JobRegistry.getInstance().addJob("testJob", jobScheduler);
        jobOperationListenerManager.new ConnectionLostListener().stateChanged(null, ConnectionState.RECONNECTED);
        verify(jobScheduler).resumeCrashedJob();
    }
    
    @Test
    public void assertJobStopedStatusJobListenerWhenIsNotJobStopedPath() {
        jobOperationListenerManager.new JobStopedStatusJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/testJob/servers/" + ip + "/other", null, "".getBytes())), "/testJob/servers/" + ip + "/other");
        verify(jobScheduler, times(0)).stopJob();
        verify(jobScheduler, times(0)).resumeManualStopedJob();
    }
    
    @Test
    public void assertJobStopedStatusJobListenerWhenIsJobStopedPathButJobIsNotExisted() {
        jobOperationListenerManager.new JobStopedStatusJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/testJob/servers/" + ip + "/stoped", null, "".getBytes())), "/testJob/servers/" + ip + "/stoped");
        verify(jobScheduler, times(0)).stopJob();
        verify(jobScheduler, times(0)).resumeManualStopedJob();
    }
    
    @Test
    public void assertJobStopedStatusJobListenerWhenIsJobStopedPathAndUpdate() {
        JobRegistry.getInstance().addJob("testJob", jobScheduler);
        jobOperationListenerManager.new JobStopedStatusJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/testJob/servers/" + ip + "/stoped", null, "".getBytes())), "/testJob/servers/" + ip + "/stoped");
        verify(jobScheduler, times(0)).stopJob();
        verify(jobScheduler, times(0)).resumeManualStopedJob();
    }
    
    @Test
    public void assertJobStopedStatusJobListenerWhenIsJobStopedPathAndAdd() {
        JobRegistry.getInstance().addJob("testJob", jobScheduler);
        jobOperationListenerManager.new JobStopedStatusJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/testJob/servers/" + ip + "/stoped", null, "".getBytes())), "/testJob/servers/" + ip + "/stoped");
        verify(jobScheduler).stopJob();
        verify(jobScheduler, times(0)).resumeManualStopedJob();
    }
    
    @Test
    public void assertJobStopedStatusJobListenerWhenIsJobStopedPathAndRemove() {
        JobRegistry.getInstance().addJob("testJob", jobScheduler);
        jobOperationListenerManager.new JobStopedStatusJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/testJob/servers/" + ip + "/stoped", null, "".getBytes())), "/testJob/servers/" + ip + "/stoped");
        verify(jobScheduler, times(0)).stopJob();
        verify(jobScheduler).resumeManualStopedJob();
    }
}
