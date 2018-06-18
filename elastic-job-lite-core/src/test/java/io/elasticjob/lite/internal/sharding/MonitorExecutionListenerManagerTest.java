package io.elasticjob.lite.internal.sharding;

import io.elasticjob.lite.fixture.LiteJsonConstants;
import io.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public final class MonitorExecutionListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ExecutionService executionService;
    
    
    private final MonitorExecutionListenerManager monitorExecutionListenerManager = new MonitorExecutionListenerManager(null, "test_job");
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(monitorExecutionListenerManager, monitorExecutionListenerManager.getClass().getSuperclass().getDeclaredField("jobNodeStorage"), jobNodeStorage);
        ReflectionUtils.setFieldValue(monitorExecutionListenerManager, "executionService", executionService);
    }
    
    @Test
    public void assertMonitorExecutionSettingsChangedJobListenerWhenIsNotFailoverPath() {
        monitorExecutionListenerManager.new MonitorExecutionSettingsChangedJobListener().dataChanged("/test_job/other", TreeCacheEvent.Type.NODE_ADDED, LiteJsonConstants.getJobJson());
        verify(executionService, times(0)).clearAllRunningInfo();
    }
    
    @Test
    public void assertMonitorExecutionSettingsChangedJobListenerWhenIsFailoverPathButNotUpdate() {
        monitorExecutionListenerManager.new MonitorExecutionSettingsChangedJobListener().dataChanged("/test_job/config", TreeCacheEvent.Type.NODE_ADDED, "");
        verify(executionService, times(0)).clearAllRunningInfo();
    }
    
    @Test
    public void assertMonitorExecutionSettingsChangedJobListenerWhenIsFailoverPathAndUpdateButEnableFailover() {
        monitorExecutionListenerManager.new MonitorExecutionSettingsChangedJobListener().dataChanged("/test_job/config", TreeCacheEvent.Type.NODE_UPDATED, LiteJsonConstants.getJobJson());
        verify(executionService, times(0)).clearAllRunningInfo();
    }
    
    @Test
    public void assertMonitorExecutionSettingsChangedJobListenerWhenIsFailoverPathAndUpdateButDisableFailover() {
        monitorExecutionListenerManager.new MonitorExecutionSettingsChangedJobListener().dataChanged(
                "/test_job/config", TreeCacheEvent.Type.NODE_UPDATED, LiteJsonConstants.getJobJsonWithMonitorExecution(false));
        verify(executionService).clearAllRunningInfo();
    }
}
