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

package com.dangdang.ddframe.job.cloud.scheduler.producer;

import com.dangdang.ddframe.job.cloud.scheduler.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.config.JobExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import com.dangdang.ddframe.job.cloud.scheduler.state.ready.ReadyService;
import com.dangdang.ddframe.job.exception.JobConfigurationException;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ProducerManagerTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private ReadyService readyService;
    
    @Mock
    private TransientProducerScheduler transientProducerScheduler;
    
    private ProducerManager producerManager;
    
    private final CloudJobConfiguration jobConfig = CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job");
    
    @Before
    public void setUp() throws NoSuchFieldException {
        producerManager = ProducerManagerFactory.getInstance(regCenter);
        ReflectionUtils.setFieldValue(producerManager, "transientProducerScheduler", transientProducerScheduler);
        ReflectionUtils.setFieldValue(producerManager, "configService", configService);
        ReflectionUtils.setFieldValue(producerManager, "readyService", readyService);
    }
    
    @Test
    public void assertStartup() {
        when(configService.loadAll()).thenReturn(Arrays.asList(jobConfig, CloudJobConfigurationBuilder.createCloudJobConfiguration("other_job", JobExecutionType.DAEMON)));
        producerManager.startup();
        verify(configService).loadAll();
        verify(readyService).addDaemon("other_job");
    }
    
    @Test
    public void assertRegisterNew() {
        when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        producerManager.register(jobConfig);
        verify(configService).add(jobConfig);
        when(configService.load("test_job")).thenReturn(Optional.of(jobConfig));
        producerManager.deregister("test_job");
        CloudJobConfiguration daemonJobConfig = CloudJobConfigurationBuilder.createCloudJobConfiguration("test_daemon_job", JobExecutionType.DAEMON);
        when(configService.load("test_daemon_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        producerManager.register(daemonJobConfig);
        verify(configService).add(daemonJobConfig);
        when(configService.load("test_daemon_job")).thenReturn(Optional.of(daemonJobConfig));
        producerManager.deregister("test_daemon_job");
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertRegisterExisted() {
        when(configService.load("test_job")).thenReturn(Optional.of(jobConfig));
        producerManager.register(jobConfig);
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertUpdateNotExisted() {
        when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        producerManager.update(jobConfig);
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertUpdateJobExecutionType() {
        when(configService.load("test_job")).thenReturn(Optional.of(jobConfig));
        producerManager.update(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job", JobExecutionType.DAEMON));
    }
    
    @Test
    public void assertUpdateExisted() {
        when(configService.load("test_job")).thenReturn(Optional.of(jobConfig));
        producerManager.update(jobConfig);
        verify(configService).update(jobConfig);
    }
    
    @Test
    public void assertDeregisterNotExisted() {
        when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        producerManager.deregister("test_job");
        verify(configService, times(0)).remove("test_job");
    }
    
    @Test
    public void assertDeregisterExisted() {
        when(configService.load("test_job")).thenReturn(Optional.of(jobConfig));
        producerManager.deregister("test_job");
        verify(configService).remove("test_job");
    }
    
    @Test
    public void assertShutdown() {
        producerManager.shutdown();
        verify(transientProducerScheduler).shutdown();
    }
}
