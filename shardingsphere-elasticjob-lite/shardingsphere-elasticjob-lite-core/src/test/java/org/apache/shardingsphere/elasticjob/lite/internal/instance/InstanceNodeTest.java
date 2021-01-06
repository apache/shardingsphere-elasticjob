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

package org.apache.shardingsphere.elasticjob.lite.internal.instance;

import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class InstanceNodeTest {
    
    private static InstanceNode instanceNode;
    
    @BeforeClass
    public static void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        instanceNode = new InstanceNode("test_job");
    }
    
    @Test
    public void assertGetInstanceFullPath() {
        assertThat(instanceNode.getInstanceFullPath(), is("/test_job/instances"));
    }
    
    @Test
    public void assertIsInstancePath() {
        assertTrue(instanceNode.isInstancePath("/test_job/instances/127.0.0.1@-@0"));
    }
    
    @Test
    public void assertIsNotInstancePath() {
        assertFalse(instanceNode.isInstancePath("/test_job/other/127.0.0.1@-@0"));
    }
    
    @Test
    public void assertIsLocalInstancePath() {
        assertTrue(instanceNode.isLocalInstancePath("/test_job/instances/127.0.0.1@-@0"));
    }
    
    @Test
    public void assertIsNotLocalInstancePath() {
        assertFalse(instanceNode.isLocalInstancePath("/test_job/instances/127.0.0.2@-@0"));
    }
    
    @Test
    public void assertGetLocalInstancePath() {
        assertThat(instanceNode.getLocalInstancePath(), is("instances/127.0.0.1@-@0"));
    }

    @Test
    public void assertGetInstancePath() {
        assertThat(instanceNode.getInstancePath("127.0.0.1@-@0"), is("instances/127.0.0.1@-@0"));
    }
}
