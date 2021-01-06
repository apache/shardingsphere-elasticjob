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

import org.apache.shardingsphere.elasticjob.cloud.console.AbstractCloudControllerTest;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.ha.HANode;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.MesosStateService.ExecutorStateInfo;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MesosStateServiceTest extends AbstractCloudControllerTest {
    
    @Mock
    private CoordinatorRegistryCenter registryCenter;
    
    @Test
    public void assertSandbox() {
        when(registryCenter.getDirectly(HANode.FRAMEWORK_ID_NODE)).thenReturn("d8701508-41b7-471e-9b32-61cf824a660d-0000");
        MesosStateService service = new MesosStateService(registryCenter);
        Collection<Map<String, String>> sandbox = service.sandbox("foo_app");
        assertThat(sandbox.size(), is(1));
        assertThat(sandbox.iterator().next().get("hostname"), is("127.0.0.1"));
        assertThat(sandbox.iterator().next().get("path"), is("/slaves/d8701508-41b7-471e-9b32-61cf824a660d-S0/"
                + "frameworks/d8701508-41b7-471e-9b32-61cf824a660d-0000/executors/foo_app@-@d8701508-41b7-471e-9b32-61cf824a660d-S0/runs/53fb4af7-aee2-44f6-9e47-6f418d9f27e1"));
    }
    
    @Test
    public void assertExecutors() {
        when(registryCenter.getDirectly(HANode.FRAMEWORK_ID_NODE)).thenReturn("d8701508-41b7-471e-9b32-61cf824a660d-0000");
        MesosStateService service = new MesosStateService(registryCenter);
        Collection<ExecutorStateInfo> executorStateInfo = service.executors("foo_app");
        assertThat(executorStateInfo.size(), is(1));
        ExecutorStateInfo executor = executorStateInfo.iterator().next();
        assertThat(executor.getId(), is("foo_app@-@d8701508-41b7-471e-9b32-61cf824a660d-S0"));
        assertThat(executor.getSlaveId(), is("d8701508-41b7-471e-9b32-61cf824a660d-S0"));
    }
    
    @Test
    public void assertAllExecutors() {
        when(registryCenter.getDirectly(HANode.FRAMEWORK_ID_NODE)).thenReturn("d8701508-41b7-471e-9b32-61cf824a660d-0000");
        MesosStateService service = new MesosStateService(registryCenter);
        Collection<ExecutorStateInfo> executorStateInfo = service.executors();
        assertThat(executorStateInfo.size(), is(1));
        ExecutorStateInfo executor = executorStateInfo.iterator().next();
        assertThat(executor.getId(), is("foo_app@-@d8701508-41b7-471e-9b32-61cf824a660d-S0"));
        assertThat(executor.getSlaveId(), is("d8701508-41b7-471e-9b32-61cf824a660d-S0"));
    }
}
