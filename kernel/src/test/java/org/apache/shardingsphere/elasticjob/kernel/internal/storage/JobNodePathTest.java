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

package org.apache.shardingsphere.elasticjob.kernel.internal.storage;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class JobNodePathTest {
    
    private final JobNodePath jobNodePath = new JobNodePath("test_job");
    
    @Test
    void assertGetFullPath() {
        assertThat(jobNodePath.getFullPath("node"), is("/test_job/node"));
    }
    
    @Test
    void assertGetServerNodePath() {
        assertThat(jobNodePath.getServerNodePath(), is("/test_job/servers"));
    }
    
    @Test
    void assertGetServerNodePathForServerIp() {
        assertThat(jobNodePath.getServerNodePath("ip0"), is("/test_job/servers/ip0"));
    }
    
    @Test
    void assertGetShardingNodePath() {
        assertThat(jobNodePath.getShardingNodePath(), is("/test_job/sharding"));
    }
    
    @Test
    void assertGetShardingNodePathWihItemAndNode() {
        assertThat(jobNodePath.getShardingNodePath("0", "running"), is("/test_job/sharding/0/running"));
    }
    
    @Test
    void assertGetLeaderIpNodePath() {
        assertThat(jobNodePath.getLeaderHostNodePath(), is("/test_job/leader/election/instance"));
    }
}
