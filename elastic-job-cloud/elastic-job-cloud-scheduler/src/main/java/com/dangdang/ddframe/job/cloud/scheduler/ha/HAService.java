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

package com.dangdang.ddframe.job.cloud.scheduler.ha;

import com.dangdang.ddframe.job.cloud.scheduler.boot.MasterBootstrap;
import com.dangdang.ddframe.job.cloud.scheduler.boot.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.base.ElectionCandidate;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperElectionService;
import com.dangdang.ddframe.job.util.env.LocalHostService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

/**
 * 高可用服务.
 * 
 * @author gaohongtao
 */
@Slf4j
public final class HAService {
    
    private static final LocalHostService LOCAL_HOST_SERVICE = new LocalHostService();
    
    private static final String ELECTION_NODE = "/ha";
    
    private final ZookeeperElectionService electionService;
    
    public HAService(final CoordinatorRegistryCenter registryCenter) {
        electionService = ZookeeperElectionService.builder()
                .identity(String.format("%s:%d", LOCAL_HOST_SERVICE.getHostName(), BootstrapEnvironment.getInstance().getRestfulServerConfiguration().getPort()))
                .client((CuratorFramework) registryCenter.getRawClient()).electionPath(ELECTION_NODE)
                .electionCandidate(new ElectionCandidate() {
    
                    private final MasterBootstrap masterBootstrap = new MasterBootstrap();
    
                    @Override
                    public void startLeadership() throws Exception {
                        masterBootstrap.start();
                    }
    
                    @Override
                    public void stopLeadership() {
                        masterBootstrap.stop();
                    }
                }).build();
    }
    
    /**
     * 启动高可用服务.
     */
    public void start() {
        electionService.startLeadership();
        log.info("Elastic job: The framework {} leader", electionService.isLeader() ? "is" : "is not");
    }
    
    /**
     * 关闭高可用服务.
     */
    public void stop() {
        log.info("Elastic job: HA container stop");
        electionService.close();
    }
}
