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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job;

import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJsonConstants;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CloudJobConfigurationServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @InjectMocks
    private CloudJobConfigurationService configService;
    
    @Test
    public void assertAdd() {
        CloudJobConfiguration jobConfig = CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job");
        configService.add(jobConfig);
        verify(regCenter).persist("/config/job/test_job", CloudJsonConstants.getJobJson());
    }
    
    @Test
    public void assertUpdate() {
        CloudJobConfiguration jobConfig = CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job");
        configService.update(jobConfig);
        verify(regCenter).update("/config/job/test_job", CloudJsonConstants.getJobJson());
    }
    
    @Test
    public void assertAddSpringJob() {
        CloudJobConfiguration jobConfig = CloudJobConfigurationBuilder.createCloudSpringJobConfiguration("test_spring_job");
        configService.add(jobConfig);
        verify(regCenter).persist("/config/job/test_spring_job", CloudJsonConstants.getSpringJobJson());
    }
    
    @Test
    public void assertLoadAllWithoutRootNode() {
        when(regCenter.isExisted("/config/job")).thenReturn(false);
        assertTrue(configService.loadAll().isEmpty());
        verify(regCenter).isExisted("/config/job");
    }
    
    @Test
    public void assertLoadAllWithRootNode() {
        when(regCenter.isExisted("/config/job")).thenReturn(true);
        when(regCenter.getChildrenKeys(CloudJobConfigurationNode.ROOT)).thenReturn(Arrays.asList("test_job_1", "test_job_2"));
        when(regCenter.get("/config/job/test_job_1")).thenReturn(CloudJsonConstants.getJobJson("test_job_1"));
        Collection<CloudJobConfiguration> actual = configService.loadAll();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getJobName(), is("test_job_1"));
        verify(regCenter).isExisted("/config/job");
        verify(regCenter).getChildrenKeys("/config/job");
        verify(regCenter).get("/config/job/test_job_1");
        verify(regCenter).get("/config/job/test_job_2");
    }
    
    @Test
    public void assertLoadWithoutConfig() {
        assertFalse(configService.load("test_job").isPresent());
    }
    
    @Test
    public void assertLoadWithConfig() {
        when(regCenter.get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        Optional<CloudJobConfiguration> actual = configService.load("test_job");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getJobName(), is("test_job"));
    }
    
    @Test
    public void assertLoadWithSpringConfig() {
        when(regCenter.get("/config/job/test_spring_job")).thenReturn(CloudJsonConstants.getSpringJobJson());
        Optional<CloudJobConfiguration> actual = configService.load("test_spring_job");
        assertTrue(actual.isPresent());
    }
    
    @Test
    public void assertRemove() {
        configService.remove("test_job");
        verify(regCenter).remove("/config/job/test_job");
    }
}
