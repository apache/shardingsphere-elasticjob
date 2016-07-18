/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.rest;

import com.dangdang.ddframe.job.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.producer.TaskProducerSchedulerRegistry;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Preconditions;

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
public final class RestfulApi {
    
    private static CoordinatorRegistryCenter regCenter;
    
    private final TaskProducerSchedulerRegistry taskProducerSchedulerRegistry;
    
    public RestfulApi() {
        Preconditions.checkNotNull(regCenter);
        taskProducerSchedulerRegistry = TaskProducerSchedulerRegistry.getInstance(regCenter);
    }
    
    /**
     * 初始化.
     * 
     * @param regCenter 注册中心
     */
    public static void init(final CoordinatorRegistryCenter regCenter) {
        RestfulApi.regCenter = regCenter;
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
    }
}
