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

import com.dangdang.ddframe.job.cloud.scheduler.ha.HANode;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.MesosStateService.ExecutorStateInfo;
import com.dangdang.ddframe.job.cloud.scheduler.restful.AbstractCloudRestfulApiTest;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.gson.JsonArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MesosStateServiceTest extends AbstractCloudRestfulApiTest {
    
    @Mock
    private CoordinatorRegistryCenter registryCenter;
    
    @Test
    public void assertSandbox() throws Exception {
        when(registryCenter.getDirectly(HANode.FRAMEWORK_ID_NODE)).thenReturn("d8701508-41b7-471e-9b32-61cf824a660d-0000");
        MesosStateService service = new MesosStateService(registryCenter);
        JsonArray sandbox = service.sandbox("foo_app");
        assertThat(sandbox.size(), is(1));
        assertThat(sandbox.get(0).getAsJsonObject().get("hostname").getAsString(), is("127.0.0.1"));
        assertThat(sandbox.get(0).getAsJsonObject().get("path").getAsString(), is("/slaves/d8701508-41b7-471e-9b32-61cf824a660d-S0/"
                + "frameworks/d8701508-41b7-471e-9b32-61cf824a660d-0000/executors/foo_app@-@d8701508-41b7-471e-9b32-61cf824a660d-S0/runs/53fb4af7-aee2-44f6-9e47-6f418d9f27e1"));
    }
    
    @Test
    public void assertExecutors() throws Exception {
        when(registryCenter.getDirectly(HANode.FRAMEWORK_ID_NODE)).thenReturn("d8701508-41b7-471e-9b32-61cf824a660d-0000");
        MesosStateService service = new MesosStateService(registryCenter);
        Collection<ExecutorStateInfo> executorStateInfo = service.executors("foo_app");
        assertThat(executorStateInfo.size(), is(1));
        ExecutorStateInfo executor = executorStateInfo.iterator().next();
        assertThat(executor.getId(), is("foo_app@-@d8701508-41b7-471e-9b32-61cf824a660d-S0"));
        assertThat(executor.getSlaveId(), is("d8701508-41b7-471e-9b32-61cf824a660d-S0"));
    }
    
    @Test
    public void assertAllExecutors() throws Exception {
        when(registryCenter.getDirectly(HANode.FRAMEWORK_ID_NODE)).thenReturn("d8701508-41b7-471e-9b32-61cf824a660d-0000");
        MesosStateService service = new MesosStateService(registryCenter);
        Collection<ExecutorStateInfo> executorStateInfo = service.executors();
        assertThat(executorStateInfo.size(), is(1));
        ExecutorStateInfo executor = executorStateInfo.iterator().next();
        assertThat(executor.getId(), is("foo_app@-@d8701508-41b7-471e-9b32-61cf824a660d-S0"));
        assertThat(executor.getSlaveId(), is("d8701508-41b7-471e-9b32-61cf824a660d-S0"));
    }
}
