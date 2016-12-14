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

package com.dangdang.ddframe.job.cloud.scheduler;

import com.dangdang.ddframe.job.cloud.scheduler.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.cloud.scheduler.framework.AbstractFramework;
import com.dangdang.ddframe.job.cloud.scheduler.framework.Frameworks;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.StatisticsScheduledService;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;

import java.util.concurrent.CountDownLatch;

/**
 * 启动入口.
 */
public final class MasterMain {
    
    private final CountDownLatch latch = new CountDownLatch(1);
    
    private final AbstractFramework framework;
    
    private final StatisticsScheduledService statisticsScheduledService;
    
    private MasterMain() {
        framework = Frameworks.newFramework(getRegistryCenter());
        statisticsScheduledService = new StatisticsScheduledService();
        Runtime.getRuntime().addShutdownHook(new Thread("stop-hook") {
        
            @Override
            public void run() {
                framework.stop();
                statisticsScheduledService.stopAsync();
                latch.countDown();
            }
        });
    }
    
    private CoordinatorRegistryCenter getRegistryCenter() {
        CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(BootstrapEnvironment.getInstance().getZookeeperConfiguration());
        result.init();
        return result;
    }
    
    private void run() throws Exception {
        framework.start();
        statisticsScheduledService.startAsync();
        latch.await();
    }
    
    // CHECKSTYLE:OFF
    public static void main(final String[] args) throws Exception {
    // CHECKSTYLE:ON
        new MasterMain().run();
    }
}
