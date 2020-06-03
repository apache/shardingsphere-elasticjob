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

package io.elasticjob.lite.console.restful;

import io.elasticjob.lite.console.service.JobAPIService;
import io.elasticjob.lite.console.service.impl.JobAPIServiceImpl;
import io.elasticjob.lite.lifecycle.domain.ShardingInfo;
import io.elasticjob.lite.lifecycle.domain.JobBriefInfo;
import com.google.common.base.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

/**
 * Job operation RESTful API.
 */
@Path("/jobs")
public final class JobOperationRESTfulAPI {
    
    private JobAPIService jobAPIService = new JobAPIServiceImpl();
    
    /**
     * Get jobs total count.
     * 
     * @return jobs total count
     */
    @GET
    @Path("/count")
    public int getJobsTotalCount() {
        return jobAPIService.getJobStatisticsAPI().getJobsTotalCount();
    }
    
    /**
     * Get all jobs brief info.
     * 
     * @return all jobs brief info
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<JobBriefInfo> getAllJobsBriefInfo() {
        return jobAPIService.getJobStatisticsAPI().getAllJobsBriefInfo();
    }
    
    /**
     * Trigger job.
     * 
     * @param jobName job name
     */
    @POST
    @Path("/{jobName}/trigger")
    public void triggerJob(@PathParam("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().trigger(Optional.of(jobName), Optional.<String>absent());
    }
    
    /**
     * Disable job.
     * 
     * @param jobName job name
     */
    @POST
    @Path("/{jobName}/disable")
    @Consumes(MediaType.APPLICATION_JSON)
    public void disableJob(@PathParam("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().disable(Optional.of(jobName), Optional.<String>absent());
    }
    
    /**
     * Enable job.
     *
     * @param jobName job name
     */
    @DELETE
    @Path("/{jobName}/disable")
    @Consumes(MediaType.APPLICATION_JSON)
    public void enableJob(@PathParam("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().enable(Optional.of(jobName), Optional.<String>absent());
    }
    
    /**
     * Shutdown job.
     * 
     * @param jobName job name
     */
    @POST
    @Path("/{jobName}/shutdown")
    @Consumes(MediaType.APPLICATION_JSON)
    public void shutdownJob(@PathParam("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().shutdown(Optional.of(jobName), Optional.<String>absent());
    }
    
    /**
     * Get sharding info.
     * 
     * @param jobName job name
     * @return sharding info
     */
    @GET
    @Path("/{jobName}/sharding")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ShardingInfo> getShardingInfo(@PathParam("jobName") final String jobName) {
        return jobAPIService.getShardingStatisticsAPI().getShardingInfo(jobName);
    }

    /**
     * Disable sharding.
     *
     * @param jobName job name
     * @param item sharding item
     */
    @POST
    @Path("/{jobName}/sharding/{item}/disable")
    @Consumes(MediaType.APPLICATION_JSON)
    public void disableSharding(@PathParam("jobName") final String jobName, @PathParam("item") final String item) {
        jobAPIService.getShardingOperateAPI().disable(jobName, item);
    }

    /**
     * Enable sharding.
     *
     * @param jobName job name
     * @param item sharding item
     */
    @DELETE
    @Path("/{jobName}/sharding/{item}/disable")
    @Consumes(MediaType.APPLICATION_JSON)
    public void enableSharding(@PathParam("jobName") final String jobName, @PathParam("item") final String item) {
        jobAPIService.getShardingOperateAPI().enable(jobName, item);
    }
}
