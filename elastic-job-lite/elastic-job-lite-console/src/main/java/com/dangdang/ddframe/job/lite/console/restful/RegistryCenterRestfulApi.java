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

package com.dangdang.ddframe.job.lite.console.restful;

import com.dangdang.ddframe.job.lite.console.domain.RegistryCenterConfiguration;
import com.dangdang.ddframe.job.lite.console.service.RegistryCenterService;
import com.dangdang.ddframe.job.lite.console.service.impl.RegistryCenterServiceImpl;
import com.dangdang.ddframe.job.lite.console.util.SessionRegistryCenterConfiguration;
import com.dangdang.ddframe.job.lite.lifecycle.internal.reg.RegistryCenterFactory;
import com.dangdang.ddframe.job.reg.exception.RegException;
import com.google.common.base.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/registry_center")
public class RegistryCenterRestfulApi {
    
    public static final String REG_CENTER_CONFIG_KEY = "reg_center_config_key";
    
    private RegistryCenterService regCenterService = new RegistryCenterServiceImpl();
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<RegistryCenterConfiguration> load(final @Context HttpServletRequest request) {
        Optional<RegistryCenterConfiguration> regCenterConfig = regCenterService.loadActivated();
        if (regCenterConfig.isPresent()) {
            setRegistryCenterNameToSession(regCenterConfig.get(), request.getSession());
        }
        return regCenterService.loadAll();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public boolean add(final RegistryCenterConfiguration config) {
        return regCenterService.add(config);
    }
    
    @POST
    @Path("/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public void delete(final RegistryCenterConfiguration config) {
        regCenterService.delete(config.getName());
    }
    
    @POST
    @Path("/connect")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Boolean connect(final RegistryCenterConfiguration config, final @Context HttpServletRequest request) {
        return setRegistryCenterNameToSession(regCenterService.load(config.getName()), request.getSession());
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
