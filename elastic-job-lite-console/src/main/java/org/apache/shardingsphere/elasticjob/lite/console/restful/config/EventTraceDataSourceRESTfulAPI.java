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

package org.apache.shardingsphere.elasticjob.lite.console.restful.config;

import org.apache.shardingsphere.elasticjob.lite.console.domain.EventTraceDataSourceConfiguration;
import org.apache.shardingsphere.elasticjob.lite.console.domain.EventTraceDataSourceFactory;
import org.apache.shardingsphere.elasticjob.lite.console.service.EventTraceDataSourceConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.console.service.impl.EventTraceDataSourceConfigurationServiceImpl;
import org.apache.shardingsphere.elasticjob.lite.console.util.SessionEventTraceDataSourceConfiguration;
import com.google.common.base.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

/**
 * Event trace data source RESTful API.
 */
@Path("/data-source")
public final class EventTraceDataSourceRESTfulAPI {
    
    public static final String DATA_SOURCE_CONFIG_KEY = "data_source_config_key";
    
    private EventTraceDataSourceConfigurationService eventTraceDataSourceConfigurationService = new EventTraceDataSourceConfigurationServiceImpl();
    
    /**
     * Judge whether event trace data source is activated.
     *
     * @param request HTTP request
     * @return event trace data source is activated or not
     */
    @GET
    @Path("/activated")
    public boolean activated(final @Context HttpServletRequest request) {
        return eventTraceDataSourceConfigurationService.loadActivated().isPresent();
    }
    
    /**
     * Load event trace data source configuration.
     * 
     * @param request HTTP request
     * @return event trace data source configurations
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<EventTraceDataSourceConfiguration> load(final @Context HttpServletRequest request) {
        Optional<EventTraceDataSourceConfiguration> dataSourceConfig = eventTraceDataSourceConfigurationService.loadActivated();
        if (dataSourceConfig.isPresent()) {
            setDataSourceNameToSession(dataSourceConfig.get(), request.getSession());
        }
        return eventTraceDataSourceConfigurationService.loadAll().getEventTraceDataSourceConfiguration();
    }
    
    /**
     * Add event trace data source configuration.
     * 
     * @param config event trace data source configuration
     * @return success to added or not
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean add(final EventTraceDataSourceConfiguration config) {
        return eventTraceDataSourceConfigurationService.add(config);
    }
    
    /**
     * Delete event trace data source configuration.
     * 
     * @param config event trace data source configuration
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public void delete(final EventTraceDataSourceConfiguration config) {
        eventTraceDataSourceConfigurationService.delete(config.getName());
    }
    
    /**
     * Test event trace data source connection.
     *
     * @param config event trace data source configuration
     * @param request HTTP request
     * @return success or not
     */
    @POST
    @Path("/connectTest")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean connectTest(final EventTraceDataSourceConfiguration config, final @Context HttpServletRequest request) {
        return setDataSourceNameToSession(config, request.getSession());
    }
    
    /**
     * Connect event trace data source.
     *
     * @param config event trace data source
     * @param request HTTP request
     * @return success or not
     */
    @POST
    @Path("/connect")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean connect(final EventTraceDataSourceConfiguration config, final @Context HttpServletRequest request) {
        boolean isConnected = setDataSourceNameToSession(eventTraceDataSourceConfigurationService.find(config.getName(), eventTraceDataSourceConfigurationService.loadAll()), request.getSession());
        if (isConnected) {
            eventTraceDataSourceConfigurationService.load(config.getName());
        }
        return isConnected;
    }
    
    private boolean setDataSourceNameToSession(final EventTraceDataSourceConfiguration dataSourceConfig, final HttpSession session) {
        session.setAttribute(DATA_SOURCE_CONFIG_KEY, dataSourceConfig);
        try {
            EventTraceDataSourceFactory.createEventTraceDataSource(dataSourceConfig.getDriver(), dataSourceConfig.getUrl(), 
                    dataSourceConfig.getUsername(), Optional.fromNullable(dataSourceConfig.getPassword()));
            SessionEventTraceDataSourceConfiguration.setDataSourceConfiguration((EventTraceDataSourceConfiguration) session.getAttribute(DATA_SOURCE_CONFIG_KEY));
        // CHECKSTYLE:OFF
        } catch (final Exception ex) {
        // CHECKSTYLE:ON
            System.out.println(ex);
            return false;
        }
        return true;
    }
}
