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

package org.apache.shardingsphere.elasticjob.lite.console.restful;

import org.apache.shardingsphere.elasticjob.lite.console.service.JobAPIService;
import org.apache.shardingsphere.elasticjob.lite.console.service.impl.JobAPIServiceImpl;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.JobBriefInfo;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.ServerBriefInfo;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

/**
 * Server operation RESTful API.
 */
@Path("/servers")
public final class ServerOperationRESTfulAPI {
    
    private JobAPIService jobAPIService = new JobAPIServiceImpl();
    
    /**
     * Get servers total count.
     * 
     * @return servers total count
     */
    @GET
    @Path("/count")
    public int getServersTotalCount() {
        return jobAPIService.getServerStatisticsAPI().getServersTotalCount();
    }
    
    /**
     * Get all servers brief info.
     * 
     * @return all servers brief info
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ServerBriefInfo> getAllServersBriefInfo() {
        return jobAPIService.getServerStatisticsAPI().getAllServersBriefInfo();
    }
    
    /**
     * Disable server.
     *
     * @param serverIp server IP address
     */
    @POST
    @Path("/{serverIp}/disable")
    public void disableServer(@PathParam("serverIp") final String serverIp) {
        jobAPIService.getJobOperatorAPI().disable(null, serverIp);
    }
    
    /**
     * Enable server.
     *
     * @param serverIp server IP address
     */
    @DELETE
    @Path("/{serverIp}/disable")
    public void enableServer(@PathParam("serverIp") final String serverIp) {
        jobAPIService.getJobOperatorAPI().enable(null, serverIp);
    }
    
    /**
     * Shutdown server.
     *
     * @param serverIp server IP address
     */
    @POST
    @Path("/{serverIp}/shutdown")
    public void shutdownServer(@PathParam("serverIp") final String serverIp) {
        jobAPIService.getJobOperatorAPI().shutdown(null, serverIp);
    }
    
    /**
     * Remove server.
     *
     * @param serverIp server IP address
     */
    @DELETE
    @Path("/{serverIp}")
    public void removeServer(@PathParam("serverIp") final String serverIp) {
        jobAPIService.getJobOperatorAPI().remove(null, serverIp);
    }
    
    /**
     * Get jobs.
     *
     * @param serverIp server IP address
     * @return Job brief info
     */
    @GET
    @Path("/{serverIp}/jobs")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<JobBriefInfo> getJobs(@PathParam("serverIp") final String serverIp) {
        return jobAPIService.getJobStatisticsAPI().getJobsBriefInfo(serverIp);
    }
    
    /**
     * Disable server job.
     * 
     * @param serverIp server IP address
     * @param jobName job name
     */
    @POST
    @Path("/{serverIp}/jobs/{jobName}/disable")
    public void disableServerJob(@PathParam("serverIp") final String serverIp, @PathParam("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().disable(jobName, serverIp);
    }
    
    /**
     * Enable server job.
     *
     * @param serverIp server IP address
     * @param jobName job name
     */
    @DELETE
    @Path("/{serverIp}/jobs/{jobName}/disable")
    public void enableServerJob(@PathParam("serverIp") final String serverIp, @PathParam("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().enable(jobName, serverIp);
    }
    
    /**
     * Shutdown server job.
     *
     * @param serverIp server IP address
     * @param jobName job name
     */
    @POST
    @Path("/{serverIp}/jobs/{jobName}/shutdown")
    public void shutdownServerJob(@PathParam("serverIp") final String serverIp, @PathParam("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().shutdown(jobName, serverIp);
    }
    
    /**
     * Remove server job.
     *
     * @param serverIp server IP address
     * @param jobName job name
     */
    @DELETE
    @Path("/{serverIp}/jobs/{jobName}")
    public void removeServerJob(@PathParam("serverIp") final String serverIp, @PathParam("jobName") final String jobName) {
        jobAPIService.getJobOperatorAPI().remove(jobName, serverIp);
    }
}
