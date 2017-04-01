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
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobBriefInfo;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ServerBriefInfo;
import com.google.common.base.Optional;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/servers")
public class ServerOperationRestfulApi {
    
    private JobAPIService jobAPIService = new JobAPIServiceImpl();
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ServerBriefInfo> getAllServersBriefInfo() {
        return jobAPIService.getServerStatisticsAPI().getAllServersBriefInfo();
    }
    
    @POST
    @Path("/{serverIp}/disable")
    public void disableServer(@PathParam("serverIp") final String serverIp) {
        jobAPIService.getJobOperatorAPI().disable(Optional.<String>absent(), Optional.of(serverIp));
    }
    
    @DELETE
    @Path("/{serverIp}/disable")
    public void enableServer(@PathParam("serverIp") final String serverIp) {
        jobAPIService.getJobOperatorAPI().enable(Optional.<String>absent(), Optional.of(serverIp));
    }
    
    @POST
    @Path("/{serverIp}/shutdown")
    public void shutdownServer(@PathParam("serverIp") final String serverIp) {
        jobAPIService.getJobOperatorAPI().shutdown(Optional.<String>absent(), Optional.of(serverIp));
    }
    
    @DELETE
    @Path("/{serverIp}")
    public void removeServer(@PathParam("serverIp") final String serverIp) {
        jobAPIService.getJobOperatorAPI().remove(Optional.<String>absent(), Optional.of(serverIp));
    }
    
    @GET
    @Path("/{serverIp}/jobs")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<JobBriefInfo> getJobs(@PathParam("serverIp") final String serverIp) {
        return jobAPIService.getJobStatisticsAPI().getJobsBriefInfo(serverIp);
    }
    
    @POST
    @Path("/{serverIp}/jobs/{jobName}/disable")
    public void disableServerJob(@PathParam("serverIp") final String serverIp, @PathParam("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().disable(Optional.of(jobName), Optional.of(serverIp));
    }
    
    @DELETE
    @Path("/{serverIp}/jobs/{jobName}/disable")
    public void enableServerJob(@PathParam("serverIp") final String serverIp, @PathParam("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().enable(Optional.of(jobName), Optional.of(serverIp));
    }
    
    @POST
    @Path("/{serverIp}/jobs/{jobName}/shutdown")
    public void shutdownServerJob(@PathParam("serverIp") final String serverIp, @PathParam("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().shutdown(Optional.of(jobName), Optional.of(serverIp));
    }
}
