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

package com.dangdang.ddframe.job.cloud.scheduler.mesos;

import com.dangdang.ddframe.job.cloud.scheduler.context.ExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.context.JobContext;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.fixture.OfferBuilder;
import com.dangdang.ddframe.job.cloud.scheduler.state.fixture.CloudJobConfigurationBuilder;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.TaskInfo;
import org.junit.Test;
import org.unitils.util.ReflectionUtils;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class HardwareResourceTest {
    
    @Test
    public void assertCalculateShardingCountWithEnoughResource() {
        HardwareResource hardwareResource = new HardwareResource(OfferBuilder.createOffer(10d, 1280d));
        assertThat(hardwareResource.calculateShardingCount(5, 1d, 128d), is(5));
    }
    
    @Test
    public void assertCalculateShardingCountWhenCpuNotEnough() {
        HardwareResource hardwareResource = new HardwareResource(OfferBuilder.createOffer(5d, 1280d));
        assertThat(hardwareResource.calculateShardingCount(10, 1d, 128d), is(5));
    }
    
    @Test
    public void assertCalculateShardingCountWhenMemoryNotEnough() {
        HardwareResource hardwareResource = new HardwareResource(OfferBuilder.createOffer(10d, 512d));
        assertThat(hardwareResource.calculateShardingCount(10, 1d, 128d), is(4));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertReserveResourcesWhenCpuNotEnough() {
        HardwareResource hardwareResource = new HardwareResource(OfferBuilder.createOffer(10d, 1280d));
        hardwareResource.reserveResources(20d, 1280d);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertReserveResourcesWhenMemoryNotEnough() {
        HardwareResource hardwareResource = new HardwareResource(OfferBuilder.createOffer(10d, 1280d));
        hardwareResource.reserveResources(10d, 12800d);
    }
    
    @Test
    public void assertReserveResources() {
        HardwareResource hardwareResource = new HardwareResource(OfferBuilder.createOffer(10d, 1280d));
        hardwareResource.reserveResources(4d, 512d);
        assertThat((BigDecimal) ReflectionUtils.getFieldValue(hardwareResource, ReflectionUtils.getFieldWithName(HardwareResource.class, "reservedCpuCount", false)), is(new BigDecimal("4.0")));
        assertThat((BigDecimal) ReflectionUtils.getFieldValue(hardwareResource, ReflectionUtils.getFieldWithName(HardwareResource.class, "reservedMemoryMB", false)), is(new BigDecimal("512.0")));
    }
    
    @Test
    public void assertCommitReservedResources() {
        HardwareResource hardwareResource = new HardwareResource(OfferBuilder.createOffer(10d, 1280d));
        hardwareResource.reserveResources(4d, 512d);
        hardwareResource.commitReservedResources();
        assertThat((BigDecimal) ReflectionUtils.getFieldValue(hardwareResource, ReflectionUtils.getFieldWithName(HardwareResource.class, "reservedCpuCount", false)), is(new BigDecimal("0")));
        assertThat((BigDecimal) ReflectionUtils.getFieldValue(hardwareResource, ReflectionUtils.getFieldWithName(HardwareResource.class, "reservedMemoryMB", false)), is(new BigDecimal("0")));
        assertThat((BigDecimal) ReflectionUtils.getFieldValue(hardwareResource, ReflectionUtils.getFieldWithName(HardwareResource.class, "availableCpuCount", false)), is(new BigDecimal("6.0")));
        assertThat((BigDecimal) ReflectionUtils.getFieldValue(hardwareResource, ReflectionUtils.getFieldWithName(HardwareResource.class, "availableMemoryMB", false)), is(new BigDecimal("768.0")));
    }
    
    @Test
    public void assertCreateTaskInfo() {
        HardwareResource hardwareResource = new HardwareResource(OfferBuilder.createOffer(10d, 1280d));
        Protos.TaskInfo actual = hardwareResource.createTaskInfo(JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job"), ExecutionType.READY), 0);
        assertGetBasicTaskInfo(actual);
        assertGetResources(actual);
        assertGetCommand(actual);
    }
    
    private void assertGetBasicTaskInfo(final TaskInfo actual) {
        assertThat(actual.getTaskId().getValue(), startsWith("test_job@-@0@-@READY@-@"));
        assertThat(actual.getName(), startsWith("test_job@-@0@-@READY@-@"));
        assertThat(actual.getSlaveId().getValue(), is("slave-offer_id_0"));
    }
    
    private void assertGetResources(final TaskInfo actual) {
        assertThat(actual.getResources(0).getName(), is("cpus"));
        assertThat(actual.getResources(0).getScalar().getValue(), is(1d));
        assertThat(actual.getResources(1).getName(), is("mem"));
        assertThat(actual.getResources(1).getScalar().getValue(), is(128d));
    }
    
    private void assertGetCommand(final TaskInfo actual) {
        assertThat(actual.getExecutor().getCommand().getUris(0).getValue(), is("http://localhost/app.jar"));
        assertThat(actual.getExecutor().getCommand().getValue(), is("bin/start.sh"));
    }
    
    @Test
    public void assertCreateTaskInfoWithEventConfig() {
        HardwareResource hardwareResource = new HardwareResource(OfferBuilder.createOffer(10d, 1280d));
        Protos.TaskInfo actual = hardwareResource.createTaskInfo(JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfigurationWithEventConfiguration("test_job"), ExecutionType.READY), 0);
        assertGetCommandWithEventConfig(actual);
    }
    
    private void assertGetCommandWithEventConfig(final TaskInfo actual) {
        assertThat(actual.getExecutor().getCommand().getValue(), is("bin/start.sh"));
    }
    
    @Test
    public void assertEquals() {
        assertThat(new HardwareResource(OfferBuilder.createOffer("offer_id_0", 10d, 1280d)), is(new HardwareResource(OfferBuilder.createOffer("offer_id_0", 1d, 128d))));
        assertThat(new HardwareResource(OfferBuilder.createOffer("offer_id_0", 10d, 1280d)), not(new HardwareResource(OfferBuilder.createOffer("offer_id_1", 10d, 1280d))));
    }
    
    @Test
    public void assertInitWithoutCpuResource() {
        HardwareResource hardwareResource = new HardwareResource(OfferBuilder.createOffer(1280d));
        assertThat((BigDecimal) ReflectionUtils.getFieldValue(hardwareResource, ReflectionUtils.getFieldWithName(HardwareResource.class, "availableCpuCount", false)), is(new BigDecimal("0")));
        assertThat((BigDecimal) ReflectionUtils.getFieldValue(hardwareResource, ReflectionUtils.getFieldWithName(HardwareResource.class, "availableMemoryMB", false)), is(new BigDecimal("1280.0")));
    }
}
