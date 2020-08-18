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

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.shardingsphere.elasticjob.restful.HandleContext;
import org.apache.shardingsphere.elasticjob.restful.Handler;
import org.apache.shardingsphere.elasticjob.restful.serializer.ResponseBodySerializer;
import org.apache.shardingsphere.elasticjob.restful.serializer.ResponseBodySerializerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * The handler which actually executes handle method and creates HTTP response for responding.
 * If an exception occurred when executing handle method, this handler would pass it to Handler named {@link ExceptionHandling}.
 */
@Sharable
public final class HandleMethodExecutor extends ChannelInboundHandlerAdapter {
    
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        HandleContext<Handler> handleContext = (HandleContext<Handler>) msg;
        Handler handler = handleContext.getMappingContext().payload();
        Object[] args = handleContext.getArgs();
        
        Object handleResult = handler.execute(args);
        
        String mimeType = HttpUtil.getMimeType(handler.getProducing()).toString();
        ResponseBodySerializer serializer = ResponseBodySerializerFactory.getResponseBodySerializer(mimeType);
        byte[] bodyBytes = serializer.serialize(handleResult);
        FullHttpResponse response = createHttpResponse(handler.getProducing(), bodyBytes, handler.getHttpStatusCode());
        ctx.writeAndFlush(response);
    }
    
    private FullHttpResponse createHttpResponse(final String producingContentType, final byte[] bodyBytes, final int statusCode) {
        HttpResponseStatus httpResponseStatus = HttpResponseStatus.valueOf(statusCode);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus, Unpooled.wrappedBuffer(bodyBytes));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, producingContentType);
        HttpUtil.setContentLength(response, bodyBytes.length);
        HttpUtil.setKeepAlive(response, true);
        return response;
    }
    
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        if (cause instanceof InvocationTargetException) {
            ctx.fireExceptionCaught(cause.getCause());
        } else {
            ctx.fireExceptionCaught(cause);
        }
    }
}
