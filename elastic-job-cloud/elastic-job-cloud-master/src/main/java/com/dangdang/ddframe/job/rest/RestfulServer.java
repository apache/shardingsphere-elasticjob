/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.rest;

import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * REST API的内嵌服务器.
 *
 * @author zhangliang
 */
public class RestfulServer {
    
    private final Server server;
    
    public RestfulServer(final int port, final CoordinatorRegistryCenter regCenter) {
        RestfulApi.init(regCenter);
        server = new Server(port);
    }
    
    /**
     * 启动内嵌的restful服务器.
     * 
     * @throws Exception 启动服务器异常
     */
    public void start() throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(getServletHolder(), "/*");
        server.start();
    }
    
    public ServletHolder getServletHolder() {
        ServletHolder result = new ServletHolder(ServletContainer.class);
        result.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", PackagesResourceConfig.class.getName());
        result.setInitParameter("com.sun.jersey.config.property.packages", RestfulApi.class.getPackage().getName());
        result.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", Boolean.TRUE.toString());
        result.setInitParameter("resteasy.scan.providers", Boolean.TRUE.toString());
        result.setInitParameter("resteasy.use.builtin.providers", Boolean.FALSE.toString());
        return result;
    }
    
    /**
     * 停止内嵌的restful服务器.
     * 
     * @throws Exception 停止服务器异常
     */
    public void stop() throws Exception {
        server.stop();
    }
}
