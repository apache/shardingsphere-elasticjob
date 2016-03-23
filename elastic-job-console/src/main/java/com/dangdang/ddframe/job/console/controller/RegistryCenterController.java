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

package com.dangdang.ddframe.job.console.controller;

import java.util.Collection;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.dangdang.ddframe.job.console.domain.RegistryCenterClient;
import com.dangdang.ddframe.job.console.domain.RegistryCenterConfiguration;
import com.dangdang.ddframe.job.console.service.RegistryCenterService;

@RestController
@RequestMapping("registry_center")
public class RegistryCenterController {
    
    public static final String CURATOR_CLIENT_KEY = "curator_client_key";
    
    @Resource
    private RegistryCenterService registryCenterService;
    
    @RequestMapping(method = RequestMethod.GET)
    public Collection<RegistryCenterConfiguration> load(final HttpSession session) {
        setClientToSession(registryCenterService.connectActivated(), session);
        return registryCenterService.loadAll();
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public boolean add(final RegistryCenterConfiguration config) {
        return registryCenterService.add(config);
    }
    
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public void delete(final RegistryCenterConfiguration config) {
        registryCenterService.delete(config.getName());
    }
    
    @RequestMapping(value = "connect", method = RequestMethod.POST)
    public boolean connect(final RegistryCenterConfiguration config, final HttpSession session) {
        return setClientToSession(registryCenterService.connect(config.getName()), session);
    }
    
    private boolean setClientToSession(final RegistryCenterClient client, final HttpSession session) {
        boolean result = client.isConnected();
        if (result) {
            session.setAttribute(CURATOR_CLIENT_KEY, client);
        }
        return result;
    }
}
