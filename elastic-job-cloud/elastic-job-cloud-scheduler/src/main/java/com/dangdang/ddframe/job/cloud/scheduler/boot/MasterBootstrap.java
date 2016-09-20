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

package com.dangdang.ddframe.job.cloud.scheduler.boot;

import com.dangdang.ddframe.job.cloud.scheduler.boot.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.cloud.scheduler.boot.env.MesosConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.CloudJobConfigurationListener;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.SchedulerEngine;
import com.dangdang.ddframe.job.cloud.scheduler.restful.CloudJobRestfulApi;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;
import com.dangdang.ddframe.restful.RestfulServer;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;

import java.io.IOException;

/**
 * Mesos框架启动器.
 *
 * @author zhangliang
 */
public final class MasterBootstrap {
    
    private final BootstrapEnvironment env;
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final SchedulerDriver schedulerDriver;
    
    private final RestfulServer restfulServer;
    
    public MasterBootstrap() throws IOException {
        env = new BootstrapEnvironment();
        regCenter = getRegistryCenter();
        schedulerDriver = getSchedulerDriver();
        restfulServer = new RestfulServer(env.getRestfulServerConfiguration().getPort());
        CloudJobRestfulApi.init(schedulerDriver, regCenter);
        initListener();
    }
    
    private CoordinatorRegistryCenter getRegistryCenter() {
        CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(env.getZookeeperConfiguration());
        result.init();
        return result;
    }
    
    private SchedulerDriver getSchedulerDriver() {
        MesosConfiguration mesosConfig = env.getMesosConfiguration();
        Protos.FrameworkInfo frameworkInfo = 
                Protos.FrameworkInfo.newBuilder().setUser(mesosConfig.getUser()).setName(MesosConfiguration.FRAMEWORK_NAME).setHostname(mesosConfig.getHostname()).build();
        return new MesosSchedulerDriver(new SchedulerEngine(regCenter), frameworkInfo, mesosConfig.getUrl());
    }
    
    private void initListener() {
        regCenter.addCacheData("/");
        ((TreeCache) regCenter.getRawCache("/")).getListenable().addListener(new CloudJobConfigurationListener(regCenter, schedulerDriver));
    }
    
    /**
     * 以守护进程方式运行Elastic-Job-Cloud的Mesos框架.
     * 
     * @return 框架运行状态
     * @throws Exception 运行时异常
     */
    public Protos.Status runAsDaemon() throws Exception {
        restfulServer.start(CloudJobRestfulApi.class.getPackage().getName());
        return schedulerDriver.run();
    }
    
    /**
     * 停止运行Elastic-Job-Cloud的Mesos框架.
     * 
     * @param status 框架运行状态
     * @return 是否正常停止
     * @throws Exception 运行时异常
     */
    public boolean stop(final Protos.Status status) throws Exception {
        schedulerDriver.stop();
        restfulServer.stop();
        return Protos.Status.DRIVER_STOPPED == status;
    }
}
