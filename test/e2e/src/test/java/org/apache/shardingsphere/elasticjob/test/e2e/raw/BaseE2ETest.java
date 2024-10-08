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

package org.apache.shardingsphere.elasticjob.test.e2e.raw;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.bootstrap.JobBootstrap;
import org.apache.shardingsphere.elasticjob.bootstrap.type.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.bootstrap.type.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.kernel.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.test.util.EmbedTestingServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

@Getter(AccessLevel.PROTECTED)
public abstract class BaseE2ETest {
    
    private static final EmbedTestingServer EMBED_TESTING_SERVER = new EmbedTestingServer();
    
    private static ZookeeperConfiguration zookeeperConfig;
    
    @Getter(AccessLevel.PROTECTED)
    private static CoordinatorRegistryCenter registryCenter;
    
    private final ElasticJob elasticJob;
    
    private final JobConfiguration jobConfiguration;
    
    private final JobBootstrap jobBootstrap;
    
    private final LeaderService leaderService;
    
    private final String jobName = System.nanoTime() + "_test_job";
    
    protected BaseE2ETest(final TestType type, final ElasticJob elasticJob) {
        this.elasticJob = elasticJob;
        jobConfiguration = getJobConfiguration(jobName);
        jobBootstrap = createJobBootstrap(type, elasticJob);
        leaderService = new LeaderService(registryCenter, jobName);
    }
    
    protected abstract JobConfiguration getJobConfiguration(String jobName);
    
    private JobBootstrap createJobBootstrap(final TestType type, final ElasticJob elasticJob) {
        switch (type) {
            case SCHEDULE:
                return new ScheduleJobBootstrap(registryCenter, elasticJob, jobConfiguration);
            case ONE_OFF:
                return new OneOffJobBootstrap(registryCenter, elasticJob, jobConfiguration);
            default:
                throw new RuntimeException(String.format("Cannot support `%s`", type));
        }
    }
    
    @BeforeAll
    static void init() {
        EMBED_TESTING_SERVER.start();
        zookeeperConfig = new ZookeeperConfiguration(EMBED_TESTING_SERVER.getConnectionString(), "zkRegTestCenter");
        registryCenter = new ZookeeperRegistryCenter(zookeeperConfig);
        zookeeperConfig.setConnectionTimeoutMilliseconds(30000);
        registryCenter.init();
    }
    
    @BeforeEach
    void setUp() {
        if (jobBootstrap instanceof ScheduleJobBootstrap) {
            ((ScheduleJobBootstrap) jobBootstrap).schedule();
        } else {
            ((OneOffJobBootstrap) jobBootstrap).execute();
        }
    }
    
    @AfterEach
    void tearDown() {
        jobBootstrap.shutdown();
        ReflectionUtils.setFieldValue(JobRegistry.getInstance(), "instance", null);
    }
    
    public enum TestType {
        
        SCHEDULE, ONE_OFF
    }
}
