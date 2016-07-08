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

package com.dangdang.ddframe.job.cloud;

import com.dangdang.ddframe.job.cloud.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.cloud.env.MesosConfiguration;
import com.dangdang.ddframe.job.cloud.mesos.SchedulerEngine;
import com.dangdang.ddframe.job.cloud.rest.RestfulServer;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;

/**
 * 作业云启动入口.
 */
public final class MasterBootstrap {
    
    // CHECKSTYLE:OFF
    public static void main(final String[] args) throws Exception {
    // CHECKSTYLE:ON
        BootstrapEnvironment env = new BootstrapEnvironment();
        CoordinatorRegistryCenter regCenter = getRegistryCenter(env);
        MesosSchedulerDriver schedulerDriver = getSchedulerDriver(env, regCenter);
        RestfulServer restfulServer = new RestfulServer(env.getRestfulServerConfiguration().getPort(), regCenter);
        restfulServer.start();
        Protos.Status status = schedulerDriver.run();
        schedulerDriver.stop();
        restfulServer.stop();
        System.exit(Protos.Status.DRIVER_STOPPED == status ? 0 : -1);
    }
    
    private static CoordinatorRegistryCenter getRegistryCenter(final BootstrapEnvironment env) {
        CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(env.getZookeeperConfiguration());
        result.init();
        return result;
    }
    
    private static MesosSchedulerDriver getSchedulerDriver(final BootstrapEnvironment env, final CoordinatorRegistryCenter regCenter) {
        MesosConfiguration mesosConfig = env.getMesosConfiguration();
        Protos.FrameworkInfo frameworkInfo = Protos.FrameworkInfo.newBuilder().setUser(mesosConfig.getUsername()).setName(MesosConfiguration.FRAMEWORK_NAME).build();
        return new MesosSchedulerDriver(new SchedulerEngine(regCenter), frameworkInfo, mesosConfig.getUrl());
    }
}
