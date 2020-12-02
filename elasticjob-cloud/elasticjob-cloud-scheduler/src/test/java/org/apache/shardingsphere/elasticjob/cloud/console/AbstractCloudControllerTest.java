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

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.mesos.SchedulerDriver;
import org.apache.shardingsphere.elasticjob.cloud.console.controller.search.JobEventRdbSearch;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.RestfulServerConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.FacadeService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.MesosStateService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.ReconcileService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.fixture.master.MesosMasterServerMock;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.fixture.slave.MesosSlaveServerMock;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.producer.ProducerManager;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.restful.NettyRestfulService;
import org.apache.shardingsphere.elasticjob.restful.NettyRestfulServiceConfiguration;
import org.apache.shardingsphere.elasticjob.restful.RestfulService;
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
    
    private static ConsoleBootstrap consoleBootstrap;
    
    private static RestfulService masterServer;
    
    private static RestfulService slaveServer;
    
    @BeforeClass
    public static void setUpClass() {
        initRestfulServer();
        initMesosServer();
    }
    
    private static void initRestfulServer() {
        regCenter = mock(CoordinatorRegistryCenter.class);
        jobEventRdbSearch = mock(JobEventRdbSearch.class);
        SchedulerDriver schedulerDriver = mock(SchedulerDriver.class);
        ProducerManager producerManager = new ProducerManager(schedulerDriver, regCenter);
        producerManager.startup();
        consoleBootstrap = new ConsoleBootstrap(regCenter, new RestfulServerConfiguration(19000),
                producerManager, new ReconcileService(schedulerDriver, new FacadeService(regCenter)));
        consoleBootstrap.start();
    }
    
    private static void initMesosServer() {
        MesosStateService.register("127.0.0.1", 9050);
        NettyRestfulServiceConfiguration masterServerConfiguration = new NettyRestfulServiceConfiguration(9050);
        masterServerConfiguration.addControllerInstances(new MesosMasterServerMock());
        masterServer = new NettyRestfulService(masterServerConfiguration);
        masterServer.startup();
        NettyRestfulServiceConfiguration slaveServerConfiguration = new NettyRestfulServiceConfiguration(9051);
        slaveServerConfiguration.addControllerInstances(new MesosSlaveServerMock());
        slaveServer = new NettyRestfulService(slaveServerConfiguration);
        slaveServer.startup();
    }
    
    @AfterClass
    public static void tearDown() {
        consoleBootstrap.stop();
        masterServer.shutdown();
        slaveServer.shutdown();
        MesosStateService.deregister();
    }
    
    @Before
    public void setUp() {
        reset(regCenter);
        reset(jobEventRdbSearch);
    }
}
