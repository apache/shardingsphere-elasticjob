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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.restful.Filter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DefaultFilterChainTest {
    
    @Mock
    private ChannelHandlerContext ctx;
    
    @Mock
    private FullHttpRequest httpRequest;
    
    @Mock
    private FullHttpResponse httpResponse;
    
    @Mock
    private Filter firstFilter;
    
    @Mock
    private Filter secondFilter;
    
    @Mock
    private Filter thirdFilter;
    
    @Test
    public void assertNoFilter() {
        DefaultFilterChain filterChain = new DefaultFilterChain(ctx, Collections.emptyList());
        filterChain.next(httpRequest);
        verify(ctx, never()).writeAndFlush(httpResponse);
        verify(ctx).fireChannelRead(httpRequest);
    }
    
    @Test
    public void assertWithSingleFilterPassed() {
        DefaultFilterChain filterChain = new DefaultFilterChain(ctx, Collections.singletonList(firstFilter));
        when(firstFilter.doFilter(httpRequest, filterChain)).thenReturn(true);
        filterChain.next(httpRequest);
        verify(firstFilter).doFilter(httpRequest, filterChain);
        verify(firstFilter, never()).doResponse(httpRequest);
        filterChain.next(httpRequest);
        verify(ctx).fireChannelRead(httpRequest);
        verify(ctx, never()).writeAndFlush(httpResponse);
    }
    
    @Test
    public void assertWithSingleFilterDoResponse() {
        DefaultFilterChain filterChain = new DefaultFilterChain(ctx, Collections.singletonList(firstFilter));
        when(firstFilter.doResponse(httpRequest)).thenReturn(httpResponse);
        filterChain.next(httpRequest);
        verify(firstFilter).doFilter(httpRequest, filterChain);
        verify(firstFilter).doResponse(httpRequest);
        verify(ctx, never()).fireChannelRead(any(FullHttpRequest.class));
        verify(ctx).writeAndFlush(httpResponse);
    }
    
    @Test
    public void assertWithThreeFiltersPassed() {
        DefaultFilterChain filterChain = new DefaultFilterChain(ctx, Arrays.asList(firstFilter, secondFilter, thirdFilter));
        when(firstFilter.doFilter(httpRequest, filterChain)).thenReturn(true);
        filterChain.next(httpRequest);
        verify(firstFilter).doFilter(httpRequest, filterChain);
        when(secondFilter.doFilter(httpRequest, filterChain)).thenReturn(true);
        filterChain.next(httpRequest);
        verify(secondFilter).doFilter(httpRequest, filterChain);
        when(thirdFilter.doFilter(httpRequest, filterChain)).thenReturn(true);
        filterChain.next(httpRequest);
        verify(thirdFilter).doFilter(httpRequest, filterChain);
        filterChain.next(httpRequest);
        verify(ctx).fireChannelRead(httpRequest);
        verify(ctx, never()).writeAndFlush(any(FullHttpResponse.class));
    }
    
    @Test
    public void assertWithThreeFiltersDoResponseByTheSecond() {
        DefaultFilterChain filterChain = new DefaultFilterChain(ctx, Arrays.asList(firstFilter, secondFilter, thirdFilter));
        when(firstFilter.doFilter(httpRequest, filterChain)).thenReturn(true);
        filterChain.next(httpRequest);
        verify(firstFilter).doFilter(httpRequest, filterChain);
        when(secondFilter.doFilter(httpRequest, filterChain)).thenReturn(false);
        when(secondFilter.doResponse(httpRequest)).thenReturn(httpResponse);
        assertFalse(isFinished(filterChain));
        filterChain.next(httpRequest);
        verify(secondFilter).doFilter(httpRequest, filterChain);
        verify(secondFilter).doResponse(httpRequest);
        assertTrue(isFinished(filterChain));
        verify(thirdFilter, never()).doFilter(httpRequest, filterChain);
        verify(ctx, never()).fireChannelRead(any(FullHttpRequest.class));
        verify(ctx).writeAndFlush(httpResponse);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertInvokeFinishedFilterChainWithoutFilter() {
        DefaultFilterChain filterChain = new DefaultFilterChain(ctx, Collections.emptyList());
        filterChain.next(httpRequest);
        filterChain.next(httpRequest);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertInvokeFinishedFilterChainWithTwoFilters() {
        DefaultFilterChain filterChain = new DefaultFilterChain(ctx, Arrays.asList(firstFilter, secondFilter));
        when(firstFilter.doFilter(httpRequest, filterChain)).thenReturn(true);
        filterChain.next(httpRequest);
        verify(firstFilter).doFilter(httpRequest, filterChain);
        when(secondFilter.doFilter(httpRequest, filterChain)).thenReturn(true);
        filterChain.next(httpRequest);
        verify(secondFilter).doFilter(httpRequest, filterChain);
        filterChain.next(httpRequest);
        verify(ctx).fireChannelRead(httpRequest);
        filterChain.next(httpRequest);
    }
    
    @SneakyThrows
    private boolean isFinished(final DefaultFilterChain filterChain) {
        Field field = DefaultFilterChain.class.getDeclaredField("finished");
        field.setAccessible(true);
        return (boolean) field.get(filterChain);
    }
}
