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
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import org.apache.shardingsphere.elasticjob.restful.Filter;

import java.util.List;

/**
 * Default filter chain.
 */
public final class DefaultFilterChain implements FilterChain {
    
    private final ChannelHandlerContext ctx;
    
    private final Filter[] filters;
    
    private int current;
    
    private boolean finished;
    
    public DefaultFilterChain(final ChannelHandlerContext ctx, final List<Filter> filterInstances) {
        this.ctx = ctx;
        filters = filterInstances.toArray(new Filter[0]);
    }
    
    @Override
    public void next(final FullHttpRequest httpRequest) {
        Preconditions.checkState(!finished, "FilterChain has already finished.");
        if (current < filters.length) {
            Filter currentFilter = filters[current++];
            boolean pass = currentFilter.doFilter(httpRequest, this);
            if (!pass) {
                finished = true;
                doResponse(httpRequest, currentFilter);
            }
            return;
        }
        finished = true;
        ctx.fireChannelRead(httpRequest);
    }
    
    private void doResponse(final FullHttpRequest httpRequest, final Filter currentFilter) {
        try {
            FullHttpResponse httpResponse = currentFilter.doResponse(httpRequest);
            Preconditions.checkNotNull(httpResponse, "Returning null is not allowed in method Filter#doResponse.");
            ctx.writeAndFlush(httpResponse);
        } finally {
            ReferenceCountUtil.release(httpRequest);
        }
    }
}
