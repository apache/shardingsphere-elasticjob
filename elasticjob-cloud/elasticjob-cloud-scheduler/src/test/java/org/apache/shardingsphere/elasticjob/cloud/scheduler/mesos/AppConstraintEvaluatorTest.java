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

import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.CloudAppConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudAppConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.context.TaskContext;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.netflix.fenzo.ConstraintEvaluator;
import com.netflix.fenzo.SchedulingResult;
import com.netflix.fenzo.TaskRequest;
import com.netflix.fenzo.TaskScheduler;
import com.netflix.fenzo.VMAssignmentResult;
import com.netflix.fenzo.VirtualMachineLease;
import com.netflix.fenzo.functions.Action1;
import com.netflix.fenzo.plugins.VMLeaseObject;
import org.apache.mesos.Protos;
import org.codehaus.jettison.json.JSONException;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class AppConstraintEvaluatorTest {
    
    private static final double SUFFICIENT_CPU = 1.0 * 13;
    
    private static final double INSUFFICIENT_CPU = 1.0 * 11;
    
    private static final double SUFFICIENT_MEM = 128.0 * 13;
    
    private static final double INSUFFICIENT_MEM = 128.0 * 11;
    
    private static FacadeService facadeService;
    
    private TaskScheduler taskScheduler;
    
    @BeforeClass
    public static void init() throws Exception {
        facadeService = Mockito.mock(FacadeService.class);
        AppConstraintEvaluator.init(facadeService);
    }
    
    @Before
    public void setUp() throws Exception {
        taskScheduler = new TaskScheduler.Builder().withLeaseOfferExpirySecs(1000000000L).withLeaseRejectAction(new Action1<VirtualMachineLease>() {
            
            @Override
            public void call(final VirtualMachineLease virtualMachineLease) {
            }
        }).build();
    }
    
    @After
    public void tearDown() throws Exception {
        AppConstraintEvaluator.getInstance().clearAppRunningState();
    }
    
    @Test
    public void assertFirstLaunch() throws Exception {
        SchedulingResult result = taskScheduler.scheduleOnce(getTasks(), Arrays.asList(getLease(0, SUFFICIENT_CPU, SUFFICIENT_MEM), getLease(1, SUFFICIENT_CPU, SUFFICIENT_MEM)));
        Assert.assertThat(result.getResultMap().size(), Is.is(2));
        Assert.assertThat(result.getFailures().size(), Is.is(0));
        Assert.assertThat(getAssignedTaskNumber(result), Is.is(20));
    }
    
    @Test
    public void assertFirstLaunchLackCpu() throws Exception {
        SchedulingResult result = taskScheduler.scheduleOnce(getTasks(), Arrays.asList(getLease(0, INSUFFICIENT_CPU, SUFFICIENT_MEM), getLease(1, INSUFFICIENT_CPU, SUFFICIENT_MEM)));
        Assert.assertThat(result.getResultMap().size(), Is.is(2));
        Assert.assertThat(getAssignedTaskNumber(result), Is.is(18));
    }
    
    @Test
    public void assertFirstLaunchLackMem() throws Exception {
        SchedulingResult result = taskScheduler.scheduleOnce(getTasks(), Arrays.asList(getLease(0, SUFFICIENT_CPU, INSUFFICIENT_MEM), getLease(1, SUFFICIENT_CPU, INSUFFICIENT_MEM)));
        Assert.assertThat(result.getResultMap().size(), Is.is(2));
        Assert.assertThat(getAssignedTaskNumber(result), Is.is(18));
    }
    
    @Test
    public void assertExistExecutorOnS0() throws Exception {
        Mockito.when(facadeService.loadExecutorInfo()).thenReturn(ImmutableList.of(new MesosStateService.ExecutorStateInfo("foo-app@-@S0", "S0")));
        AppConstraintEvaluator.getInstance().loadAppRunningState();
        SchedulingResult result = taskScheduler.scheduleOnce(getTasks(), Arrays.asList(getLease(0, INSUFFICIENT_CPU, INSUFFICIENT_MEM), getLease(1, INSUFFICIENT_CPU, INSUFFICIENT_MEM)));
        Assert.assertThat(result.getResultMap().size(), Is.is(2));
        Assert.assertTrue(getAssignedTaskNumber(result) > 18);
    }
    
    @Test
    public void assertGetExecutorError() throws Exception {
        Mockito.when(facadeService.loadExecutorInfo()).thenThrow(JSONException.class);
        AppConstraintEvaluator.getInstance().loadAppRunningState();
        SchedulingResult result = taskScheduler.scheduleOnce(getTasks(), Arrays.asList(getLease(0, INSUFFICIENT_CPU, INSUFFICIENT_MEM), getLease(1, INSUFFICIENT_CPU, INSUFFICIENT_MEM)));
        Assert.assertThat(result.getResultMap().size(), Is.is(2));
        Assert.assertThat(getAssignedTaskNumber(result), Is.is(18));
    }
    
    @Test
    public void assertLackJobConfig() throws Exception {
        Mockito.when(facadeService.load("test")).thenReturn(Optional.<CloudJobConfiguration>absent());
        SchedulingResult result = taskScheduler.scheduleOnce(Collections.singletonList(getTask("test")), Collections.singletonList(getLease(0, 1.5, 192)));
        Assert.assertThat(result.getResultMap().size(), Is.is(1));
        Assert.assertThat(getAssignedTaskNumber(result), Is.is(1));
    }
    
    @Test
    public void assertLackAppConfig() throws Exception {
        Mockito.when(facadeService.load("test")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test")));
        Mockito.when(facadeService.loadAppConfig("test_app")).thenReturn(Optional.<CloudAppConfiguration>absent());
        SchedulingResult result = taskScheduler.scheduleOnce(Collections.singletonList(getTask("test")), Collections.singletonList(getLease(0, 1.5, 192)));
        Assert.assertThat(result.getResultMap().size(), Is.is(1));
        Assert.assertThat(getAssignedTaskNumber(result), Is.is(1));
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
            Mockito.when(facadeService.load(jobName)).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration(jobName, appName)));
        }
        Mockito.when(facadeService.loadAppConfig("foo-app")).thenReturn(Optional.of(CloudAppConfigurationBuilder.createCloudAppConfiguration("foo-app")));
        Mockito.when(facadeService.loadAppConfig("bar-app")).thenReturn(Optional.of(CloudAppConfigurationBuilder.createCloudAppConfiguration("bar-app")));
        return result;
    }
    
    private TaskRequest getTask(final String jobName) {
        TaskRequest result = Mockito.mock(TaskRequest.class);
        Mockito.when(result.getCPUs()).thenReturn(1.0d);
        Mockito.when(result.getMemory()).thenReturn(128.0d);
        Mockito.when(result.getHardConstraints()).thenAnswer(new Answer<List<? extends ConstraintEvaluator>>() {
            @Override
            public List<? extends ConstraintEvaluator> answer(final InvocationOnMock invocationOnMock) throws Throwable {
                return ImmutableList.of(AppConstraintEvaluator.getInstance());
            }
        });
        Mockito.when(result.getId()).thenReturn(new TaskContext(jobName, Collections.singletonList(0), ExecutionType.READY).getId());
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
