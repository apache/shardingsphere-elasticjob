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

package com.dangdang.ddframe.job.console.controller;

import java.util.Collection;

import javax.annotation.Resource;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.dangdang.ddframe.job.console.domain.ExecutionInfo;
import com.dangdang.ddframe.job.console.domain.JobBriefInfo;
import com.dangdang.ddframe.job.console.domain.JobServer;
import com.dangdang.ddframe.job.console.domain.JobSettings;
import com.dangdang.ddframe.job.console.service.JobDimensionService;
import com.dangdang.ddframe.job.console.service.JobOperationService;

@RestController
@RequestMapping("job")
public class JobController {
    
    @Resource
    private JobDimensionService jobDimensionService;
    
    @Resource
    private JobOperationService jobOperationService;
    
    @RequestMapping(value = "jobs", method = RequestMethod.GET)
    public Collection<JobBriefInfo> getAllJobsBriefInfo() {
        return jobDimensionService.getAllJobsBriefInfo();
    }
    
    @RequestMapping(value = "settings", method = RequestMethod.GET)
    public JobSettings getJobSettings(final JobSettings jobSettings, final ModelMap model) {
        model.put("jobName", jobSettings.getJobName());
        return jobDimensionService.getJobSettings(jobSettings.getJobName());
    }
    
    @RequestMapping(value = "settings", method = RequestMethod.POST)
    public void updateJobSettings(final JobSettings jobSettings) {
        jobDimensionService.updateJobSettings(jobSettings);
    }
    
    @RequestMapping(value = "servers", method = RequestMethod.GET)
    public Collection<JobServer> getServers(final JobServer jobServer) {
        return jobDimensionService.getServers(jobServer.getJobName());
    }
    
    @RequestMapping(value = "execution", method = RequestMethod.GET)
    public Collection<ExecutionInfo> getExecutionInfo(final JobSettings config) {
        return jobDimensionService.getExecutionInfo(config.getJobName());
    }
}
