package com.dangdang.ddframe.job.lite.api.strategy;

import com.dangdang.ddframe.job.util.env.IpUtils;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class JobInstanceTest {
    
    @Test
    public void assertGetJobInstanceId() {
        assertThat(new JobInstance(JobInstance.DEFAULT_INSTANCE_ID).getJobInstanceId(), is("1.1.1.1@-@1"));
    }
    
    @Test
    public void assertGetIp() {
        assertThat(new JobInstance(JobInstance.DEFAULT_INSTANCE_ID).getIp(), is("1.1.1.1"));
        assertThat(new JobInstance().getIp(), is(IpUtils.getIp()));
    }
}
