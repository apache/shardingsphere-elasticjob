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

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.ReferenceCountUtil;
import org.apache.shardingsphere.elasticjob.restful.RestfulController;
import org.apache.shardingsphere.elasticjob.restful.annotation.ContextPath;
import org.apache.shardingsphere.elasticjob.restful.annotation.Mapping;
import org.apache.shardingsphere.elasticjob.restful.handler.HandleContext;
import org.apache.shardingsphere.elasticjob.restful.handler.Handler;
import org.apache.shardingsphere.elasticjob.restful.handler.HandlerMappingRegistry;
import org.apache.shardingsphere.elasticjob.restful.handler.HandlerNotFoundException;
import org.apache.shardingsphere.elasticjob.restful.mapping.MappingContext;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * If a HTTP request reached, HTTP request dispatcher would lookup a proper Handler for the request.
 * Assemble a {@link HandleContext} with HTTP request and {@link MappingContext}, then pass it to the next in-bound handler.
 */
@Sharable
public final class HttpRequestDispatcher extends ChannelInboundHandlerAdapter {
    
    private static final String TRAILING_SLASH = "/";
    
    private final HandlerMappingRegistry mappingRegistry = new HandlerMappingRegistry();
    
    private final boolean trailingSlashSensitive;
    
    public HttpRequestDispatcher(final List<RestfulController> restfulControllers, final boolean trailingSlashSensitive) {
        this.trailingSlashSensitive = trailingSlashSensitive;
        initMappingRegistry(restfulControllers);
    }
    
    @SuppressWarnings({"unchecked", "NullableProblems"})
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        HandleContext<Handler> handleContext = (HandleContext<Handler>) msg;
        FullHttpRequest request = handleContext.getHttpRequest();
        if (!trailingSlashSensitive) {
            request.setUri(appendTrailingSlashIfAbsent(request.uri()));
        }
        Optional<MappingContext<Handler>> mappingContext = mappingRegistry.getMappingContext(request);
        if (mappingContext.isPresent()) {
            handleContext.setMappingContext(mappingContext.get());
            ctx.fireChannelRead(handleContext);
        } else {
            ReferenceCountUtil.release(request);
            throw new HandlerNotFoundException(request.uri());
        }
    }
    
    private void initMappingRegistry(final List<RestfulController> restfulControllers) {
        for (RestfulController restfulController : restfulControllers) {
            Class<? extends RestfulController> controllerClass = restfulController.getClass();
            String contextPath = Optional.ofNullable(controllerClass.getAnnotation(ContextPath.class)).map(ContextPath::value).orElse("");
            for (Method method : controllerClass.getMethods()) {
                Mapping mapping = method.getAnnotation(Mapping.class);
                if (null == mapping) {
                    continue;
                }
                HttpMethod httpMethod = HttpMethod.valueOf(mapping.method());
                String path = mapping.path();
                String fullPathPattern = resolveFullPath(contextPath, path);
                if (!trailingSlashSensitive) {
                    fullPathPattern = appendTrailingSlashIfAbsent(fullPathPattern);
                }
                mappingRegistry.addMapping(httpMethod, fullPathPattern, new Handler(restfulController, method));
            }
        }
    }
    
    private String resolveFullPath(final String contextPath, final String pattern) {
        return Optional.ofNullable(contextPath).orElse("") + pattern;
    }
    
    private String appendTrailingSlashIfAbsent(final String uri) {
        String[] split = uri.split("\\?");
        if (1 == split.length) {
            return uri.endsWith(TRAILING_SLASH) ? uri : uri + TRAILING_SLASH;
        }
        String path = split[0];
        return path.endsWith(TRAILING_SLASH) ? uri : path + TRAILING_SLASH + "?" + split[1];
    }
}
