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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.restful.handler.ExceptionHandleResult;
import org.apache.shardingsphere.elasticjob.restful.handler.ExceptionHandler;
import org.apache.shardingsphere.elasticjob.restful.handler.HandlerNotFoundException;
import org.apache.shardingsphere.elasticjob.restful.handler.impl.DefaultExceptionHandler;
import org.apache.shardingsphere.elasticjob.restful.handler.impl.DefaultHandlerNotFoundExceptionHandler;
import org.apache.shardingsphere.elasticjob.restful.serializer.ResponseBodySerializer;
import org.apache.shardingsphere.elasticjob.restful.serializer.ResponseBodySerializerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Catch exceptions and look for exception handler.
 */
@Slf4j
@Sharable
public final class ExceptionHandling extends ChannelInboundHandlerAdapter {
    
    private static final DefaultExceptionHandler DEFAULT_EXCEPTION_HANDLER = new DefaultExceptionHandler();
    
    private final Map<Class<? extends Throwable>, ExceptionHandler<? extends Throwable>> exceptionHandlers = new ConcurrentHashMap<>();
    
    public ExceptionHandling(final Map<Class<? extends Throwable>, ExceptionHandler<? extends Throwable>> exceptionHandlers) {
        initDefaultExceptionHandlers();
        addCustomExceptionHandlers(exceptionHandlers);
    }
    
    private void initDefaultExceptionHandlers() {
        exceptionHandlers.put(HandlerNotFoundException.class, new DefaultHandlerNotFoundExceptionHandler());
    }
    
    private void addCustomExceptionHandlers(final Map<Class<? extends Throwable>, ExceptionHandler<? extends Throwable>> exceptionHandlers) {
        for (Map.Entry<Class<? extends Throwable>, ExceptionHandler<? extends Throwable>> entry : exceptionHandlers.entrySet()) {
            Class<? extends Throwable> exceptionType = entry.getKey();
            ExceptionHandler<? extends Throwable> oldHandler = this.exceptionHandlers.put(exceptionType, entry.getValue());
            if (null != oldHandler) {
                log.info("Overriding ExceptionHandler for [{}]", exceptionType.getName());
            }
        }
    }
    
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        ExceptionHandler<Throwable> exceptionHandler = searchExceptionHandler(cause);
        ExceptionHandleResult handleResult = exceptionHandler.handleException(cause);
        String mimeType = HttpUtil.getMimeType(handleResult.getContentType()).toString();
        ResponseBodySerializer serializer = ResponseBodySerializerFactory.getResponseBodySerializer(mimeType);
        byte[] body = serializer.serialize(handleResult.getResult());
        FullHttpResponse response = createHttpResponse(handleResult.getStatusCode(), handleResult.getContentType(), body);
        ctx.writeAndFlush(response);
    }
    
    private FullHttpResponse createHttpResponse(final int statusCode, final String contentType, final byte[] body) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(statusCode), Unpooled.copiedBuffer(body));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        HttpUtil.setContentLength(response, body.length);
        return response;
    }
    
    private <T extends Throwable> ExceptionHandler<T> searchExceptionHandler(final Throwable cause) {
        Class<? extends Throwable> exceptionType = cause.getClass();
        ExceptionHandler<? extends Throwable> exceptionHandler = exceptionHandlers.get(exceptionType);
        if (null == exceptionHandler) {
            for (Map.Entry<Class<? extends Throwable>, ExceptionHandler<? extends Throwable>> entry : exceptionHandlers.entrySet()) {
                Class<? extends Throwable> clazz = entry.getKey();
                ExceptionHandler<? extends Throwable> handler = entry.getValue();
                if (clazz.isAssignableFrom(exceptionType)) {
                    exceptionHandler = handler;
                    break;
                }
            }
        }
        if (null == exceptionHandler) {
            exceptionHandler = DEFAULT_EXCEPTION_HANDLER;
        }
        return (ExceptionHandler<T>) exceptionHandler;
    }
}
