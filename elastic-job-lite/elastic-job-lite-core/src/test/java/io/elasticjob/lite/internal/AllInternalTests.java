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

package io.elasticjob.lite.internal;

import io.elasticjob.lite.internal.config.ConfigurationNodeTest;
import io.elasticjob.lite.internal.config.ConfigurationServiceTest;
import io.elasticjob.lite.internal.config.LiteJobConfigurationGsonFactoryTest;
import io.elasticjob.lite.internal.config.RescheduleListenerManagerTest;
import io.elasticjob.lite.internal.election.ElectionListenerManagerTest;
import io.elasticjob.lite.internal.election.LeaderNodeTest;
import io.elasticjob.lite.internal.election.LeaderServiceTest;
import io.elasticjob.lite.internal.failover.FailoverListenerManagerTest;
import io.elasticjob.lite.internal.failover.FailoverNodeTest;
import io.elasticjob.lite.internal.failover.FailoverServiceTest;
import io.elasticjob.lite.internal.guarantee.GuaranteeNodeTest;
import io.elasticjob.lite.internal.guarantee.GuaranteeServiceTest;
import io.elasticjob.lite.internal.instance.InstanceNodeTest;
import io.elasticjob.lite.internal.instance.InstanceServiceTest;
import io.elasticjob.lite.internal.instance.ShutdownListenerManagerTest;
import io.elasticjob.lite.internal.instance.TriggerListenerManagerTest;
import io.elasticjob.lite.internal.listener.JobListenerTest;
import io.elasticjob.lite.internal.listener.ListenerManagerTest;
import io.elasticjob.lite.internal.listener.RegistryCenterConnectionStateListenerTest;
import io.elasticjob.lite.internal.monitor.MonitorServiceDisableTest;
import io.elasticjob.lite.internal.monitor.MonitorServiceEnableTest;
import io.elasticjob.lite.internal.reconcile.ReconcileServiceTest;
import io.elasticjob.lite.internal.schedule.JobRegistryTest;
import io.elasticjob.lite.internal.schedule.JobScheduleControllerTest;
import io.elasticjob.lite.internal.schedule.JobTriggerListenerTest;
import io.elasticjob.lite.internal.schedule.LiteJobFacadeTest;
import io.elasticjob.lite.internal.schedule.SchedulerFacadeTest;
import io.elasticjob.lite.internal.server.ServerNodeTest;
import io.elasticjob.lite.internal.server.ServerServiceTest;
import io.elasticjob.lite.internal.sharding.ExecutionContextServiceTest;
import io.elasticjob.lite.internal.sharding.ExecutionServiceTest;
import io.elasticjob.lite.internal.sharding.MonitorExecutionListenerManagerTest;
import io.elasticjob.lite.internal.sharding.ShardingListenerManagerTest;
import io.elasticjob.lite.internal.sharding.ShardingNodeTest;
import io.elasticjob.lite.internal.sharding.ShardingServiceTest;
import io.elasticjob.lite.internal.storage.JobNodePathTest;
import io.elasticjob.lite.internal.storage.JobNodeStorageTest;
import io.elasticjob.lite.internal.util.SensitiveInfoUtilsTest;
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
        RescheduleListenerManagerTest.class,
        LiteJobConfigurationGsonFactoryTest.class, 
        LeaderServiceTest.class, 
        LeaderNodeTest.class,
        ElectionListenerManagerTest.class, 
        ServerServiceTest.class, 
        InstanceNodeTest.class,
        InstanceServiceTest.class,
        ShutdownListenerManagerTest.class,
        TriggerListenerManagerTest.class,
        ShardingServiceTest.class, 
        ServerNodeTest.class,
        ShardingListenerManagerTest.class, 
        ExecutionContextServiceTest.class, 
        ExecutionServiceTest.class,
        MonitorExecutionListenerManagerTest.class, 
        ShardingNodeTest.class,
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
        ReconcileServiceTest.class,
        RegistryCenterConnectionStateListenerTest.class
    })
public final class AllInternalTests {
}
