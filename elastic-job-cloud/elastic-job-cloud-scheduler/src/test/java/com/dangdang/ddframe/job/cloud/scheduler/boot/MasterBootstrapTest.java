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

import com.dangdang.ddframe.job.cloud.scheduler.boot.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.cloud.scheduler.boot.env.BootstrapEnvironment.EnvironmentArgument;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.EmbedTestingServer;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperElectionService;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MasterBootstrapTest {
    
    @Mock
    private CuratorFramework client;
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private ZookeeperElectionService electionService;
    
    @Mock
    private CountDownLatch latch;
    
    private MasterBootstrap masterBootstrap;
    
    @Before
    public void init() throws NoSuchFieldException {
        EmbedTestingServer.start();
        Properties properties = new Properties();
        properties.setProperty(EnvironmentArgument.ZOOKEEPER_SERVERS.getKey(), "localhost:3181");
        ReflectionUtils.setFieldValue(BootstrapEnvironment.getInstance(), "properties", properties);
        masterBootstrap = new MasterBootstrap();
        ReflectionUtils.setFieldValue(masterBootstrap, "regCenter", regCenter);
        ReflectionUtils.setFieldValue(masterBootstrap, "electionService", electionService);
        ReflectionUtils.setFieldValue(masterBootstrap, "latch", latch);
    }
    
    @Test
    public void assertStart() throws InterruptedException {
        masterBootstrap.start();
        verify(electionService).startLeadership();
        verify(latch).await();
    }
    
    @Test
    public void assertStop() {
        masterBootstrap.stop();
        verify(electionService).close();
    }
}
