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

package com.dangdang.ddframe.job.lite.console.restful;

import com.dangdang.ddframe.job.lite.console.service.JobAPIService;
import com.dangdang.ddframe.job.lite.console.service.impl.JobAPIServiceImpl;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ExecutionInfo;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobBriefInfo;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobSettings;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ServerInfo;
import com.google.common.base.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/jobs")
public class LiteJobRestfulApi {
    
    private JobAPIService jobAPIService = new JobAPIServiceImpl();
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<JobBriefInfo> getAllJobsBriefInfo() {
        return jobAPIService.getJobStatisticsAPI().getAllJobsBriefInfo();
    }
    
    @GET
    @Path("/config/{jobName}")
    @Produces(MediaType.APPLICATION_JSON)
    public JobSettings getJobSettings(@PathParam("jobName") final String jobName) {
        return jobAPIService.getJobSettingsAPI().getJobSettings(jobName);
    }
    
    @PUT
    @Path("/config")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateJobSettings(final JobSettings jobSettings) {
        jobAPIService.getJobSettingsAPI().updateJobSettings(jobSettings);
    }
    
    @DELETE
    @Path("/config/{jobName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> removeJob(@PathParam("jobName") final String jobName) {
        return jobAPIService.getJobOperatorAPI().remove(Optional.of(jobName), Optional.<String>absent(), Optional.<String>absent());
    }
    
    @POST
    @Path("/{jobName}/trigger")
    public void triggerJob(@PathParam("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().trigger(Optional.of(jobName), Optional.<String>absent(), Optional.<String>absent());
    }
    
    @POST
    @Path("/{jobName}/disable")
    @Consumes(MediaType.APPLICATION_JSON)
    public void disableJob(@PathParam("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().disable(Optional.of(jobName), Optional.<String>absent(), Optional.<String>absent());
    }
    
    @DELETE
    @Path("/{jobName}/disable")
    @Consumes(MediaType.APPLICATION_JSON)
    public void enableJob(@PathParam("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().enable(Optional.of(jobName), Optional.<String>absent(), Optional.<String>absent());
    }
    
    @POST
    @Path("/{jobName}/shutdown")
    @Consumes(MediaType.APPLICATION_JSON)
    public void shutdownJob(@PathParam("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().shutdown(Optional.of(jobName), Optional.<String>absent(), Optional.<String>absent());
    }
    
    @POST
    @Path("/disable")
    @Consumes(MediaType.APPLICATION_JSON)
    public void disableShardingJob(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().disable(Optional.of(jobServer.getJobName()), Optional.of(jobServer.getIp()), Optional.of(jobServer.getInstanceId()));
    }
    
    @DELETE
    @Path("/disable")
    @Consumes(MediaType.APPLICATION_JSON)
    public void enableShardingJob(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().enable(Optional.of(jobServer.getJobName()), Optional.of(jobServer.getIp()), Optional.of(jobServer.getInstanceId()));
    }
    
    @GET
    @Path("/{jobName}/servers")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ServerInfo> getServers(@PathParam("jobName") final String jobName) {
        return jobAPIService.getJobStatisticsAPI().getServers(jobName);
    }
    
    @GET
    @Path("/{jobName}/execution")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ExecutionInfo> getExecutionInfo(@PathParam("jobName") final String jobName) {
        return jobAPIService.getJobStatisticsAPI().getExecutionInfo(jobName);
    }
}
