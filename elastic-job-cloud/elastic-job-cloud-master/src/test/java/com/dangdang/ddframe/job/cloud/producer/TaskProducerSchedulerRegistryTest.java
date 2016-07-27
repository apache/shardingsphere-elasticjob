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

package com.dangdang.ddframe.job.cloud.producer;

import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.state.fixture.CloudJobConfigurationBuilder;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Collections;

import static org.hamcrest.core.Is.is;
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
    
    private TaskProducerSchedulerRegistry taskProducerSchedulerRegistry;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        taskProducerSchedulerRegistry = TaskProducerSchedulerRegistry.getInstance(regCenter);
        ReflectionUtils.setFieldValue(taskProducerSchedulerRegistry, "configService", configService);
    }
    
    @After
    public void tearDown() {
        taskProducerSchedulerRegistry.deregister("test_job");
    }
    
    @Test
    public void assertGetInstance() {
        assertThat(TaskProducerSchedulerRegistry.getInstance(regCenter), is(TaskProducerSchedulerRegistry.getInstance(regCenter)));
    }
    
    @Test
    public void assertRegisterFromRegistryCenter() {
        CloudJobConfiguration jobConfig = CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job");
        when(configService.loadAll()).thenReturn(Collections.singletonList(jobConfig));
        when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        taskProducerSchedulerRegistry.registerFromRegistryCenter();
        verify(configService).loadAll();
        verify(configService).add(jobConfig);
        
    }
    
    @Test
    public void assertRegisterNew() {
        CloudJobConfiguration jobConfig = CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job");
        when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        taskProducerSchedulerRegistry.register(jobConfig);
        verify(configService).add(jobConfig);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertRegisterExistedAndSame() {
        CloudJobConfiguration jobConfig = CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job");
        when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent(), Optional.of(jobConfig));
        taskProducerSchedulerRegistry.register(jobConfig);
        taskProducerSchedulerRegistry.register(jobConfig);
        verify(configService).add(jobConfig);
        verify(configService, times(0)).update(jobConfig);
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
    }
    
    @Test
    public void assertDeregisterWhenNotExisted() {
        taskProducerSchedulerRegistry.deregister("test_job");
        verify(configService).remove("test_job");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertDeregisterWhenExisted() {
        CloudJobConfiguration jobConfig = CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job");
        when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent(), Optional.of(jobConfig));
        taskProducerSchedulerRegistry.register(jobConfig);
        taskProducerSchedulerRegistry.deregister("test_job");
        verify(configService).remove("test_job");
    }
}
