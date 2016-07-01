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

public class Bootstrap {
    
    // -Djava.library.path=/usr/local/lib
    // CHECKSTYLE:OFF
    public static void main(final String[] args) {
    // CHECKSTYLE:ON
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration("localhost:2181", "elastic-job-cloud");
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);
        regCenter.init();
        Protos.FrameworkInfo frameworkInfo = Protos.FrameworkInfo.newBuilder().setUser("").setName("Elastic-Job-Cloud").build();
        MesosSchedulerDriver schedulerDriver = new MesosSchedulerDriver(new SchedulerEngine(regCenter), frameworkInfo, "zk://localhost:2181/mesos");
        Protos.Status status = schedulerDriver.run();
        schedulerDriver.stop();
        System.exit(Protos.Status.DRIVER_STOPPED == status ? 0 : -1);
    }
}
