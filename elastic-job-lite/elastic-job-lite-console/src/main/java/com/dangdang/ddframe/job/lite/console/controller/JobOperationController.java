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

package com.dangdang.ddframe.job.lite.console.controller;

import com.dangdang.ddframe.job.lite.console.service.JobAPIService;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ServerInfo;
import com.google.common.base.Optional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collection;

@RestController
@RequestMapping("job")
public class JobOperationController {
    
    @Resource
    private JobAPIService jobAPIService;
    
    @RequestMapping(value = "trigger", method = RequestMethod.POST)
    public void triggerJob(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().trigger(Optional.of(jobServer.getJobName()), Optional.of(jobServer.getIp()));
    }
    
    @RequestMapping(value = "pause", method = RequestMethod.POST)
    public void pauseJob(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().pause(Optional.of(jobServer.getJobName()), Optional.of(jobServer.getIp()));
    }
    
    @RequestMapping(value = "resume", method = RequestMethod.POST)
    public void resumeJob(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().resume(Optional.of(jobServer.getJobName()), Optional.of(jobServer.getIp()));
    }
    
    @RequestMapping(value = "triggerAll/name", method = RequestMethod.POST)
    public void triggerAllJobsByJobName(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().trigger(Optional.of(jobServer.getJobName()), Optional.<String>absent());
    }
    
    @RequestMapping(value = "pauseAll/name", method = RequestMethod.POST)
    public void pauseAllJobsByJobName(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().pause(Optional.of(jobServer.getJobName()), Optional.<String>absent());
    }
    
    @RequestMapping(value = "resumeAll/name", method = RequestMethod.POST)
    public void resumeAllJobsByJobName(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().resume(Optional.of(jobServer.getJobName()), Optional.<String>absent());
    }
    
    @RequestMapping(value = "triggerAll/ip", method = RequestMethod.POST)
    public void triggerAllJobs(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().trigger(Optional.<String>absent(), Optional.of(jobServer.getIp()));
    }
    
    @RequestMapping(value = "pauseAll/ip", method = RequestMethod.POST)
    public void pauseAllJobs(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().pause(Optional.<String>absent(), Optional.of(jobServer.getIp()));
    }
    
    @RequestMapping(value = "resumeAll/ip", method = RequestMethod.POST)
    public void resumeAllJobs(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().resume(Optional.<String>absent(), Optional.of(jobServer.getIp()));
    }
    
    @RequestMapping(value = "shutdown", method = RequestMethod.POST)
    public void shutdownJob(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().shutdown(Optional.of(jobServer.getJobName()), Optional.of(jobServer.getIp()));
    }
    
    @RequestMapping(value = "remove", method = RequestMethod.POST)
    public Collection<String> removeJob(final ServerInfo jobServer) {
        return jobAPIService.getJobOperatorAPI().remove(Optional.of(jobServer.getJobName()), Optional.of(jobServer.getIp()));
    }
    
    @RequestMapping(value = "disable", method = RequestMethod.POST)
    public void disableJob(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().disable(Optional.of(jobServer.getJobName()), Optional.of(jobServer.getIp()));
    }
    
    @RequestMapping(value = "enable", method = RequestMethod.POST)
    public void enableJob(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().enable(Optional.of(jobServer.getJobName()), Optional.of(jobServer.getIp()));
    }
}
