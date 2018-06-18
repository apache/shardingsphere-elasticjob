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

package io.elasticjob.lite.lifecycle.security;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * 认证过滤器.
 * 
 * @author zhangliang 
 */
@Slf4j
public final class WwwAuthFilter implements Filter {
    
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    
    private static final String AUTH_PREFIX = "Basic ";
    
    private static final String ROOT_IDENTIFY = "root";
    
    private static final String ROOT_DEFAULT_USERNAME = "root";
    
    private static final String ROOT_DEFAULT_PASSWORD = "root";
    
    private static final String GUEST_IDENTIFY = "guest";
    
    private static final String GUEST_DEFAULT_USERNAME = "guest";
    
    private static final String GUEST_DEFAULT_PASSWORD = "guest";
    
    
    private String rootUsername;
    
    private String rootPassword;
    
    private String guestUsername;
    
    private String guestPassword;
    
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        Properties props = new Properties();
        URL classLoaderURL = Thread.currentThread().getContextClassLoader().getResource("");
        if (null != classLoaderURL) {
            String configFilePath = Joiner.on(FILE_SEPARATOR).join(classLoaderURL.getPath(), "conf", "auth.properties");
            try {
                props.load(new FileInputStream(configFilePath));
            } catch (final IOException ex) {
                log.warn("Cannot found auth config file, use default auth config.");
            }
        }
        rootUsername = props.getProperty("root.username", ROOT_DEFAULT_USERNAME);
        rootPassword = props.getProperty("root.password", ROOT_DEFAULT_PASSWORD);
        guestUsername = props.getProperty("guest.username", GUEST_DEFAULT_USERNAME);
        guestPassword = props.getProperty("guest.password", GUEST_DEFAULT_PASSWORD);
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
        response.setHeader("WWW-authenticate", AUTH_PREFIX + "Realm=\"Elastic Job Console Auth\"");
    }
    
    @Override
    public void destroy() {
    }
}
