/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.servlet.*;
import com.google.common.base.Strings;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Slf4j
public final class WwwAuthFilter implements Filter {
    
    private static final String AUTH_PREFIX = "Basic ";

    private static final String GUEST = "guest";

    private static final String ROOT = "root";

    private String rootUsername;
    
    private String rootPassword;
    
    private String guestUsername;
    
    private String guestPassword;
    
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        String fileSeparator = System.getProperty("file.separator");
        String configFilePath = Thread.currentThread().getContextClassLoader().getResource("").getPath() + fileSeparator + "conf" + fileSeparator + "auth.properties";
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(configFilePath));
        } catch (final IOException ex) {
            log.warn("Cannot found auth config file, use default auth config.");
        }
        if (Strings.isNullOrEmpty(props.getProperty("root.username"))) {
            rootUsername = "root";
        } else {
            rootUsername = props.getProperty("root.username");
        }
        if (Strings.isNullOrEmpty(props.getProperty("guest.username"))) {
            guestUsername = "guest";
        } else {
            guestUsername = props.getProperty("guest.username");
        }
        rootPassword = props.getProperty("root.password", "root");
        guestPassword = props.getProperty("guest.password", "guest");
    }
    
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String authorization = httpRequest.getHeader("authorization");
        if (null != authorization && authorization.length() > AUTH_PREFIX.length()) {
            authorization = authorization.substring(AUTH_PREFIX.length(), authorization.length());
            if ((rootUsername + ":" + rootPassword).equals(new String(Base64.decodeBase64(authorization)))) {
                authenticateSuccess(httpResponse, false);
                chain.doFilter(httpRequest, httpResponse);
            } else if ((guestUsername + ":" + guestPassword).equals(new String(Base64.decodeBase64(authorization)))) {
                authenticateSuccess(httpResponse, true);
                chain.doFilter(httpRequest, httpResponse);
            } else {
                needAuthenticate(httpRequest, httpResponse);
            }
        } else {
            needAuthenticate(httpRequest, httpResponse);
        }
    }
    
    private void authenticateSuccess(final HttpServletResponse response, boolean isGuset) {
        response.setStatus(200);
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
        response.setHeader("identify", true == isGuset ? GUEST : ROOT);
    }
    
    private void needAuthenticate(final HttpServletRequest request, final HttpServletResponse response) {
        response.setStatus(401);
        response.setHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
        response.setHeader("WWW-authenticate", AUTH_PREFIX + "Realm=\"Elastic Job Console Auth\"");
    }
    
    @Override
    public void destroy() {
    }
}
