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
import com.dangdang.ddframe.job.cloud.scheduler.config.CloudJobConfigurationListener;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.FacadeService;
import com.dangdang.ddframe.job.cloud.scheduler.producer.ProducerManager;
import com.dangdang.ddframe.job.cloud.scheduler.restful.RestfulService;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.StatisticManager;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.util.concurrent.Service;
import org.apache.mesos.SchedulerDriver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Properties;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MasterBootstrapTest {
    
    private BootstrapEnvironment env = BootstrapEnvironment.getInstance();
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private CloudJobConfigurationListener cloudJobConfigurationListener;
    
    @Mock
    private FacadeService facadeService;
    
    @Mock
    private SchedulerDriver schedulerDriver;
    
    @Mock
    private ProducerManager producerManager;
    
    @Mock
    private Service reconcileScheduledService;
    
    @Mock
    private Service statisticsScheduledService;
    
    @Mock
    private StatisticManager statisticManager;
    
    @Mock
    private Service taskLaunchScheduledService;
    
    @Mock
    private RestfulService restfulService;
    
    private MasterBootstrap masterBootstrap;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        Properties properties = new Properties();
        properties.setProperty(BootstrapEnvironment.EnvironmentArgument.HOSTNAME.getKey(), "127.0.0.1");
        System.setProperty("LIBPROCESS_IP", "127.0.0.1");
        ReflectionUtils.setFieldValue(env, "properties", properties);
        masterBootstrap = new MasterBootstrap(regCenter);
        ReflectionUtils.setFieldValue(masterBootstrap, "env", env);
        ReflectionUtils.setFieldValue(masterBootstrap, "facadeService", facadeService);
        ReflectionUtils.setFieldValue(masterBootstrap, "schedulerDriver", schedulerDriver);
        ReflectionUtils.setFieldValue(masterBootstrap, "producerManager", producerManager);
        ReflectionUtils.setFieldValue(masterBootstrap, "cloudJobConfigurationListener", cloudJobConfigurationListener);
        ReflectionUtils.setFieldValue(masterBootstrap, "reconcileScheduledService", reconcileScheduledService);
        ReflectionUtils.setFieldValue(masterBootstrap, "statisticsScheduledService", statisticsScheduledService);
        ReflectionUtils.setFieldValue(masterBootstrap, "statisticManager", statisticManager);
        ReflectionUtils.setFieldValue(masterBootstrap, "taskLaunchScheduledService", taskLaunchScheduledService);
        ReflectionUtils.setFieldValue(masterBootstrap, "restfulService", restfulService);
    }
    
    @Test
    public void assertStart() {
        masterBootstrap.start();
        verify(facadeService).start();
        verify(statisticManager).startup();
        verify(restfulService).start();
        verify(schedulerDriver).start();
        verify(cloudJobConfigurationListener).start();
        verify(producerManager).startup();
        verify(reconcileScheduledService).startAsync();
        verify(statisticsScheduledService).startAsync();
        verify(statisticManager).startup();
        verify(taskLaunchScheduledService).startAsync();
        verify(restfulService).start();
    }
    
    @Test
    public void assertStop() {
        masterBootstrap.stop();
        verify(facadeService).stop();
        verify(restfulService).stop();
        verify(schedulerDriver).stop(true);
        verify(reconcileScheduledService).stopAsync();
        verify(statisticsScheduledService).stopAsync();
        verify(statisticManager).shutdown();
        verify(taskLaunchScheduledService).stopAsync();
        verify(restfulService).stop();
    }
}
    
