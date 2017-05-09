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

import com.dangdang.ddframe.job.cloud.scheduler.config.app.CloudAppConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.app.CloudAppConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudAppConfigurationBuilder;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import com.dangdang.ddframe.job.cloud.scheduler.state.disable.job.DisableJobService;
import com.dangdang.ddframe.job.cloud.scheduler.state.ready.ReadyService;
import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningService;
import com.dangdang.ddframe.job.context.TaskContext;
import com.dangdang.ddframe.job.exception.AppConfigurationException;
import com.dangdang.ddframe.job.exception.JobConfigurationException;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ProducerManagerTest {
    
    @Mock
    private SchedulerDriver schedulerDriver;
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private CloudAppConfigurationService appConfigService;
    
    @Mock
    private CloudJobConfigurationService configService;
   
    @Mock
    private ReadyService readyService;
    
    @Mock
    private RunningService runningService;
    
    @Mock
    private DisableJobService disableJobService;
    
    @Mock
    private TransientProducerScheduler transientProducerScheduler;
    
    private ProducerManager producerManager;
    
    private final CloudAppConfiguration appConfig = CloudAppConfigurationBuilder.createCloudAppConfiguration("test_app");
    
    private final CloudJobConfiguration transientJobConfig = CloudJobConfigurationBuilder.createCloudJobConfiguration("transient_test_job");
    
    private final CloudJobConfiguration daemonJobConfig = CloudJobConfigurationBuilder.createCloudJobConfiguration("daemon_test_job", CloudJobExecutionType.DAEMON);
    
    
    
    @Before
    public void setUp() throws NoSuchFieldException {
        producerManager = new ProducerManager(schedulerDriver, regCenter);
        ReflectionUtils.setFieldValue(producerManager, "appConfigService", appConfigService);
        ReflectionUtils.setFieldValue(producerManager, "configService", configService);
        ReflectionUtils.setFieldValue(producerManager, "readyService", readyService);
        ReflectionUtils.setFieldValue(producerManager, "runningService", runningService);
        ReflectionUtils.setFieldValue(producerManager, "disableJobService", disableJobService);
        ReflectionUtils.setFieldValue(producerManager, "transientProducerScheduler", transientProducerScheduler);
    }
    
    @Test
    public void assertStartup() {
        when(configService.loadAll()).thenReturn(Arrays.asList(transientJobConfig, daemonJobConfig));
        producerManager.startup();
        verify(configService).loadAll();
        verify(transientProducerScheduler).register(transientJobConfig);
        verify(readyService).addDaemon("daemon_test_job");
    }
    
    
    @Test(expected = AppConfigurationException.class)
    public void assertRegisterJobWithoutApp() {
        when(appConfigService.load("test_app")).thenReturn(Optional.<CloudAppConfiguration>absent());
        producerManager.register(transientJobConfig);
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertRegisterExistedJob() {
        when(appConfigService.load("test_app")).thenReturn(Optional.of(appConfig));
        when(configService.load("transient_test_job")).thenReturn(Optional.of(transientJobConfig));
        producerManager.register(transientJobConfig);
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertRegisterDisabledJob() {
        when(disableJobService.isDisabled("transient_test_job")).thenReturn(true);
        producerManager.register(transientJobConfig);
    }
    
    @Test
    public void assertRegisterTransientJob() {
        when(appConfigService.load("test_app")).thenReturn(Optional.of(appConfig));
        when(configService.load("transient_test_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        producerManager.register(transientJobConfig);
        verify(configService).add(transientJobConfig);
        verify(transientProducerScheduler).register(transientJobConfig);
    }
    
    @Test
    public void assertRegisterDaemonJob() {
        when(appConfigService.load("test_app")).thenReturn(Optional.of(appConfig));
        when(configService.load("daemon_test_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        producerManager.register(daemonJobConfig);
        verify(configService).add(daemonJobConfig);
        verify(readyService).addDaemon("daemon_test_job");
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertUpdateNotExisted() {
        when(configService.load("transient_test_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        producerManager.update(transientJobConfig);
    }
    
    @Test
    public void assertUpdateExisted() {
        when(configService.load("transient_test_job")).thenReturn(Optional.of(transientJobConfig));
        List<TaskContext> taskContexts = Arrays.asList(
                TaskContext.from("transient_test_job@-@0@-@READY@-@SLAVE-S0@-@UUID"), TaskContext.from("transient_test_job@-@1@-@READY@-@SLAVE-S0@-@UUID"));
        when(runningService.getRunningTasks("transient_test_job")).thenReturn(taskContexts);
        producerManager.update(transientJobConfig);
        verify(configService).update(transientJobConfig);
        for (TaskContext each : taskContexts) {
            verify(schedulerDriver).killTask(Protos.TaskID.newBuilder().setValue(each.getId()).build());
        }
        verify(runningService).remove("transient_test_job");
        verify(readyService).remove(Lists.newArrayList("transient_test_job"));
    }
    
    @Test
    public void assertDeregisterNotExisted() {
        when(configService.load("transient_test_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        producerManager.deregister("transient_test_job");
        verify(configService, times(0)).remove("transient_test_job");
    }
    
    @Test
    public void assertDeregisterExisted() {
        when(configService.load("transient_test_job")).thenReturn(Optional.of(transientJobConfig));
        List<TaskContext> taskContexts = Arrays.asList(
                TaskContext.from("transient_test_job@-@0@-@READY@-@SLAVE-S0@-@UUID"), TaskContext.from("transient_test_job@-@1@-@READY@-@SLAVE-S0@-@UUID"));
        when(runningService.getRunningTasks("transient_test_job")).thenReturn(taskContexts);
        producerManager.deregister("transient_test_job");
        for (TaskContext each : taskContexts) {
            verify(schedulerDriver).killTask(Protos.TaskID.newBuilder().setValue(each.getId()).build());
        }
        verify(disableJobService).remove("transient_test_job");
        verify(configService).remove("transient_test_job");
        verify(runningService).remove("transient_test_job");
        verify(readyService).remove(Lists.newArrayList("transient_test_job"));
    }
    
    @Test
    public void assertShutdown() {
        producerManager.shutdown();
        verify(transientProducerScheduler).shutdown();
    }
}
