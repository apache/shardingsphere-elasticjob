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

import com.dangdang.ddframe.job.cloud.scheduler.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.CloudJobConfigurationGsonFactory;
import com.dangdang.ddframe.job.cloud.scheduler.lifecycle.LifecycleService;
import com.dangdang.ddframe.job.cloud.scheduler.producer.TaskProducerSchedulerRegistry;
import com.dangdang.ddframe.json.GsonFactory;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Preconditions;
import org.apache.mesos.SchedulerDriver;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * 作业云的REST API.
 *
 * @author zhangliang
 */
@Path("/job")
public final class CloudJobRestfulApi {
    
    private static SchedulerDriver schedulerDriver;
    
    private static CoordinatorRegistryCenter regCenter;
    
    private final TaskProducerSchedulerRegistry taskProducerSchedulerRegistry;
    
    private final LifecycleService lifecycleService;
    
    public CloudJobRestfulApi() {
        Preconditions.checkNotNull(schedulerDriver);
        Preconditions.checkNotNull(regCenter);
        taskProducerSchedulerRegistry = TaskProducerSchedulerRegistry.getInstance(regCenter);
        lifecycleService = new LifecycleService(schedulerDriver, regCenter);
    }
    
    /**
     * 初始化.
     * 
     * @param schedulerDriver Mesos控制器
     * @param regCenter 注册中心
     */
    public static void init(final SchedulerDriver schedulerDriver, final CoordinatorRegistryCenter regCenter) {
        CloudJobRestfulApi.schedulerDriver = schedulerDriver;
        CloudJobRestfulApi.regCenter = regCenter;
        GsonFactory.registerTypeAdapter(CloudJobConfiguration.class, new CloudJobConfigurationGsonFactory.CloudJobConfigurationGsonTypeAdapter());
    }
    
    /**
     * 注册作业.
     * 
     * @param jobConfig 作业配置
     */
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public void register(final CloudJobConfiguration jobConfig) {
        taskProducerSchedulerRegistry.register(jobConfig);
    }
    
    /**
     * 注销作业.
     * 
     * @param jobName 作业名称
     */
    @DELETE
    @Path("/deregister")
    @Consumes(MediaType.APPLICATION_JSON)
    public void deregister(final String jobName) {
        taskProducerSchedulerRegistry.deregister(jobName);
        lifecycleService.killJob(jobName);
    }
}
