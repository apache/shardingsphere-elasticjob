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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos;

import com.google.common.util.concurrent.Service;
import org.apache.mesos.SchedulerDriver;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationListener;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.BootstrapEnvironment;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.FrameworkConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.producer.ProducerManager;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.restful.RestfulService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.StatisticManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SchedulerServiceTest {
    
    @Mock
    private BootstrapEnvironment env;
    
    @Mock
    private CloudJobConfigurationListener cloudJobConfigurationListener;
    
    @Mock
    private FacadeService facadeService;
    
    @Mock
    private SchedulerDriver schedulerDriver;
    
    @Mock
    private ProducerManager producerManager;
    
    @Mock
    private StatisticManager statisticManager;
    
    @Mock
    private Service taskLaunchScheduledService;
    
    @Mock
    private RestfulService restfulService;
    
    @Mock
    private ReconcileService reconcileService;
    
    private SchedulerService schedulerService;
    
    @Before
    public void setUp() {
        schedulerService = new SchedulerService(env, facadeService, schedulerDriver,  
                producerManager, statisticManager, cloudJobConfigurationListener, 
                taskLaunchScheduledService, restfulService, reconcileService);
    }
    
    @Test
    public void assertStart() {
        setReconcileEnabled(true);
        schedulerService.start();
        InOrder inOrder = getInOrder();
        inOrder.verify(facadeService).start();
        inOrder.verify(producerManager).startup();
        inOrder.verify(statisticManager).startup();
        inOrder.verify(cloudJobConfigurationListener).start();
        inOrder.verify(taskLaunchScheduledService).startAsync();
        inOrder.verify(restfulService).start();
        inOrder.verify(schedulerDriver).start();
        inOrder.verify(reconcileService).startAsync();
    }
    
    @Test
    public void assertStartWithoutReconcile() {
        setReconcileEnabled(false);
        schedulerService.start();
        InOrder inOrder = getInOrder();
        inOrder.verify(facadeService).start();
        inOrder.verify(producerManager).startup();
        inOrder.verify(statisticManager).startup();
        inOrder.verify(cloudJobConfigurationListener).start();
        inOrder.verify(taskLaunchScheduledService).startAsync();
        inOrder.verify(restfulService).start();
        inOrder.verify(schedulerDriver).start();
        inOrder.verify(reconcileService, never()).stopAsync();
    }
    
    @Test
    public void assertStop() {
        setReconcileEnabled(true);
        schedulerService.stop();
        InOrder inOrder = getInOrder();
        inOrder.verify(restfulService).stop();
        inOrder.verify(taskLaunchScheduledService).stopAsync();
        inOrder.verify(cloudJobConfigurationListener).stop();
        inOrder.verify(statisticManager).shutdown();
        inOrder.verify(producerManager).shutdown();
        inOrder.verify(schedulerDriver).stop(true);
        inOrder.verify(facadeService).stop();
        inOrder.verify(reconcileService).stopAsync();
    }
    
    @Test
    public void assertStopWithoutReconcile() {
        setReconcileEnabled(false);
        schedulerService.stop();
        InOrder inOrder = getInOrder();
        inOrder.verify(restfulService).stop();
        inOrder.verify(taskLaunchScheduledService).stopAsync();
        inOrder.verify(cloudJobConfigurationListener).stop();
        inOrder.verify(statisticManager).shutdown();
        inOrder.verify(producerManager).shutdown();
        inOrder.verify(schedulerDriver).stop(true);
        inOrder.verify(facadeService).stop();
        inOrder.verify(reconcileService, never()).stopAsync();
    }
    
    private InOrder getInOrder() {
        return inOrder(facadeService, schedulerDriver, producerManager, 
                statisticManager, cloudJobConfigurationListener, taskLaunchScheduledService, restfulService, reconcileService);
    }
    
    private void setReconcileEnabled(final boolean isEnabled) {
        FrameworkConfiguration frameworkConfiguration = mock(FrameworkConfiguration.class);
        when(frameworkConfiguration.isEnabledReconcile()).thenReturn(isEnabled);
        when(env.getFrameworkConfiguration()).thenReturn(frameworkConfiguration);
    }
}
    
