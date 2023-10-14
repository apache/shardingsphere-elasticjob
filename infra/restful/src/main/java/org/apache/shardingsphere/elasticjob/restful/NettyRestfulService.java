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
import io.netty.util.NettyRuntime;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.restful.pipeline.RestfulServiceChannelInitializer;

/**
 * Implemented {@link RestfulService} via Netty.
 */
@Slf4j
@RequiredArgsConstructor
public final class NettyRestfulService implements RestfulService {
    
    private static final int DEFAULT_WORKER_GROUP_THREADS = 1 + 2 * NettyRuntime.availableProcessors();
    
    private final NettyRestfulServiceConfiguration configuration;
    
    private ServerBootstrap serverBootstrap;
    
    private EventLoopGroup bossEventLoopGroup;
    
    private EventLoopGroup workerEventLoopGroup;
    
    private void initServerBootstrap() {
        bossEventLoopGroup = new NioEventLoopGroup();
        workerEventLoopGroup = new NioEventLoopGroup(DEFAULT_WORKER_GROUP_THREADS);
        serverBootstrap = new ServerBootstrap()
                .group(bossEventLoopGroup, workerEventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new RestfulServiceChannelInitializer(configuration));
    }
    
    @SneakyThrows
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
                log.error("Failed to start Restful Service.", future.cause());
            }
        }).sync();
    }
    
    @Override
    public void shutdown() {
        bossEventLoopGroup.shutdownGracefully();
        workerEventLoopGroup.shutdownGracefully();
    }
}
