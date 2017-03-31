package com.dangdang.ddframe.job.lite.internal.instance;

import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
import com.dangdang.ddframe.job.lite.internal.election.LeaderService;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class InstanceShutdownListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private InstanceService instanceService;
    
    @Mock
    private LeaderService leaderService;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    private InstanceShutdownListenerManager instanceShutdownListenerManager;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        instanceShutdownListenerManager = new InstanceShutdownListenerManager(null, "test_job");
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(instanceShutdownListenerManager, "instanceService", instanceService);
        ReflectionUtils.setFieldValue(instanceShutdownListenerManager, "leaderService", leaderService);
        ReflectionUtils.setFieldValue(instanceShutdownListenerManager, instanceShutdownListenerManager.getClass().getSuperclass().getDeclaredField("jobNodeStorage"), jobNodeStorage);
    }
    
    @Test
    public void assertStart() {
        instanceShutdownListenerManager.start();
        verify(jobNodeStorage).addDataListener(Matchers.<TreeCacheListener>any());
    }
    
    @Test
    public void assertIsNotLocalInstancePath() {
        String path = "/test_job/instances/127.0.0.2@-@0";
        instanceShutdownListenerManager.new InstanceShutdownStatusJobListener().dataChanged(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_REMOVED, new ChildData(path, null, "".getBytes())), path);
        verify(instanceService, times(0)).removeStatus();
        verify(jobScheduleController, times(0)).shutdown();
    }
    
    @Test
    public void assertUpdateLocalInstancePath() {
        String path = "/test_job/instances/127.0.0.1@-@0";
        instanceShutdownListenerManager.new InstanceShutdownStatusJobListener().dataChanged(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED, new ChildData(path, null, "".getBytes())), path);
        verify(instanceService, times(0)).removeStatus();
        verify(jobScheduleController, times(0)).shutdown();
    }
    
    @Test
    public void assertRemoveLocalInstancePathAndIsNotLeaderAndJobControllerIsNull() {
        String path = "/test_job/instances/127.0.0.1@-@0";
        instanceShutdownListenerManager.new InstanceShutdownStatusJobListener().dataChanged(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_REMOVED, new ChildData(path, null, "".getBytes())), path);
        verify(instanceService).removeStatus();
        verify(leaderService, times(0)).removeLeader();
        verify(jobScheduleController, times(0)).shutdown();
    }
    
    @Test
    public void assertRemoveLocalInstancePathAndIsLeader() {
        when(leaderService.isLeader()).thenReturn(true);
        JobRegistry.getInstance().addJobScheduleController("test_job", jobScheduleController);
        String path = "/test_job/instances/127.0.0.1@-@0";
        instanceShutdownListenerManager.new InstanceShutdownStatusJobListener().dataChanged(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_REMOVED, new ChildData(path, null, "".getBytes())), path);
        verify(instanceService).removeStatus();
        verify(leaderService).removeLeader();
        verify(jobScheduleController).shutdown();
    }
}