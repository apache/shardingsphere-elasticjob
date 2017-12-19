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

package io.elasticjob.lite.internal.storage;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class JobNodePathTest {
    
    private JobNodePath jobNodePath = new JobNodePath("test_job");
    
    @Test
    public void assertGetFullPath() {
        assertThat(jobNodePath.getFullPath("node"), is("/test_job/node"));
    }
    
    @Test
    public void assertGetServerNodePath() {
        assertThat(jobNodePath.getServerNodePath(), is("/test_job/servers"));
    }
    
    @Test
    public void assertGetServerNodePathForServerIp() {
        assertThat(jobNodePath.getServerNodePath("ip0"), is("/test_job/servers/ip0"));
    }
    
    @Test
    public void assertGetShardingNodePath() {
        assertThat(jobNodePath.getShardingNodePath(), is("/test_job/sharding"));
    }
    
    @Test
    public void assertGetShardingNodePathWihItemAndNode() {
        assertThat(jobNodePath.getShardingNodePath("0", "running"), is("/test_job/sharding/0/running"));
    }
    
    @Test
    public void assertGetLeaderIpNodePath() {
        assertThat(jobNodePath.getLeaderHostNodePath(), is("/test_job/leader/election/instance"));
    }
}
