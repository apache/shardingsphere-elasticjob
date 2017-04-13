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

import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudAppJsonConstants;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudJsonConstants;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static com.dangdang.ddframe.job.cloud.scheduler.restful.RestfulTestsUtil.sentGetRequest;
import static com.dangdang.ddframe.job.cloud.scheduler.restful.RestfulTestsUtil.sentRequest;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CloudAppRestfulApiTest extends AbstractCloudRestfulApiTest {
    
    @Test
    public void assertRegister() throws Exception {
        when(getRegCenter().isExisted("/config/app/test_app")).thenReturn(false);
        assertThat(sentRequest("http://127.0.0.1:19000/api/app", "POST", CloudAppJsonConstants.getAppJson("test_app")), is(204));
        verify(getRegCenter()).persist("/config/app/test_app", CloudAppJsonConstants.getAppJson("test_app"));
    }
    
    @Test
    public void assertRegisterWithExistedName() throws Exception {
        when(getRegCenter().isExisted("/config/app/test_app")).thenReturn(false);
        assertThat(sentRequest("http://127.0.0.1:19000/api/app", "POST", CloudAppJsonConstants.getAppJson("test_app")), is(204));
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(sentRequest("http://127.0.0.1:19000/api/app", "POST", CloudAppJsonConstants.getAppJson("test_app")), is(500));
    }
    
    @Test
    public void assertRegisterWithBadRequest() throws Exception {
        assertThat(sentRequest("http://127.0.0.1:19000/api/app", "POST", "\"{\"appName\":\"wrong_job\"}"), is(500));
    }
    
    @Test
    public void assertUpdate() throws Exception {
        when(getRegCenter().isExisted("/config/app/test_app")).thenReturn(true);
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(sentRequest("http://127.0.0.1:19000/api/app", "PUT", CloudAppJsonConstants.getAppJson("test_app")), is(204));
        verify(getRegCenter()).update("/config/app/test_app", CloudAppJsonConstants.getAppJson("test_app"));
    }
    
    @Test
    public void assertDetail() throws Exception {
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(sentGetRequest("http://127.0.0.1:19000/api/app/test_app"), is(CloudAppJsonConstants.getAppJson("test_app")));
        verify(getRegCenter()).get("/config/app/test_app");
    }
    
    @Test
    public void assertDetailWithNotExistedJob() throws Exception {
        assertThat(sentRequest("http://127.0.0.1:19000/api/app/notExistedJobName", "GET", ""), is(404));
    }
    
    @Test
    public void assertFindAllJobs() throws Exception {
        when(getRegCenter().isExisted("/config/app")).thenReturn(true);
        when(getRegCenter().getChildrenKeys("/config/app")).thenReturn(Lists.newArrayList("test_app"));
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(sentGetRequest("http://127.0.0.1:19000/api/app/list"), is("[" + CloudAppJsonConstants.getAppJson("test_app") + "]"));
        verify(getRegCenter()).isExisted("/config/app");
        verify(getRegCenter()).getChildrenKeys("/config/app");
        verify(getRegCenter()).get("/config/app/test_app");
    }
    
    @Test
    public void assertDeregister() throws Exception {
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(sentRequest("http://127.0.0.1:19000/api/app/test_app", "DELETE", CloudAppJsonConstants.getAppJson("test_app")), is(204));
        verify(getRegCenter()).get("/config/app/test_app");
    }
    
    @Test
    public void assertIsDisabled() throws Exception {
        when(getRegCenter().isExisted("/state/disable/app/test_app")).thenReturn(true);
        assertThat(sentGetRequest("http://127.0.0.1:19000/api/app/test_app/disable"), is("true"));
    }
    
    @Test
    public void assertDisable() throws Exception {
        when(getRegCenter().isExisted("/config/job")).thenReturn(true);
        when(getRegCenter().getChildrenKeys("/config/job")).thenReturn(Lists.newArrayList("test_job"));
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(sentRequest("http://127.0.0.1:19000/api/app/test_app/disable", "POST"), is(204));
        verify(getRegCenter()).get("/config/app/test_app");
        verify(getRegCenter()).persist("/state/disable/app/test_app", "test_app");
    }
    
    @Test
    public void assertEnable() throws Exception {
        when(getRegCenter().isExisted("/config/job")).thenReturn(true);
        when(getRegCenter().getChildrenKeys("/config/job")).thenReturn(Lists.newArrayList("test_job"));
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(sentRequest("http://127.0.0.1:19000/api/app/test_app/disable", "DELETE"), is(204));
        verify(getRegCenter()).get("/config/app/test_app");
        verify(getRegCenter()).remove("/state/disable/app/test_app");
    }
}
