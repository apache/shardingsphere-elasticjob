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

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.restful.Filter;
import org.apache.shardingsphere.elasticjob.restful.filter.FilterChain;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class FilterChainInboundHandlerTest {
    
    @Mock
    private Filter mockFilter;
    
    @Test
    @SneakyThrows
    public void assertFilterPassed() {
        EmbeddedChannel channel = new EmbeddedChannel(new FilterChainInboundHandler(Collections.singletonList(mockFilter)));
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        when(mockFilter.doFilter(eq(httpRequest), any(FilterChain.class))).thenReturn(true);
        channel.writeOneInbound(httpRequest);
        verify(mockFilter).doFilter(eq(httpRequest), any(FilterChain.class));
        verify(mockFilter, never()).doResponse(any(FullHttpRequest.class));
    }
    
    @Test
    public void assertFilterDoResponse() {
        EmbeddedChannel channel = new EmbeddedChannel(new FilterChainInboundHandler(Collections.singletonList(mockFilter)));
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        channel.writeOneInbound(httpRequest);
        verify(mockFilter).doFilter(eq(httpRequest), any(FilterChain.class));
        verify(mockFilter).doResponse(any(FullHttpRequest.class));
    }
}
