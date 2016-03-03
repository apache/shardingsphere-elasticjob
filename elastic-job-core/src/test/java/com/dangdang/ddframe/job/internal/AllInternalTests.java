/**
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

package com.dangdang.ddframe.job.internal;

import com.dangdang.ddframe.job.internal.config.ConfigurationListenerManagerTest;
import com.dangdang.ddframe.job.internal.config.ConfigurationNodeTest;
import com.dangdang.ddframe.job.internal.config.ConfigurationServiceTest;
import com.dangdang.ddframe.job.internal.election.ElectionListenerManagerTest;
import com.dangdang.ddframe.job.internal.election.ElectionNodeTest;
import com.dangdang.ddframe.job.internal.election.LeaderElectionServiceTest;
import com.dangdang.ddframe.job.internal.env.LocalHostServiceTest;
import com.dangdang.ddframe.job.internal.execution.ExecutionContextServiceTest;
import com.dangdang.ddframe.job.internal.execution.ExecutionListenerManagerTest;
import com.dangdang.ddframe.job.internal.execution.ExecutionNodeTest;
import com.dangdang.ddframe.job.internal.execution.ExecutionServiceTest;
import com.dangdang.ddframe.job.internal.failover.FailoverListenerManagerTest;
import com.dangdang.ddframe.job.internal.failover.FailoverNodeTest;
import com.dangdang.ddframe.job.internal.failover.FailoverServiceTest;
import com.dangdang.ddframe.job.internal.listener.JobListenerTest;
import com.dangdang.ddframe.job.internal.listener.ListenerManagerTest;
import com.dangdang.ddframe.job.internal.monitor.MonitorServiceDisableTest;
import com.dangdang.ddframe.job.internal.monitor.MonitorServiceEnableTest;
import com.dangdang.ddframe.job.internal.offset.OffsetNodeTest;
import com.dangdang.ddframe.job.internal.offset.OffsetServiceTest;
import com.dangdang.ddframe.job.internal.schedule.JobRegistryTest;
import com.dangdang.ddframe.job.internal.schedule.JobTriggerListenerTest;
import com.dangdang.ddframe.job.internal.server.JobOperationListenerManagerTest;
import com.dangdang.ddframe.job.internal.server.ServerNodeTest;
import com.dangdang.ddframe.job.internal.server.ServerServiceTest;
import com.dangdang.ddframe.job.internal.sharding.ShardingListenerManagerTest;
import com.dangdang.ddframe.job.internal.sharding.ShardingNodeTest;
import com.dangdang.ddframe.job.internal.sharding.ShardingServiceTest;
import com.dangdang.ddframe.job.internal.sharding.strategy.JobShardingStrategyFactoryTest;
import com.dangdang.ddframe.job.internal.statistics.ProcessCountJobTest;
import com.dangdang.ddframe.job.internal.statistics.ProcessCountStatisticsTest;
import com.dangdang.ddframe.job.internal.statistics.StatisticsServiceTest;
import com.dangdang.ddframe.job.internal.storage.JobNodePathTest;
import com.dangdang.ddframe.job.internal.storage.JobNodeStorageTest;
import com.dangdang.ddframe.job.internal.util.ItemUtilsTest;
import com.dangdang.ddframe.job.internal.util.SensitiveInfoUtilsTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    JobNodePathTest.class, 
    JobNodeStorageTest.class, 
    ItemUtilsTest.class,
    SensitiveInfoUtilsTest.class, 
    LocalHostServiceTest.class, 
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
    JobTriggerListenerTest.class, 
    ListenerManagerTest.class, 
    JobListenerTest.class, 
    MonitorServiceEnableTest.class, 
    MonitorServiceDisableTest.class
    })
public final class AllInternalTests {
}
