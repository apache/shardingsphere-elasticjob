package com.dangdang.ddframe.job.lite.internal.instance;

import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
import com.dangdang.ddframe.job.lite.internal.election.LeaderService;
import com.dangdang.ddframe.job.lite.internal.monitor.MonitorService;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
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
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private LeaderService leaderService;
    
    @Mock
    private MonitorService monitorService;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    private ShutdownListenerManager shutdownListenerManager;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        shutdownListenerManager = new ShutdownListenerManager(null, "test_job");
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(shutdownListenerManager, "leaderService", leaderService);
        ReflectionUtils.setFieldValue(shutdownListenerManager, "monitorService", monitorService);
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
        verify(jobScheduleController, times(0)).shutdown();
    }
    
    @Test
    public void assertUpdateLocalInstancePath() {
        shutdownListenerManager.new InstanceShutdownStatusJobListener().dataChanged("/test_job/instances/127.0.0.1@-@0", Type.NODE_UPDATED, "");
        verify(jobScheduleController, times(0)).shutdown();
    }
    
    @Test
    public void assertRemoveLocalInstancePathAndIsNotLeaderAndJobControllerIsNull() {
        shutdownListenerManager.new InstanceShutdownStatusJobListener().dataChanged("/test_job/instances/127.0.0.1@-@0", Type.NODE_REMOVED, "");
        verify(leaderService, times(0)).removeLeader();
        verify(jobScheduleController, times(0)).shutdown();
    }
    
    @Test
    public void assertRemoveLocalInstancePathAndIsLeader() {
        when(leaderService.isLeader()).thenReturn(true);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController, regCenter);
        shutdownListenerManager.new InstanceShutdownStatusJobListener().dataChanged("/test_job/instances/127.0.0.1@-@0", Type.NODE_REMOVED, "");
        verify(monitorService).close();
        verify(jobScheduleController).shutdown();
    }
}
