/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.internal;

import com.dangdang.ddframe.job.cloud.internal.election.ElectionListenerManagerTest;
import com.dangdang.ddframe.job.cloud.internal.election.ElectionNodeTest;
import com.dangdang.ddframe.job.cloud.internal.schedule.JobFacadeTest;
import com.dangdang.ddframe.job.cloud.internal.sharding.ShardingServiceTest;
import com.dangdang.ddframe.job.cloud.internal.sharding.strategy.JobShardingStrategyFactoryTest;
import com.dangdang.ddframe.job.cloud.internal.statistics.ProcessCountJobTest;
import com.dangdang.ddframe.job.cloud.internal.config.ConfigurationListenerManagerTest;
import com.dangdang.ddframe.job.cloud.internal.config.ConfigurationNodeTest;
import com.dangdang.ddframe.job.cloud.internal.config.ConfigurationServiceTest;
import com.dangdang.ddframe.job.cloud.internal.election.LeaderElectionServiceTest;
import com.dangdang.ddframe.job.cloud.internal.execution.ExecutionContextServiceTest;
import com.dangdang.ddframe.job.cloud.internal.execution.ExecutionListenerManagerTest;
import com.dangdang.ddframe.job.cloud.internal.execution.ExecutionNodeTest;
import com.dangdang.ddframe.job.cloud.internal.execution.ExecutionServiceTest;
import com.dangdang.ddframe.job.cloud.internal.executor.JobExecutorTest;
import com.dangdang.ddframe.job.cloud.internal.failover.FailoverListenerManagerTest;
import com.dangdang.ddframe.job.cloud.internal.failover.FailoverNodeTest;
import com.dangdang.ddframe.job.cloud.internal.failover.FailoverServiceTest;
import com.dangdang.ddframe.job.cloud.internal.guarantee.GuaranteeNodeTest;
import com.dangdang.ddframe.job.cloud.internal.guarantee.GuaranteeServiceTest;
import com.dangdang.ddframe.job.cloud.internal.listener.JobListenerTest;
import com.dangdang.ddframe.job.cloud.internal.listener.ListenerManagerTest;
import com.dangdang.ddframe.job.cloud.internal.monitor.MonitorServiceDisableTest;
import com.dangdang.ddframe.job.cloud.internal.monitor.MonitorServiceEnableTest;
import com.dangdang.ddframe.job.cloud.internal.offset.OffsetNodeTest;
import com.dangdang.ddframe.job.cloud.internal.offset.OffsetServiceTest;
import com.dangdang.ddframe.job.cloud.internal.reg.ItemUtilsTest;
import com.dangdang.ddframe.job.cloud.internal.reg.SensitiveInfoUtilsTest;
import com.dangdang.ddframe.job.cloud.internal.schedule.JobRegistryTest;
import com.dangdang.ddframe.job.cloud.internal.schedule.JobScheduleControllerTest;
import com.dangdang.ddframe.job.cloud.internal.schedule.JobTriggerListenerTest;
import com.dangdang.ddframe.job.cloud.internal.schedule.SchedulerFacadeTest;
import com.dangdang.ddframe.job.cloud.internal.server.JobOperationListenerManagerTest;
import com.dangdang.ddframe.job.cloud.internal.server.ServerNodeTest;
import com.dangdang.ddframe.job.cloud.internal.server.ServerServiceTest;
import com.dangdang.ddframe.job.cloud.internal.sharding.ShardingListenerManagerTest;
import com.dangdang.ddframe.job.cloud.internal.sharding.ShardingNodeTest;
import com.dangdang.ddframe.job.cloud.internal.statistics.ProcessCountStatisticsTest;
import com.dangdang.ddframe.job.cloud.internal.statistics.StatisticsServiceTest;
import com.dangdang.ddframe.job.cloud.internal.storage.JobNodePathTest;
import com.dangdang.ddframe.job.cloud.internal.storage.JobNodeStorageTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    JobNodePathTest.class, 
    JobNodeStorageTest.class, 
    ItemUtilsTest.class,
    SensitiveInfoUtilsTest.class,
    ConfigurationServiceTest.class, 
    ConfigurationNodeTest.class, 
    ConfigurationListenerManagerTest.class, 
    LeaderElectionServiceTest.class, 
    ElectionNodeTest.class, 
    ElectionListenerManagerTest.class, 
    ServerServiceTest.class, 
    ServerNodeTest.class, 
    JobOperationListenerManagerTest.class, 
    ShardingServiceTest.class, 
    ShardingNodeTest.class, 
    ShardingListenerManagerTest.class, 
    ExecutionContextServiceTest.class, 
    ExecutionServiceTest.class, 
    ExecutionNodeTest.class, 
    ExecutionListenerManagerTest.class, 
    FailoverServiceTest.class, 
    FailoverNodeTest.class, 
    FailoverListenerManagerTest.class, 
    OffsetServiceTest.class, 
    OffsetNodeTest.class, 
    StatisticsServiceTest.class, 
    ProcessCountJobTest.class, 
    ProcessCountStatisticsTest.class, 
    JobShardingStrategyFactoryTest.class, 
    JobRegistryTest.class,
    JobScheduleControllerTest.class, 
    JobTriggerListenerTest.class, 
    ListenerManagerTest.class, 
    JobListenerTest.class, 
    MonitorServiceEnableTest.class, 
    MonitorServiceDisableTest.class, 
    GuaranteeNodeTest.class,
    GuaranteeServiceTest.class, 
    SchedulerFacadeTest.class,
    JobFacadeTest.class, 
    JobExecutorTest.class
    })
public final class AllInternalTests {
}
