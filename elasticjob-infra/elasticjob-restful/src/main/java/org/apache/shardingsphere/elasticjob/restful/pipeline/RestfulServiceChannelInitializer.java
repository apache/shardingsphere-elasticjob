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

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.apache.shardingsphere.elasticjob.restful.NettyRestfulServiceConfiguration;

/**
 * Initialize channel pipeline.
 */
public final class RestfulServiceChannelInitializer extends ChannelInitializer<Channel> {
    
    private final ContextInitializationInboundHandler contextInitializationInboundHandler;
    
    private final FilterChainInboundHandler filterChainInboundHandler;
    
    private final HttpRequestDispatcher httpRequestDispatcher;
    
    private final HandlerParameterDecoder handlerParameterDecoder;
    
    private final HandleMethodExecutor handleMethodExecutor;
    
    private final ExceptionHandling exceptionHandling;
    
    public RestfulServiceChannelInitializer(final NettyRestfulServiceConfiguration configuration) {
        contextInitializationInboundHandler = new ContextInitializationInboundHandler();
        filterChainInboundHandler = new FilterChainInboundHandler(configuration.getFilterInstances());
        httpRequestDispatcher = new HttpRequestDispatcher(configuration.getControllerInstances(), configuration.isTrailingSlashSensitive());
        handlerParameterDecoder = new HandlerParameterDecoder();
        handleMethodExecutor = new HandleMethodExecutor();
        exceptionHandling = new ExceptionHandling(configuration.getExceptionHandlers());
    }
    
    @Override
    protected void initChannel(final Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("codec", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024));
        pipeline.addLast("contextInitialization", contextInitializationInboundHandler);
        pipeline.addLast("filterChain", filterChainInboundHandler);
        pipeline.addLast("dispatcher", httpRequestDispatcher);
        pipeline.addLast("handlerParameterDecoder", handlerParameterDecoder);
        pipeline.addLast("handleMethodExecutor", handleMethodExecutor);
        pipeline.addLast("exceptionHandling", exceptionHandling);
    }
}
