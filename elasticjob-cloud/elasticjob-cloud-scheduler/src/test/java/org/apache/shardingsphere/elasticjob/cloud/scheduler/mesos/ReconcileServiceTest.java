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

import org.apache.shardingsphere.elasticjob.infra.context.TaskContext;
import com.google.common.collect.Sets;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ReconcileServiceTest {
    
    @Mock
    private SchedulerDriver schedulerDriver;
    
    @Mock
    private FacadeService facadeService;
    
    private ReconcileService reconcileService;
    
    @Captor
    private ArgumentCaptor<Collection<Protos.TaskStatus>> taskStatusCaptor;
    
    @Before
    public void setUp() {
        reconcileService = new ReconcileService(schedulerDriver, facadeService);
    }
    
    @Test
    public void assertRunOneIteration() throws Exception {
        reconcileService.runOneIteration();
        verify(schedulerDriver).reconcileTasks(Collections.<Protos.TaskStatus>emptyList());
    }
    
    @Test
    public void assertImplicitReconcile() {
        reconcileService.implicitReconcile();
        verify(schedulerDriver).reconcileTasks(Collections.<Protos.TaskStatus>emptyList());
    }
    
    @Test
    public void assertExplicitReconcile() {
        Map<String, Set<TaskContext>> runningTaskMap = new HashMap<>();
        runningTaskMap.put("transient_test_job", Sets.newHashSet(
                TaskContext.from("transient_test_job@-@0@-@READY@-@SLAVE-S0@-@UUID"), TaskContext.from("transient_test_job@-@1@-@READY@-@SLAVE-S0@-@UUID")));
        when(facadeService.getAllRunningTasks()).thenReturn(runningTaskMap);
        reconcileService.explicitReconcile();
        verify(schedulerDriver).reconcileTasks(taskStatusCaptor.capture());
        assertThat(taskStatusCaptor.getValue().size(), is(2));
        for (Protos.TaskStatus each : taskStatusCaptor.getValue()) {
            assertThat(each.getSlaveId().getValue(), is("SLAVE-S0"));
            assertThat(each.getState(), is(Protos.TaskState.TASK_RUNNING));
        }
    }
}
