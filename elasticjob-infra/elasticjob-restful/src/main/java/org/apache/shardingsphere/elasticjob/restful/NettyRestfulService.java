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

package org.apache.shardingsphere.elasticjob.restful;

import com.google.common.base.Strings;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * Implemented {@link RestfulService} via Netty.
 */
@Slf4j
public class NettyRestfulService implements RestfulService {
    
    private final NettyRestfulServiceConfiguration configuration;
    
    private ServerBootstrap serverBootstrap;
    
    private EventLoopGroup eventLoopGroup;
    
    public NettyRestfulService(final NettyRestfulServiceConfiguration configuration) {
        this.configuration = configuration;
    }
    
    private void initServerBootstrap() {
        eventLoopGroup = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap()
                .group(eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new RestfulServiceChannelInitializer(configuration));
    }
    
    @Override
    public void startup() {
        initServerBootstrap();
        ChannelFuture channelFuture;
        if (!Strings.isNullOrEmpty(configuration.getHost())) {
            channelFuture = serverBootstrap.bind(configuration.getHost(), configuration.getPort());
        } else {
            channelFuture = serverBootstrap.bind(configuration.getPort());
        }
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                log.info("Restful Service started on port {}.", configuration.getPort());
            } else {
                log.error("", future.cause());
            }
        });
    }
    
    @Override
    public void shutdown() {
        eventLoopGroup.shutdownGracefully();
    }
}
