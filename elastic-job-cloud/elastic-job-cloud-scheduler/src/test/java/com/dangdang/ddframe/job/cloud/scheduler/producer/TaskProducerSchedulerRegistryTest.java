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
import com.dangdang.ddframe.job.cloud.scheduler.config.JobExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.state.fixture.CloudJobConfigurationBuilder;
import com.dangdang.ddframe.job.cloud.scheduler.state.ready.ReadyService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TaskProducerSchedulerRegistryTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private ReadyService readyService;
    
    @Mock
    private TaskProducerScheduler taskProducerScheduler;
    
    private TaskProducerSchedulerRegistry taskProducerSchedulerRegistry;
    
    private final CloudJobConfiguration jobConfig = CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job");
    
    @Before
    public void setUp() throws NoSuchFieldException {
        taskProducerSchedulerRegistry = TaskProducerSchedulerRegistry.getInstance(regCenter);
        ReflectionUtils.setFieldValue(taskProducerSchedulerRegistry, "schedulerInstance", taskProducerScheduler);
        ReflectionUtils.setFieldValue(taskProducerSchedulerRegistry, "configService", configService);
        ReflectionUtils.setFieldValue(taskProducerSchedulerRegistry, "readyService", readyService);
    }
    
    @Test
    public void assertGetInstance() {
        assertThat(TaskProducerSchedulerRegistry.getInstance(regCenter), is(TaskProducerSchedulerRegistry.getInstance(regCenter)));
    }
    
    @Test
    public void assertStartup() {
        when(configService.loadAll()).thenReturn(Arrays.asList(jobConfig, CloudJobConfigurationBuilder.createCloudJobConfiguration("other_job", JobExecutionType.DAEMON)));
        taskProducerSchedulerRegistry.startup();
        verify(configService).loadAll();
        verify(taskProducerScheduler).startup(Matchers.<Collection<CloudJobConfiguration>>any());
        verify(readyService).addDaemon("other_job");
    }
    
    @Test
    public void assertRegisterNew() {
        when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        taskProducerSchedulerRegistry.register(jobConfig);
        verify(configService).add(jobConfig);
        when(configService.load("test_job")).thenReturn(Optional.of(jobConfig));
        taskProducerSchedulerRegistry.deregister("test_job");
    
        CloudJobConfiguration daemonJobConfig = CloudJobConfigurationBuilder.createCloudJobConfiguration("test_daemon_job", JobExecutionType.DAEMON);
        when(configService.load("test_daemon_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        taskProducerSchedulerRegistry.register(daemonJobConfig);
        verify(configService).add(daemonJobConfig);
        when(configService.load("test_daemon_job")).thenReturn(Optional.of(daemonJobConfig));
        taskProducerSchedulerRegistry.deregister("test_daemon_job");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertRegisterExistedAndSame() {
        when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent(), Optional.of(jobConfig));
        taskProducerSchedulerRegistry.register(jobConfig);
        taskProducerSchedulerRegistry.register(jobConfig);
        verify(configService, times(1)).add(jobConfig);
        verify(configService, times(0)).update(jobConfig);
        taskProducerSchedulerRegistry.deregister("test_job");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertRegisterExistedAndDifferent() {
        CloudJobConfiguration oldJobConfig = CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job");
        CloudJobConfiguration newJobConfig = CloudJobConfigurationBuilder.createOtherCloudJobConfiguration("test_job");
        when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent(), Optional.of(oldJobConfig));
        taskProducerSchedulerRegistry.register(oldJobConfig);
        taskProducerSchedulerRegistry.register(newJobConfig);
        verify(configService).add(oldJobConfig);
        verify(configService).update(newJobConfig);
        taskProducerSchedulerRegistry.deregister("test_job");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertDeregisterWhenExisted() {
        when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent(), Optional.of(jobConfig));
        taskProducerSchedulerRegistry.register(jobConfig);
        taskProducerSchedulerRegistry.deregister("test_job");
        verify(configService).remove("test_job");
    }
    
    @Test
    public void assertShutdown() {
        taskProducerSchedulerRegistry.shutdown();
        verify(taskProducerScheduler).shutdown();
    }
}
