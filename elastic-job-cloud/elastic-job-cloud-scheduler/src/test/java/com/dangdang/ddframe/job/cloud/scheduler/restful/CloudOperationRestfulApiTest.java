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

package com.dangdang.ddframe.job.cloud.scheduler.restful;

import com.dangdang.ddframe.job.cloud.scheduler.mesos.MesosStateService;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.fixture.master.MesosMasterServerMock;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.fixture.slave.MesosSlaveServerMock;
import com.dangdang.ddframe.job.restful.RestfulServer;
import com.google.common.base.Optional;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.util.ReflectionUtils;

import static com.dangdang.ddframe.job.cloud.scheduler.restful.RestfulTestsUtil.sentGetRequest;
import static com.dangdang.ddframe.job.cloud.scheduler.restful.RestfulTestsUtil.sentRequest;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CloudOperationRestfulApiTest extends AbstractCloudRestfulApiTest {
    
    private static RestfulServer masterServer;
    
    private static RestfulServer slaveServer;
    
    @BeforeClass
    public static void startServer() throws Exception {
        MesosStateService.register("127.0.0.1", 5050);
        masterServer = new RestfulServer(5050);
        masterServer.start(MesosMasterServerMock.class.getPackage().getName(), Optional.<String>absent());
        slaveServer = new RestfulServer(5051);
        slaveServer.start(MesosSlaveServerMock.class.getPackage().getName(), Optional.<String>absent());
    }
    
    @AfterClass
    public static void stopServer() {
        masterServer.stop();
        slaveServer.stop();
        MesosStateService.deregister();
    }
    
    @Test
    public void assertExplicitReconcile() throws Exception {
        ReflectionUtils.setFieldValue(new CloudOperationRestfulApi(), "lastReconcileTime", 0);
        assertThat(sentRequest("http://127.0.0.1:19000/operate/reconcile/explicit", "POST", ""), is(204));
        assertThat(sentRequest("http://127.0.0.1:19000/operate/reconcile/explicit", "POST", ""), is(500));
        ReflectionUtils.setFieldValue(new CloudOperationRestfulApi(), "lastReconcileTime", 0);
        assertThat(sentRequest("http://127.0.0.1:19000/operate/reconcile/explicit?taskId=unknown", "POST", ""), is(500));
    }
    
    @Test
    public void assertImplicitReconcile() throws Exception {
        ReflectionUtils.setFieldValue(new CloudOperationRestfulApi(), "lastReconcileTime", 0);
        assertThat(sentRequest("http://127.0.0.1:19000/operate/reconcile/implicit", "POST", ""), is(204));
        assertThat(sentRequest("http://127.0.0.1:19000/operate/reconcile/implicit", "POST", ""), is(500));
    }
    
    @Test
    public void assertSandbox() throws Exception {
        ReflectionUtils.setFieldValue(new CloudOperationRestfulApi(), "lastReconcileTime", 0);
        assertThat(sentGetRequest("http://127.0.0.1:19000/operate/sandbox?appName=foo_app"), is("[{\"hostname\":\"127.0.0.1\","
                + "\"path\":\"/slaves/d8701508-41b7-471e-9b32-61cf824a660d-S0/frameworks/d8701508-41b7-471e-9b32-61cf824a660d-0000/executors/foo_app@-@"
                + "d8701508-41b7-471e-9b32-61cf824a660d-S0/runs/53fb4af7-aee2-44f6-9e47-6f418d9f27e1\"}]"));
    }
}
