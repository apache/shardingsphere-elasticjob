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
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

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
    public void setUp() throws Exception {
        masterBootstrap = new MasterBootstrap(env, regCenter, facadeService, schedulerDriver,  
                producerManager, statisticManager, cloudJobConfigurationListener, reconcileScheduledService, 
                statisticsScheduledService, taskLaunchScheduledService, restfulService);
    }
    
    @Test
    public void assertStart() {
        masterBootstrap.start();
        InOrder inOrder = getInOrder();
        inOrder.verify(facadeService).start();
        inOrder.verify(producerManager).startup();
        inOrder.verify(statisticManager).startup();
        inOrder.verify(cloudJobConfigurationListener).start();
        inOrder.verify(reconcileScheduledService).startAsync();
        inOrder.verify(statisticsScheduledService).startAsync();
        inOrder.verify(taskLaunchScheduledService).startAsync();
        inOrder.verify(restfulService).start();
        inOrder.verify(schedulerDriver).start();
    }
    
    @Test
    public void assertStop() {
        masterBootstrap.stop();
        InOrder inOrder = getInOrder();
        inOrder.verify(restfulService).stop();
        inOrder.verify(taskLaunchScheduledService).stopAsync();
        inOrder.verify(statisticsScheduledService).stopAsync();
        inOrder.verify(reconcileScheduledService).stopAsync();
        inOrder.verify(cloudJobConfigurationListener).stop();
        inOrder.verify(statisticManager).shutdown();
        inOrder.verify(producerManager).shutdown();
        inOrder.verify(schedulerDriver).stop(true);
        inOrder.verify(facadeService).stop();
    }
    
    private InOrder getInOrder() {
        return Mockito.inOrder(facadeService, schedulerDriver,
                producerManager, reconcileScheduledService, statisticsScheduledService, statisticManager, cloudJobConfigurationListener,
                taskLaunchScheduledService, restfulService);
    } 
}
    
