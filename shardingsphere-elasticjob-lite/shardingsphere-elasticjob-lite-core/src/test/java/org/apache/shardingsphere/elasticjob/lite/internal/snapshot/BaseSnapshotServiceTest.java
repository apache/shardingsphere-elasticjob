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

package org.apache.shardingsphere.elasticjob.lite.internal.snapshot;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.fixture.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class BaseSnapshotServiceTest {
    
    static final int DUMP_PORT = 9000;
    
    private static final ZookeeperConfiguration ZOOKEEPER_CONFIG = new ZookeeperConfiguration(EmbedTestingServer.getConnectionString(), "zkRegTestCenter");
    
    @Getter(value = AccessLevel.PROTECTED)
    private static final CoordinatorRegistryCenter REG_CENTER = new ZookeeperRegistryCenter(ZOOKEEPER_CONFIG);
    
    @Getter(value = AccessLevel.PROTECTED)
    private static SnapshotService snapshotService = new SnapshotService(REG_CENTER, DUMP_PORT);
    
    private final ScheduleJobBootstrap bootstrap;
    
    @Getter(value = AccessLevel.PROTECTED)
    private final String jobName = System.nanoTime() + "_test_job";
    
    public BaseSnapshotServiceTest(final ElasticJob elasticJob) {
        bootstrap = new ScheduleJobBootstrap(REG_CENTER, elasticJob, JobConfiguration.newBuilder(jobName, 3).cron("0/1 * * * * ?").overwrite(true).build());
    }
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
        ZOOKEEPER_CONFIG.setConnectionTimeoutMilliseconds(30000);
        REG_CENTER.init();
    }
    
    @Before
    public final void setUp() {
        REG_CENTER.init();
        bootstrap.schedule();
    }
    
    @After
    public final void tearDown() {
        bootstrap.shutdown();
        ReflectionUtils.setFieldValue(JobRegistry.getInstance(), "instance", null);
    }
}
