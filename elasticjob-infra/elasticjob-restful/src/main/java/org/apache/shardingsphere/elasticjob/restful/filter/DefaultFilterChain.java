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

package org.apache.shardingsphere.elasticjob.restful.filter;

import com.google.common.base.Preconditions;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import org.apache.shardingsphere.elasticjob.restful.Filter;
import org.apache.shardingsphere.elasticjob.restful.handler.HandleContext;

import java.util.List;

/**
 * Default filter chain.
 */
public final class DefaultFilterChain implements FilterChain {
    
    private final Filter[] filters;
    
    private final ChannelHandlerContext ctx;
    
    private final HandleContext<?> handleContext;
    
    private int current;
    
    private boolean finished;
    
    public DefaultFilterChain(final List<Filter> filterInstances, final ChannelHandlerContext ctx, final HandleContext<?> handleContext) {
        filters = filterInstances.toArray(new Filter[0]);
        this.ctx = ctx;
        this.handleContext = handleContext;
    }
    
    @Override
    public void next(final FullHttpRequest httpRequest) {
        Preconditions.checkState(!finished, "FilterChain has already finished.");
        if (current < filters.length) {
            Filter currentFilter = filters[current++];
            boolean passThrough = currentFilter.doFilter(httpRequest, handleContext.getHttpResponse(), this);
            if (!passThrough) {
                finished = true;
                doResponse();
            }
            return;
        }
        finished = true;
        ctx.fireChannelRead(handleContext);
    }
    
    private void doResponse() {
        try {
            ctx.writeAndFlush(handleContext.getHttpResponse());
        } finally {
            ReferenceCountUtil.release(handleContext.getHttpRequest());
        }
    }
}
