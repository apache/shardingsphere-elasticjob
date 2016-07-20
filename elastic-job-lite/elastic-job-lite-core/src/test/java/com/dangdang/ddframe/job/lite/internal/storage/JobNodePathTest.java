/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.lite.internal.storage;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class JobNodePathTest {
    
    private JobNodePath jobNodePath = new JobNodePath("testJob");
    
    @Test
    public void assertGetFullPath() {
        assertThat(jobNodePath.getFullPath("node"), is("/testJob/node"));
    }
    
    @Test
    public void assertGetServerNodePath() {
        assertThat(jobNodePath.getServerNodePath(), is("/testJob/servers"));
    }
    
    @Test
    public void assertGetServerNodePathForServerIp() {
        assertThat(jobNodePath.getServerNodePath("ip0"), is("/testJob/servers/ip0"));
    }
    
    @Test
    public void assertGetServerNodePathForServerIpAndNameNode() {
        assertThat(jobNodePath.getServerNodePath("ip0", "node"), is("/testJob/servers/ip0/node"));
    }
}
