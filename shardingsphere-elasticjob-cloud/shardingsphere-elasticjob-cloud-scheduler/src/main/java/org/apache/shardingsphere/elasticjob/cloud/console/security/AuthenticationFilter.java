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

package org.apache.shardingsphere.elasticjob.cloud.console.security;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.restful.Filter;
import org.apache.shardingsphere.elasticjob.restful.Http;
import org.apache.shardingsphere.elasticjob.restful.deserializer.RequestBodyDeserializer;
import org.apache.shardingsphere.elasticjob.restful.deserializer.RequestBodyDeserializerFactory;
import org.apache.shardingsphere.elasticjob.restful.filter.FilterChain;

import java.util.Collections;
import java.util.Optional;

/**
 * Authentication filter.
 */
@RequiredArgsConstructor
public final class AuthenticationFilter implements Filter {
    
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    
    private final AuthenticationService authenticationService;
    
    @Override
    public void doFilter(final FullHttpRequest httpRequest, final FullHttpResponse httpResponse, final FilterChain filterChain) {
        if (HttpMethod.POST.equals(httpRequest.method()) && AuthenticationConstants.LOGIN_URI.equals(httpRequest.uri())) {
            handleLogin(httpRequest, httpResponse);
            return;
        }
        String accessToken = httpRequest.headers().get(AuthenticationConstants.HEADER_NAME);
        if (Strings.isNullOrEmpty(accessToken) || !accessToken.equals(authenticationService.getToken())) {
            respondWithUnauthorized(httpResponse);
            return;
        }
        filterChain.next(httpRequest);
    }
    
    private void handleLogin(final FullHttpRequest httpRequest, final FullHttpResponse httpResponse) {
        byte[] bytes = ByteBufUtil.getBytes(httpRequest.content());
        String mimeType = Optional.ofNullable(HttpUtil.getMimeType(httpRequest)).orElseGet(() -> HttpUtil.getMimeType(Http.DEFAULT_CONTENT_TYPE)).toString();
        RequestBodyDeserializer deserializer = RequestBodyDeserializerFactory.getRequestBodyDeserializer(mimeType);
        AuthenticationInfo authenticationInfo = deserializer.deserialize(AuthenticationInfo.class, bytes);
        boolean result = authenticationService.check(authenticationInfo);
        if (!result) {
            respondWithUnauthorized(httpResponse);
            return;
        }
        String token = gson.toJson(Collections.singletonMap(AuthenticationConstants.HEADER_NAME, authenticationService.getToken()));
        respond(httpResponse, HttpResponseStatus.OK, token.getBytes());
    }
    
    private void respondWithUnauthorized(final FullHttpResponse httpResponse) {
        String result = gson.toJson(Collections.singletonMap("message", "Unauthorized."));
        respond(httpResponse, HttpResponseStatus.UNAUTHORIZED, result.getBytes());
    }
    
    private void respond(final FullHttpResponse httpResponse, final HttpResponseStatus status, final byte[] result) {
        httpResponse.setStatus(status);
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, Http.DEFAULT_CONTENT_TYPE);
        httpResponse.content().writeBytes(result);
        HttpUtil.setContentLength(httpResponse, httpResponse.content().readableBytes());
        HttpUtil.setKeepAlive(httpResponse, true);
    }
}
