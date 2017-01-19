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
import com.dangdang.ddframe.job.lite.internal.worker.reconcile.ReconcileWorkerListenerManager.ReconcileCycleTimeChangedJobListener;

public class ReconcileWorkerListenerManagerTest{
    
    @Mock
    public JobNodeStorage jobNodeStorage;

    public final ReconcileWorkerListenerManager manager = new ReconcileWorkerListenerManager(null, "job_test");
    
    @Before
    public void setup() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(manager, manager.getClass().getSuperclass().getDeclaredField("jobNodeStorage"), jobNodeStorage);
    }
    
    @Test
    public void assertStart() {
        manager.start();
        Mockito.verify(jobNodeStorage).addDataListener(Matchers.<ReconcileCycleTimeChangedJobListener>any());
    }
    
    @Test
    public void assertChangeReconcileCycleTime() throws NoSuchFieldException {
        manager.new ReconcileCycleTimeChangedJobListener()
                .dataChanged(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/job_test/config", null , LiteJsonConstants.getJobJson().getBytes()))
                             , "/job_test/config");
        Assert.assertThat((Long) ReflectionUtils.getFieldValue(manager, manager.getClass().getDeclaredField("reconcileCycleTime"))
                                    , CoreMatchers.is(new Long(15)));
    }
    
    @Test
    public void assertNotChangeEventInConfigNode() throws NoSuchFieldException {
        manager.new ReconcileCycleTimeChangedJobListener()
                .dataChanged(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_ADDED, new ChildData("/job_test/config", null , LiteJsonConstants.getJobJson().getBytes()))
                     , "/job_test/config");
        Assert.assertThat((Long) ReflectionUtils.getFieldValue(manager, manager.getClass().getDeclaredField("reconcileCycleTime"))
                            , CoreMatchers.is(new Long(60)));
    }
    
    @Test
    public void assertChangeEventNotInConfigNode() throws NoSuchFieldException {
        manager.new ReconcileCycleTimeChangedJobListener()
                .dataChanged(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_ADDED, new ChildData("/job_test/config", null , LiteJsonConstants.getJobJson().getBytes()))
                     , "/job_test/test_node");
        Assert.assertThat((Long) ReflectionUtils.getFieldValue(manager, manager.getClass().getDeclaredField("reconcileCycleTime"))
                            , CoreMatchers.is(new Long(60)));
    }
    
    @Test
    public void assertNotChangeReconcileCycleTimeInConfigNode() throws NoSuchFieldException {
        manager.new ReconcileCycleTimeChangedJobListener()
                .dataChanged(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/job_test/config", null , LiteJsonConstants.getJobJson(60L).getBytes()))
                     , "/job_test/config");
        Assert.assertThat((Long) ReflectionUtils.getFieldValue(manager, manager.getClass().getDeclaredField("reconcileCycleTime"))
                            , CoreMatchers.is(new Long(60)));
    }
}
