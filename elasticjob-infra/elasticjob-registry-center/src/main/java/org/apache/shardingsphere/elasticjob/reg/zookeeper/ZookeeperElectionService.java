/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.reg.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.shardingsphere.elasticjob.reg.base.ElectionCandidate;
import org.apache.shardingsphere.elasticjob.reg.exception.RegException;

import java.util.concurrent.CountDownLatch;

/**
 * Use {@link LeaderSelector} to implement election service.
 */
@Slf4j
public final class ZookeeperElectionService {
    
    private final CountDownLatch leaderLatch = new CountDownLatch(1);
    
    private final LeaderSelector leaderSelector;
    
    public ZookeeperElectionService(final String identity, final CuratorFramework client, final String electionPath, final ElectionCandidate electionCandidate) {
        leaderSelector = new LeaderSelector(client, electionPath, new LeaderSelectorListenerAdapter() {
            
            @Override
            public void takeLeadership(final CuratorFramework client) throws Exception {
                log.info("Elastic job: {} has leadership", identity);
                try {
                    electionCandidate.startLeadership();
                    leaderLatch.await();
                    log.warn("Elastic job: {} lost leadership.", identity);
                    electionCandidate.stopLeadership();
                } catch (final RegException exception) {
                    log.error("Elastic job: Starting error", exception);
                    System.exit(1);
                }
            }
        });
        leaderSelector.autoRequeue();
        leaderSelector.setId(identity);
    }
    
    /**
     * Start election.
     */
    public void start() {
        log.debug("Elastic job: {} start to elect leadership", leaderSelector.getId());
        leaderSelector.start();
    }
    
    /**
     * Stop election.
     */
    public void stop() {
        log.info("Elastic job: stop leadership election");
        leaderLatch.countDown();
        try {
            leaderSelector.close();
            // CHECKSTYLE:OFF
        } catch (final Exception ignore) {
        }
        // CHECKSTYLE:ON
    }
}
