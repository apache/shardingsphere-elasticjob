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
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.base.ElectionCandidate;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperElectionService;
import com.dangdang.ddframe.job.util.env.LocalHostService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

/**
 * 高可用框架.
 * 
 * @author gaohongtao
 */
@Slf4j
final class HAFramework extends AbstractFramework {
    
    private static final LocalHostService LOCAL_HOST_SERVICE = new LocalHostService();
    
    private static final String ELECTION_NODE = "/ha";
    
    private static HAFramework instance;
    
    private ZookeeperElectionService electionService;
    
    private HAFramework(final CoordinatorRegistryCenter regCenter) {
        super(regCenter);
    }
    
    /**
     * 工厂方法.
     * 
     * @param regCenter 协调注册中心
     * @return HA框架
     */
    public static HAFramework getInstance(final CoordinatorRegistryCenter regCenter) {
        if (null != instance) {
            return instance;
        }
        return instance = new HAFramework(regCenter);
    }
    
    @Override
    public void start() throws Exception {
        electionService = ZookeeperElectionService.builder()
                .identity(String.format("%s:%d", LOCAL_HOST_SERVICE.getHostName(), BootstrapEnvironment.getInstance().getRestfulServerConfiguration().getPort()))
                .client((CuratorFramework) getRegCenter().getRawClient()).electionPath(ELECTION_NODE)
                .electionCandidate(new ElectionCandidate() {
                    @Override
                    public void startLeadership() throws Exception {
                        getDelegate().start();
                    }
                
                    @Override
                    public void stopLeadership() {
                        getDelegate().stop();
                    }
                }).build();
        electionService.startLeadership();
        log.info("Elastic job: The framework {} leader", electionService.isLeader() ? "is" : "is not");
    }
    
    @Override
    public void stop() {
        log.info("Elastic job: HA container stop");
        electionService.close();
    }
    
    static synchronized void interrupt() {
        instance.electionService.abdicateLeadership();
    }
}
