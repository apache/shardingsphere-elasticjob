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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpClient {
    
    /**
     * Send a HTTP request and invoke consumer when server response.
     *
     * @param host server host
     * @param port server port
     * @param request HTTP request
     * @param consumer HTTP response consumer
     * @param timeoutSeconds wait for consume
     */
    @SneakyThrows
    public static void request(final String host, final int port, final FullHttpRequest request, final Consumer<FullHttpResponse> consumer, final Long timeoutSeconds) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Channel channel = new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress(host, port)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(final Channel ch) {
                        ch.pipeline()
                                .addLast(new HttpClientCodec())
                                .addLast(new HttpObjectAggregator(1024 * 1024))
                                .addLast(new SimpleChannelInboundHandler<FullHttpResponse>() {
                                    @Override
                                    protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpResponse httpResponse) throws Exception {
                                        try {
                                            consumer.accept(httpResponse);
                                        } finally {
                                            countDownLatch.countDown();
                                        }
                                    }
                                });
                    }
                }).connect()
                .sync().channel();
        channel.writeAndFlush(request);
        countDownLatch.await(timeoutSeconds, TimeUnit.SECONDS);
        channel.close().sync();
    }
}
