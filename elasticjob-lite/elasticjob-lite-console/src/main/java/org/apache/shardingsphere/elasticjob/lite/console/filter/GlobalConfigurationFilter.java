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

package org.apache.shardingsphere.elasticjob.lite.console.filter;

import org.apache.shardingsphere.elasticjob.lite.console.controller.EventTraceDataSourceController;
import org.apache.shardingsphere.elasticjob.lite.console.controller.RegistryCenterController;
import org.apache.shardingsphere.elasticjob.lite.console.domain.EventTraceDataSourceConfiguration;
import org.apache.shardingsphere.elasticjob.lite.console.domain.EventTraceDataSourceFactory;
import org.apache.shardingsphere.elasticjob.lite.console.domain.RegistryCenterConfiguration;
import org.apache.shardingsphere.elasticjob.lite.console.service.EventTraceDataSourceConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.console.service.RegistryCenterConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.console.service.impl.EventTraceDataSourceConfigurationServiceImpl;
import org.apache.shardingsphere.elasticjob.lite.console.service.impl.RegistryCenterConfigurationServiceImpl;
import org.apache.shardingsphere.elasticjob.lite.console.util.SessionEventTraceDataSourceConfiguration;
import org.apache.shardingsphere.elasticjob.lite.console.util.SessionRegistryCenterConfiguration;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.reg.RegistryCenterFactory;
import org.apache.shardingsphere.elasticjob.reg.exception.RegException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

/**
 * Global configuration filter.
 */
public final class GlobalConfigurationFilter implements Filter {
    
    private final RegistryCenterConfigurationService regCenterService = new RegistryCenterConfigurationServiceImpl();
    
    private final EventTraceDataSourceConfigurationService rdbService = new EventTraceDataSourceConfigurationServiceImpl();
    
    @Override
    public void init(final FilterConfig filterConfig) {
    }
    
    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpSession httpSession = httpRequest.getSession();
        if (null == httpSession.getAttribute(RegistryCenterController.REG_CENTER_CONFIG_KEY)) {
            loadActivatedRegCenter(httpSession);
        }
        if (null == httpSession.getAttribute(EventTraceDataSourceController.DATA_SOURCE_CONFIG_KEY)) {
            loadActivatedEventTraceDataSource(httpSession);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
    
    private void loadActivatedRegCenter(final HttpSession httpSession) {
        Optional<RegistryCenterConfiguration> config = regCenterService.loadActivated();
        if (config.isPresent()) {
            String configName = config.get().getName();
            boolean isConnected = setRegistryCenterNameToSession(regCenterService.find(configName, regCenterService.loadAll()), httpSession);
            if (isConnected) {
                regCenterService.load(configName);
            }
        }
    }
    
    private boolean setRegistryCenterNameToSession(final RegistryCenterConfiguration regCenterConfig, final HttpSession session) {
        session.setAttribute(RegistryCenterController.REG_CENTER_CONFIG_KEY, regCenterConfig);
        try {
            RegistryCenterFactory.createCoordinatorRegistryCenter(regCenterConfig.getZkAddressList(), regCenterConfig.getNamespace(), regCenterConfig.getDigest());
            SessionRegistryCenterConfiguration.setRegistryCenterConfiguration((RegistryCenterConfiguration) session.getAttribute(RegistryCenterController.REG_CENTER_CONFIG_KEY));
        } catch (final RegException ex) {
            return false;
        }
        return true;
    }
    
    private void loadActivatedEventTraceDataSource(final HttpSession httpSession) {
        Optional<EventTraceDataSourceConfiguration> config = rdbService.loadActivated();
        if (config.isPresent()) {
            String configName = config.get().getName();
            boolean isConnected = setEventTraceDataSourceNameToSession(rdbService.find(configName, rdbService.loadAll()), httpSession);
            if (isConnected) {
                rdbService.load(configName);
            }
        }
    }
    
    private boolean setEventTraceDataSourceNameToSession(final EventTraceDataSourceConfiguration dataSourceConfig, final HttpSession session) {
        session.setAttribute(EventTraceDataSourceController.DATA_SOURCE_CONFIG_KEY, dataSourceConfig);
        try {
            EventTraceDataSourceFactory.createEventTraceDataSource(dataSourceConfig.getDriver(), dataSourceConfig.getUrl(), dataSourceConfig.getUsername(), dataSourceConfig.getPassword());
            SessionEventTraceDataSourceConfiguration.setDataSourceConfiguration((EventTraceDataSourceConfiguration) session.getAttribute(EventTraceDataSourceController.DATA_SOURCE_CONFIG_KEY));
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            return false;
        }
        return true;
    }
    
    @Override
    public void destroy() {
    }
}
