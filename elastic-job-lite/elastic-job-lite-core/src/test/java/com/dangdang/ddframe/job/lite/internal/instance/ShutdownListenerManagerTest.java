package com.dangdang.ddframe.job.lite.internal.instance;

import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
import com.dangdang.ddframe.job.lite.internal.election.LeaderService;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
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

public final class ShutdownListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private InstanceService instanceService;
    
    @Mock
    private LeaderService leaderService;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    private ShutdownListenerManager shutdownListenerManager;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        shutdownListenerManager = new ShutdownListenerManager(null, "test_job");
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(shutdownListenerManager, "instanceService", instanceService);
        ReflectionUtils.setFieldValue(shutdownListenerManager, "leaderService", leaderService);
        ReflectionUtils.setFieldValue(shutdownListenerManager, shutdownListenerManager.getClass().getSuperclass().getDeclaredField("jobNodeStorage"), jobNodeStorage);
    }
    
    @Test
    public void assertStart() {
        shutdownListenerManager.start();
        verify(jobNodeStorage).addDataListener(Matchers.<TreeCacheListener>any());
    }
    
    @Test
    public void assertIsNotLocalInstancePath() {
        shutdownListenerManager.new InstanceShutdownStatusJobListener().dataChanged("/test_job/instances/127.0.0.2@-@0", Type.NODE_REMOVED, "");
        verify(instanceService, times(0)).removeStatus();
        verify(jobScheduleController, times(0)).shutdown();
    }
    
    @Test
    public void assertUpdateLocalInstancePath() {
        String path = "/test_job/instances/127.0.0.1@-@0";
        shutdownListenerManager.new InstanceShutdownStatusJobListener().dataChanged("/test_job/instances/127.0.0.1@-@0", Type.NODE_UPDATED, "");
        verify(instanceService, times(0)).removeStatus();
        verify(jobScheduleController, times(0)).shutdown();
    }
    
    @Test
    public void assertRemoveLocalInstancePathAndIsNotLeaderAndJobControllerIsNull() {
        shutdownListenerManager.new InstanceShutdownStatusJobListener().dataChanged("/test_job/instances/127.0.0.1@-@0", Type.NODE_REMOVED, "");
        verify(instanceService).removeStatus();
        verify(leaderService, times(0)).removeLeader();
        verify(jobScheduleController, times(0)).shutdown();
    }
    
    @Test
    public void assertRemoveLocalInstancePathAndIsLeader() {
        when(leaderService.isLeader()).thenReturn(true);
        JobRegistry.getInstance().addJobScheduleController("test_job", jobScheduleController);
        shutdownListenerManager.new InstanceShutdownStatusJobListener().dataChanged("/test_job/instances/127.0.0.1@-@0", Type.NODE_REMOVED, "");
        verify(instanceService).removeStatus();
        verify(leaderService).removeLeader();
        verify(jobScheduleController).shutdown();
    }
}
