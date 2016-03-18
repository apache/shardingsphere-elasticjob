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

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.dangdang.ddframe.job.console.domain.JobServer;
import com.dangdang.ddframe.job.console.service.JobOperationService;

@RestController
@RequestMapping("job")
public class JobOperationController {
    
    @Resource
    private JobOperationService jobOperationService;
    
    @RequestMapping(value = "stop", method = RequestMethod.POST)
    public void stopJob(final JobServer jobServer) {
        jobOperationService.stopJob(jobServer.getJobName(), jobServer.getIp());
    }
    
    @RequestMapping(value = "resume", method = RequestMethod.POST)
    public void resumeJob(final JobServer jobServer) {
        jobOperationService.resumeJob(jobServer.getJobName(), jobServer.getIp());
    }
    
    @RequestMapping(value = "stopAll/name", method = RequestMethod.POST)
    public void stopAllJobsByJobName(final JobServer jobServer) {
        jobOperationService.stopAllJobsByJobName(jobServer.getJobName());
    }
    
    @RequestMapping(value = "resumeAll/name", method = RequestMethod.POST)
    public void resumeAllJobsByJobName(final JobServer jobServer) {
        jobOperationService.resumeAllJobsByJobName(jobServer.getJobName());
    }
    
    @RequestMapping(value = "stopAll/ip", method = RequestMethod.POST)
    public void stopAllJobs(final JobServer jobServer) {
        jobOperationService.stopAllJobsByServer(jobServer.getIp());
    }
    
    @RequestMapping(value = "resumeAll/ip", method = RequestMethod.POST)
    public void resumeAllJobs(final JobServer jobServer) {
        jobOperationService.resumeAllJobsByServer(jobServer.getIp());
    }
    
    @RequestMapping(value = "shutdown", method = RequestMethod.POST)
    public void shutdownJob(final JobServer jobServer) {
        jobOperationService.shutdownJob(jobServer.getJobName(), jobServer.getIp());
    }
}
