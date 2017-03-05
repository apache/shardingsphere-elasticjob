package com.dangdang.ddframe.job.reg.zookeeper;

import com.dangdang.ddframe.job.fixture.EmbedTestingServer;
import com.dangdang.ddframe.job.reg.base.ElectionCandidate;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.KillSession;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ZookeeperElectionServiceTest {
    
    private static final String HOST_AND_PORT = "localhost:8899";
    
    private static final String ELECTION_PATH = "/election";
    
    private CuratorFramework client;
    
    private ZookeeperElectionService service;
    
    @Mock
    private ElectionCandidate electionCandidate;
    
    @BeforeClass
    public static void init() throws InterruptedException {
        EmbedTestingServer.start();
    }
    
    @Before
    public void setUp() throws InterruptedException {
        client = CuratorFrameworkFactory.newClient(EmbedTestingServer.getConnectionString(), new RetryOneTime(2000));
        client.start();
        client.blockUntilConnected();
        service = new ZookeeperElectionService(HOST_AND_PORT, client, ELECTION_PATH, electionCandidate);
        service.start();
    }
    
    @After
    public void clean() {
        service.stop();
        client.close();
    }
    
    @Test
    public void assertContend() throws Exception {
        ElectionCandidate anotherElectionCandidate = mock(ElectionCandidate.class);
        CuratorFramework anotherClient = CuratorFrameworkFactory.newClient(EmbedTestingServer.getConnectionString(), new RetryOneTime(2000));
        ZookeeperElectionService anotherService = new ZookeeperElectionService("ANOTHER_CLIENT:8899", anotherClient, ELECTION_PATH, anotherElectionCandidate);
        anotherClient.start();
        anotherClient.blockUntilConnected();
        anotherService.start();
        KillSession.kill(client.getZookeeperClient().getZooKeeper(), EmbedTestingServer.getConnectionString());
        service.stop();
        verify(anotherElectionCandidate).startLeadership();
    }
}
