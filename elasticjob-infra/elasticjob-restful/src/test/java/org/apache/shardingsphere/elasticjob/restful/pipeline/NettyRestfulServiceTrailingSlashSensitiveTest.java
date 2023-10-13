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

import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.shardingsphere.elasticjob.restful.NettyRestfulService;
import org.apache.shardingsphere.elasticjob.restful.NettyRestfulServiceConfiguration;
import org.apache.shardingsphere.elasticjob.restful.RestfulService;
import org.apache.shardingsphere.elasticjob.restful.controller.TrailingSlashTestController;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class NettyRestfulServiceTrailingSlashSensitiveTest {
    
    private static final long TESTCASE_TIMEOUT = 10000L;
    
    private static final String HOST = "localhost";
    
    private static final int PORT = 18081;
    
    private static RestfulService restfulService;
    
    @BeforeAll
    static void init() {
        NettyRestfulServiceConfiguration configuration = new NettyRestfulServiceConfiguration(PORT);
        configuration.setHost(HOST);
        configuration.setTrailingSlashSensitive(true);
        configuration.addControllerInstances(new TrailingSlashTestController());
        restfulService = new NettyRestfulService(configuration);
        restfulService.startup();
    }
    
    @Test
    @Timeout(value = TESTCASE_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    void assertWithoutTrailingSlash() {
        DefaultFullHttpRequest requestWithSlash = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/trailing/slash");
        HttpClient.request(HOST, PORT, requestWithSlash, httpResponse -> {
            assertThat(httpResponse.status().code(), is(200));
            byte[] bytes = ByteBufUtil.getBytes(httpResponse.content());
            String body = new String(bytes, StandardCharsets.UTF_8);
            assertThat(body, is("without trailing slash"));
        }, TESTCASE_TIMEOUT);
    }
    
    @Test
    @Timeout(value = TESTCASE_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    void assertWithTrailingSlash() {
        DefaultFullHttpRequest requestWithoutSlash = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/trailing/slash/");
        HttpClient.request(HOST, PORT, requestWithoutSlash, httpResponse -> {
            assertThat(httpResponse.status().code(), is(200));
            byte[] bytes = ByteBufUtil.getBytes(httpResponse.content());
            String body = new String(bytes, StandardCharsets.UTF_8);
            assertThat(body, is("with trailing slash"));
        }, TESTCASE_TIMEOUT);
    }
    
    @AfterAll
    static void tearDown() {
        if (null != restfulService) {
            restfulService.shutdown();
        }
    }
}
