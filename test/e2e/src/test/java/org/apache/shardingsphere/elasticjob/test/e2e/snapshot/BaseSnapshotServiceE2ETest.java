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

package org.apache.shardingsphere.elasticjob.test.e2e.snapshot;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.kernel.internal.snapshot.SnapshotService;
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.test.util.EmbedTestingServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseSnapshotServiceE2ETest {
    
    static final int DUMP_PORT = 9000;
    
    private static final EmbedTestingServer EMBED_TESTING_SERVER = new EmbedTestingServer(7181);
    
    private static final ZookeeperConfiguration ZOOKEEPER_CONFIG = new ZookeeperConfiguration(EMBED_TESTING_SERVER.getConnectionString(), "zkRegTestCenter");
    
    @Getter(value = AccessLevel.PROTECTED)
    private static final CoordinatorRegistryCenter REG_CENTER = new ZookeeperRegistryCenter(ZOOKEEPER_CONFIG);
    
    @Getter(value = AccessLevel.PROTECTED)
    private static SnapshotService snapshotService = new SnapshotService(REG_CENTER, DUMP_PORT);
    
    private final ScheduleJobBootstrap bootstrap;
    
    @Getter(value = AccessLevel.PROTECTED)
    private final String jobName = System.nanoTime() + "_test_job";
    
    public BaseSnapshotServiceE2ETest(final ElasticJob elasticJob) {
        bootstrap = new ScheduleJobBootstrap(REG_CENTER, elasticJob, JobConfiguration.newBuilder(jobName, 3).cron("0/1 * * * * ?").overwrite(true).build());
    }
    
    @BeforeAll
    static void init() {
        EMBED_TESTING_SERVER.start();
        ZOOKEEPER_CONFIG.setConnectionTimeoutMilliseconds(30000);
        REG_CENTER.init();
    }
    
    @BeforeEach
    void setUp() {
        REG_CENTER.init();
        bootstrap.schedule();
    }
    
    @AfterEach
    void tearDown() {
        bootstrap.shutdown();
        ReflectionUtils.setFieldValue(JobRegistry.getInstance(), "instance", null);
    }
}
