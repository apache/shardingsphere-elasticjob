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

import com.google.common.collect.Lists;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudAppJsonConstants;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJsonConstants;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class CloudAppRestfulApiTest extends AbstractCloudRestfulApiTest {
    
    @Test
    public void assertRegister() throws Exception {
        Mockito.when(getRegCenter().isExisted("/config/app/test_app")).thenReturn(false);
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/app", "POST", CloudAppJsonConstants.getAppJson("test_app")), Is.is(204));
        Mockito.verify(getRegCenter()).persist("/config/app/test_app", CloudAppJsonConstants.getAppJson("test_app"));
    }
    
    @Test
    public void assertRegisterWithExistedName() throws Exception {
        Mockito.when(getRegCenter().isExisted("/config/app/test_app")).thenReturn(false);
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/app", "POST", CloudAppJsonConstants.getAppJson("test_app")), Is.is(204));
        Mockito.when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/app", "POST", CloudAppJsonConstants.getAppJson("test_app")), Is.is(500));
    }
    
    @Test
    public void assertRegisterWithBadRequest() throws Exception {
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/app", "POST", "\"{\"appName\":\"wrong_job\"}"), Is.is(500));
    }
    
    @Test
    public void assertUpdate() throws Exception {
        Mockito.when(getRegCenter().isExisted("/config/app/test_app")).thenReturn(true);
        Mockito.when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/app", "PUT", CloudAppJsonConstants.getAppJson("test_app")), Is.is(204));
        Mockito.verify(getRegCenter()).update("/config/app/test_app", CloudAppJsonConstants.getAppJson("test_app"));
    }
    
    @Test
    public void assertDetail() throws Exception {
        Mockito.when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        Assert.assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/app/test_app"), Is.is(CloudAppJsonConstants.getAppJson("test_app")));
        Mockito.verify(getRegCenter()).get("/config/app/test_app");
    }
    
    @Test
    public void assertDetailWithNotExistedJob() throws Exception {
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/app/notExistedJobName", "GET", ""), Is.is(404));
    }
    
    @Test
    public void assertFindAllJobs() throws Exception {
        Mockito.when(getRegCenter().isExisted("/config/app")).thenReturn(true);
        Mockito.when(getRegCenter().getChildrenKeys("/config/app")).thenReturn(Lists.newArrayList("test_app"));
        Mockito.when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        Assert.assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/app/list"), Is.is("[" + CloudAppJsonConstants.getAppJson("test_app") + "]"));
        Mockito.verify(getRegCenter()).isExisted("/config/app");
        Mockito.verify(getRegCenter()).getChildrenKeys("/config/app");
        Mockito.verify(getRegCenter()).get("/config/app/test_app");
    }
    
    @Test
    public void assertDeregister() throws Exception {
        Mockito.when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/app/test_app", "DELETE", CloudAppJsonConstants.getAppJson("test_app")), Is.is(204));
        Mockito.verify(getRegCenter()).get("/config/app/test_app");
    }
    
    @Test
    public void assertIsDisabled() throws Exception {
        Mockito.when(getRegCenter().isExisted("/state/disable/app/test_app")).thenReturn(true);
        Assert.assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/app/test_app/disable"), Is.is("true"));
    }
    
    @Test
    public void assertDisable() throws Exception {
        Mockito.when(getRegCenter().isExisted("/config/job")).thenReturn(true);
        Mockito.when(getRegCenter().getChildrenKeys("/config/job")).thenReturn(Lists.newArrayList("test_job"));
        Mockito.when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        Mockito.when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/app/test_app/disable", "POST"), Is.is(204));
        Mockito.verify(getRegCenter()).get("/config/app/test_app");
        Mockito.verify(getRegCenter()).persist("/state/disable/app/test_app", "test_app");
    }
    
    @Test
    public void assertEnable() throws Exception {
        Mockito.when(getRegCenter().isExisted("/config/job")).thenReturn(true);
        Mockito.when(getRegCenter().getChildrenKeys("/config/job")).thenReturn(Lists.newArrayList("test_job"));
        Mockito.when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        Mockito.when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        Assert.assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/app/test_app/enable", "POST"), Is.is(204));
        Mockito.verify(getRegCenter()).get("/config/app/test_app");
        Mockito.verify(getRegCenter()).remove("/state/disable/app/test_app");
    }
}
