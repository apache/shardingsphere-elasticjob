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
import com.dangdang.ddframe.job.cloud.scheduler.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.config.JobExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.lifecycle.LifecycleService;
import com.dangdang.ddframe.job.cloud.scheduler.producer.ProducerManager;
import com.dangdang.ddframe.job.cloud.scheduler.producer.ProducerManagerFactory;
import com.dangdang.ddframe.job.cloud.scheduler.state.ready.ReadyService;
import com.dangdang.ddframe.job.exception.JobSystemException;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.util.json.GsonFactory;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.SchedulerDriver;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * 作业云的REST API.
 *
 * @author zhangliang
 */
@Path("/job")
@Slf4j
public final class CloudJobRestfulApi {
    
    private static SchedulerDriver schedulerDriver;
    
    private static CoordinatorRegistryCenter regCenter;
    
    private static ProducerManager producerManager;
    
    private final LifecycleService lifecycleService;
    
    private final ConfigurationService configService;
    
    private final ReadyService readyService;
    
    public CloudJobRestfulApi() {
        Preconditions.checkNotNull(schedulerDriver);
        Preconditions.checkNotNull(regCenter);
        lifecycleService = new LifecycleService(schedulerDriver);
        configService = new ConfigurationService(regCenter);
        readyService = new ReadyService(regCenter);
    }
    
    /**
     * 初始化.
     * 
     * @param regCenter 注册中心
     */
    public static void init(final CoordinatorRegistryCenter regCenter) {
        CloudJobRestfulApi.regCenter = regCenter;
        GsonFactory.registerTypeAdapter(CloudJobConfiguration.class, new CloudJobConfigurationGsonFactory.CloudJobConfigurationGsonTypeAdapter());
    }
    
    /**
     * 启动服务.
     * 
     * @param schedulerDriver Mesos控制器
     */
    public static void start(final SchedulerDriver schedulerDriver) {
        log.info("Elastic Job: Start REST Api");
        CloudJobRestfulApi.schedulerDriver = schedulerDriver;
        producerManager = ProducerManagerFactory.getInstance(schedulerDriver, regCenter);
        producerManager.startup();
    }
    
    /**
     * 停止服务.
     */
    public static void stop() {
        log.info("Elastic Job: Stop REST Api");
        producerManager.shutdown();
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
        producerManager.register(jobConfig);
    }
    
    /**
     * 更新作业配置.
     *
     * @param jobConfig 作业配置
     */
    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(final CloudJobConfiguration jobConfig) {
        producerManager.update(jobConfig);
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
        producerManager.deregister(jobName);
        lifecycleService.killJob(jobName);
    }
    
    /**
     * 触发一次作业.
     *
     * @param jobName 作业名称
     */
    @POST
    @Path("/trigger")
    @Consumes(MediaType.APPLICATION_JSON)
    public void trigger(final String jobName) {
        Optional<CloudJobConfiguration> config = configService.load(jobName);
        if (config.isPresent() && JobExecutionType.DAEMON == config.get().getJobExecutionType()) {
            throw new JobSystemException("Daemon job '%s' cannot support trigger.", jobName);
        }
        readyService.addTransient(jobName);
    }
}
