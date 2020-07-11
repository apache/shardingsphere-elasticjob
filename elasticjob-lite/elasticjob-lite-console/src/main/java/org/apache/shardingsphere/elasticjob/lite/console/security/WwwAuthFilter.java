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

package org.apache.shardingsphere.elasticjob.lite.console.security;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.FilterChain;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * WWW auth filter.
 */
@Slf4j
public final class WwwAuthFilter implements Filter {

    private static final String AUTH_PREFIX = "Basic ";

    private static final String ROOT_IDENTIFY = "root";

    private static final String GUEST_IDENTIFY = "guest";

    private static final String DIGEST_REALM = "127.0.0.1";

    private static final String DIGEST_QOP = "AUTH";

    private static final String DIGEST_OPAQUE = "740faacf85fd450e90f57e9b16d0725c";

    private static final String DIGEST_ALGORITHM = "MD5";

    @Setter
    private UserAuthenticationService userAuthenticationService;

    @Override
    public void init(final FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String authorization = httpRequest.getHeader("authorization");
        if (null != authorization && authorization.length() > AUTH_PREFIX.length()) {
            authorization = authorization.substring(AUTH_PREFIX.length());
            AuthenticationResult authenticationResult = userAuthenticationService.checkUser(authorization, ((HttpServletRequest) request).getMethod());
            if (authenticationResult.isSuccess()) {
                authenticateSuccess(httpResponse, authenticationResult.isGuest());
                chain.doFilter(httpRequest, httpResponse);
            } else {
                needAuthenticate(httpResponse);
            }
        } else {
            needAuthenticate(httpResponse);
        }
    }

    private void authenticateSuccess(final HttpServletResponse response, final boolean isGuest) {
        response.setStatus(200);
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
        response.setHeader("identify", isGuest ? GUEST_IDENTIFY : ROOT_IDENTIFY);
    }
    
    private void needAuthenticate(final HttpServletResponse response) {
        response.setStatus(401);
        response.setHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
        String nonce = UUID.randomUUID().toString();
        String authorization = String.format("Digest realm=\"%s\", qop=\"%s\", nonce=\"%s\", opaque=\"%s\", algorithm=\"%s\"",
                DIGEST_REALM, DIGEST_QOP, nonce, DIGEST_OPAQUE, DIGEST_ALGORITHM);
        response.setHeader("WWW-authenticate", authorization);
    }
    
    @Override
    public void destroy() {
    }
}
