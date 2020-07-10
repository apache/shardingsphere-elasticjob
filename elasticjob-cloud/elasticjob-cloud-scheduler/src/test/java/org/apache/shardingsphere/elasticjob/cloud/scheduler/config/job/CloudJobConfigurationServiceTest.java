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

import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJsonConstants;
import org.apache.shardingsphere.elasticjob.cloud.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;

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
        Mockito.verify(regCenter).persist("/config/job/test_job", CloudJsonConstants.getJobJson());
    }
    
    @Test
    public void assertUpdate() {
        CloudJobConfiguration jobConfig = CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job");
        configService.update(jobConfig);
        Mockito.verify(regCenter).update("/config/job/test_job", CloudJsonConstants.getJobJson());
    }
    
    @Test
    public void assertAddSpringJob() {
        CloudJobConfiguration jobConfig = CloudJobConfigurationBuilder.createCloudSpringJobConfiguration("test_spring_job");
        configService.add(jobConfig);
        Mockito.verify(regCenter).persist("/config/job/test_spring_job", CloudJsonConstants.getSpringJobJson());
    }
    
    @Test
    public void assertLoadAllWithoutRootNode() {
        Mockito.when(regCenter.isExisted("/config/job")).thenReturn(false);
        Assert.assertTrue(configService.loadAll().isEmpty());
        Mockito.verify(regCenter).isExisted("/config/job");
    }
    
    @Test
    public void assertLoadAllWithRootNode() {
        Mockito.when(regCenter.isExisted("/config/job")).thenReturn(true);
        Mockito.when(regCenter.getChildrenKeys(CloudJobConfigurationNode.ROOT)).thenReturn(Arrays.asList("test_job_1", "test_job_2"));
        Mockito.when(regCenter.get("/config/job/test_job_1")).thenReturn(CloudJsonConstants.getJobJson("test_job_1"));
        Collection<CloudJobConfiguration> actual = configService.loadAll();
        Assert.assertThat(actual.size(), Is.is(1));
        Assert.assertThat(actual.iterator().next().getJobName(), Is.is("test_job_1"));
        Mockito.verify(regCenter).isExisted("/config/job");
        Mockito.verify(regCenter).getChildrenKeys("/config/job");
        Mockito.verify(regCenter).get("/config/job/test_job_1");
        Mockito.verify(regCenter).get("/config/job/test_job_2");
    }
    
    @Test
    public void assertLoadWithoutConfig() {
        Optional<CloudJobConfiguration> actual = configService.load("test_job");
        Assert.assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertLoadWithConfig() {
        Mockito.when(regCenter.get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        Optional<CloudJobConfiguration> actual = configService.load("test_job");
        Assert.assertTrue(actual.isPresent());
        Assert.assertThat(actual.get().getJobName(), Is.is("test_job"));
    }
    
    @Test
    public void assertLoadWithSpringConfig() {
        Mockito.when(regCenter.get("/config/job/test_spring_job")).thenReturn(CloudJsonConstants.getSpringJobJson());
        Optional<CloudJobConfiguration> actual = configService.load("test_spring_job");
        Assert.assertTrue(actual.isPresent());
        Assert.assertThat(actual.get().getBeanName(), Is.is("springSimpleJob"));
        Assert.assertThat(actual.get().getApplicationContext(), Is.is("applicationContext.xml"));
    }
    
    @Test
    public void assertRemove() {
        configService.remove("test_job");
        Mockito.verify(regCenter).remove("/config/job/test_job");
    }
}
