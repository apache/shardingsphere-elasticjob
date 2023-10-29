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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.KillSession;
import org.apache.shardingsphere.elasticjob.reg.base.ElectionCandidate;
import org.apache.shardingsphere.elasticjob.test.util.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ZookeeperElectionServiceTest {
    
    private static final EmbedTestingServer EMBED_TESTING_SERVER = new EmbedTestingServer(9181);
    
    private static final String HOST_AND_PORT = "localhost:8899";
    
    private static final String ELECTION_PATH = "/election";
    
    @Mock
    private ElectionCandidate electionCandidate;
    
    @BeforeAll
    static void init() {
        EMBED_TESTING_SERVER.start();
    }
    
    @Test
    void assertContend() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(EMBED_TESTING_SERVER.getConnectionString(), new RetryOneTime(2000));
        client.start();
        client.blockUntilConnected();
        ZookeeperElectionService service = new ZookeeperElectionService(HOST_AND_PORT, client, ELECTION_PATH, electionCandidate);
        service.start();
        ElectionCandidate anotherElectionCandidate = mock(ElectionCandidate.class);
        CuratorFramework anotherClient = CuratorFrameworkFactory.newClient(EMBED_TESTING_SERVER.getConnectionString(), new RetryOneTime(2000));
        ZookeeperElectionService anotherService = new ZookeeperElectionService("ANOTHER_CLIENT:8899", anotherClient, ELECTION_PATH, anotherElectionCandidate);
        anotherClient.start();
        anotherClient.blockUntilConnected();
        anotherService.start();
        KillSession.kill(client.getZookeeperClient().getZooKeeper());
        service.stop();
        blockUntilCondition(() -> hasLeadership(anotherService));
        ((CountDownLatch) ReflectionUtils.getFieldValue(anotherService, "leaderLatch")).countDown();
        blockUntilCondition(() -> !hasLeadership(anotherService));
        anotherService.stop();
        verify(anotherElectionCandidate, atLeastOnce()).startLeadership();
        verify(anotherElectionCandidate, atLeastOnce()).stopLeadership();
    }
    
    private void blockUntilCondition(final Supplier<Boolean> condition) {
        Awaitility.await().pollDelay(100L, TimeUnit.MILLISECONDS).until(condition::get);
    }
    
    private boolean hasLeadership(final ZookeeperElectionService zookeeperElectionService) {
        return ((LeaderSelector) ReflectionUtils.getFieldValue(zookeeperElectionService, "leaderSelector")).hasLeadership();
    }
}
