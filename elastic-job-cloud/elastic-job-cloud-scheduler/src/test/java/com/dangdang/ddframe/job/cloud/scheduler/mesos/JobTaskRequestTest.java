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

package com.dangdang.ddframe.job.cloud.scheduler.mesos;

import com.dangdang.ddframe.job.cloud.scheduler.context.ExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.context.TaskContext;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import com.netflix.fenzo.TaskRequest;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class JobTaskRequestTest {
    
    private JobTaskRequest jobTaskRequest = 
            new JobTaskRequest(new TaskContext("test_job", 0, ExecutionType.READY, "fake-slave"), CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job"));
    
    @Test
    public void assertGetId() {
        assertThat(jobTaskRequest.getId(), startsWith("test_job@-@0@-@READY@-@fake-slave"));
    }
    
    @Test
    public void assertTaskGroupName() {
        assertThat(jobTaskRequest.taskGroupName(), is(""));
    }
    
    @Test
    public void assertGetCPUs() {
        assertThat(jobTaskRequest.getCPUs(), is(1.0d));
    }
    
    @Test
    public void assertGetMemory() {
        assertThat(jobTaskRequest.getMemory(), is(128.0d));
    }
    
    @Test
    public void assertGetNetworkMbps() {
        assertThat(jobTaskRequest.getNetworkMbps(), is(0d));
    }
    
    @Test
    public void assertGetDisk() {
        assertThat(jobTaskRequest.getDisk(), is(10d));
    }
    
    @Test
    public void assertGetPorts() {
        assertThat(jobTaskRequest.getPorts(), is(1));
    }
    
    @Test
    public void assertGetScalarRequests() {
        assertNull(jobTaskRequest.getScalarRequests());
    }
    
    @Test
    public void assertGetHardConstraints() {
        assertNull(jobTaskRequest.getHardConstraints());
    }
    
    @Test
    public void assertGetSoftConstraints() {
        assertNull(jobTaskRequest.getSoftConstraints());
    }
    
    @Test
    public void assertSetAssignedResources() {
        jobTaskRequest.setAssignedResources(null);
    }
    
    @Test
    public void assertGetAssignedResources() {
        assertNull(jobTaskRequest.getAssignedResources());
    }
    
    @Test
    public void asertGetCustomNamedResources() {
        assertThat(jobTaskRequest.getCustomNamedResources(), is(Collections.<String, TaskRequest.NamedResourceSetRequest>emptyMap()));
    }
}
