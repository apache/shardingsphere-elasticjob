package org.apache.shardingsphere.elasticjob.lite.internal.state;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JobStateEnumTest {

    @Test
    public void assertEnumOf() {
        assertThat(JobStateEnum.of("none"), is(JobStateEnum.NONE));
        assertThat(JobStateEnum.of("running"), is(JobStateEnum.RUNNING));
        assertThat(JobStateEnum.of("success"), is(JobStateEnum.SUCCESS));
        assertThat(JobStateEnum.of("skip"), is(JobStateEnum.SKIP));
        assertThat(JobStateEnum.of("fail"), is(JobStateEnum.FAIL));
    }
}
