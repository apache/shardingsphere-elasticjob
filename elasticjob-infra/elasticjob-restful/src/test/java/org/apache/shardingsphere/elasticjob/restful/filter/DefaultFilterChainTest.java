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
import org.apache.shardingsphere.elasticjob.restful.handler.HandleContext;
import org.apache.shardingsphere.elasticjob.restful.handler.Handler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DefaultFilterChainTest {
    
    @Mock
    private ChannelHandlerContext ctx;
    
    @Mock
    private FullHttpRequest httpRequest;
    
    @Mock
    private FullHttpResponse httpResponse;
    
    private HandleContext<Handler> handleContext;
    
    @BeforeEach
    void setUp() {
        handleContext = new HandleContext<>(httpRequest, httpResponse);
    }
    
    @Test
    void assertNoFilter() {
        DefaultFilterChain filterChain = new DefaultFilterChain(Collections.emptyList(), ctx, handleContext);
        filterChain.next(httpRequest);
        verify(ctx, never()).writeAndFlush(httpResponse);
        verify(ctx).fireChannelRead(handleContext);
        assertTrue(isPassedThrough(filterChain));
        assertFalse(isReplied(filterChain));
    }
    
    @Test
    void assertWithSingleFilterPassed() {
        Filter passableFilter = spy(new PassableFilter());
        DefaultFilterChain filterChain = new DefaultFilterChain(Collections.singletonList(passableFilter), ctx, handleContext);
        filterChain.next(httpRequest);
        verify(passableFilter).doFilter(httpRequest, httpResponse, filterChain);
        verify(ctx).fireChannelRead(handleContext);
        verify(ctx, never()).writeAndFlush(httpResponse);
        assertTrue(isPassedThrough(filterChain));
        assertFalse(isReplied(filterChain));
    }
    
    @Test
    void assertWithSingleFilterDoResponse() {
        Filter impassableFilter = mock(Filter.class);
        DefaultFilterChain filterChain = new DefaultFilterChain(Collections.singletonList(impassableFilter), ctx, handleContext);
        filterChain.next(httpRequest);
        verify(impassableFilter).doFilter(httpRequest, httpResponse, filterChain);
        verify(ctx, never()).fireChannelRead(any(HandleContext.class));
        verify(ctx).writeAndFlush(httpResponse);
        assertTrue(isReplied(filterChain));
        assertFalse(isPassedThrough(filterChain));
    }
    
    @Test
    void assertWithThreeFiltersPassed() {
        Filter firstFilter = spy(new PassableFilter());
        Filter secondFilter = spy(new PassableFilter());
        Filter thirdFilter = spy(new PassableFilter());
        DefaultFilterChain filterChain = new DefaultFilterChain(Arrays.asList(firstFilter, secondFilter, thirdFilter), ctx, handleContext);
        filterChain.next(httpRequest);
        verify(firstFilter).doFilter(httpRequest, httpResponse, filterChain);
        verify(secondFilter).doFilter(httpRequest, httpResponse, filterChain);
        verify(thirdFilter).doFilter(httpRequest, httpResponse, filterChain);
        assertTrue(isPassedThrough(filterChain));
        assertFalse(isReplied(filterChain));
        verify(ctx).fireChannelRead(handleContext);
        verify(ctx, never()).writeAndFlush(any(FullHttpResponse.class));
    }
    
    @Test
    void assertWithThreeFiltersDoResponseByTheSecond() {
        Filter firstFilter = spy(new PassableFilter());
        Filter secondFilter = mock(Filter.class);
        Filter thirdFilter = spy(new PassableFilter());
        DefaultFilterChain filterChain = new DefaultFilterChain(Arrays.asList(firstFilter, secondFilter, thirdFilter), ctx, handleContext);
        filterChain.next(httpRequest);
        verify(firstFilter).doFilter(httpRequest, httpResponse, filterChain);
        verify(secondFilter).doFilter(httpRequest, httpResponse, filterChain);
        assertFalse(isPassedThrough(filterChain));
        assertTrue(isReplied(filterChain));
        verify(thirdFilter, never()).doFilter(httpRequest, httpResponse, filterChain);
        verify(ctx, never()).fireChannelRead(any(HandleContext.class));
        verify(ctx).writeAndFlush(httpResponse);
    }
    
    @Test
    void assertInvokeFinishedFilterChainWithoutFilter() {
        assertThrows(IllegalStateException.class, () -> {
            DefaultFilterChain filterChain = new DefaultFilterChain(Collections.emptyList(), ctx, handleContext);
            filterChain.next(httpRequest);
            filterChain.next(httpRequest);
        });
    }
    
    @Test
    void assertInvokePassedThroughFilterChainWithTwoFilters() {
        assertThrows(IllegalStateException.class, () -> {
            Filter firstFilter = spy(new PassableFilter());
            Filter secondFilter = spy(new PassableFilter());
            DefaultFilterChain filterChain = new DefaultFilterChain(Arrays.asList(firstFilter, secondFilter), ctx, handleContext);
            filterChain.next(httpRequest);
            verify(firstFilter).doFilter(httpRequest, httpResponse, filterChain);
            verify(secondFilter).doFilter(httpRequest, httpResponse, filterChain);
            verify(ctx).fireChannelRead(handleContext);
            filterChain.next(httpRequest);
        });
    }
    
    private boolean isPassedThrough(final DefaultFilterChain filterChain) {
        return getBoolean(filterChain, "passedThrough");
    }
    
    private boolean isReplied(final DefaultFilterChain filterChain) {
        return getBoolean(filterChain, "replied");
    }
    
    @SneakyThrows
    private boolean getBoolean(final DefaultFilterChain filterChain, final String fieldName) {
        Field field = DefaultFilterChain.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (boolean) field.get(filterChain);
    }
    
    private static class PassableFilter implements Filter {
        
        @Override
        public void doFilter(final FullHttpRequest httpRequest, final FullHttpResponse httpResponse, final FilterChain filterChain) {
            filterChain.next(httpRequest);
        }
    }
}
