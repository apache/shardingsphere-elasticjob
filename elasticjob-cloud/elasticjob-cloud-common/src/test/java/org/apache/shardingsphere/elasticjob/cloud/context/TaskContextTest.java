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

package org.apache.shardingsphere.elasticjob.cloud.context;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.elasticjob.cloud.fixture.context.TaskNode;
import org.hamcrest.core.Is;
import org.hamcrest.core.StringStartsWith;
import org.junit.Assert;
import org.junit.Test;

public final class TaskContextTest {
    
    @Test
    public void assertNew() {
        TaskContext actual = new TaskContext("test_job", Lists.newArrayList(0), ExecutionType.READY, "slave-S0");
        Assert.assertThat(actual.getMetaInfo().getJobName(), Is.is("test_job"));
        Assert.assertThat(actual.getMetaInfo().getShardingItems().get(0), Is.is(0));
        Assert.assertThat(actual.getType(), Is.is(ExecutionType.READY));
        Assert.assertThat(actual.getSlaveId(), Is.is("slave-S0"));
        Assert.assertThat(actual.getId(), StringStartsWith.startsWith(TaskNode.builder().build().getTaskNodeValue().substring(0, TaskNode.builder().build().getTaskNodeValue().length() - 1)));
    }
    
    @Test
    public void assertNewWithoutSlaveId() {
        TaskContext actual = new TaskContext("test_job", Lists.newArrayList(0), ExecutionType.READY);
        Assert.assertThat(actual.getSlaveId(), Is.is("unassigned-slave"));
    }
    
    @Test
    public void assertGetMetaInfo() {
        TaskContext actual = new TaskContext("test_job", Lists.newArrayList(0), ExecutionType.READY, "slave-S0");
        Assert.assertThat(actual.getMetaInfo().toString(), Is.is("test_job@-@0"));
    }
    
    @Test
    public void assertTaskContextFrom() {
        TaskContext actual = TaskContext.from(TaskNode.builder().build().getTaskNodeValue());
        Assert.assertThat(actual.getId(), Is.is(TaskNode.builder().build().getTaskNodeValue()));
        Assert.assertThat(actual.getMetaInfo().getJobName(), Is.is("test_job"));
        Assert.assertThat(actual.getMetaInfo().getShardingItems().get(0), Is.is(0));
        Assert.assertThat(actual.getType(), Is.is(ExecutionType.READY));
        Assert.assertThat(actual.getSlaveId(), Is.is("slave-S0"));
    }
    
    @Test
    public void assertMetaInfoFromWithMetaInfo() {
        TaskContext.MetaInfo actual = TaskContext.MetaInfo.from("test_job@-@1");
        Assert.assertThat(actual.getJobName(), Is.is("test_job"));
        Assert.assertThat(actual.getShardingItems().get(0), Is.is(1));
    }
    
    @Test
    public void assertMetaInfoFromWithTaskId() {
        TaskContext.MetaInfo actual = TaskContext.MetaInfo.from("test_job@-@1@-@READY@-@unassigned-slave@-@0");
        Assert.assertThat(actual.getJobName(), Is.is("test_job"));
        Assert.assertThat(actual.getShardingItems().get(0), Is.is(1));
    }
    
    @Test
    public void assertMetaInfoFromWithMetaInfoWithoutShardingItems() {
        TaskContext.MetaInfo actual = TaskContext.MetaInfo.from("test_job@-@");
        Assert.assertThat(actual.getJobName(), Is.is("test_job"));
        Assert.assertTrue(actual.getShardingItems().isEmpty());
    }
    
    @Test
    public void assertMetaInfoFromWithTaskIdWithoutShardingItems() {
        TaskContext.MetaInfo actual = TaskContext.MetaInfo.from("test_job@-@@-@READY@-@unassigned-slave@-@0");
        Assert.assertThat(actual.getJobName(), Is.is("test_job"));
        Assert.assertTrue(actual.getShardingItems().isEmpty());
    }
    
    @Test
    public void assertGetIdForUnassignedSlave() {
        Assert.assertThat(TaskContext.getIdForUnassignedSlave("test_job@-@0@-@READY@-@slave-S0@-@0"), Is.is("test_job@-@0@-@READY@-@unassigned-slave@-@0"));
    }
    
    @Test
    public void assertGetTaskName() {
        TaskContext actual = TaskContext.from(TaskNode.builder().build().getTaskNodeValue());
        Assert.assertThat(actual.getTaskName(), Is.is("test_job@-@0@-@READY@-@slave-S0"));
    }
    
    @Test
    public void assertGetExecutorId() {
        TaskContext actual = TaskContext.from(TaskNode.builder().build().getTaskNodeValue());
        Assert.assertThat(actual.getExecutorId("app"), Is.is("app@-@slave-S0"));
    }
    
    @Test
    public void assertSetSlaveId() {
        TaskContext actual = new TaskContext("test_job", Lists.newArrayList(0), ExecutionType.READY, "slave-S0");
        Assert.assertThat(actual.getSlaveId(), Is.is("slave-S0"));
        actual.setSlaveId("slave-S1");
        Assert.assertThat(actual.getSlaveId(), Is.is("slave-S1"));
    }
    
    @Test
    public void assertSetIdle() {
        TaskContext actual = new TaskContext("test_job", Lists.newArrayList(0), ExecutionType.READY, "slave-S0");
        Assert.assertFalse(actual.isIdle());
        actual.setIdle(true);
        Assert.assertTrue(actual.isIdle());
    }
}
