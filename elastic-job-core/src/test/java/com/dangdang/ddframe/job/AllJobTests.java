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

package com.dangdang.ddframe.job;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContextTest;
import com.dangdang.ddframe.job.integrate.std.DisabledJobTest;
import com.dangdang.ddframe.job.integrate.std.OneOffElasticJobTest;
import com.dangdang.ddframe.job.integrate.std.PerpetualElasticJobForExecuteFailureTest;
import com.dangdang.ddframe.job.integrate.std.PerpetualElasticJobForExecuteThrowsExceptionTest;
import com.dangdang.ddframe.job.integrate.std.PerpetualElasticJobForMultipleThreadsTest;
import com.dangdang.ddframe.job.integrate.std.PerpetualElasticJobForNotMonitorTest;
import com.dangdang.ddframe.job.integrate.std.PerpetualElasticJobForStopedTest;
import com.dangdang.ddframe.job.integrate.std.PerpetualElasticJobTest;
import com.dangdang.ddframe.job.integrate.std.SequencePerpetualElasticJobTest;
import com.dangdang.ddframe.job.internal.config.ConfigurationNodeTest;
import com.dangdang.ddframe.job.internal.config.ConfigurationServiceTest;
import com.dangdang.ddframe.job.internal.election.ElectionListenerManagerTest;
import com.dangdang.ddframe.job.internal.election.ElectionNodeTest;
import com.dangdang.ddframe.job.internal.election.LeaderElectionServiceTest;
import com.dangdang.ddframe.job.internal.execution.ExecutionContextServiceTest;
import com.dangdang.ddframe.job.internal.execution.ExecutionListenerManagerTest;
import com.dangdang.ddframe.job.internal.execution.ExecutionNodeTest;
import com.dangdang.ddframe.job.internal.execution.ExecutionServiceTest;
import com.dangdang.ddframe.job.internal.failover.FailoverListenerManagerTest;
import com.dangdang.ddframe.job.internal.failover.FailoverNodeTest;
import com.dangdang.ddframe.job.internal.failover.FailoverServiceTest;
import com.dangdang.ddframe.job.internal.offset.OffsetNodeTest;
import com.dangdang.ddframe.job.internal.offset.OffsetServiceTest;
import com.dangdang.ddframe.job.internal.server.ServerNodeTest;
import com.dangdang.ddframe.job.internal.server.ServerServiceTest;
import com.dangdang.ddframe.job.internal.sharding.ShardingListenerManagerTest;
import com.dangdang.ddframe.job.internal.sharding.ShardingNodeTest;
import com.dangdang.ddframe.job.internal.sharding.ShardingServiceTest;
import com.dangdang.ddframe.job.internal.sharding.strategy.AverageAllocationJobShardingStrategyTest;
import com.dangdang.ddframe.job.internal.sharding.strategy.JobShardingStrategyFactoryTest;
import com.dangdang.ddframe.job.internal.statistics.ProcessCountJobTest;
import com.dangdang.ddframe.job.internal.statistics.ProcessCountStatisticsTest;
import com.dangdang.ddframe.job.internal.storage.JobNodePathTest;
import com.dangdang.ddframe.job.internal.util.ItemUtilsTest;

@RunWith(Suite.class)
@SuiteClasses({
    JobExecutionMultipleShardingContextTest.class, 
    JobNodePathTest.class, 
    ItemUtilsTest.class, 
    ConfigurationServiceTest.class, 
    ConfigurationNodeTest.class, 
    LeaderElectionServiceTest.class, 
    ElectionNodeTest.class, 
    ElectionListenerManagerTest.class, 
    ServerServiceTest.class, 
    ServerNodeTest.class, 
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
    JobShardingStrategyFactoryTest.class, 
    AverageAllocationJobShardingStrategyTest.class, 
    ProcessCountJobTest.class, 
    ProcessCountStatisticsTest.class, 
    DisabledJobTest.class, 
    OneOffElasticJobTest.class, 
    SequencePerpetualElasticJobTest.class, 
    PerpetualElasticJobTest.class, 
    PerpetualElasticJobForNotMonitorTest.class, 
    PerpetualElasticJobForMultipleThreadsTest.class, 
    PerpetualElasticJobForExecuteFailureTest.class, 
    PerpetualElasticJobForExecuteThrowsExceptionTest.class, 
    PerpetualElasticJobForStopedTest.class
    })
public class AllJobTests {
}
