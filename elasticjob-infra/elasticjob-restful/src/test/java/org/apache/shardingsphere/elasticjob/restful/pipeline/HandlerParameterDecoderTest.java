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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringEncoder;
import org.apache.shardingsphere.elasticjob.restful.Http;
import org.apache.shardingsphere.elasticjob.restful.RestfulController;
import org.apache.shardingsphere.elasticjob.restful.annotation.Mapping;
import org.apache.shardingsphere.elasticjob.restful.annotation.Param;
import org.apache.shardingsphere.elasticjob.restful.annotation.ParamSource;
import org.apache.shardingsphere.elasticjob.restful.annotation.RequestBody;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class HandlerParameterDecoderTest {
    
    private EmbeddedChannel channel;
    
    @Before
    public void setUp() {
        ContextInitializationInboundHandler contextInitializationInboundHandler = new ContextInitializationInboundHandler();
        HttpRequestDispatcher httpRequestDispatcher = new HttpRequestDispatcher(Collections.singletonList(new DecoderTestController()), false);
        HandlerParameterDecoder handlerParameterDecoder = new HandlerParameterDecoder();
        HandleMethodExecutor handleMethodExecutor = new HandleMethodExecutor();
        channel = new EmbeddedChannel(contextInitializationInboundHandler, httpRequestDispatcher, handlerParameterDecoder, handleMethodExecutor);
    }
    
    @Test
    public void assertDecodeParameters() {
        QueryStringEncoder queryStringEncoder = new QueryStringEncoder("/myApp/C");
        queryStringEncoder.addParam("cron", "0 * * * * ?");
        queryStringEncoder.addParam("integer", "30");
        queryStringEncoder.addParam("bool", "true");
        queryStringEncoder.addParam("long", "3000");
        queryStringEncoder.addParam("double", "23.33");
        String uri = queryStringEncoder.toString();
        ByteBuf body = Unpooled.wrappedBuffer("BODY".getBytes());
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("Message", "some_message");
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri, body, headers, headers);
        channel.writeInbound(httpRequest);
        FullHttpResponse httpResponse = channel.readOutbound();
        assertThat(httpResponse.status().code(), is(200));
        assertThat(new String(ByteBufUtil.getBytes(httpResponse.content())), is("ok"));
    }
    
    public static class DecoderTestController implements RestfulController {
        
        /**
         * A handle method for decode testing.
         *
         * @param appName     string from path
         * @param ch          character from path
         * @param cron        cron from query
         * @param message     message from header
         * @param body        from request body
         * @param integer     integer from query
         * @param bool        boolean from query
         * @param longValue   long from query
         * @param doubleValue double from query
         * @return OK
         */
        @Mapping(method = Http.GET, path = "/{appName}/{ch}")
        public String handle(
                final @Param(source = ParamSource.PATH, name = "appName") String appName,
                final @Param(source = ParamSource.PATH, name = "ch") char ch,
                final @Param(source = ParamSource.QUERY, name = "cron") String cron,
                final @Param(source = ParamSource.HEADER, name = "Message") String message,
                final @RequestBody String body,
                final @Param(source = ParamSource.QUERY, name = "integer") int integer,
                final @Param(source = ParamSource.QUERY, name = "bool") Boolean bool,
                final @Param(source = ParamSource.QUERY, name = "long") Long longValue,
                final @Param(source = ParamSource.QUERY, name = "double") double doubleValue
        ) {
            assertThat(appName, is("myApp"));
            assertThat(ch, is('C'));
            assertThat(cron, is("0 * * * * ?"));
            assertThat(message, is("some_message"));
            assertThat(body, is("BODY"));
            assertThat(integer, is(30));
            assertThat(bool, is(true));
            assertThat(longValue, is(3000L));
            assertThat(doubleValue, is(23.33));
            return "ok";
        }
    }
}
