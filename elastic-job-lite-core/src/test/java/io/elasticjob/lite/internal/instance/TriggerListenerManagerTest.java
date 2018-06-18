package io.elasticjob.lite.internal.instance;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.schedule.JobScheduleController;
import io.elasticjob.lite.internal.storage.JobNodeStorage;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public final class TriggerListenerManagerTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private InstanceService instanceService;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    private TriggerListenerManager triggerListenerManager;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        triggerListenerManager = new TriggerListenerManager(null, "test_job");
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(triggerListenerManager, "instanceService", instanceService);
        ReflectionUtils.setFieldValue(triggerListenerManager, triggerListenerManager.getClass().getSuperclass().getDeclaredField("jobNodeStorage"), jobNodeStorage);
    }
    
    @Test
    public void assertStart() {
        triggerListenerManager.start();
        verify(jobNodeStorage).addDataListener(ArgumentMatchers.<TreeCacheListener>any());
    }
    
    @Test
    public void assertNotTriggerWhenIsNotTriggerOperation() {
        triggerListenerManager.new JobTriggerStatusJobListener().dataChanged("/test_job/instances/127.0.0.1@-@0", Type.NODE_UPDATED, "");
        verify(instanceService, times(0)).clearTriggerFlag();
    }
    
    @Test
    public void assertNotTriggerWhenIsNotLocalInstancePath() {
        triggerListenerManager.new JobTriggerStatusJobListener().dataChanged("/test_job/instances/127.0.0.2@-@0", Type.NODE_UPDATED, InstanceOperation.TRIGGER.name());
        verify(instanceService, times(0)).clearTriggerFlag();
    }
    
    @Test
    public void assertNotTriggerWhenIsNotUpdate() {
        triggerListenerManager.new JobTriggerStatusJobListener().dataChanged("/test_job/instances/127.0.0.1@-@0", Type.NODE_ADDED, InstanceOperation.TRIGGER.name());
        verify(instanceService, times(0)).clearTriggerFlag();
    }
    
    @Test
    public void assertTriggerWhenJobScheduleControllerIsNull() {
        triggerListenerManager.new JobTriggerStatusJobListener().dataChanged("/test_job/instances/127.0.0.1@-@0", Type.NODE_UPDATED, InstanceOperation.TRIGGER.name());
        verify(instanceService).clearTriggerFlag();
        verify(jobScheduleController, times(0)).triggerJob();
    }
    
    @Test
    public void assertTriggerWhenJobIsRunning() {
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController, regCenter);
        JobRegistry.getInstance().setJobRunning("test_job", true);
        triggerListenerManager.new JobTriggerStatusJobListener().dataChanged("/test_job/instances/127.0.0.1@-@0", Type.NODE_UPDATED, InstanceOperation.TRIGGER.name());
        verify(instanceService).clearTriggerFlag();
        verify(jobScheduleController, times(0)).triggerJob();
        JobRegistry.getInstance().setJobRunning("test_job", false);
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertTriggerWhenJobIsNotRunning() {
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController, regCenter);
        triggerListenerManager.new JobTriggerStatusJobListener().dataChanged("/test_job/instances/127.0.0.1@-@0", Type.NODE_UPDATED, InstanceOperation.TRIGGER.name());
        verify(instanceService).clearTriggerFlag();
        verify(jobScheduleController).triggerJob();
        JobRegistry.getInstance().shutdown("test_job");
    }
}
