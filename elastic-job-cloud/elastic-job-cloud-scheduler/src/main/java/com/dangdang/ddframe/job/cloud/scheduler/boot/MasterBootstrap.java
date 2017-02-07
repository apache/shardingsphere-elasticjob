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

import com.dangdang.ddframe.job.cloud.scheduler.ha.HANode;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.SchedulerService;
import com.dangdang.ddframe.job.cloud.scheduler.boot.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.base.ElectionCandidate;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperElectionService;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.util.concurrent.CountDownLatch;

/**
 * Mesos框架启动器.
 *
 * @author gaohongtao 
 * @author caohao
 */
@Slf4j
public final class MasterBootstrap {
    
    private final CountDownLatch latch = new CountDownLatch(1);
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final ZookeeperElectionService electionService;
    
    public MasterBootstrap() {
        regCenter = getRegistryCenter();
        electionService = new ZookeeperElectionService(
                String.format("%s:%d", BootstrapEnvironment.getInstance().getMesosConfiguration().getHostname(), BootstrapEnvironment.getInstance().getRestfulServerConfiguration().getPort()),
                HANode.ELECTION_NODE, (CuratorFramework) regCenter.getRawClient(), getElectionCandidate());
        Runtime.getRuntime().addShutdownHook(new Thread("stop-hook") {
        
            @Override
            public void run() {
                MasterBootstrap.this.stop();
                latch.countDown();
            }
        });
    }
    
    private CoordinatorRegistryCenter getRegistryCenter() {
        CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(BootstrapEnvironment.getInstance().getZookeeperConfiguration());
        result.init();
        return result;
    }
    
    private ElectionCandidate getElectionCandidate() {
        return new ElectionCandidate() {
            
                private SchedulerService schedulerService;
            
                @Override
                public void startLeadership() throws Exception {
                    try {
                        schedulerService = new SchedulerService(regCenter);
                        schedulerService.start();
                        //CHECKSTYLE:OFF
                    } catch (final Throwable throwable) {
                        //CHECKSTYLE:ON
                        if (throwable instanceof InterruptedException) {
                            throw throwable;
                        }
                        log.error("Elastic job: Starting error", throwable);
                        System.exit(1);
                    }
                }
            
                @Override
                public void stopLeadership() {
                    schedulerService.stop();
                }
            };
    }
    
    /**
     * 开始选举,如果是leader,会启动调度服务.
     */
    public void start() {
        electionService.startLeadership();
        log.info("Elastic job: The framework {} {} leader", BootstrapEnvironment.getInstance().getMesosConfiguration().getHostname(), electionService.isLeader() ? "is" : "is not");
        try {
            latch.await();
        } catch (final InterruptedException ex) {
            log.error("Elastic job: MasterBootstrap start with exception:" + ex);
        }
    }
    
    /**
     * 停止选举及调度服务.
     */
    public void stop() {
        log.info("Elastic job: MasterBootstrap stopped.");
        electionService.close();
    }
}
