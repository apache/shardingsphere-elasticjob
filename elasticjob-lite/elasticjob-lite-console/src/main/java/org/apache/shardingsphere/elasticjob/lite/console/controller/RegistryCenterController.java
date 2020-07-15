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

package org.apache.shardingsphere.elasticjob.lite.console.controller;

import org.apache.shardingsphere.elasticjob.lite.console.domain.RegistryCenterConfiguration;
import org.apache.shardingsphere.elasticjob.lite.console.service.RegistryCenterConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.console.util.SessionRegistryCenterConfiguration;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.reg.RegistryCenterFactory;
import org.apache.shardingsphere.elasticjob.reg.exception.RegException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

/**
 * Registry center RESTful API.
 */
@RestController
@RequestMapping("/registry-center")
public final class RegistryCenterController {
    
    public static final String REG_CENTER_CONFIG_KEY = "reg_center_config_key";
    
    private RegistryCenterConfigurationService regCenterService;
    
    @Autowired
    public RegistryCenterController(final RegistryCenterConfigurationService regCenterService) {
        this.regCenterService = regCenterService;
    }
    
    /**
     * Judge whether registry center is activated.
     *
     * @return registry center is activated or not
     */
    @GetMapping("/activated")
    public boolean activated() {
        return regCenterService.loadActivated().isPresent();
    }
    
    /**
     * Load configuration from registry center.
     *
     * @param request HTTP request
     * @return registry center configurations
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON)
    public Collection<RegistryCenterConfiguration> load(final HttpServletRequest request) {
        regCenterService.loadActivated().ifPresent(regCenterConfig -> setRegistryCenterNameToSession(regCenterConfig, request.getSession()));
        return regCenterService.loadAll().getRegistryCenterConfiguration();
    }
    
    /**
     * Add registry center.
     *
     * @param config registry center configuration
     * @return success to add or not
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON)
    public boolean add(@RequestBody final RegistryCenterConfiguration config) {
        return regCenterService.add(config);
    }
    
    /**
     * Delete registry center.
     *
     * @param config registry center configuration
     */
    @DeleteMapping(consumes = MediaType.APPLICATION_JSON)
    public void delete(@RequestBody final RegistryCenterConfiguration config) {
        regCenterService.delete(config.getName());
    }
    
    /**
     * Connect to registry center.
     *
     * @param config  config of registry center
     * @param request HTTP request
     * @return connected or not
     */
    @PostMapping(value = "/connect", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public boolean connect(@RequestBody final RegistryCenterConfiguration config, @Context final HttpServletRequest request) {
        boolean isConnected = setRegistryCenterNameToSession(regCenterService.find(config.getName(), regCenterService.loadAll()), request.getSession());
        if (isConnected) {
            regCenterService.load(config.getName());
        }
        return isConnected;
    }
    
    private boolean setRegistryCenterNameToSession(final RegistryCenterConfiguration regCenterConfig, final HttpSession session) {
        session.setAttribute(REG_CENTER_CONFIG_KEY, regCenterConfig);
        try {
            RegistryCenterFactory.createCoordinatorRegistryCenter(regCenterConfig.getZkAddressList(), regCenterConfig.getNamespace(), regCenterConfig.getDigest());
            SessionRegistryCenterConfiguration.setRegistryCenterConfiguration((RegistryCenterConfiguration) session.getAttribute(REG_CENTER_CONFIG_KEY));
        } catch (final RegException ex) {
            return false;
        }
        return true;
    }
}
