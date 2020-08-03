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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.app;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.shardingsphere.elasticjob.cloud.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationListenerTest;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.EmbedTestingServer;
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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class CloudAppDisableListenerTest {
    
    private static ZookeeperRegistryCenter regCenter;
    
    @Mock
    private ProducerManager producerManager;
    
    @Mock
    private CloudJobConfigurationService jobConfigService;
    
    @InjectMocks
    private CloudAppDisableListener cloudAppDisableListener;
    
    @Before
    public void setUp() {
        ReflectionUtils.setFieldValue(cloudAppDisableListener, "producerManager", producerManager);
        initRegistryCenter();
        ReflectionUtils.setFieldValue(cloudAppDisableListener, "regCenter", regCenter);
        ReflectionUtils.setFieldValue(cloudAppDisableListener, "jobConfigService", jobConfigService);
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
    public void assertDisableWithInvalidPath() {
        cloudAppDisableListener.event(CuratorCacheListener.Type.NODE_CREATED, null, new ChildData("/other/test_app", null, "".getBytes()));
        verify(jobConfigService, times(0)).loadAll();
        verify(producerManager, times(0)).unschedule(ArgumentMatchers.any());
    }
    
    @Test
    public void assertDisableWithNoAppNamePath() {
        cloudAppDisableListener.event(CuratorCacheListener.Type.NODE_CREATED, null, new ChildData("/state/disable/app", null, "".getBytes()));
        verify(jobConfigService, times(0)).loadAll();
        verify(producerManager, times(0)).unschedule(ArgumentMatchers.any());
    }
    
    @Test
    public void assertDisable() {
        cloudAppDisableListener.event(CuratorCacheListener.Type.NODE_CREATED, null, new ChildData("/state/disable/app/app_test", null, "".getBytes()));
        verify(jobConfigService).loadAll();
    }
    
    @Test
    public void assertEnableWithInvalidPath() {
        cloudAppDisableListener.event(CuratorCacheListener.Type.NODE_DELETED, new ChildData("/other/test_app", null, "".getBytes()),
                new ChildData("/other/test_app", null, "".getBytes()));
        verify(jobConfigService, times(0)).loadAll();
        verify(producerManager, times(0)).reschedule(ArgumentMatchers.any());
    }
    
    @Test
    public void assertEnableWithNoAppNamePath() {
        cloudAppDisableListener.event(CuratorCacheListener.Type.NODE_DELETED, new ChildData("/state/disable/app", null, "".getBytes()),
                new ChildData("/state/disable/app", null, "".getBytes()));
        verify(jobConfigService, times(0)).loadAll();
        verify(producerManager, times(0)).unschedule(ArgumentMatchers.any());
    }
    
    @Test
    public void assertEnable() {
        cloudAppDisableListener.event(CuratorCacheListener.Type.NODE_DELETED, new ChildData("/state/disable/app/app_test", null, "".getBytes()),
                new ChildData("/state/disable/app/app_test", null, "".getBytes()));
        verify(jobConfigService).loadAll();
    }
    
    @Test
    public void start() {
        cloudAppDisableListener.start();
    }
    
    @Test
    public void stop() {
        regCenter.addCacheData("/state/disable/app");
        ReflectionUtils.setFieldValue(cloudAppDisableListener, "regCenter", regCenter);
        cloudAppDisableListener.stop();
    }
}
