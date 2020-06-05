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

import com.google.common.base.Optional;
import org.apache.shardingsphere.elasticjob.lite.console.domain.RegistryCenterConfiguration;
import org.apache.shardingsphere.elasticjob.lite.console.service.RegistryCenterConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.console.service.impl.RegistryCenterConfigurationServiceImpl;
import org.apache.shardingsphere.elasticjob.lite.console.util.SessionRegistryCenterConfiguration;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.reg.RegistryCenterFactory;
import org.apache.shardingsphere.elasticjob.lite.reg.exception.RegException;

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
 * Registry center RESTful API.
 */
@Path("/registry-center")
public final class RegistryCenterRESTfulAPI {
    
    public static final String REG_CENTER_CONFIG_KEY = "reg_center_config_key";
    
    private RegistryCenterConfigurationService regCenterService = new RegistryCenterConfigurationServiceImpl();
    
    /**
     * Judge whether registry center is activated.
     *
     * @param request HTTP request
     * @return registry center is activated or not
     */
    @GET
    @Path("/activated")
    public boolean activated(final @Context HttpServletRequest request) {
        return regCenterService.loadActivated().isPresent();
    }
    
    /**
     * 读取注册中心配置集合.
     * 
     * @param request HTTP request
     * @return registry center configurations
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<RegistryCenterConfiguration> load(final @Context HttpServletRequest request) {
        Optional<RegistryCenterConfiguration> regCenterConfig = regCenterService.loadActivated();
        if (regCenterConfig.isPresent()) {
            setRegistryCenterNameToSession(regCenterConfig.get(), request.getSession());
        }
        return regCenterService.loadAll().getRegistryCenterConfiguration();
    }
    
    /**
     * 添加注册中心.
     * 
     * @param config registry center configuration
     * @return success to add or not
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public boolean add(final RegistryCenterConfiguration config) {
        return regCenterService.add(config);
    }
    
    /**
     * Delete registry center.
     *
     * @param config registry center configuration
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public void delete(final RegistryCenterConfiguration config) {
        regCenterService.delete(config.getName());
    }

    /**
     * Connect to registry center.
     *
     * @param config config of registry center
     * @param request HTTP request
     * @return connected or not
     */
    @POST
    @Path("/connect")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean connect(final RegistryCenterConfiguration config, final @Context HttpServletRequest request) {
        boolean isConnected = setRegistryCenterNameToSession(regCenterService.find(config.getName(), regCenterService.loadAll()), request.getSession());
        if (isConnected) {
            regCenterService.load(config.getName());
        }
        return isConnected;
    }
    
    private boolean setRegistryCenterNameToSession(final RegistryCenterConfiguration regCenterConfig, final HttpSession session) {
        session.setAttribute(REG_CENTER_CONFIG_KEY, regCenterConfig);
        try {
            RegistryCenterFactory.createCoordinatorRegistryCenter(regCenterConfig.getZkAddressList(), regCenterConfig.getNamespace(), Optional.fromNullable(regCenterConfig.getDigest()));
            SessionRegistryCenterConfiguration.setRegistryCenterConfiguration((RegistryCenterConfiguration) session.getAttribute(REG_CENTER_CONFIG_KEY));
        } catch (final RegException ex) {
            return false;
        }
        return true;
    }
}
