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

import com.google.common.collect.Lists;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.shardingsphere.elasticjob.restful.controller.JobController;
import org.apache.shardingsphere.elasticjob.restful.handler.HandleContext;
import org.apache.shardingsphere.elasticjob.restful.handler.HandlerNotFoundException;
import org.junit.Test;

public final class HttpRequestDispatcherTest {
    
    @Test(expected = HandlerNotFoundException.class)
    public void assertDispatcherHandlerNotFound() {
        EmbeddedChannel channel = new EmbeddedChannel(new HttpRequestDispatcher(Lists.newArrayList(new JobController()), false));
        FullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/hello/myJob/myCron");
        channel.writeInbound(new HandleContext<>(fullHttpRequest, null));
    }
}
