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

package org.apache.shardingsphere.elasticjob.cloud.restful;

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.EnumSet;

/**
 * RESTful API embed server.
 */
@Slf4j
public final class RestfulServer {
    
    private final Server server;
    
    private final ServletContextHandler servletContextHandler;
    
    public RestfulServer(final int port) {
        server = new Server(port);
        servletContextHandler = buildServletContextHandler();
    }
    
    /**
     * Start server.
     * 
     * @param packages restful implementation class package
     * @param resourcePath resource path
     * @throws Exception exception when startup
     */
    public void start(final String packages, final String resourcePath) throws Exception {
        start(packages, resourcePath, "/api");
    }
    
    /**
     * Start server.
     *
     * @param packages restful implementation class package
     * @param resourcePath resource path
     * @param servletPath servlet path
     * @throws Exception exception when startup
     */
    public void start(final String packages, final String resourcePath, final String servletPath) throws Exception {
        log.info("Elastic Job: Start RESTful server");
        HandlerList handlers = new HandlerList();
        if (null != resourcePath) {
            servletContextHandler.setBaseResource(Resource.newClassPathResource(resourcePath));
            servletContextHandler.addServlet(new ServletHolder(DefaultServlet.class), "/*");
        }
        String servletPathStr = (null != servletPath ? servletPath : "") + "/*";
        servletContextHandler.addServlet(getServletHolder(packages), servletPathStr);
        handlers.addHandler(servletContextHandler);
        server.setHandler(handlers);
        server.start();
    }
    
    /**
     * Add filter.
     *
     * @param filterClass filter implementation class
     * @param urlPattern url pattern to filter
     * @return restful server
     */
    public RestfulServer addFilter(final Class<? extends Filter> filterClass, final String urlPattern) {
        servletContextHandler.addFilter(filterClass, urlPattern, EnumSet.of(DispatcherType.REQUEST));
        return this;
    }
    
    private ServletContextHandler buildServletContextHandler() {
        ServletContextHandler result = new ServletContextHandler(ServletContextHandler.SESSIONS);
        result.setContextPath("/");
        return result;
    }
    
    private ServletHolder getServletHolder(final String packages) {
        ServletHolder result = new ServletHolder(ServletContainer.class);
        result.setInitParameter(PackagesResourceConfig.PROPERTY_PACKAGES, String.join(",", RestfulServer.class.getPackage().getName(), packages));
        result.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", PackagesResourceConfig.class.getName());
        result.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", Boolean.TRUE.toString());
        result.setInitParameter("resteasy.scan.providers", Boolean.TRUE.toString());
        result.setInitParameter("resteasy.use.builtin.providers", Boolean.FALSE.toString());
        return result;
    }
    
    /**
     * Stop the embed server gracefully.
     * 
     */
    public void stop() {
        log.info("Elastic Job: Stop RESTful server");
        try {
            server.stop();
            // CHECKSTYLE:OFF
        } catch (final Exception e) {
            // CHECKSTYLE:ON
            log.error("Elastic Job: Stop RESTful server error", e);
        }
    }
}
