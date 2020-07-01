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

package org.apache.shardingsphere.elasticjob.lite.internal.monitor;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.elasticjob.lite.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.lite.api.JobType;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.fixture.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class BaseMonitorServiceTest {
    
    protected static final int MONITOR_PORT = 9000;
    
    private static ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(EmbedTestingServer.getConnectionString(), "zkRegTestCenter");
    
    private static CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);

    @Getter(value = AccessLevel.PROTECTED)
    private static MonitorService monitorService = new MonitorService(regCenter, MONITOR_PORT);
    
    private final ScheduleJobBootstrap bootstrap;
    
    @Getter(value = AccessLevel.PROTECTED)
    private final String jobName = System.nanoTime() + "_test_job";
    
    public BaseMonitorServiceTest(final ElasticJob elasticJob) {
        bootstrap = new ScheduleJobBootstrap(regCenter, elasticJob, JobConfiguration.newBuilder(jobName, JobType.SIMPLE, 3).cron("0/1 * * * * ?").overwrite(true).build());
    }
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
        zkConfig.setConnectionTimeoutMilliseconds(30000);
        regCenter.init();
    }
    
    @Before
    public void setUp() {
        regCenter.init();
    }
    
    @After
    public void tearDown() {
        bootstrap.shutdown();
        ReflectionUtils.setFieldValue(JobRegistry.getInstance(), "instance", null);
    }
    
    protected final void scheduleJob() {
        bootstrap.schedule();
    }
}
