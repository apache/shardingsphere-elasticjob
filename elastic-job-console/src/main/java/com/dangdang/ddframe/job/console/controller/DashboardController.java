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

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.dangdang.ddframe.job.console.domain.RegistryCenterClient;
import com.dangdang.ddframe.job.console.service.RegistryCenterService;

@Controller
@RequestMapping("/")
@SessionAttributes(RegistryCenterController.CURATOR_CLIENT_KEY)
public class DashboardController {
    
    @Resource
    private RegistryCenterService registryCenterService;
    
    @RequestMapping(method = RequestMethod.GET)
    public String homepage(final ModelMap model) {
        RegistryCenterClient client = registryCenterService.connectActivated();
        if (!client.isConnected()) {
            return "redirect:registry_center_page";
        }
        model.put(RegistryCenterController.CURATOR_CLIENT_KEY, client);
        return "redirect:overview";
    }
    
    @RequestMapping(value = "registry_center_page", method = RequestMethod.GET)
    public String registryCenterPage(final ModelMap model) {
        model.put("activeTab", 1);
        return "registry_center";
    }
    
    @RequestMapping(value = "job_detail", method = RequestMethod.GET)
    public String jobDetail(@RequestParam final String jobName, final ModelMap model) {
        model.put("jobName", jobName);
        return "job_detail";
    }
    
    @RequestMapping(value = "server_detail", method = RequestMethod.GET)
    public String serverDetail(@RequestParam final String serverIp, final ModelMap model) {
        model.put("serverIp", serverIp);
        return "server_detail";
    }
    
    @RequestMapping(value = "overview", method = RequestMethod.GET)
    public String overview(final ModelMap model) {
        model.put("activeTab", 0);
        return "overview";
    }
    
    @RequestMapping(value = "help", method = RequestMethod.GET)
    public String help(final ModelMap model) {
        model.put("activeTab", 2);
        return "help";
    }
}
