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

package com.dangdang.ddframe.job.restful;

import com.dangdang.ddframe.job.restful.fixture.Caller;
import com.dangdang.ddframe.job.restful.fixture.TestFilter;
import com.dangdang.ddframe.job.restful.fixture.TestRestfulApi;
import com.google.common.base.Optional;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.hamcrest.core.Is;
import org.hamcrest.core.StringStartsWith;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.core.MediaType;

@RunWith(MockitoJUnitRunner.class)
public final class RestfulServerTest {
    
    private static final String URL = "http://127.0.0.1:17000/api/test/call";
    
    private static RestfulServer server;
    
    private Caller caller;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        server = new RestfulServer(17000);
        server.addFilter(TestFilter.class, "/*");
        server.start(TestRestfulApi.class.getPackage().getName(), Optional.<String>absent());
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
    }
    
    @Before
    public void setUp() throws Exception {
        caller = Mockito.mock(Caller.class);
        TestRestfulApi.setCaller(caller);
    }
    
    @Test
    public void assertCallSuccess() throws Exception {
        ContentExchange actual = sentRequest("{\"string\":\"test\",\"integer\":1}");
        Assert.assertThat(actual.getResponseStatus(), Is.is(200));
        Assert.assertThat(actual.getResponseContent(), Is.is("{\"string\":\"test_processed\",\"integer\":\"1_processed\"}"));
        Mockito.verify(caller).call("test");
        Mockito.verify(caller).call(1);
    }
    
    @Test
    public void assertCallFailure() throws Exception {
        ContentExchange actual = sentRequest("{\"string\":\"test\",\"integer\":\"invalid_number\"}");
        Assert.assertThat(actual.getResponseStatus(), Is.is(500));
        Assert.assertThat(actual.getResponseContent(), StringStartsWith.startsWith("java.lang.NumberFormatException"));
        Mockito.verify(caller).call("test");
    }
    
    private static ContentExchange sentRequest(final String content) throws Exception {
        HttpClient httpClient = new HttpClient();
        try {
            httpClient.start();
            ContentExchange result = new ContentExchange();
            result.setMethod("POST");
            result.setRequestContentType(MediaType.APPLICATION_JSON);
            result.setRequestContent(new ByteArrayBuffer(content.getBytes("UTF-8")));
            httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
            result.setURL(URL);
            httpClient.send(result);
            result.waitForDone();
            return result;
        } finally {
            httpClient.stop();
        }
    }
}
