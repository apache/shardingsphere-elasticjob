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

package org.apache.shardingsphere.elasticjob.restful.pipeline;

import com.google.gson.Gson;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.restful.NettyRestfulService;
import org.apache.shardingsphere.elasticjob.restful.NettyRestfulServiceConfiguration;
import org.apache.shardingsphere.elasticjob.restful.RestfulService;
import org.apache.shardingsphere.elasticjob.restful.controller.IndexController;
import org.apache.shardingsphere.elasticjob.restful.controller.JobController;
import org.apache.shardingsphere.elasticjob.restful.handler.CustomIllegalStateExceptionHandler;
import org.apache.shardingsphere.elasticjob.restful.pojo.JobPojo;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public final class NettyRestfulServiceTest {
    
    private static final long TESTCASE_TIMEOUT = 10000L;
    
    private static final String HOST = "localhost";
    
    private static final int PORT = 18080;
    
    private static RestfulService restfulService;
    
    @BeforeClass
    public static void init() {
        NettyRestfulServiceConfiguration configuration = new NettyRestfulServiceConfiguration(PORT);
        configuration.setHost(HOST);
        configuration.addControllerInstances(new JobController(), new IndexController());
        configuration.addExceptionHandler(IllegalStateException.class, new CustomIllegalStateExceptionHandler());
        restfulService = new NettyRestfulService(configuration);
        restfulService.startup();
    }
    
    @SneakyThrows
    @Test(timeout = TESTCASE_TIMEOUT)
    public void assertRequestWithParameters() {
        String cron = "0 * * * * ?";
        String uri = String.format("/job/myGroup/myJob?cron=%s", URLEncoder.encode(cron, "UTF-8"));
        String description = "Descriptions about this job.";
        byte[] body = description.getBytes(StandardCharsets.UTF_8);
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri, Unpooled.wrappedBuffer(body));
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
        HttpUtil.setContentLength(request, body.length);
        HttpClient.request(HOST, PORT, request, httpResponse -> {
            assertEquals(200, httpResponse.status().code());
            byte[] bytes = ByteBufUtil.getBytes(httpResponse.content());
            String json = new String(bytes, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            JobPojo jobPojo = gson.fromJson(json, JobPojo.class);
            assertThat(jobPojo.getCron(), is(cron));
            assertThat(jobPojo.getGroup(), is("myGroup"));
            assertThat(jobPojo.getName(), is("myJob"));
            assertThat(jobPojo.getDescription(), is(description));
        }, TESTCASE_TIMEOUT);
    }
    
    @Test(timeout = TESTCASE_TIMEOUT)
    public void assertCustomExceptionHandler() {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/job/throw/IllegalState");
        request.headers().set("Exception-Message", "An illegal state exception message.");
        HttpClient.request(HOST, PORT, request, httpResponse -> {
            // Handle by CustomExceptionHandler
            assertThat(httpResponse.status().code(), is(403));
        }, TESTCASE_TIMEOUT);
    }
    
    @Test(timeout = TESTCASE_TIMEOUT)
    public void assertUsingDefaultExceptionHandler() {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/job/throw/IllegalArgument");
        request.headers().set("Exception-Message", "An illegal argument exception message.");
        HttpClient.request(HOST, PORT, request, httpResponse -> {
            // Handle by DefaultExceptionHandler
            assertThat(httpResponse.status().code(), is(500));
        }, TESTCASE_TIMEOUT);
    }
    
    @Test(timeout = TESTCASE_TIMEOUT)
    public void assertReturnStatusCode() {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/job/code/204");
        HttpClient.request(HOST, PORT, request, httpResponse -> {
            assertThat(httpResponse.status().code(), is(204));
        }, TESTCASE_TIMEOUT);
    }
    
    @Test(timeout = TESTCASE_TIMEOUT)
    public void assertHandlerNotFound() {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/not/found");
        HttpClient.request(HOST, PORT, request, httpResponse -> {
            assertThat(httpResponse.status().code(), is(404));
        }, TESTCASE_TIMEOUT);
    }
    
    @Test(timeout = TESTCASE_TIMEOUT)
    public void assertRequestIndexWithSlash() {
        DefaultFullHttpRequest requestWithSlash = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        HttpClient.request(HOST, PORT, requestWithSlash, httpResponse -> {
            assertThat(httpResponse.status().code(), is(200));
        }, TESTCASE_TIMEOUT);
    }
    
    @Test(timeout = TESTCASE_TIMEOUT)
    public void assertRequestIndexWithoutSlash() {
        DefaultFullHttpRequest requestWithoutSlash = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "");
        HttpClient.request(HOST, PORT, requestWithoutSlash, httpResponse -> {
            assertThat(httpResponse.status().code(), is(200));
        }, TESTCASE_TIMEOUT);
    }
    
    @AfterClass
    public static void tearDown() {
        if (null != restfulService) {
            restfulService.shutdown();
        }
    }
}
