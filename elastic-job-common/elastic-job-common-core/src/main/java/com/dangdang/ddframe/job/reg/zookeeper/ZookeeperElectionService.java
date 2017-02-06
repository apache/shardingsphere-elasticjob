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

package com.dangdang.ddframe.job.reg.zookeeper;

import com.dangdang.ddframe.job.reg.base.ElectionCandidate;
import com.dangdang.ddframe.job.util.concurrent.BlockUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.recipes.leader.Participant;
import org.apache.curator.utils.CloseableExecutorService;
import org.apache.curator.utils.ThreadUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

/**
 * 使用{@link LeaderSelector}实现选举服务.
 * 
 * @author gaohongtao
 */
@Slf4j
public class ZookeeperElectionService implements AutoCloseable {
    
    private final CountDownLatch leaderLatch = new CountDownLatch(1);
    
    private final LeaderSelector leaderSelector;
    
    public ZookeeperElectionService(final String identity, final String electionPath, final CuratorFramework client, final ElectionCandidate electionCandidate) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(identity));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(electionPath));
        Preconditions.checkNotNull(client);
        Preconditions.checkNotNull(electionCandidate);
        leaderSelector = new LeaderSelector(client, electionPath, getExecutorService(identity), new LeaderSelectorListenerAdapter() {
            
            @Override
            public void takeLeadership(final CuratorFramework client) throws Exception {
                log.info("Elastic job: {} has leadership", identity);
                try {
                    electionCandidate.startLeadership();
                    leaderLatch.await();
                    log.warn("Elastic job: {} lost leadership because of latch down", identity);
                } finally {
                    electionCandidate.stopLeadership();
                }
            }
        });
        leaderSelector.autoRequeue();
        leaderSelector.setId(identity);
    }
    
    private CloseableExecutorService getExecutorService(final String identity) {
        return new CloseableExecutorService(Executors.newSingleThreadExecutor(ThreadUtils.newGenericThreadFactory(Joiner.on("-").join("LeaderSelector", identity))));
    }
    
    /**
     * 开始进行选举.
     */
    public void startLeadership() {
        log.debug("Elastic job: {} start to elect leadership", leaderSelector.getId());
        leaderSelector.start();
    }
    
    /**
     * 放弃领导权.
     */
    public void abdicateLeadership() {
        if (!isLeader()) {
            return;
        }
        log.info("Elastic job: {} abdicate leadership", leaderSelector.getId());
        leaderSelector.interruptLeadership();
    }
    
    /**
     * 当前服务是否获取了领导权.
     * 
     * @return true 是领导 false 不是领导
     */
    public boolean isLeader() {
        return leaderSelector.getId().equals(getLeader().getId());
    }
    
    /**
     * 获取当前服务的标志.
     * 
     * @return 服务标志
     */
    public String getIdentity() {
        return getLeader().getId();
    }
    
    private Participant getLeader() {
        while (true) {
            Participant leader;
            try {
                leader = leaderSelector.getLeader();
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.debug("Elastic job: Leader node is electing({}), {} is waiting for {} ms", ex.getMessage(), leaderSelector.getId(), 100);
                BlockUtils.waitingShortTime();
                continue;
            }
            if (!Strings.isNullOrEmpty(leader.getId())) {
                return leader;
            }
            log.debug("Elastic job: Leader node is electing, {} is waiting for {} ms", leaderSelector.getId(), 100);
            BlockUtils.waitingShortTime();
        }
    }
    
    @Override
    public void close() {
        log.info("Elastic job: Close leadership election");
        leaderLatch.countDown();
        try {
            leaderSelector.close();
            // CHECKSTYLE:OFF
        } catch (final Exception ignored) {
        }
        // CHECKSTYLE:ON
    }
}
