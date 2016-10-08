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

package com.dangdang.ddframe.job.util.restful;

import com.dangdang.ddframe.job.util.restful.fixture.Caller;
import com.dangdang.ddframe.job.util.restful.fixture.TestRestfulApi;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.MediaType;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class RestfulServerTest {
    
    private static final String URL = "http://127.0.0.1:17000/test/call";
    
    private static RestfulServer server;
    
    private Caller caller;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        server = new RestfulServer(17000);
        server.start(TestRestfulApi.class.getPackage().getName());
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
    }
    
    @Before
    public void setUp() throws Exception {
        caller = mock(Caller.class);
        TestRestfulApi.setCaller(caller);
    }
    
    @Test
    public void assertCallSuccess() throws Exception {
        ContentExchange actual = sentRequest("{\"string\":\"test\",\"integer\":1}");
        assertThat(actual.getResponseStatus(), is(200));
        assertThat(actual.getResponseContent(), is("{\"string\":\"test_processed\",\"integer\":\"1_processed\"}"));
        verify(caller).call("test");
        verify(caller).call(1);
    }
    
    @Test
    public void assertCallFailure() throws Exception {
        ContentExchange actual = sentRequest("{\"string\":\"test\",\"integer\":\"invalid_number\"}");
        assertThat(actual.getResponseStatus(), is(500));
        assertThat(actual.getResponseContent(), startsWith("java.lang.NumberFormatException"));
        verify(caller).call("test");
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
