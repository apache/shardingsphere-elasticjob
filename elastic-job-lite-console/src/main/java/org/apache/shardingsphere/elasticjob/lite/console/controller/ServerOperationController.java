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

import org.apache.shardingsphere.elasticjob.lite.console.service.JobAPIService;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.JobBriefInfo;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.ServerBriefInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;
import java.util.Collection;

/**
 * Server operation RESTful API.
 */
@RestController
@RequestMapping("/servers")
public final class ServerOperationController {
    
    private JobAPIService jobAPIService;
    
    @Autowired
    public ServerOperationController(final JobAPIService jobAPIService) {
        this.jobAPIService = jobAPIService;
    }
    
    /**
     * Get servers total count.
     * 
     * @return servers total count
     */
    @GetMapping("/count")
    public int getServersTotalCount() {
        return jobAPIService.getServerStatisticsAPI().getServersTotalCount();
    }
    
    /**
     * Get all servers brief info.
     * 
     * @return all servers brief info
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON)
    public Collection<ServerBriefInfo> getAllServersBriefInfo() {
        return jobAPIService.getServerStatisticsAPI().getAllServersBriefInfo();
    }
    
    /**
     * Disable server.
     *
     * @param serverIp server IP address
     */
    @PostMapping("/{serverIp}/disable")
    public void disableServer(@PathVariable("serverIp") final String serverIp) {
        jobAPIService.getJobOperatorAPI().disable(null, serverIp);
    }
    
    /**
     * Enable server.
     *
     * @param serverIp server IP address
     */
    @PostMapping("/{serverIp}/enable")
    public void enableServer(@PathVariable("serverIp") final String serverIp) {
        jobAPIService.getJobOperatorAPI().enable(null, serverIp);
    }
    
    /**
     * Shutdown server.
     *
     * @param serverIp server IP address
     */
    @PostMapping("/{serverIp}/shutdown")
    public void shutdownServer(@PathVariable("serverIp") final String serverIp) {
        jobAPIService.getJobOperatorAPI().shutdown(null, serverIp);
    }
    
    /**
     * Remove server.
     *
     * @param serverIp server IP address
     */
    @DeleteMapping("/{serverIp}")
    public void removeServer(@PathVariable("serverIp") final String serverIp) {
        jobAPIService.getJobOperatorAPI().remove(null, serverIp);
    }
    
    /**
     * Get jobs.
     *
     * @param serverIp server IP address
     * @return Job brief info
     */
    @GetMapping(value = "/{serverIp}/jobs", produces = MediaType.APPLICATION_JSON)
    public Collection<JobBriefInfo> getJobs(@PathVariable("serverIp") final String serverIp) {
        return jobAPIService.getJobStatisticsAPI().getJobsBriefInfo(serverIp);
    }
    
    /**
     * Disable server job.
     * 
     * @param serverIp server IP address
     * @param jobName job name
     */
    @PostMapping(value = "/{serverIp}/jobs/{jobName}/disable")
    public void disableServerJob(@PathVariable("serverIp") final String serverIp, @PathVariable("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().disable(jobName, serverIp);
    }
    
    /**
     * Enable server job.
     *
     * @param serverIp server IP address
     * @param jobName job name
     */
    @PostMapping("/{serverIp}/jobs/{jobName}/enable")
    public void enableServerJob(@PathVariable("serverIp") final String serverIp, @PathVariable("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().enable(jobName, serverIp);
    }
    
    /**
     * Shutdown server job.
     *
     * @param serverIp server IP address
     * @param jobName job name
     */
    @PostMapping("/{serverIp}/jobs/{jobName}/shutdown")
    public void shutdownServerJob(@PathVariable("serverIp") final String serverIp, @PathVariable("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().shutdown(jobName, serverIp);
    }
    
    /**
     * Remove server job.
     *
     * @param serverIp server IP address
     * @param jobName job name
     */
    @DeleteMapping("/{serverIp}/jobs/{jobName}")
    public void removeServerJob(@PathVariable("serverIp") final String serverIp, @PathVariable("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().remove(jobName, serverIp);
    }
}
