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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener.Type;
import org.apache.shardingsphere.elasticjob.cloud.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJsonConstants;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.producer.ProducerManager;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.ready.ReadyService;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class CloudJobConfigurationListenerTest {
    
    private static ZookeeperRegistryCenter regCenter;
    
    @Mock
    private ProducerManager producerManager;
    
    @Mock
    private ReadyService readyService;
    
    @InjectMocks
    private CloudJobConfigurationListener cloudJobConfigurationListener;
    
    @Before
    public void setUp() {
        ReflectionUtils.setFieldValue(cloudJobConfigurationListener, "producerManager", producerManager);
        ReflectionUtils.setFieldValue(cloudJobConfigurationListener, "readyService", readyService);
        initRegistryCenter();
        ReflectionUtils.setFieldValue(cloudJobConfigurationListener, "regCenter", regCenter);
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
    public void assertChildEventWhenIsNotConfigPath() {
        cloudJobConfigurationListener.event(Type.NODE_CHANGED, null, new ChildData("/other/test_job", null, "".getBytes()));
        verify(producerManager, times(0)).schedule(ArgumentMatchers.any());
        verify(producerManager, times(0)).reschedule(ArgumentMatchers.any());
        verify(producerManager, times(0)).unschedule(ArgumentMatchers.any());
    }
    
    @Test
    public void assertChildEventWhenIsRootConfigPath() {
        cloudJobConfigurationListener.event(Type.NODE_DELETED, new ChildData("/config/job", null, "".getBytes()),
                new ChildData("/config/job", null, "".getBytes()));
        verify(producerManager, times(0)).schedule(ArgumentMatchers.any());
        verify(producerManager, times(0)).reschedule(ArgumentMatchers.any());
        verify(producerManager, times(0)).unschedule(ArgumentMatchers.any());
    }
    
    @Test
    public void assertChildEventWhenStateIsAddAndIsConfigPathAndInvalidData() {
        cloudJobConfigurationListener.event(Type.NODE_CREATED, null, new ChildData("/config/job/test_job", null, "".getBytes()));
        verify(producerManager, times(0)).schedule(ArgumentMatchers.any());
        verify(producerManager, times(0)).reschedule(ArgumentMatchers.any());
        verify(producerManager, times(0)).unschedule(ArgumentMatchers.any());
    }
    
    @Test
    public void assertChildEventWhenStateIsAddAndIsConfigPath() {
        cloudJobConfigurationListener.event(Type.NODE_CREATED, null, new ChildData("/config/job/test_job", null, CloudJsonConstants.getJobJson().getBytes()));
        verify(producerManager).schedule(ArgumentMatchers.any());
    }
    
    @Test
    public void assertChildEventWhenStateIsUpdateAndIsConfigPathAndTransientJob() {
        cloudJobConfigurationListener.event(Type.NODE_CHANGED, null, new ChildData("/config/job/test_job", null, CloudJsonConstants.getJobJson().getBytes()));
        verify(readyService, times(0)).remove(Collections.singletonList("test_job"));
        verify(producerManager).reschedule(ArgumentMatchers.any());
    }
    
    @Test
    public void assertChildEventWhenStateIsUpdateAndIsConfigPathAndDaemonJob() {
        cloudJobConfigurationListener.event(Type.NODE_CHANGED, null, new ChildData("/config/job/test_job", null, CloudJsonConstants.getJobJson(CloudJobExecutionType.DAEMON).getBytes()));
        verify(readyService).remove(Collections.singletonList("test_job"));
        verify(producerManager).reschedule(ArgumentMatchers.any());
    }
    
    @Test
    public void assertChildEventWhenStateIsUpdateAndIsConfigPathAndMisfireDisabled() {
        cloudJobConfigurationListener.event(Type.NODE_CHANGED, null, new ChildData("/config/job/test_job", null, CloudJsonConstants.getJobJson(false).getBytes()));
        verify(readyService).setMisfireDisabled("test_job");
        verify(producerManager).reschedule(ArgumentMatchers.any());
    }
    
    @Test
    public void assertChildEventWhenStateIsRemovedAndIsJobConfigPath() {
        cloudJobConfigurationListener.event(Type.NODE_DELETED, new ChildData("/config/job/test_job", null, "".getBytes()),
                new ChildData("/config/job/test_job", null, "".getBytes()));
        verify(producerManager).unschedule("test_job");
    }
    
    @Test
    public void assertChildEventWhenStateIsUpdateAndIsConfigPath() {
        cloudJobConfigurationListener.event(Type.NODE_CHANGED, null, new ChildData("/config/job/test_job", null, "".getBytes()));
    }
    
    @Test
    public void assertStart() {
        cloudJobConfigurationListener.start();
    }
    
    @Test
    public void assertStop() {
        regCenter.addCacheData(CloudJobConfigurationNode.ROOT);
        ReflectionUtils.setFieldValue(cloudJobConfigurationListener, "regCenter", regCenter);
        cloudJobConfigurationListener.stop();
    }
    
}
