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

package org.apache.shardingsphere.elasticjob.lite.internal.setup;

import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.lite.internal.instance.InstanceService;
import org.apache.shardingsphere.elasticjob.lite.internal.listener.ListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.reconcile.ReconcileService;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerService;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SetUpFacadeTest {
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private LeaderService leaderService;
    
    @Mock
    private ServerService serverService;
    
    @Mock
    private InstanceService instanceService;
    
    @Mock
    private ShardingService shardingService;
    
    @Mock
    private ReconcileService reconcileService;
    
    @Mock
    private ListenerManager listenerManager;
    
    private SetUpFacade setUpFacade;
    
    @Before
    public void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        setUpFacade = new SetUpFacade(null, "test_job", Collections.emptyList());
        ReflectionUtils.setFieldValue(setUpFacade, "configService", configService);
        ReflectionUtils.setFieldValue(setUpFacade, "leaderService", leaderService);
        ReflectionUtils.setFieldValue(setUpFacade, "serverService", serverService);
        ReflectionUtils.setFieldValue(setUpFacade, "instanceService", instanceService);
        ReflectionUtils.setFieldValue(setUpFacade, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(setUpFacade, "reconcileService", reconcileService);
        ReflectionUtils.setFieldValue(setUpFacade, "listenerManager", listenerManager);
    }
    
    @Test
    public void assertSetUpJobConfiguration() {
        JobConfiguration jobConfig = JobConfiguration.newBuilder("test_job", 3)
                .cron("0/1 * * * * ?").setProperty("streaming.process", Boolean.TRUE.toString()).build();
        when(configService.setUpJobConfiguration(ElasticJob.class.getName(), jobConfig)).thenReturn(jobConfig);
        assertThat(setUpFacade.setUpJobConfiguration(ElasticJob.class.getName(), jobConfig), is(jobConfig));
        verify(configService).setUpJobConfiguration(ElasticJob.class.getName(), jobConfig);
    }
    
    @Test
    public void assertRegisterStartUpInfo() {
        setUpFacade.registerStartUpInfo(true);
        verify(listenerManager).startAllListeners();
        verify(leaderService).electLeader();
        verify(serverService).persistOnline(true);
        verify(shardingService).setReshardingFlag();
    }
}
