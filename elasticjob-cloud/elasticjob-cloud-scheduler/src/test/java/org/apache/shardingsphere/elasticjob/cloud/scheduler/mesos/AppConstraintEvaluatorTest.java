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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos;

import com.google.gson.JsonParseException;
import com.netflix.fenzo.ConstraintEvaluator;
import com.netflix.fenzo.SchedulingResult;
import com.netflix.fenzo.TaskRequest;
import com.netflix.fenzo.TaskScheduler;
import com.netflix.fenzo.VMAssignmentResult;
import com.netflix.fenzo.VirtualMachineLease;
import com.netflix.fenzo.plugins.VMLeaseObject;
import org.apache.mesos.Protos;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudAppConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.MesosStateService.ExecutorStateInfo;
import org.apache.shardingsphere.elasticjob.infra.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AppConstraintEvaluatorTest {
    
    private static final double SUFFICIENT_CPU = 1.0 * 13;
    
    private static final double INSUFFICIENT_CPU = 1.0 * 11;
    
    private static final double SUFFICIENT_MEM = 128.0 * 13;
    
    private static final double INSUFFICIENT_MEM = 128.0 * 11;
    
    private static FacadeService facadeService;
    
    private TaskScheduler taskScheduler;
    
    @BeforeClass
    public static void init() {
        facadeService = mock(FacadeService.class);
        AppConstraintEvaluator.init(facadeService);
    }
    
    @Before
    public void setUp() {
        taskScheduler = new TaskScheduler.Builder().withLeaseOfferExpirySecs(1000000000L).withLeaseRejectAction(virtualMachineLease -> {
        }).build();
    }
    
    @After
    public void tearDown() {
        AppConstraintEvaluator.getInstance().clearAppRunningState();
    }
    
    @Test
    public void assertFirstLaunch() {
        SchedulingResult result = taskScheduler.scheduleOnce(getTasks(), Arrays.asList(getLease(0, SUFFICIENT_CPU, SUFFICIENT_MEM), getLease(1, SUFFICIENT_CPU, SUFFICIENT_MEM)));
        assertThat(result.getResultMap().size(), is(2));
        assertThat(result.getFailures().size(), is(0));
        assertThat(getAssignedTaskNumber(result), is(20));
    }
    
    @Test
    public void assertFirstLaunchLackCpu() {
        SchedulingResult result = taskScheduler.scheduleOnce(getTasks(), Arrays.asList(getLease(0, INSUFFICIENT_CPU, SUFFICIENT_MEM), getLease(1, INSUFFICIENT_CPU, SUFFICIENT_MEM)));
        assertThat(result.getResultMap().size(), is(2));
        assertThat(getAssignedTaskNumber(result), is(18));
    }
    
    @Test
    public void assertFirstLaunchLackMem() {
        SchedulingResult result = taskScheduler.scheduleOnce(getTasks(), Arrays.asList(getLease(0, SUFFICIENT_CPU, INSUFFICIENT_MEM), getLease(1, SUFFICIENT_CPU, INSUFFICIENT_MEM)));
        assertThat(result.getResultMap().size(), is(2));
        assertThat(getAssignedTaskNumber(result), is(18));
    }
    
    @Test
    public void assertExistExecutorOnS0() {
        when(facadeService.loadExecutorInfo()).thenReturn(Collections.singletonList(new ExecutorStateInfo("foo-app@-@S0", "S0")));
        AppConstraintEvaluator.getInstance().loadAppRunningState();
        SchedulingResult result = taskScheduler.scheduleOnce(getTasks(), Arrays.asList(getLease(0, INSUFFICIENT_CPU, INSUFFICIENT_MEM), getLease(1, INSUFFICIENT_CPU, INSUFFICIENT_MEM)));
        assertThat(result.getResultMap().size(), is(2));
        assertTrue(getAssignedTaskNumber(result) > 18);
    }
    
    @Test
    public void assertGetExecutorError() {
        when(facadeService.loadExecutorInfo()).thenThrow(JsonParseException.class);
        AppConstraintEvaluator.getInstance().loadAppRunningState();
        SchedulingResult result = taskScheduler.scheduleOnce(getTasks(), Arrays.asList(getLease(0, INSUFFICIENT_CPU, INSUFFICIENT_MEM), getLease(1, INSUFFICIENT_CPU, INSUFFICIENT_MEM)));
        assertThat(result.getResultMap().size(), is(2));
        assertThat(getAssignedTaskNumber(result), is(18));
    }
    
    @Test
    public void assertLackJobConfig() {
        when(facadeService.load("test")).thenReturn(Optional.empty());
        SchedulingResult result = taskScheduler.scheduleOnce(Collections.singletonList(getTask("test")), Collections.singletonList(getLease(0, 1.5, 192)));
        assertThat(result.getResultMap().size(), is(1));
        assertThat(getAssignedTaskNumber(result), is(1));
    }
    
    @Test
    public void assertLackAppConfig() {
        when(facadeService.load("test")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test")));
        when(facadeService.loadAppConfig("test_app")).thenReturn(Optional.empty());
        SchedulingResult result = taskScheduler.scheduleOnce(Collections.singletonList(getTask("test")), Collections.singletonList(getLease(0, 1.5, 192)));
        assertThat(result.getResultMap().size(), is(1));
        assertThat(getAssignedTaskNumber(result), is(1));
    }
    
    private VirtualMachineLease getLease(final int index, final double cpus, final double mem) {
        return new VMLeaseObject(Protos.Offer.newBuilder()
                .setId(Protos.OfferID.newBuilder().setValue("offer" + index))
                .setSlaveId(Protos.SlaveID.newBuilder().setValue("S" + index))
                .setHostname("slave" + index)
                .setFrameworkId(Protos.FrameworkID.newBuilder().setValue("f1"))
                .addResources(Protos.Resource.newBuilder().setName("cpus").setType(Protos.Value.Type.SCALAR).setScalar(Protos.Value.Scalar.newBuilder().setValue(cpus)))
                .addResources(Protos.Resource.newBuilder().setName("mem").setType(Protos.Value.Type.SCALAR).setScalar(Protos.Value.Scalar.newBuilder().setValue(mem)))
                .build());
    }
    
    private List<TaskRequest> getTasks() {
        List<TaskRequest> result = new ArrayList<>(20);
        for (int i = 0; i < 20; i++) {
            String jobName;
            String appName;
            if (i % 2 == 0) {
                jobName = String.format("foo-%d", i);
                appName = "foo-app";
            } else {
                jobName = String.format("bar-%d", i);
                appName = "bar-app";
            }
            result.add(getTask(jobName));
            when(facadeService.load(jobName)).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration(jobName, appName)));
        }
        when(facadeService.loadAppConfig("foo-app")).thenReturn(Optional.of(CloudAppConfigurationBuilder.createCloudAppConfiguration("foo-app")));
        when(facadeService.loadAppConfig("bar-app")).thenReturn(Optional.of(CloudAppConfigurationBuilder.createCloudAppConfiguration("bar-app")));
        return result;
    }
    
    private TaskRequest getTask(final String jobName) {
        TaskRequest result = mock(TaskRequest.class);
        when(result.getCPUs()).thenReturn(1.0d);
        when(result.getMemory()).thenReturn(128.0d);
        when(result.getHardConstraints()).thenAnswer((Answer<List<? extends ConstraintEvaluator>>) invocationOnMock -> Collections.singletonList(AppConstraintEvaluator.getInstance()));
        when(result.getId()).thenReturn(new TaskContext(jobName, Collections.singletonList(0), ExecutionType.READY).getId());
        return result;
    }
    
    private int getAssignedTaskNumber(final SchedulingResult schedulingResult) {
        int result = 0;
        for (VMAssignmentResult each : schedulingResult.getResultMap().values()) {
            result += each.getTasksAssigned().size();
        }
        return result;
    }
}
