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

import org.apache.shardingsphere.elasticjob.cloud.console.AbstractCloudControllerTest;
import org.apache.shardingsphere.elasticjob.cloud.console.HttpTestUtil;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudAppJsonConstants;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJsonConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudAppControllerTest extends AbstractCloudControllerTest {
    
    private static final String YAML = "appCacheEnable: true\n"
            + "appName: test_app\n"
            + "appURL: http://localhost/app.jar\n"
            + "bootstrapScript: bin/start.sh\n"
            + "cpuCount: 1.0\n"
            + "eventTraceSamplingCount: 0\n"
            + "memoryMB: 128.0\n";
    
    @Test
    void assertRegister() {
        when(getRegCenter().isExisted("/config/app/test_app")).thenReturn(false);
        assertThat(HttpTestUtil.post("http://127.0.0.1:19000/api/app", CloudAppJsonConstants.getAppJson("test_app")), is(200));
        verify(getRegCenter()).persist("/config/app/test_app", YAML);
    }
    
    @Test
    void assertRegisterWithExistedName() {
        when(getRegCenter().isExisted("/config/app/test_app")).thenReturn(false);
        assertThat(HttpTestUtil.post("http://127.0.0.1:19000/api/app", CloudAppJsonConstants.getAppJson("test_app")), is(200));
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(HttpTestUtil.post("http://127.0.0.1:19000/api/app", CloudAppJsonConstants.getAppJson("test_app")), is(500));
    }
    
    @Test
    void assertRegisterWithBadRequest() {
        assertThat(HttpTestUtil.post("http://127.0.0.1:19000/api/app", "\"{\"appName\":\"wrong_job\"}"), is(500));
    }
    
    @Test
    void assertUpdate() {
        when(getRegCenter().isExisted("/config/app/test_app")).thenReturn(true);
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(HttpTestUtil.put("http://127.0.0.1:19000/api/app", CloudAppJsonConstants.getAppJson("test_app")), is(200));
        verify(getRegCenter()).update("/config/app/test_app", YAML);
    }
    
    @Test
    void assertDetail() {
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/app/test_app"), is(CloudAppJsonConstants.getAppJson("test_app")));
        verify(getRegCenter()).get("/config/app/test_app");
    }
    
    @Test
    void assertDetailWithNotExistedJob() {
        Map<String, String> content = new HashMap<>(1);
        content.put("appName", "");
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/app/notExistedJobName", content), is(""));
    }
    
    @Test
    void assertFindAllJobs() {
        when(getRegCenter().isExisted("/config/app")).thenReturn(true);
        when(getRegCenter().getChildrenKeys("/config/app")).thenReturn(Collections.singletonList("test_app"));
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/app/list"), is("[" + CloudAppJsonConstants.getAppJson("test_app") + "]"));
        verify(getRegCenter()).isExisted("/config/app");
        verify(getRegCenter()).getChildrenKeys("/config/app");
        verify(getRegCenter()).get("/config/app/test_app");
    }
    
    @Test
    void assertDeregister() {
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(HttpTestUtil.delete("http://127.0.0.1:19000/api/app/test_app"), is(200));
        verify(getRegCenter()).get("/config/app/test_app");
    }
    
    @Test
    void assertIsDisabled() {
        when(getRegCenter().isExisted("/state/disable/app/test_app")).thenReturn(true);
        assertThat(HttpTestUtil.get("http://127.0.0.1:19000/api/app/test_app/disable"), is("true"));
    }
    
    @Test
    void assertDisable() {
        when(getRegCenter().isExisted("/config/job")).thenReturn(true);
        when(getRegCenter().getChildrenKeys("/config/job")).thenReturn(Collections.singletonList("test_job"));
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(HttpTestUtil.post("http://127.0.0.1:19000/api/app/test_app/disable"), is(200));
        verify(getRegCenter()).get("/config/app/test_app");
        verify(getRegCenter()).persist("/state/disable/app/test_app", "test_app");
    }
    
    @Test
    void assertEnable() {
        when(getRegCenter().isExisted("/config/job")).thenReturn(true);
        when(getRegCenter().getChildrenKeys("/config/job")).thenReturn(Collections.singletonList("test_job"));
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(HttpTestUtil.post("http://127.0.0.1:19000/api/app/test_app/enable"), is(200));
        verify(getRegCenter()).get("/config/app/test_app");
        verify(getRegCenter()).remove("/state/disable/app/test_app");
    }
    
}
