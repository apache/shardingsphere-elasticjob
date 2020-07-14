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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.restful;

import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudAppJsonConstants;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJsonConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CloudAppRestfulApiTest extends AbstractCloudRestfulApiTest {
    
    private static final String YAML = "appCacheEnable: true\n"
            + "appName: test_app\n"
            + "appURL: http://localhost/app.jar\n"
            + "bootstrapScript: bin/start.sh\n"
            + "cpuCount: 1.0\n"
            + "eventTraceSamplingCount: 0\n"
            + "memoryMB: 128.0\n";
    
    @Test
    public void assertRegister() throws Exception {
        when(getRegCenter().isExisted("/config/app/test_app")).thenReturn(false);
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/app", "POST", CloudAppJsonConstants.getAppJson("test_app")), is(204));
        verify(getRegCenter()).persist("/config/app/test_app", YAML);
    }
    
    @Test
    public void assertRegisterWithExistedName() throws Exception {
        when(getRegCenter().isExisted("/config/app/test_app")).thenReturn(false);
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/app", "POST", CloudAppJsonConstants.getAppJson("test_app")), is(204));
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/app", "POST", CloudAppJsonConstants.getAppJson("test_app")), is(500));
    }
    
    @Test
    public void assertRegisterWithBadRequest() throws Exception {
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/app", "POST", "\"{\"appName\":\"wrong_job\"}"), is(500));
    }
    
    @Test
    public void assertUpdate() throws Exception {
        when(getRegCenter().isExisted("/config/app/test_app")).thenReturn(true);
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/app", "PUT", CloudAppJsonConstants.getAppJson("test_app")), is(204));
        verify(getRegCenter()).update("/config/app/test_app", YAML);
    }
    
    @Test
    public void assertDetail() throws Exception {
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/app/test_app"), is(CloudAppJsonConstants.getAppJson("test_app")));
        verify(getRegCenter()).get("/config/app/test_app");
    }
    
    @Test
    public void assertDetailWithNotExistedJob() throws Exception {
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/app/notExistedJobName", "GET", ""), is(404));
    }
    
    @Test
    public void assertFindAllJobs() throws Exception {
        when(getRegCenter().isExisted("/config/app")).thenReturn(true);
        when(getRegCenter().getChildrenKeys("/config/app")).thenReturn(Collections.singletonList("test_app"));
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/app/list"), is("[" + CloudAppJsonConstants.getAppJson("test_app") + "]"));
        verify(getRegCenter()).isExisted("/config/app");
        verify(getRegCenter()).getChildrenKeys("/config/app");
        verify(getRegCenter()).get("/config/app/test_app");
    }
    
    @Test
    public void assertDeregister() throws Exception {
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/app/test_app", "DELETE", CloudAppJsonConstants.getAppJson("test_app")), is(204));
        verify(getRegCenter()).get("/config/app/test_app");
    }
    
    @Test
    public void assertIsDisabled() throws Exception {
        when(getRegCenter().isExisted("/state/disable/app/test_app")).thenReturn(true);
        assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/app/test_app/disable"), is("true"));
    }
    
    @Test
    public void assertDisable() throws Exception {
        when(getRegCenter().isExisted("/config/job")).thenReturn(true);
        when(getRegCenter().getChildrenKeys("/config/job")).thenReturn(Collections.singletonList("test_job"));
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/app/test_app/disable", "POST"), is(204));
        verify(getRegCenter()).get("/config/app/test_app");
        verify(getRegCenter()).persist("/state/disable/app/test_app", "test_app");
    }
    
    @Test
    public void assertEnable() throws Exception {
        when(getRegCenter().isExisted("/config/job")).thenReturn(true);
        when(getRegCenter().getChildrenKeys("/config/job")).thenReturn(Collections.singletonList("test_job"));
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/app/test_app/enable", "POST"), is(204));
        verify(getRegCenter()).get("/config/app/test_app");
        verify(getRegCenter()).remove("/state/disable/app/test_app");
    }
}
