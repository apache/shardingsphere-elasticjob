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
import com.dangdang.ddframe.job.cloud.scheduler.ha.HANode;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.SchedulerService;
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
 * @author caohao
 */
@Slf4j
public final class Bootstrap {
    
    private final CountDownLatch latch = new CountDownLatch(1);
    
    private final ZookeeperElectionService electionService;
    
    public Bootstrap() {
        CoordinatorRegistryCenter regCenter = getRegistryCenter();
        electionService = new ZookeeperElectionService(BootstrapEnvironment.getInstance().getFrameworkHostPort(),
                (CuratorFramework) regCenter.getRawClient(), HANode.ELECTION_NODE, new SchedulerElectionCandidate(regCenter));
        
        Runtime.getRuntime().addShutdownHook(new Thread("stop-hook") {
            @Override
            public void run() {
                electionService.close();
                latch.countDown();
            }
        });
    }
    
    private CoordinatorRegistryCenter getRegistryCenter() {
        CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(BootstrapEnvironment.getInstance().getZookeeperConfiguration());
        result.init();
        return result;
    }
    
    /**
     * 开始启动,如果是leader,会启动调度相关服务.
     */
    public void start() {
        electionService.start();
        try {
            latch.await();
        } catch (final InterruptedException ex) {
            log.error("Elastic job: Bootstrap start with exception:" + ex);
        }
    }
    
    // CHECKSTYLE:OFF
    public static void main(final String[] args) {
        // CHECKSTYLE:ON
        new Bootstrap().start();
    }
    
    private class SchedulerElectionCandidate implements ElectionCandidate {
        
        private final CoordinatorRegistryCenter regCenter;
        
        private SchedulerService schedulerService;
        
        SchedulerElectionCandidate(final CoordinatorRegistryCenter regCenter) {
            this.regCenter = regCenter;
        }
        
        /**
         * 开始领导状态.
         */
        public void startLeadership() {
            schedulerService = new SchedulerService(regCenter);
            schedulerService.start();
        }
        
        public void stopLeadership() {
            schedulerService.stop();
        }
    }
}
