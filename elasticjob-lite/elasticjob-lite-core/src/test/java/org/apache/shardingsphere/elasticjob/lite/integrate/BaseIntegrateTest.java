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

package org.apache.shardingsphere.elasticjob.lite.integrate;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.JobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.fixture.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.lite.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

@Getter(AccessLevel.PROTECTED)
public abstract class BaseIntegrateTest {
    
    private static final ZookeeperConfiguration ZOOKEEPER_CONFIG = new ZookeeperConfiguration(EmbedTestingServer.getConnectionString(), "zkRegTestCenter");
    
    @Getter(AccessLevel.PROTECTED)
    private static final CoordinatorRegistryCenter REGISTRY_CENTER = new ZookeeperRegistryCenter(ZOOKEEPER_CONFIG);
    
    private final ElasticJob elasticJob;
            
    private final JobConfiguration jobConfiguration;
    
    private final JobBootstrap jobBootstrap;
    
    private final LeaderService leaderService;
    
    private final String jobName = System.nanoTime() + "_test_job";
    
    protected BaseIntegrateTest(final TestType type, final ElasticJob elasticJob) {
        this.elasticJob = elasticJob;
        jobConfiguration = getJobConfiguration(jobName);
        jobBootstrap = createJobBootstrap(type, elasticJob);
        leaderService = new LeaderService(REGISTRY_CENTER, jobName);
    }
    
    protected abstract JobConfiguration getJobConfiguration(String jobName);
    
    private JobBootstrap createJobBootstrap(final TestType type, final ElasticJob elasticJob) {
        switch (type) {
            case SCHEDULE:
                return new ScheduleJobBootstrap(REGISTRY_CENTER, elasticJob, jobConfiguration);
            case ONE_OFF:
                return new OneOffJobBootstrap(REGISTRY_CENTER, elasticJob, jobConfiguration);
            default:
                throw new RuntimeException(String.format("Cannot support `%s`", type));
        }
    }
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
        ZOOKEEPER_CONFIG.setConnectionTimeoutMilliseconds(30000);
        REGISTRY_CENTER.init();
    }
    
    @Before
    public void setUp() {
        if (jobBootstrap instanceof ScheduleJobBootstrap) {
            ((ScheduleJobBootstrap) jobBootstrap).schedule();
        } else {
            ((OneOffJobBootstrap) jobBootstrap).execute();
        }
    }
    
    @After
    public void tearDown() {
        jobBootstrap.shutdown();
        ReflectionUtils.setFieldValue(JobRegistry.getInstance(), "instance", null);
    }
    
    public enum TestType {
        
        SCHEDULE, ONE_OFF
    }
}
