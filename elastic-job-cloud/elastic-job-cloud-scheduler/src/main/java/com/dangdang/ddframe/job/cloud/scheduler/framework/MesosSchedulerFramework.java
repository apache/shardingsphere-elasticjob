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

package com.dangdang.ddframe.job.cloud.scheduler.framework;

import com.dangdang.ddframe.job.cloud.scheduler.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.cloud.scheduler.env.MesosConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.FrameworkIDHolder;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.SchedulerEngine;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;

/**
 * Mesos调度器框架.
 * 
 * @author gaohongtao
 */
@Slf4j
class MesosSchedulerFramework extends AbstractFramework {
    
    private static final double ONE_WEEK_TIMEOUT = 60 * 60 * 24 * 7;
    
    private final MesosConfiguration mesosConfig;
    
    private final Protos.FrameworkInfo frameworkInfo;
    
    private MesosSchedulerContext context;
    
    private SchedulerDriver schedulerDriver;
    
    MesosSchedulerFramework(final CoordinatorRegistryCenter regCenter) {
        super(regCenter);
        FrameworkIDHolder.setRegCenter(regCenter);
        mesosConfig = BootstrapEnvironment.getInstance().getMesosConfiguration();
        frameworkInfo = FrameworkIDHolder.supply(Protos.FrameworkInfo.newBuilder()).setFailoverTimeout(ONE_WEEK_TIMEOUT)
                .setUser(mesosConfig.getUser()).setName(MesosConfiguration.FRAMEWORK_NAME).setHostname(mesosConfig.getHostname()).build();
    }
    
    @Override
    public void start() throws Exception {
        context = new MesosSchedulerContext(getRegCenter());
        schedulerDriver = new MesosSchedulerDriver(new SchedulerEngine(context), frameworkInfo, mesosConfig.getUrl());
        context.setSchedulerDriver(schedulerDriver);
        Frameworks.invoke("Start driver", new Frameworks.Invokable() {
            @Override
            public void invoke() throws Exception {
                schedulerDriver.start();
            }
        });
        getDelegate().setSchedulerDriver(schedulerDriver);
        context.setDelegate(getDelegate());
    }
    
    @Override
    public void stop() {
        getDelegate().stop();
        Frameworks.safetyInvoke("Stop driver", new Frameworks.Invokable() {
            @Override
            public void invoke() throws Exception {
                schedulerDriver.stop(true);
            }
        });
        context.close();
    }
}
