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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.restful;

import org.apache.mesos.Protos.ExecutorID;
import org.apache.mesos.Protos.SlaveID;
import org.apache.shardingsphere.elasticjob.cloud.config.pojo.CloudJobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.cloud.exception.AppConfigurationException;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.CloudAppConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.pojo.CloudAppConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.MesosStateService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.MesosStateService.ExecutorStateInfo;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.producer.ProducerManager;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.app.DisableAppService;
import org.apache.shardingsphere.elasticjob.infra.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.codehaus.jettison.json.JSONException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Optional;

/**
 * Cloud app restful api.
 */
@Path("/app")
public final class CloudAppRestfulApi {
    
    private static CoordinatorRegistryCenter regCenter;
    
    private static ProducerManager producerManager;
    
    private final CloudAppConfigurationService appConfigService;
    
    private final CloudJobConfigurationService jobConfigService;
    
    private final DisableAppService disableAppService;
    
    private final MesosStateService mesosStateService;
    
    public CloudAppRestfulApi() {
        appConfigService = new CloudAppConfigurationService(regCenter);
        jobConfigService = new CloudJobConfigurationService(regCenter);
        mesosStateService = new MesosStateService(regCenter);
        disableAppService = new DisableAppService(regCenter);
    }
    
    /**
     * Init.
     *
     * @param producerManager producer manager
     * @param regCenter registry center
     */
    public static void init(final CoordinatorRegistryCenter regCenter, final ProducerManager producerManager) {
        CloudAppRestfulApi.regCenter = regCenter;
        CloudAppRestfulApi.producerManager = producerManager;
    }
    
    /**
     * Register app config.
     * 
     * @param appConfig cloud app config
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void register(final CloudAppConfigurationPOJO appConfig) {
        Optional<CloudAppConfigurationPOJO> appConfigFromZk = appConfigService.load(appConfig.getAppName());
        if (appConfigFromZk.isPresent()) {
            throw new AppConfigurationException("app '%s' already existed.", appConfig.getAppName());
        }
        appConfigService.add(appConfig);
    }
    
    /**
     * Update app config.
     *
     * @param appConfig cloud app config
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(final CloudAppConfigurationPOJO appConfig) {
        appConfigService.update(appConfig);
    }
    
    /**
     * Query app config.
     *
     * @param appName app name
     * @return cloud app config
     */
    @GET
    @Path("/{appName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response detail(@PathParam("appName") final String appName) {
        Optional<CloudAppConfigurationPOJO> appConfig = appConfigService.load(appName);
        if (!appConfig.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(appConfig.get()).build();
    }
    
    /**
     * Find all registered app configs.
     * 
     * @return collection of registered app configs
     */
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<CloudAppConfigurationPOJO> findAllApps() {
        return appConfigService.loadAll();
    }
    
    /**
     * Query the app is disabled or not.
     * 
     * @param appName app name
     * @return true is disabled, otherwise not
     */
    @GET
    @Path("/{appName}/disable")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean isDisabled(@PathParam("appName") final String appName) {
        return disableAppService.isDisabled(appName);
    }
    
    /**
     * Disable app config.
     *
     * @param appName app name
     */
    @POST
    @Path("/{appName}/disable")
    public void disable(@PathParam("appName") final String appName) {
        if (appConfigService.load(appName).isPresent()) {
            disableAppService.add(appName);
            for (CloudJobConfigurationPOJO each : jobConfigService.loadAll()) {
                if (appName.equals(each.getAppName())) {
                    producerManager.unschedule(each.getJobName());
                }
            }
        }
    }
    
    /**
     * Enable app.
     * 
     * @param appName app name
     */
    @POST
    @Path("/{appName}/enable")
    public void enable(@PathParam("appName") final String appName) {
        if (appConfigService.load(appName).isPresent()) {
            disableAppService.remove(appName);
            for (CloudJobConfigurationPOJO each : jobConfigService.loadAll()) {
                if (appName.equals(each.getAppName())) {
                    producerManager.reschedule(each.getJobName());
                }
            }
        }
    }
    
    /**
     * Deregister app.
     *
     * @param appName app name
     */
    @DELETE
    @Path("/{appName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void deregister(@PathParam("appName") final String appName) {
        if (appConfigService.load(appName).isPresent()) {
            removeAppAndJobConfigurations(appName);
            stopExecutors(appName);
        }
    }
    
    private void removeAppAndJobConfigurations(final String appName) {
        for (CloudJobConfigurationPOJO each : jobConfigService.loadAll()) {
            if (appName.equals(each.getAppName())) {
                producerManager.deregister(each.getJobName());
            }
        }
        disableAppService.remove(appName);
        appConfigService.remove(appName);
    }
    
    private void stopExecutors(final String appName) {
        try {
            Collection<ExecutorStateInfo> executorBriefInfo = mesosStateService.executors(appName);
            for (ExecutorStateInfo each : executorBriefInfo) {
                producerManager.sendFrameworkMessage(ExecutorID.newBuilder().setValue(each.getId()).build(),
                        SlaveID.newBuilder().setValue(each.getSlaveId()).build(), "STOP".getBytes());
            }
        } catch (final JSONException ex) {
            throw new JobSystemException(ex);
        }
    }
}
