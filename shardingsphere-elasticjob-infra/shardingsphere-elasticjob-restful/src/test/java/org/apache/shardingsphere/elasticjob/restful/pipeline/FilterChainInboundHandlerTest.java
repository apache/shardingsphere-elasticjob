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
import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.restful.Filter;
import org.apache.shardingsphere.elasticjob.restful.handler.HandleContext;
import org.apache.shardingsphere.elasticjob.restful.handler.Handler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class FilterChainInboundHandlerTest {
    
    @Mock
    private List<Filter> filterInstances;
    
    @Mock
    private HandleContext<Handler> handleContext;
    
    private EmbeddedChannel channel;
    
    @Before
    public void setUp() {
        channel = new EmbeddedChannel(new FilterChainInboundHandler(filterInstances));
    }
    
    @Test
    @SneakyThrows
    public void assertNoFilter() {
        when(filterInstances.isEmpty()).thenReturn(true);
        channel.writeOneInbound(handleContext);
        verify(handleContext, never()).getHttpRequest();
    }
    
    @Test
    public void assertFilterExists() {
        when(filterInstances.isEmpty()).thenReturn(false);
        channel.writeOneInbound(handleContext);
        verify(handleContext, atLeastOnce()).getHttpRequest();
    }
}
