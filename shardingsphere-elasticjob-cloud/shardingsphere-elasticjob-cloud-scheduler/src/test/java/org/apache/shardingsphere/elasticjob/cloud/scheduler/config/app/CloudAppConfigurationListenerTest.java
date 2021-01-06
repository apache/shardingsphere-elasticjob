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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.mesos.Protos;
import org.apache.shardingsphere.elasticjob.cloud.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationListenerTest;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.MesosStateService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.producer.ProducerManager;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class CloudAppConfigurationListenerTest {
    
    private static ZookeeperRegistryCenter regCenter;
    
    @Mock
    private ProducerManager producerManager;
    
    @Mock
    private MesosStateService mesosStateService;
    
    @InjectMocks
    private CloudAppConfigurationListener cloudAppConfigurationListener;
    
    @Before
    public void setUp() {
        ReflectionUtils.setFieldValue(cloudAppConfigurationListener, "producerManager", producerManager);
        ReflectionUtils.setFieldValue(cloudAppConfigurationListener, "mesosStateService", mesosStateService);
        initRegistryCenter();
        ReflectionUtils.setFieldValue(cloudAppConfigurationListener, "regCenter", regCenter);
    }
    
    private void initRegistryCenter() {
        EmbedTestingServer.start();
        ZookeeperConfiguration configuration = new ZookeeperConfiguration(EmbedTestingServer.getConnectionString(), CloudJobConfigurationListenerTest.class.getName());
        configuration.setDigest("digest:password");
        configuration.setSessionTimeoutMilliseconds(5000);
        configuration.setConnectionTimeoutMilliseconds(5000);
        regCenter = new ZookeeperRegistryCenter(configuration);
        regCenter.init();
    }
    
    @Test
    public void assertRemoveWithInvalidPath() {
        cloudAppConfigurationListener.event(CuratorCacheListener.Type.NODE_DELETED, new ChildData("/other/test_app", null, "".getBytes()),
                new ChildData("/other/test_app", null, "".getBytes()));
        verify(mesosStateService, times(0)).executors(ArgumentMatchers.any());
        verify(producerManager, times(0)).sendFrameworkMessage(any(Protos.ExecutorID.class), any(Protos.SlaveID.class), any());
    }
    
    @Test
    public void assertRemoveWithNoAppNamePath() {
        cloudAppConfigurationListener.event(CuratorCacheListener.Type.NODE_DELETED, new ChildData("/config/app", null, "".getBytes()),
                new ChildData("/config/app", null, "".getBytes()));
        verify(mesosStateService, times(0)).executors(ArgumentMatchers.any());
        verify(producerManager, times(0)).sendFrameworkMessage(any(Protos.ExecutorID.class), any(Protos.SlaveID.class), any());
    }
    
    @Test
    public void assertRemoveApp() {
        cloudAppConfigurationListener.event(CuratorCacheListener.Type.NODE_DELETED, new ChildData("/config/app/test_app", null, "".getBytes()),
                new ChildData("/config/app/test_app", null, "".getBytes()));
        verify(mesosStateService).executors("test_app");
    }
    
    @Test
    public void start() {
        cloudAppConfigurationListener.start();
    }
    
    @Test
    public void stop() {
        regCenter.addCacheData(CloudAppConfigurationNode.ROOT);
        ReflectionUtils.setFieldValue(cloudAppConfigurationListener, "regCenter", regCenter);
        cloudAppConfigurationListener.stop();
    }
}
