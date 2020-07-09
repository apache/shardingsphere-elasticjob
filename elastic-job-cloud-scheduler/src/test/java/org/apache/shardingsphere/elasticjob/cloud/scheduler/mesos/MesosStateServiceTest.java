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

import org.apache.shardingsphere.elasticjob.cloud.scheduler.ha.HANode;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.restful.AbstractCloudRestfulApiTest;
import org.apache.shardingsphere.elasticjob.cloud.reg.base.CoordinatorRegistryCenter;
import com.google.gson.JsonArray;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;

@RunWith(MockitoJUnitRunner.class)
public class MesosStateServiceTest extends AbstractCloudRestfulApiTest {
    
    @Mock
    private CoordinatorRegistryCenter registryCenter;
    
    @Test
    public void assertSandbox() throws Exception {
        Mockito.when(registryCenter.getDirectly(HANode.FRAMEWORK_ID_NODE)).thenReturn("d8701508-41b7-471e-9b32-61cf824a660d-0000");
        MesosStateService service = new MesosStateService(registryCenter);
        JsonArray sandbox = service.sandbox("foo_app");
        Assert.assertThat(sandbox.size(), Is.is(1));
        Assert.assertThat(sandbox.get(0).getAsJsonObject().get("hostname").getAsString(), Is.is("127.0.0.1"));
        Assert.assertThat(sandbox.get(0).getAsJsonObject().get("path").getAsString(), Is.is("/slaves/d8701508-41b7-471e-9b32-61cf824a660d-S0/"
                + "frameworks/d8701508-41b7-471e-9b32-61cf824a660d-0000/executors/foo_app@-@d8701508-41b7-471e-9b32-61cf824a660d-S0/runs/53fb4af7-aee2-44f6-9e47-6f418d9f27e1"));
    }
    
    @Test
    public void assertExecutors() throws Exception {
        Mockito.when(registryCenter.getDirectly(HANode.FRAMEWORK_ID_NODE)).thenReturn("d8701508-41b7-471e-9b32-61cf824a660d-0000");
        MesosStateService service = new MesosStateService(registryCenter);
        Collection<MesosStateService.ExecutorStateInfo> executorStateInfo = service.executors("foo_app");
        Assert.assertThat(executorStateInfo.size(), Is.is(1));
        MesosStateService.ExecutorStateInfo executor = executorStateInfo.iterator().next();
        Assert.assertThat(executor.getId(), Is.is("foo_app@-@d8701508-41b7-471e-9b32-61cf824a660d-S0"));
        Assert.assertThat(executor.getSlaveId(), Is.is("d8701508-41b7-471e-9b32-61cf824a660d-S0"));
    }
    
    @Test
    public void assertAllExecutors() throws Exception {
        Mockito.when(registryCenter.getDirectly(HANode.FRAMEWORK_ID_NODE)).thenReturn("d8701508-41b7-471e-9b32-61cf824a660d-0000");
        MesosStateService service = new MesosStateService(registryCenter);
        Collection<MesosStateService.ExecutorStateInfo> executorStateInfo = service.executors();
        Assert.assertThat(executorStateInfo.size(), Is.is(1));
        MesosStateService.ExecutorStateInfo executor = executorStateInfo.iterator().next();
        Assert.assertThat(executor.getId(), Is.is("foo_app@-@d8701508-41b7-471e-9b32-61cf824a660d-S0"));
        Assert.assertThat(executor.getSlaveId(), Is.is("d8701508-41b7-471e-9b32-61cf824a660d-S0"));
    }
}
