package com.dangdang.ddframe.job.cloud.scheduler.mesos;

import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.apache.mesos.Protos;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FrameworkIDHolderTest {
    
    @Mock
    private CoordinatorRegistryCenter registryCenter;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        FrameworkIDHolder.setRegCenter(registryCenter);
    }
    
    @Test
    public void assertSupply() throws Exception {
        when(registryCenter.getDirectly(FrameworkIDHolder.FRAMEWORK_ID_NODE)).thenReturn("1");
        Protos.FrameworkInfo.Builder builder = Protos.FrameworkInfo.newBuilder().setUser("test").setName("name");
        FrameworkIDHolder.supply(builder);
        assertThat(builder.build().getId().getValue(), is("1"));
        verify(registryCenter).getDirectly(FrameworkIDHolder.FRAMEWORK_ID_NODE);
    }
    
    @Test
    public void assertSave() throws Exception {
        when(registryCenter.isExisted(FrameworkIDHolder.FRAMEWORK_ID_NODE)).thenReturn(false);
        FrameworkIDHolder.save(Protos.FrameworkID.newBuilder().setValue("1").build());
        verify(registryCenter).isExisted(FrameworkIDHolder.FRAMEWORK_ID_NODE);
        verify(registryCenter).persist(FrameworkIDHolder.FRAMEWORK_ID_NODE, "1");
    }
    
}
