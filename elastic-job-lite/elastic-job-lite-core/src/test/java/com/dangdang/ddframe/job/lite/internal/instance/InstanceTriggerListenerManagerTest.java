package com.dangdang.ddframe.job.lite.internal.instance;

import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
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

public final class InstanceTriggerListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private InstanceService instanceService;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    private InstanceTriggerListenerManager instanceTriggerListenerManager;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        instanceTriggerListenerManager = new InstanceTriggerListenerManager(null, "test_job");
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(instanceTriggerListenerManager, "instanceService", instanceService);
        ReflectionUtils.setFieldValue(instanceTriggerListenerManager, instanceTriggerListenerManager.getClass().getSuperclass().getDeclaredField("jobNodeStorage"), jobNodeStorage);
    }
    
    @Test
    public void assertStart() {
        instanceTriggerListenerManager.start();
        verify(jobNodeStorage).addDataListener(Matchers.<TreeCacheListener>any());
    }
    
    @Test
    public void assertNotTriggerWhenIsNotTriggerOperation() {
        String path = "/test_job/instances/127.0.0.1@-@0";
        instanceTriggerListenerManager.new JobTriggerStatusJobListener().dataChanged(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED, new ChildData(path, null, "".getBytes())), path);
        verify(instanceService, times(0)).clearTriggerFlag();
    }
    
    @Test
    public void assertNotTriggerWhenIsNotLocalInstancePath() {
        String path = "/test_job/instances/127.0.0.2@-@0";
        instanceTriggerListenerManager.new JobTriggerStatusJobListener().dataChanged(
                null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED, new ChildData(path, null, InstanceOperation.TRIGGER.name().getBytes())), path);
        verify(instanceService, times(0)).clearTriggerFlag();
    }
    
    @Test
    public void assertNotTriggerWhenIsNotUpdate() {
        String path = "/test_job/instances/127.0.0.1@-@0";
        instanceTriggerListenerManager.new JobTriggerStatusJobListener().dataChanged(
                null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_ADDED, new ChildData(path, null, InstanceOperation.TRIGGER.name().getBytes())), path);
        verify(instanceService, times(0)).clearTriggerFlag();
    }
    
    @Test
    public void assertTriggerWhenJobScheduleControllerIsNull() {
        String path = "/test_job/instances/127.0.0.1@-@0";
        instanceTriggerListenerManager.new JobTriggerStatusJobListener().dataChanged(
                null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED, new ChildData(path, null, InstanceOperation.TRIGGER.name().getBytes())), path);
        verify(instanceService).clearTriggerFlag();
        verify(jobScheduleController, times(0)).triggerJob();
    }
    
    @Test
    public void assertTriggerWhenJobIsRunning() {
        String path = "/test_job/instances/127.0.0.1@-@0";
        JobRegistry.getInstance().addJobScheduleController("test_job", jobScheduleController);
        JobRegistry.getInstance().setJobRunning("test_job", true);
        instanceTriggerListenerManager.new JobTriggerStatusJobListener().dataChanged(
                null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED, new ChildData(path, null, InstanceOperation.TRIGGER.name().getBytes())), path);
        verify(instanceService).clearTriggerFlag();
        verify(jobScheduleController, times(0)).triggerJob();
        JobRegistry.getInstance().setJobRunning("test_job", false);
    }
    
    @Test
    public void assertTriggerWhenJobIsNotRunning() {
        String path = "/test_job/instances/127.0.0.1@-@0";
        JobRegistry.getInstance().addJobScheduleController("test_job", jobScheduleController);
        instanceTriggerListenerManager.new JobTriggerStatusJobListener().dataChanged(
                null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED, new ChildData(path, null, InstanceOperation.TRIGGER.name().getBytes())), path);
        verify(instanceService).clearTriggerFlag();
        verify(jobScheduleController).triggerJob();
    }
}