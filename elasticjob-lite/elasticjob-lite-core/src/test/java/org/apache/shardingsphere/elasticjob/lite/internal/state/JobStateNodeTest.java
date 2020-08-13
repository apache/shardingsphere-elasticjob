package org.apache.shardingsphere.elasticjob.lite.internal.state;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JobStateNodeTest {

    @Test
    public void assertGetRootState() {
        assertThat(JobStateNode.getRootState(), is("state/state"));
    }

    @Test
    public void assertGetRootProc() {
        assertThat(JobStateNode.getRootProc(), is("proc"));
    }

    @Test
    public void assertGetProcFail() {
        assertThat(JobStateNode.getProcFail(3), is("proc/fail/3"));
    }

    @Test
    public void assertGetProcSucc() {
        assertThat(JobStateNode.getProcSucc(2), is("proc/succ/2"));
    }
}
