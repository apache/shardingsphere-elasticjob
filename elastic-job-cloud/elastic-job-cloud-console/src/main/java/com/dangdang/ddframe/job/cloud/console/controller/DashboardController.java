/**
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

package com.dangdang.ddframe.job.cloud.console.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class DashboardController {
    
    @RequestMapping(method = RequestMethod.GET)
    public String homepage(final ModelMap model) {
        return "redirect:job_overview";
    }
    
    @RequestMapping(value = "job_overview", method = RequestMethod.GET)
    public String overview(final ModelMap model) {
        return "job_overview";
    }
    
    @RequestMapping(value = "job_exec_detail", method = RequestMethod.GET)
    public String jobExecDetail(final ModelMap model) {
        return "job_exec_detail";
    }
    
    @RequestMapping(value = "job_exec_status", method = RequestMethod.GET)
    public String jobExecStatus(final ModelMap model) {
        return "job_exec_status";
    }
    
    @RequestMapping(value = "add_job", method = RequestMethod.GET)
    public String addJob(final ModelMap model) {
        return "add_job";
    }
    
    @RequestMapping(value = "job_status", method = RequestMethod.GET)
    public String jobStatus(final ModelMap model) {
        return "job_status";
    }
    
    @RequestMapping(value = "modify_job", method = RequestMethod.GET)
    public String modifyJob(final ModelMap model) {
        return "modify_job";
    }
    
    @RequestMapping(value = "job_dashboard", method = RequestMethod.GET)
    public String job_dashboard(final ModelMap model) {
        return "job_dashboard";
    }
}
