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

package io.elasticjob.lite.context;

import com.google.common.collect.Lists;
import io.elasticjob.lite.fixture.context.TaskNode;
import org.hamcrest.core.Is;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class TaskContextTest {
    
    @Test
    public void assertNew() {
        TaskContext actual = new TaskContext("test_job", Lists.newArrayList(0), ExecutionType.READY, "slave-S0");
        assertThat(actual.getMetaInfo().getJobName(), is("test_job"));
        assertThat(actual.getMetaInfo().getShardingItems().get(0), is(0));
        assertThat(actual.getType(), is(ExecutionType.READY));
        assertThat(actual.getSlaveId(), is("slave-S0"));
        assertThat(actual.getId(), startsWith(TaskNode.builder().build().getTaskNodeValue().substring(0, TaskNode.builder().build().getTaskNodeValue().length() - 1)));
    }
    
    @Test
    public void assertNewWithoutSlaveId() {
        TaskContext actual = new TaskContext("test_job", Lists.newArrayList(0), ExecutionType.READY);
        assertThat(actual.getSlaveId(), is("unassigned-slave"));
    }
    
    @Test
    public void assertGetMetaInfo() {
        TaskContext actual = new TaskContext("test_job", Lists.newArrayList(0), ExecutionType.READY, "slave-S0");
        assertThat(actual.getMetaInfo().toString(), is("test_job@-@0"));
    }
    
    @Test
    public void assertTaskContextFrom() {
        TaskContext actual = TaskContext.from(TaskNode.builder().build().getTaskNodeValue());
        assertThat(actual.getId(), Is.is(TaskNode.builder().build().getTaskNodeValue()));
        assertThat(actual.getMetaInfo().getJobName(), is("test_job"));
        assertThat(actual.getMetaInfo().getShardingItems().get(0), is(0));
        assertThat(actual.getType(), is(ExecutionType.READY));
        assertThat(actual.getSlaveId(), is("slave-S0"));
    }
    
    @Test
    public void assertMetaInfoFromWithMetaInfo() {
        TaskContext.MetaInfo actual = TaskContext.MetaInfo.from("test_job@-@1");
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getShardingItems().get(0), is(1));
    }
    
    @Test
    public void assertMetaInfoFromWithTaskId() {
        TaskContext.MetaInfo actual = TaskContext.MetaInfo.from("test_job@-@1@-@READY@-@unassigned-slave@-@0");
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getShardingItems().get(0), is(1));
    }
    
    @Test
    public void assertMetaInfoFromWithMetaInfoWithoutShardingItems() {
        TaskContext.MetaInfo actual = TaskContext.MetaInfo.from("test_job@-@");
        assertThat(actual.getJobName(), is("test_job"));
        assertTrue(actual.getShardingItems().isEmpty());
    }
    
    @Test
    public void assertMetaInfoFromWithTaskIdWithoutShardingItems() {
        TaskContext.MetaInfo actual = TaskContext.MetaInfo.from("test_job@-@@-@READY@-@unassigned-slave@-@0");
        assertThat(actual.getJobName(), is("test_job"));
        assertTrue(actual.getShardingItems().isEmpty());
    }
    
    @Test
    public void assertGetIdForUnassignedSlave() {
        assertThat(TaskContext.getIdForUnassignedSlave("test_job@-@0@-@READY@-@slave-S0@-@0"), is("test_job@-@0@-@READY@-@unassigned-slave@-@0"));
    }
    
    @Test
    public void assertGetTaskName() {
        TaskContext actual = TaskContext.from(TaskNode.builder().build().getTaskNodeValue());
        assertThat(actual.getTaskName(), is("test_job@-@0@-@READY@-@slave-S0"));
    }
    
    @Test
    public void assertGetExecutorId() {
        TaskContext actual = TaskContext.from(TaskNode.builder().build().getTaskNodeValue());
        assertThat(actual.getExecutorId("app"), is("app@-@slave-S0"));
    }
    
    @Test
    public void assertSetSlaveId() {
        TaskContext actual = new TaskContext("test_job", Lists.newArrayList(0), ExecutionType.READY, "slave-S0");
        assertThat(actual.getSlaveId(), is("slave-S0"));
        actual.setSlaveId("slave-S1");
        assertThat(actual.getSlaveId(), is("slave-S1"));
    }
    
    @Test
    public void assertSetIdle() {
        TaskContext actual = new TaskContext("test_job", Lists.newArrayList(0), ExecutionType.READY, "slave-S0");
        assertFalse(actual.isIdle());
        actual.setIdle(true);
        assertTrue(actual.isIdle());
    }
}
