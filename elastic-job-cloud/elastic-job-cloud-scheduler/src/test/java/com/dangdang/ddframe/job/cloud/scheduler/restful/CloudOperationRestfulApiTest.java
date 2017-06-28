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

import com.dangdang.ddframe.job.cloud.scheduler.ha.HANode;
import org.junit.Test;
import org.unitils.util.ReflectionUtils;

import static com.dangdang.ddframe.job.cloud.scheduler.restful.RestfulTestsUtil.sentGetRequest;
import static com.dangdang.ddframe.job.cloud.scheduler.restful.RestfulTestsUtil.sentRequest;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class CloudOperationRestfulApiTest extends AbstractCloudRestfulApiTest {
    
    @Test
    public void assertExplicitReconcile() throws Exception {
        ReflectionUtils.setFieldValue(new CloudOperationRestfulApi(), "lastReconcileTime", 0);
        assertThat(sentRequest("http://127.0.0.1:19000/api/operate/reconcile/explicit", "POST", ""), is(204));
        assertThat(sentRequest("http://127.0.0.1:19000/api/operate/reconcile/explicit", "POST", ""), is(500));
    }
    
    @Test
    public void assertImplicitReconcile() throws Exception {
        ReflectionUtils.setFieldValue(new CloudOperationRestfulApi(), "lastReconcileTime", 0);
        assertThat(sentRequest("http://127.0.0.1:19000/api/operate/reconcile/implicit", "POST", ""), is(204));
        assertThat(sentRequest("http://127.0.0.1:19000/api/operate/reconcile/implicit", "POST", ""), is(500));
    }
    
    @Test
    public void assertSandbox() throws Exception {
        when(getRegCenter().getDirectly(HANode.FRAMEWORK_ID_NODE)).thenReturn("d8701508-41b7-471e-9b32-61cf824a660d-0000");
        assertThat(sentGetRequest("http://127.0.0.1:19000/api/operate/sandbox?appName=foo_app"), is("[{\"hostname\":\"127.0.0.1\","
                + "\"path\":\"/slaves/d8701508-41b7-471e-9b32-61cf824a660d-S0/frameworks/d8701508-41b7-471e-9b32-61cf824a660d-0000/executors/foo_app@-@"
                + "d8701508-41b7-471e-9b32-61cf824a660d-S0/runs/53fb4af7-aee2-44f6-9e47-6f418d9f27e1\"}]"));
    }
    
    @Test
    public void assertNoFrameworkSandbox() throws Exception {
        assertThat(sentGetRequest("http://127.0.0.1:19000/api/operate/sandbox?appName=foo_app"), is("[]"));
        when(getRegCenter().getDirectly(HANode.FRAMEWORK_ID_NODE)).thenReturn("not-exists");
        assertThat(sentGetRequest("http://127.0.0.1:19000/api/operate/sandbox?appName=foo_app"), is("[]"));
    }
}
