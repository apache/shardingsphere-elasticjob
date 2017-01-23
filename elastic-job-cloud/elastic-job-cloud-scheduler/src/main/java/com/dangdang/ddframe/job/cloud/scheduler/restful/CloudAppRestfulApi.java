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

package com.dangdang.ddframe.job.cloud.scheduler.restful;

import com.dangdang.ddframe.job.cloud.scheduler.config.app.CloudAppConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.app.CloudAppConfigurationGsonFactory;
import com.dangdang.ddframe.job.cloud.scheduler.config.app.CloudAppConfigurationService;
import com.dangdang.ddframe.job.exception.AppConfigurationException;
import com.dangdang.ddframe.job.exception.JobSystemException;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.util.json.GsonFactory;
import com.google.common.base.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

/**
 * 云作业App的REST API.
 *
 * @author caohao
 */
@Path("/app")
public final class CloudAppRestfulApi {
    
    private static CoordinatorRegistryCenter regCenter;
    
    private final CloudAppConfigurationService configService;
    
    public CloudAppRestfulApi() {
        configService = new CloudAppConfigurationService(regCenter);
    }
    
    /**
     * 初始化.
     *
     * @param regCenter 注册中心
     */
    public static void init(final CoordinatorRegistryCenter regCenter) {
        CloudAppRestfulApi.regCenter = regCenter;
        GsonFactory.registerTypeAdapter(CloudAppConfiguration.class, new CloudAppConfigurationGsonFactory.CloudAppConfigurationGsonTypeAdapter());
    }
    
    /**
     * 注册云作业APP配置.
     * 
     * @param appConfig 云作业APP配置
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void register(final CloudAppConfiguration appConfig) {
        Optional<CloudAppConfiguration> appConfigFromZk = configService.load(appConfig.getAppName());
        if (appConfigFromZk.isPresent()) {
            throw new AppConfigurationException("app '%s' already existed.", appConfig.getAppName());
        }
        configService.add(appConfig);
    }
    
    /**
     * 更新云作业App配置.
     *
     * @param appConfig 云作业App配置
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(final CloudAppConfiguration appConfig) {
        configService.update(appConfig);
    }
    
    /**
     * 查询云作业App配置.
     *
     * @param appName 云作业App配置名称
     * @return 云作业App配置
     */
    @GET
    @Path("/{appName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public CloudAppConfiguration detail(@PathParam("appName") final String appName) {
        Optional<CloudAppConfiguration> config = configService.load(appName);
        if (config.isPresent()) {
            return config.get();
        }
        throw new JobSystemException("Cannot find app '%s', please check the appName.", appName);
    }
    
    /**
     * 查找全部云作业App配置.
     * 
     * @return 全部云作业App配置
     */
    @GET
    @Path("/list")
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<CloudAppConfiguration> findAllApps() {
        return configService.loadAll();
    }
    
    /**
     * 注销云作业App配置.
     *
     * @param appConfig 云作业App配置
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public void deregister(final String appConfig) {
        configService.remove(appConfig);
    }
}
