/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
