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

package org.apache.shardingsphere.elasticjob.cloud.console.controller;

import org.apache.shardingsphere.elasticjob.cloud.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.cloud.console.AbstractCloudControllerTest;
import org.apache.shardingsphere.elasticjob.cloud.console.HttpTestUtil;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.ha.HANode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudOperationControllerTest extends AbstractCloudControllerTest {
    
    @Test
    void assertExplicitReconcile() {
        ReflectionUtils.setFieldValue(new CloudOperationController(), "lastReconcileTime", 0);
        assertThat(HttpTestUtil.post("http://127.0.0.1:19000/api/operate/reconcile/explicit", ""), is(200));
        assertThat(HttpTestUtil.post("http://127.0.0.1:19000/api/operate/reconcile/explicit", ""), is(500));
    }
    
    @Test
    void assertImplicitReconcile() {
        ReflectionUtils.setFieldValue(new CloudOperationController(), "lastReconcileTime", 0);
        assertThat(HttpTestUtil.post("http://127.0.0.1:19000/api/operate/reconcile/implicit", ""), is(200));
        assertThat(HttpTestUtil.post("http://127.0.0.1:19000/api/operate/reconcile/implicit", ""), is(500));
    }
    
    @Test
    void assertSandbox() {
        when(getRegCenter().getDirectly(HANode.FRAMEWORK_ID_NODE)).thenReturn("d8701508-41b7-471e-9b32-61cf824a660d-0000");
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/operate/sandbox?appName=foo_app"), is("[{\"hostname\":\"127.0.0.1\","
                + "\"path\":\"/slaves/d8701508-41b7-471e-9b32-61cf824a660d-S0/frameworks/d8701508-41b7-471e-9b32-61cf824a660d-0000/executors/foo_app@-@"
                + "d8701508-41b7-471e-9b32-61cf824a660d-S0/runs/53fb4af7-aee2-44f6-9e47-6f418d9f27e1\"}]"));
    }
    
    @Test
    void assertNoFrameworkSandbox() {
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/operate/sandbox?appName=foo_app"), is("[]"));
        when(getRegCenter().getDirectly(HANode.FRAMEWORK_ID_NODE)).thenReturn("not-exists");
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/operate/sandbox?appName=foo_app"), is("[]"));
    }
    
}
