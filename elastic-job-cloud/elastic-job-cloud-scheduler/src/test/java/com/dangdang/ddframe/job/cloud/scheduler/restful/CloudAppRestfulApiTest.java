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
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.restful.RestfulServer;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.dangdang.ddframe.job.cloud.scheduler.restful.RestfulTestsUtil.sentGetRequest;
import static com.dangdang.ddframe.job.cloud.scheduler.restful.RestfulTestsUtil.sentRequest;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CloudAppRestfulApiTest {
    
    private static RestfulServer server;
    
    private static CoordinatorRegistryCenter regCenter;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        regCenter = mock(CoordinatorRegistryCenter.class);
        CloudAppRestfulApi.init(regCenter);
        server = new RestfulServer(19000);
        server.start(CloudAppRestfulApi.class.getPackage().getName(), Optional.of("console"));
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
    }
    
    @Before
    public void setUp() {
        reset(regCenter);
    }
    
    @Test
    public void assertRegister() throws Exception {
        when(regCenter.isExisted("/config/app/test_app")).thenReturn(false);
        assertThat(sentRequest("http://127.0.0.1:19000/app", "POST", CloudAppJsonConstants.getAppJson("test_app")), is(204));
        verify(regCenter).persist("/config/app/test_app", CloudAppJsonConstants.getAppJson("test_app"));
        sentRequest("http://127.0.0.1:19000/app", "DELETE", "test_app");
    }
    
    @Test
    public void assertRegisterWithExistedName() throws Exception {
        when(regCenter.isExisted("/config/app/test_app")).thenReturn(false);
        assertThat(sentRequest("http://127.0.0.1:19000/app", "POST", CloudAppJsonConstants.getAppJson("test_app")), is(204));
        when(regCenter.get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(sentRequest("http://127.0.0.1:19000/app", "POST", CloudAppJsonConstants.getAppJson("test_app")), is(500));
        sentRequest("http://127.0.0.1:19000/app", "DELETE", "test_app");
    }
    
    @Test
    public void assertRegisterWithBadRequest() throws Exception {
        assertThat(sentRequest("http://127.0.0.1:19000/app", "POST", "\"{\"appName\":\"wrong_job\"}"), is(500));
    }
    
    @Test
    public void assertUpdate() throws Exception {
        when(regCenter.isExisted("/config/app/test_app")).thenReturn(true);
        when(regCenter.get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(sentRequest("http://127.0.0.1:19000/app", "PUT", CloudAppJsonConstants.getAppJson("test_app")), is(204));
        verify(regCenter).update("/config/app/test_app", CloudAppJsonConstants.getAppJson("test_app"));
        sentRequest("http://127.0.0.1:19000/app", "DELETE", "test_app");
    }
    
    @Test
    public void assertDetail() throws Exception {
        when(regCenter.get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(sentGetRequest("http://127.0.0.1:19000/app/test_app"), is(CloudAppJsonConstants.getAppJson("test_app")));
        verify(regCenter).get("/config/app/test_app");
    }
    
    @Test
    public void assertDetailWithNotExistedJob() throws Exception {
        assertThat(sentRequest("http://127.0.0.1:19000/app/notExistedJobName", "GET", ""), is(500));
    }
    
    @Test
    public void assertFindAllJobs() throws Exception {
        when(regCenter.isExisted("/config/app")).thenReturn(true);
        when(regCenter.getChildrenKeys("/config/app")).thenReturn(Lists.newArrayList("test_app"));
        when(regCenter.get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(sentGetRequest("http://127.0.0.1:19000/app/list"), is("[" + CloudAppJsonConstants.getAppJson("test_app") + "]"));
        verify(regCenter).isExisted("/config/app");
        verify(regCenter).getChildrenKeys("/config/app");
        verify(regCenter).get("/config/app/test_app");
    }
}
