package com.dangdang.ddframe.job.lite.internal.worker.reconcile;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import com.dangdang.ddframe.job.lite.fixture.LiteJsonConstants;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.lite.internal.worker.reconcile.ReconcileListenerManager.ReconcileIntervalSecondsChangedJobListener;

public class ReconcileListenerManagerTest {
    
    @Mock
    public JobNodeStorage jobNodeStorage;

    private final ReconcileListenerManager manager = new ReconcileListenerManager(null, "job_test");
    
    @Before
    public void setup() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(manager, manager.getClass().getSuperclass().getDeclaredField("jobNodeStorage"), jobNodeStorage);
    }
    
    @Test
    public void assertStart() {
        manager.start();
        Mockito.verify(jobNodeStorage).addDataListener(Matchers.<ReconcileIntervalSecondsChangedJobListener>any());
    }
    
    @Test
    public void assertChangeReconcileCycleTime() throws NoSuchFieldException {
        manager.new ReconcileIntervalSecondsChangedJobListener()
                .dataChanged(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/job_test/config", null, 
                        LiteJsonConstants.getJobJson().getBytes())), "/job_test/config");
        Assert.assertThat((int) ReflectionUtils.getFieldValue(manager, manager.getClass().getDeclaredField("reconcileIntervalSeconds")), CoreMatchers.is(15));
    }
    
    @Test
    public void assertNotChangeEventInConfigNode() throws NoSuchFieldException {
        manager.new ReconcileIntervalSecondsChangedJobListener()
                .dataChanged(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_ADDED, new ChildData("/job_test/config", null, 
                        LiteJsonConstants.getJobJson().getBytes())), "/job_test/config");
        Assert.assertThat((int) ReflectionUtils.getFieldValue(manager, manager.getClass().getDeclaredField("reconcileIntervalSeconds")), CoreMatchers.is(60));
    }
    
    @Test
    public void assertChangeEventNotInConfigNode() throws NoSuchFieldException {
        manager.new ReconcileIntervalSecondsChangedJobListener()
                .dataChanged(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_ADDED, new ChildData("/job_test/config", null, 
                        LiteJsonConstants.getJobJson().getBytes())), "/job_test/test_node");
        Assert.assertThat((int) ReflectionUtils.getFieldValue(manager, manager.getClass().getDeclaredField("reconcileIntervalSeconds")), CoreMatchers.is(60));
    }
    
    @Test
    public void assertNotChangeReconcileCycleTimeInConfigNode() throws NoSuchFieldException {
        manager.new ReconcileIntervalSecondsChangedJobListener()
                .dataChanged(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/job_test/config", null, 
                        LiteJsonConstants.getJobJson(60L).getBytes())), "/job_test/config");
        Assert.assertThat((int) ReflectionUtils.getFieldValue(manager, manager.getClass().getDeclaredField("reconcileIntervalSeconds")), CoreMatchers.is(60));
    }
}
