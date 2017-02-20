/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.lite.internal;

import com.dangdang.ddframe.job.lite.internal.config.ConfigurationListenerManagerTest;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationNodeTest;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationServiceTest;
import com.dangdang.ddframe.job.lite.internal.config.LiteJobConfigurationGsonFactoryTest;
import com.dangdang.ddframe.job.lite.internal.election.ElectionListenerManagerTest;
import com.dangdang.ddframe.job.lite.internal.election.ElectionNodeTest;
import com.dangdang.ddframe.job.lite.internal.election.LeaderElectionServiceTest;
import com.dangdang.ddframe.job.lite.internal.execution.ExecutionContextServiceTest;
import com.dangdang.ddframe.job.lite.internal.execution.ExecutionListenerManagerTest;
import com.dangdang.ddframe.job.lite.internal.execution.ExecutionNodeTest;
import com.dangdang.ddframe.job.lite.internal.execution.ExecutionServiceTest;
import com.dangdang.ddframe.job.lite.internal.executor.JobExecutorTest;
import com.dangdang.ddframe.job.lite.internal.failover.FailoverListenerManagerTest;
import com.dangdang.ddframe.job.lite.internal.failover.FailoverNodeTest;
import com.dangdang.ddframe.job.lite.internal.failover.FailoverServiceTest;
import com.dangdang.ddframe.job.lite.internal.guarantee.GuaranteeNodeTest;
import com.dangdang.ddframe.job.lite.internal.guarantee.GuaranteeServiceTest;
import com.dangdang.ddframe.job.lite.internal.listener.JobListenerTest;
import com.dangdang.ddframe.job.lite.internal.listener.ListenerManagerTest;
import com.dangdang.ddframe.job.lite.internal.monitor.MonitorServiceDisableTest;
import com.dangdang.ddframe.job.lite.internal.monitor.MonitorServiceEnableTest;
import com.dangdang.ddframe.job.lite.internal.reconcile.ReconcileServiceTest;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistryTest;
import com.dangdang.ddframe.job.lite.internal.schedule.JobScheduleControllerTest;
import com.dangdang.ddframe.job.lite.internal.schedule.JobTriggerListenerTest;
import com.dangdang.ddframe.job.lite.internal.schedule.LiteJobFacadeTest;
import com.dangdang.ddframe.job.lite.internal.schedule.SchedulerFacadeTest;
import com.dangdang.ddframe.job.lite.internal.server.JobOperationListenerManagerTest;
import com.dangdang.ddframe.job.lite.internal.server.ServerNodeTest;
import com.dangdang.ddframe.job.lite.internal.server.ServerServiceTest;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingListenerManagerTest;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingNodeTest;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingServiceTest;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodePathTest;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorageTest;
import com.dangdang.ddframe.job.lite.internal.util.SensitiveInfoUtilsTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    JobNodePathTest.class,
    JobNodeStorageTest.class,
    SensitiveInfoUtilsTest.class,
    ConfigurationServiceTest.class, 
    ConfigurationNodeTest.class,
    ConfigurationListenerManagerTest.class,
    LiteJobConfigurationGsonFactoryTest.class, 
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
    LiteJobFacadeTest.class, 
    JobExecutorTest.class,
    ReconcileServiceTest.class,
    ReconcileServiceTest.class
    })
public final class AllInternalTests {
}
