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

package org.apache.shardingsphere.elasticjob.cloud.console;

import java.io.IOException;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.mesos.SchedulerDriver;
import org.apache.shardingsphere.elasticjob.cloud.console.controller.search.JobEventRdbSearch;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.RestfulServerConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.FacadeService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.MesosStateService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.ReconcileService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.fixture.master.MesosMasterServerMockConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.fixture.slave.MesosSlaveServerMockConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.producer.ProducerManager;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractCloudControllerTest {
    
    @Getter(AccessLevel.PROTECTED)
    private static CoordinatorRegistryCenter regCenter;
    
    @Getter(AccessLevel.PROTECTED)
    private static JobEventRdbSearch jobEventRdbSearch;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        initRestfulServer();
        initMesosServer();
    }
    
    private static void initRestfulServer() {
        regCenter = mock(CoordinatorRegistryCenter.class);
        jobEventRdbSearch = mock(JobEventRdbSearch.class);
        SchedulerDriver schedulerDriver = mock(SchedulerDriver.class);
        ProducerManager producerManager = new ProducerManager(schedulerDriver, regCenter);
        producerManager.startup();
        ConsoleBootstrap consoleBootstrap = new ConsoleBootstrap(regCenter, new RestfulServerConfiguration(19000), producerManager, new ReconcileService(schedulerDriver, new FacadeService(regCenter)));
        consoleBootstrap.start();
    }
    
    private static void initMesosServer() throws Exception {
        MesosStateService.register("127.0.0.1", 9050);
        ConsoleBootstrap.ConsoleApplication.setPort(9050);
        ConsoleBootstrap.ConsoleApplication.setExtraSources(new Class[]{MesosMasterServerMockConfiguration.class});
        ConsoleBootstrap.ConsoleApplication.start();
        ConsoleBootstrap.ConsoleApplication.setPort(9051);
        ConsoleBootstrap.ConsoleApplication.setExtraSources(new Class[]{MesosSlaveServerMockConfiguration.class});
        ConsoleBootstrap.ConsoleApplication.start();
    }
    
    @AfterClass
    public static void tearDown() throws IOException {
        MesosStateService.deregister();
    }
    
    @Before
    public void setUp() {
        reset(regCenter);
        reset(jobEventRdbSearch);
    }
}
