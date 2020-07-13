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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.restful;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.mesos.SchedulerDriver;
import org.apache.shardingsphere.elasticjob.cloud.restful.RestfulServer;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.RestfulServerConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.FacadeService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.MesosStateService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.ReconcileService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.fixture.master.MesosMasterServerMock;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.fixture.slave.MesosSlaveServerMock;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.producer.ProducerManager;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.restful.search.JobEventRdbSearch;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractCloudRestfulApiTest {
    
    @Getter(AccessLevel.PROTECTED)
    private static CoordinatorRegistryCenter regCenter;
    
    @Getter(AccessLevel.PROTECTED)
    private static JobEventRdbSearch jobEventRdbSearch;
    
    private static RestfulService restfulService;
    
    private static RestfulServer masterServer;
    
    private static RestfulServer slaveServer;
    
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
        restfulService = new RestfulService(regCenter, new RestfulServerConfiguration(19000), producerManager, new ReconcileService(schedulerDriver, new FacadeService(regCenter)));
        restfulService.start();
    }
    
    private static void initMesosServer() throws Exception {
        MesosStateService.register("127.0.0.1", 9050);
        masterServer = new RestfulServer(9050);
        masterServer.start(MesosMasterServerMock.class.getPackage().getName(), null, null);
        slaveServer = new RestfulServer(9051);
        slaveServer.start(MesosSlaveServerMock.class.getPackage().getName(), null, null);
    }
    
    @AfterClass
    public static void tearDown() {
        restfulService.stop();
        masterServer.stop();
        slaveServer.stop();
        MesosStateService.deregister();
    }
    
    @Before
    public void setUp() {
        reset(regCenter);
        reset(jobEventRdbSearch);
    }
}
