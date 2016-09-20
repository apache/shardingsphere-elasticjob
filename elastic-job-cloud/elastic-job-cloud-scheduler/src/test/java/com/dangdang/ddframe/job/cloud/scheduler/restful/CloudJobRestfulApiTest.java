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

import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudJsonConstants;
import com.dangdang.ddframe.job.cloud.scheduler.lifecycle.LifecycleService;
import com.dangdang.ddframe.job.cloud.scheduler.producer.TaskProducerSchedulerRegistry;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.restful.RestfulServer;
import org.apache.mesos.SchedulerDriver;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import javax.ws.rs.core.MediaType;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CloudJobRestfulApiTest {
    
    private static RestfulServer server;
    
    private static SchedulerDriver schedulerDriver;
    
    private static CoordinatorRegistryCenter regCenter;
    
    @Mock
    private LifecycleService lifecycleService;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        ReflectionUtils.setFieldValue(TaskProducerSchedulerRegistry.getInstance(regCenter), "instance", null);
        schedulerDriver = mock(SchedulerDriver.class);
        regCenter = mock(CoordinatorRegistryCenter.class);
        server = new RestfulServer(19000);
        CloudJobRestfulApi.init(schedulerDriver, regCenter);
        server.start(CloudJobRestfulApi.class.getPackage().getName());
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
        sentRequest("http://127.0.0.1:19000/job/deregister", "DELETE", "test_job");
        server.stop();
    }
    
    @Test
    public void assertRegister() throws Exception {
        when(regCenter.isExisted("/config/test_job")).thenReturn(false);
        assertThat(sentRequest("http://127.0.0.1:19000/job/register", "POST", CloudJsonConstants.getJobJson()), is(204));
        verify(regCenter).persist("/config/test_job", CloudJsonConstants.getJobJson());
        sentRequest("http://127.0.0.1:19000/job/deregister", "DELETE", "test_job");
    }
    
    @Test
    public void assertRegisterWithBadRequest() throws Exception {
        assertThat(sentRequest("http://127.0.0.1:19000/job/register", "POST", "\"{\"jobName\":\"wrong_job\"}"), is(500));
    }
    
    @Test
    public void assertDeregister() throws Exception {
        when(regCenter.isExisted("/config/test_job")).thenReturn(false);
        assertThat(sentRequest("http://127.0.0.1:19000/job/deregister", "DELETE", "test_job"), is(204));
        verify(regCenter).isExisted("/config/test_job");
    }
    
    private static int sentRequest(final String url, final String method, final String content) throws Exception {
        HttpClient httpClient = new HttpClient();
        try {
            httpClient.start();
            ContentExchange contentExchange = new ContentExchange();
            contentExchange.setMethod(method);
            contentExchange.setRequestContentType(MediaType.APPLICATION_JSON);
            contentExchange.setRequestContent(new ByteArrayBuffer(content.getBytes("UTF-8")));
            httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
            contentExchange.setURL(url);
            httpClient.send(contentExchange);
            contentExchange.waitForDone();
            return contentExchange.getResponseStatus();
        } finally {
            httpClient.stop();
        }
    }
}
