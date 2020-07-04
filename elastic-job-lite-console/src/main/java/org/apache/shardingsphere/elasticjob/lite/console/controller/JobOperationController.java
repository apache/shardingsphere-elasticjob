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
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.ShardingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;
import java.util.Collection;

/**
 * Job operation RESTful API.
 */
@RestController
@RequestMapping("/jobs")
public final class JobOperationController {
    
    private JobAPIService jobAPIService;
    
    @Autowired
    public JobOperationController(final JobAPIService jobAPIService) {
        this.jobAPIService = jobAPIService;
    }
    
    /**
     * Get jobs total count.
     * 
     * @return jobs total count
     */
    @GetMapping("/count")
    public int getJobsTotalCount() {
        return jobAPIService.getJobStatisticsAPI().getJobsTotalCount();
    }
    
    /**
     * Get all jobs brief info.
     * 
     * @return all jobs brief info
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON)
    public Collection<JobBriefInfo> getAllJobsBriefInfo() {
        return jobAPIService.getJobStatisticsAPI().getAllJobsBriefInfo();
    }
    
    /**
     * Trigger job.
     * 
     * @param jobName job name
     */
    @PostMapping("/{jobName}/trigger")
    public void triggerJob(@PathVariable("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().trigger(jobName);
    }
    
    /**
     * Disable job.
     * 
     * @param jobName job name
     */
    @PostMapping(value = "/{jobName}/disable")
    public void disableJob(@PathVariable("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().disable(jobName, null);
    }
    
    /**
     * Enable job.
     *
     * @param jobName job name
     */
    @PostMapping(value = "/{jobName}/enable")
    public void enableJob(@PathVariable("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().enable(jobName, null);
    }
    
    /**
     * Shutdown job.
     * 
     * @param jobName job name
     */
    @PostMapping(value = "/{jobName}/shutdown")
    public void shutdownJob(@PathVariable("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().shutdown(jobName, null);
    }
    
    /**
     * Get sharding info.
     * 
     * @param jobName job name
     * @return sharding info
     */
    @GetMapping(value = "/{jobName}/sharding", produces = MediaType.APPLICATION_JSON)
    public Collection<ShardingInfo> getShardingInfo(@PathVariable("jobName") final String jobName) {
        return jobAPIService.getShardingStatisticsAPI().getShardingInfo(jobName);
    }
    
    /**
     * Disable sharding.
     *
     * @param jobName job name
     * @param item sharding item
     */
    @PostMapping(value = "/{jobName}/sharding/{item}/disable")
    public void disableSharding(@PathVariable("jobName") final String jobName, @PathVariable("item") final String item) {
        jobAPIService.getShardingOperateAPI().disable(jobName, item);
    }
    
    /**
     * Enable sharding.
     *
     * @param jobName job name
     * @param item sharding item
     */
    @PostMapping(value = "/{jobName}/sharding/{item}/enable")
    public void enableSharding(@PathVariable("jobName") final String jobName, @PathVariable("item") final String item) {
        jobAPIService.getShardingOperateAPI().enable(jobName, item);
    }
}
