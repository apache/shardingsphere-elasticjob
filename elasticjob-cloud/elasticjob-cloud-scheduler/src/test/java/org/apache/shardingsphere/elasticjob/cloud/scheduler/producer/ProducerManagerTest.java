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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.producer;

import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.apache.shardingsphere.elasticjob.cloud.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.config.pojo.CloudJobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.cloud.exception.AppConfigurationException;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.CloudAppConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.pojo.CloudAppConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudAppConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.job.DisableJobService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.ready.ReadyService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running.RunningService;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    
    private final CloudAppConfigurationPOJO appConfig = CloudAppConfigurationBuilder.createCloudAppConfiguration("test_app");
    
    private final CloudJobConfigurationPOJO transientJobConfig = CloudJobConfigurationBuilder.createCloudJobConfiguration("transient_test_job");
    
    private final CloudJobConfigurationPOJO daemonJobConfig = CloudJobConfigurationBuilder.createCloudJobConfiguration("daemon_test_job", CloudJobExecutionType.DAEMON);

    @Before
    public void setUp() {
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
        when(appConfigService.load("test_app")).thenReturn(Optional.empty());
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
        when(configService.load("transient_test_job")).thenReturn(Optional.empty());
        producerManager.register(transientJobConfig);
        verify(configService).add(transientJobConfig);
        verify(transientProducerScheduler).register(transientJobConfig);
    }
    
    @Test
    public void assertRegisterDaemonJob() {
        when(appConfigService.load("test_app")).thenReturn(Optional.of(appConfig));
        when(configService.load("daemon_test_job")).thenReturn(Optional.empty());
        producerManager.register(daemonJobConfig);
        verify(configService).add(daemonJobConfig);
        verify(readyService).addDaemon("daemon_test_job");
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertUpdateNotExisted() {
        when(configService.load("transient_test_job")).thenReturn(Optional.empty());
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
        verify(readyService).remove(Collections.singletonList("transient_test_job"));
    }
    
    @Test
    public void assertDeregisterNotExisted() {
        when(configService.load("transient_test_job")).thenReturn(Optional.empty());
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
        verify(readyService).remove(Collections.singletonList("transient_test_job"));
    }
    
    @Test
    public void assertShutdown() {
        producerManager.shutdown();
        verify(transientProducerScheduler).shutdown();
    }
}
