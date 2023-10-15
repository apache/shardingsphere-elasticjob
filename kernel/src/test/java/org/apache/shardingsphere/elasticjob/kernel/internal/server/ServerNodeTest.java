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

package org.apache.shardingsphere.elasticjob.kernel.internal.server;

import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerNodeTest {
    
    private final ServerNode serverNode = new ServerNode("test_job");
    
    @BeforeAll
    static void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0", null, "127.0.0.1"));
    }
    
    @Test
    void assertIsServerPath() {
        assertTrue(serverNode.isServerPath("/test_job/servers/127.0.0.1"));
    }
    
    @Test
    void assertIsNotServerPath() {
        assertFalse(serverNode.isServerPath("/test_job/servers/255.255.255.256"));
    }
    
    @Test
    void assertIsLocalServerPath() {
        assertTrue(serverNode.isLocalServerPath("/test_job/servers/127.0.0.1"));
    }
    
    @Test
    void assertIsNotLocalServerPath() {
        assertFalse(serverNode.isLocalServerPath("/test_job/servers/127.0.0.2"));
    }
    
    @Test
    void assertGetServerNode() {
        assertThat(serverNode.getServerNode("127.0.0.1"), is("servers/127.0.0.1"));
    }
}
