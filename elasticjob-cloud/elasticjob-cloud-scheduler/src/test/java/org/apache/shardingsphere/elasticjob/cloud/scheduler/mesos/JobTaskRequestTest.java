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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos;

import com.netflix.fenzo.TaskRequest;
import org.apache.shardingsphere.elasticjob.cloud.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.context.TaskContext;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.hamcrest.core.StringStartsWith;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;

public final class JobTaskRequestTest {
    
    private final JobTaskRequest jobTaskRequest = 
            new JobTaskRequest(new TaskContext("test_job", Collections.singletonList(0), ExecutionType
                    .READY, "unassigned-slave"), CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job"));
    
    @Test
    public void assertGetId() {
        Assert.assertThat(jobTaskRequest.getId(), StringStartsWith.startsWith("test_job@-@0@-@READY@-@unassigned-slave"));
    }
    
    @Test
    public void assertTaskGroupName() {
        Assert.assertThat(jobTaskRequest.taskGroupName(), is(""));
    }
    
    @Test
    public void assertGetCPUs() {
        Assert.assertThat(jobTaskRequest.getCPUs(), is(1.0d));
    }
    
    @Test
    public void assertGetMemory() {
        Assert.assertThat(jobTaskRequest.getMemory(), is(128.0d));
    }
    
    @Test
    public void assertGetNetworkMbps() {
        Assert.assertThat(jobTaskRequest.getNetworkMbps(), is(0d));
    }
    
    @Test
    public void assertGetDisk() {
        Assert.assertThat(jobTaskRequest.getDisk(), is(10d));
    }
    
    @Test
    public void assertGetPorts() {
        Assert.assertThat(jobTaskRequest.getPorts(), is(1));
    }
    
    @Test
    public void assertGetScalarRequests() {
        Assert.assertNull(jobTaskRequest.getScalarRequests());
    }
    
    @Test
    public void assertGetHardConstraints() {
        AppConstraintEvaluator.init(null);
        Assert.assertThat(jobTaskRequest.getHardConstraints().size(), is(1));
    }
    
    @Test
    public void assertGetSoftConstraints() {
        Assert.assertNull(jobTaskRequest.getSoftConstraints());
    }
    
    @Test
    public void assertSetAssignedResources() {
        jobTaskRequest.setAssignedResources(null);
    }
    
    @Test
    public void assertGetAssignedResources() {
        Assert.assertNull(jobTaskRequest.getAssignedResources());
    }
    
    @Test
    public void assertGetCustomNamedResources() {
        Assert.assertThat(jobTaskRequest.getCustomNamedResources(), is(Collections.<String, TaskRequest.NamedResourceSetRequest>emptyMap()));
    }
}
