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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/job")
public class LiteJobRestfulApi {
    
    private JobAPIService jobAPIService = new JobAPIServiceImpl();
    
    @GET
    @Path("/jobs")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<JobBriefInfo> getAllJobsBriefInfo() {
        return jobAPIService.getJobStatisticsAPI().getAllJobsBriefInfo();
    }
    
    @GET
    @Path("/settings/{jobName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public JobSettings getJobSettings(@PathParam("jobName") final String jobName) {
        return jobAPIService.getJobSettingsAPI().getJobSettings(jobName);
    }
    
    @POST
    @Path("/settings")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateJobSettings(final JobSettings jobSettings) {
        jobAPIService.getJobSettingsAPI().updateJobSettings(jobSettings);
    }
    
    @GET
    @Path("/servers")
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<ServerInfo> getServers(final @QueryParam("jobServer") String jobName) {
        return jobAPIService.getJobStatisticsAPI().getServers(jobName);
    }
    
    @GET
    @Path("/execution")
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<ExecutionInfo> getExecutionInfo(final @QueryParam("jobSettings") String jobName) {
        return jobAPIService.getJobStatisticsAPI().getExecutionInfo(jobName);
    }
    
    @POST
    @Path("/trigger")
    @Consumes(MediaType.APPLICATION_JSON)
    public void triggerJob(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().trigger(Optional.of(jobServer.getJobName()), Optional.of(jobServer.getIp()));
    }
    
    @POST
    @Path("/pause")
    @Consumes(MediaType.APPLICATION_JSON)
    public void pauseJob(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().pause(Optional.of(jobServer.getJobName()), Optional.of(jobServer.getIp()));
    }
    
    @POST
    @Path("/resume")
    @Consumes(MediaType.APPLICATION_JSON)
    public void resumeJob(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().resume(Optional.of(jobServer.getJobName()), Optional.of(jobServer.getIp()));
    }
    
    @POST
    @Path("/triggerAll/name")
    @Consumes(MediaType.APPLICATION_JSON)
    public void triggerAllJobsByJobName(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().trigger(Optional.of(jobServer.getJobName()), Optional.<String>absent());
    }
    
    @POST
    @Path("/pauseAll/name")
    @Consumes(MediaType.APPLICATION_JSON)
    public void pauseAllJobsByJobName(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().pause(Optional.of(jobServer.getJobName()), Optional.<String>absent());
    }
    
    @POST
    @Path("/resumeAll/name")
    @Consumes(MediaType.APPLICATION_JSON)
    public void resumeAllJobsByJobName(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().resume(Optional.of(jobServer.getJobName()), Optional.<String>absent());
    }
    
    @POST
    @Path("/triggerAll/ip")
    @Consumes(MediaType.APPLICATION_JSON)
    public void triggerAllJobs(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().trigger(Optional.<String>absent(), Optional.of(jobServer.getIp()));
    }
    
    @POST
    @Path("/pauseAll/ip")
    @Consumes(MediaType.APPLICATION_JSON)
    public void pauseAllJobs(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().pause(Optional.<String>absent(), Optional.of(jobServer.getIp()));
    }
    
    @POST
    @Path("/resumeAll/ip")
    @Consumes(MediaType.APPLICATION_JSON)
    public void resumeAllJobs(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().resume(Optional.<String>absent(), Optional.of(jobServer.getIp()));
    }
    
    @POST
    @Path("/shutdown")
    @Consumes(MediaType.APPLICATION_JSON)
    public void shutdownJob(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().shutdown(Optional.of(jobServer.getJobName()), Optional.of(jobServer.getIp()));
    }
    
    @POST
    @Path("/remove")
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<String> removeJob(final ServerInfo jobServer) {
        return jobAPIService.getJobOperatorAPI().remove(Optional.of(jobServer.getJobName()), Optional.of(jobServer.getIp()));
    }
    
    @POST
    @Path("/disable")
    @Consumes(MediaType.APPLICATION_JSON)
    public void disableJob(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().disable(Optional.of(jobServer.getJobName()), Optional.of(jobServer.getIp()));
    }
    
    @POST
    @Path("/enable")
    @Consumes(MediaType.APPLICATION_JSON)
    public void enableJob(final ServerInfo jobServer) {
        jobAPIService.getJobOperatorAPI().enable(Optional.of(jobServer.getJobName()), Optional.of(jobServer.getIp()));
    }
}
