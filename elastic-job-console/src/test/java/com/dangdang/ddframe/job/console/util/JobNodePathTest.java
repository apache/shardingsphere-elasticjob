/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.console.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public final class JobNodePathTest {
    
    @Test
    public void assertGetConfigNodePath() {
        assertThat(JobNodePath.getConfigNodePath("test_job", "test"), is("/test_job/config/test"));
    }
    
    @Test
    public void assertGetServerNodePath() {
        assertThat(JobNodePath.getServerNodePath("test_job"), is("/test_job/servers"));
        assertThat(JobNodePath.getServerNodePath("test_job", "localhost", "test"), is("/test_job/servers/localhost/test"));
    }
    
    @Test
    public void assertGetExecutionNodePath() {
        assertThat(JobNodePath.getExecutionNodePath("test_job"), is("/test_job/execution"));
        assertThat(JobNodePath.getExecutionNodePath("test_job", "0", "test"), is("/test_job/execution/0/test"));
    }
    
    @Test
    public void assertGetLeaderNodePath() {
        assertThat(JobNodePath.getLeaderNodePath("test_job", "test/test"), is("/test_job/leader/test/test"));
    }
}
