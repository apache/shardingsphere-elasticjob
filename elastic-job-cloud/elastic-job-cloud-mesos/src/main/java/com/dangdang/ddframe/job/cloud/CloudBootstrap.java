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

import com.dangdang.ddframe.job.cloud.mesos.SchedulerEngine;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 作业云启动入口.
 */
public final class CloudBootstrap {
    
    // CHECKSTYLE:OFF
    public static void main(final String[] args) throws IOException {
    // CHECKSTYLE:ON
        Properties properties = new Properties();
        properties.load(new FileInputStream("conf/server.properties"));
        String zKServers = properties.getProperty("zkServers", "localhost:2181");
        String zKNameSpace = properties.getProperty("zKNameSpace", "elastic-job-cloud");
        String userName = properties.getProperty("userName", "");
        String frameWorkName = properties.getProperty("frameWorkName", "Elastic-Job-Cloud");
        String mesosUrl = properties.getProperty("mesosUrl", "zk://localhost:2181/mesos");
        
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(zKServers, zKNameSpace);
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);
        regCenter.init();
        Protos.FrameworkInfo frameworkInfo = Protos.FrameworkInfo.newBuilder().setUser(userName).setName(frameWorkName).build();
        MesosSchedulerDriver schedulerDriver = new MesosSchedulerDriver(new SchedulerEngine(regCenter), frameworkInfo, mesosUrl);
        Protos.Status status = schedulerDriver.run();
        schedulerDriver.stop();
        System.exit(Protos.Status.DRIVER_STOPPED == status ? 0 : -1);
    }
}
