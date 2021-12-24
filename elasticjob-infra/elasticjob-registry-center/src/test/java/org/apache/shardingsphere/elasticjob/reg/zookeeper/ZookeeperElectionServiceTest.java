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

import lombok.SneakyThrows;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.KillSession;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.fixture.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.reg.base.ElectionCandidate;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;

@RunWith(MockitoJUnitRunner.class)
public class ZookeeperElectionServiceTest {
    
    private static final String HOST_AND_PORT = "localhost:8899";
    
    private static final String ELECTION_PATH = "/election";
    
    @Mock
    private ElectionCandidate electionCandidate;
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertContend() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(EmbedTestingServer.getConnectionString(), new RetryOneTime(2000));
        client.start();
        client.blockUntilConnected();
        ZookeeperElectionService service = new ZookeeperElectionService(HOST_AND_PORT, client, ELECTION_PATH, electionCandidate);
        service.start();
        ElectionCandidate anotherElectionCandidate = mock(ElectionCandidate.class);
        CuratorFramework anotherClient = CuratorFrameworkFactory.newClient(EmbedTestingServer.getConnectionString(), new RetryOneTime(2000));
        ZookeeperElectionService anotherService = new ZookeeperElectionService("ANOTHER_CLIENT:8899", anotherClient, ELECTION_PATH, anotherElectionCandidate);
        anotherClient.start();
        anotherClient.blockUntilConnected();
        anotherService.start();
        KillSession.kill(client.getZookeeperClient().getZooKeeper());
        service.stop();
        blockUntilCondition(() -> hasLeadership(anotherService));
        ((CountDownLatch) getFieldValue(anotherService, "leaderLatch")).countDown();
        blockUntilCondition(() -> !hasLeadership(anotherService));
        anotherService.stop();
        verify(anotherElectionCandidate, atLeastOnce()).startLeadership();
        verify(anotherElectionCandidate, atLeastOnce()).stopLeadership();
    }
    
    @SneakyThrows
    private void blockUntilCondition(final Supplier<Boolean> condition) {
        while (!condition.get()) {
            Thread.sleep(100);
        }
    }

    @SneakyThrows
    private boolean hasLeadership(final ZookeeperElectionService zookeeperElectionService) {
        return ((LeaderSelector) getFieldValue(zookeeperElectionService, "leaderSelector")).hasLeadership();
    }

    @SneakyThrows
    private Object getFieldValue(final Object target, final String fieldName) {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }
}
