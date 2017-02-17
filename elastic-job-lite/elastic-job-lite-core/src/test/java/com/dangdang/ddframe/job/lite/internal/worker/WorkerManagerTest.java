package com.dangdang.ddframe.job.lite.internal.worker;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class WorkerManagerTest {
    
    @Mock
    private ExecutorService executorService;

    private WorkersManager workersManager = new WorkersManager(null, "job-test");
    
    @Before
    public void setup() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(workersManager, "executor", executorService);
    }
    
    @Test
    public void assertStart() throws NoSuchFieldException {
        workersManager.start();
        List workers = ReflectionUtils.getFieldValue(workersManager, workersManager.getClass().getDeclaredField("workers"));
        Assert.assertThat(workers.size(), CoreMatchers.is(1));
        Mockito.verify(executorService).submit(Matchers.<AbstractWorker>any());
    }
    
}
