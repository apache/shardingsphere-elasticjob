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

import com.dangdang.ddframe.job.console.domain.JobServer;
import com.dangdang.ddframe.job.console.domain.ServerBriefInfo;
import com.dangdang.ddframe.job.console.service.ServerDimensionService;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collection;

@RestController
@RequestMapping("server")
public class ServerController {
    
    @Resource
    private ServerDimensionService serverDimensionService;
    
    @RequestMapping(value = "servers", method = RequestMethod.GET)
    public Collection<ServerBriefInfo> getAllServersBriefInfo() {
        return serverDimensionService.getAllServersBriefInfo();
    }
    
    @RequestMapping(value = "jobs", method = RequestMethod.GET)
    public Collection<JobServer> getJobs(final JobServer jobServer, final ModelMap model) {
        model.put("serverIp", jobServer.getIp());
        return serverDimensionService.getJobs(jobServer.getIp());
    }
}
