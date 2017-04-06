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

package com.dangdang.ddframe.job.lite.console.restful.config;

import com.dangdang.ddframe.job.lite.console.domain.EventTraceDataSourceConfiguration;
import com.dangdang.ddframe.job.lite.console.domain.EventTraceDataSourceFactory;
import com.dangdang.ddframe.job.lite.console.service.impl.EventTraceDataSourceServiceImpl;
import com.dangdang.ddframe.job.lite.console.util.SessionEventTraceDataSourceConfiguration;
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
 * 事件追踪数据源配置的RESTful API.
 *
 * @author caohao
 */
@Path("/data-source")
public final class EventTraceDataSourceRestfulApi {
    
    public static final String DATA_SOURCE_CONFIG_KEY = "data_source_config_key";
    
    private EventTraceDataSourceServiceImpl eventTraceDataSourceService = new EventTraceDataSourceServiceImpl();
    
    /**
     * 读取事件追踪数据源配置.
     * 
     * @param request HTTP请求对象
     * @return 事件追踪数据源配置集合
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<EventTraceDataSourceConfiguration> load(final @Context HttpServletRequest request) {
        Optional<EventTraceDataSourceConfiguration> dataSourceConfig = eventTraceDataSourceService.loadActivated();
        if (dataSourceConfig.isPresent()) {
            setDataSourceNameToSession(dataSourceConfig.get(), request.getSession());
        }
        return eventTraceDataSourceService.loadAll().getEventTraceDataSourceConfigurations().getEventTraceDataSourceConfiguration();
    }
    
    /**
     * 添加事件追踪数据源配置.
     * 
     * @param config 事件追踪数据源配置
     * @return 是否添加成功
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean add(final EventTraceDataSourceConfiguration config) {
        return eventTraceDataSourceService.add(config);
    }
    
    /**
     * 删除事件追踪数据源配置.
     * 
     * @param config 事件追踪数据源配置
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public void delete(final EventTraceDataSourceConfiguration config) {
        eventTraceDataSourceService.delete(config.getName());
    }
    
    /**
     * 连接事件追踪数据源.
     *
     * @param config 事件追踪数据源配置
     * @param request HTTP请求对象
     * @return 是否连接成功
     */
    @POST
    @Path("/connect")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean connect(final EventTraceDataSourceConfiguration config, final @Context HttpServletRequest request) {
        boolean isConnected = setDataSourceNameToSession(eventTraceDataSourceService.findDataSourceConfiguration(config.getName(), eventTraceDataSourceService.loadAll()), request.getSession());
        if (isConnected) {
            eventTraceDataSourceService.load(config.getName());
        }
        return isConnected;
    }
    
    private boolean setDataSourceNameToSession(final EventTraceDataSourceConfiguration dataSourceConfig, final HttpSession session) {
        session.setAttribute(DATA_SOURCE_CONFIG_KEY, dataSourceConfig);
        try {
            EventTraceDataSourceFactory.createCoordinatorDataSource(dataSourceConfig.getDriver(), dataSourceConfig.getUrl(), 
                    dataSourceConfig.getUsername(), Optional.fromNullable(dataSourceConfig.getPassword()));
            SessionEventTraceDataSourceConfiguration.setDataSourceConfiguration((EventTraceDataSourceConfiguration) session.getAttribute(DATA_SOURCE_CONFIG_KEY));
        // CHECKSTYLE:OFF
        } catch (final Exception ex) {
        // CHECKSTYLE:ON
            return false;
        }
        return true;
    }
}
