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

package com.dangdang.ddframe.job.lite.internal.server;

import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class InstanceNodeTest {
    
    private static InstanceNode instanceNode;
    
    @BeforeClass
    public static void setUp() throws NoSuchFieldException {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        instanceNode = new InstanceNode("test_job");
    }
    
    @Test
    public void assertGetLocalInstanceNode() {
        assertThat(instanceNode.getLocalInstanceNode(), is("instances/127.0.0.1@-@0"));
    }
    
    @Test
    public void assertGetInstanceNode() {
        assertThat(InstanceNode.getInstanceNode("host0@-@0"), is("instances/host0@-@0"));
    }
    
    @Test
    public void assertIsLocalInstancePath() {
        assertTrue(instanceNode.isLocalInstancePath("/test_job/instances/127.0.0.1@-@0"));
    }
    
    @Test
    public void assertIsNotLocalInstancePath() {
        assertFalse(instanceNode.isLocalInstancePath("/test_job/instances/other_job_instance_id"));
    }
    
    @Test
    public void assertIsInstancePath() {
        assertTrue(instanceNode.isInstancePath("/test_job/instances/127.0.0.1@-@0"));
    }
    
    @Test
    public void assertIsNotInstancePath() {
        assertFalse(instanceNode.isInstancePath("/test_job/other/127.0.0.1@-@0"));
    }
}
