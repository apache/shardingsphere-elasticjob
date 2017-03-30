package com.dangdang.ddframe.job.lite.internal.instance;

import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.server.ServerService;
import com.dangdang.ddframe.job.lite.internal.server.ServerStatus;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class InstanceServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ServerService serverService;
    
    private InstanceService instanceService;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        instanceService = new InstanceService(null, "test_job");
        MockitoAnnotations.initMocks(this);
        InstanceNode instanceNode = new InstanceNode("test_job");
        ReflectionUtils.setFieldValue(instanceService, "instanceNode", instanceNode);
        ReflectionUtils.setFieldValue(instanceService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(instanceService, "serverService", serverService);
    }
    
    @Test
    public void assertPersistOnline() {
        instanceService.persistOnline();
        verify(jobNodeStorage).fillEphemeralJobNode("instances/127.0.0.1@-@0", InstanceStatus.READY.name());
    }
    
    @Test
    public void assertUpdateStatus() {
        instanceService.updateStatus(InstanceStatus.RUNNING);
        verify(jobNodeStorage).updateJobNode("instances/127.0.0.1@-@0", InstanceStatus.RUNNING.name());
    }
    
    @Test
    public void assertRemoveStatus() {
        instanceService.removeStatus();
        verify(jobNodeStorage).removeJobNodeIfExisted("instances/127.0.0.1@-@0");
    }
    
    @Test
    public void assertGetAvailableJobInstances() {
        when(serverService.isServerEnabled("host0")).thenReturn(true);
        when(serverService.isServerEnabled("host3")).thenReturn(true);
        when(serverService.isServerEnabled("host4")).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("instances")).thenReturn(Arrays.asList("host0@-@0", "host2@-@0", "host3@-@0", "host4@-@0"));
        when(jobNodeStorage.getJobNodeData("servers/host2")).thenReturn(ServerStatus.DISABLED.name());
        assertThat(instanceService.getAvailableJobInstances(), is(Arrays.asList(new JobInstance("host0@-@0"), new JobInstance("host3@-@0"), new JobInstance("host4@-@0"))));
    }
}
