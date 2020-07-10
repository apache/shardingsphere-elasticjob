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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app;

import org.apache.shardingsphere.elasticjob.cloud.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudAppConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudAppJsonConstants;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;

@RunWith(MockitoJUnitRunner.class)
public final class CloudAppConfigurationServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @InjectMocks
    private CloudAppConfigurationService configService;
    
    @Test
    public void assertAdd() {
        CloudAppConfiguration appConfig = CloudAppConfigurationBuilder.createCloudAppConfiguration("test_app");
        configService.add(appConfig);
        Mockito.verify(regCenter).persist("/config/app/test_app", CloudAppJsonConstants.getAppJson("test_app"));
    }
    
    @Test
    public void assertUpdate() {
        CloudAppConfiguration appConfig = CloudAppConfigurationBuilder.createCloudAppConfiguration("test_app");
        configService.update(appConfig);
        Mockito.verify(regCenter).update("/config/app/test_app", CloudAppJsonConstants.getAppJson("test_app"));
    }
    
    @Test
    public void assertLoadAllWithoutRootNode() {
        Mockito.when(regCenter.isExisted("/config/app")).thenReturn(false);
        Assert.assertTrue(configService.loadAll().isEmpty());
        Mockito.verify(regCenter).isExisted("/config/app");
    }
    
    @Test
    public void assertLoadAllWithRootNode() {
        Mockito.when(regCenter.isExisted("/config/app")).thenReturn(true);
        Mockito.when(regCenter.getChildrenKeys(CloudAppConfigurationNode.ROOT)).thenReturn(Arrays.asList("test_app_1", "test_app_2"));
        Mockito.when(regCenter.get("/config/app/test_app_1")).thenReturn(CloudAppJsonConstants.getAppJson("test_app_1"));
        Collection<CloudAppConfiguration> actual = configService.loadAll();
        Assert.assertThat(actual.size(), is(1));
        Assert.assertThat(actual.iterator().next().getAppName(), is("test_app_1"));
        Mockito.verify(regCenter).isExisted("/config/app");
        Mockito.verify(regCenter).getChildrenKeys("/config/app");
        Mockito.verify(regCenter).get("/config/app/test_app_1");
        Mockito.verify(regCenter).get("/config/app/test_app_2");
    }
    
    @Test
    public void assertLoadWithoutConfig() {
        Optional<CloudAppConfiguration> actual = configService.load("test_app");
        Assert.assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertLoadWithConfig() {
        Mockito.when(regCenter.get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        Optional<CloudAppConfiguration> actual = configService.load("test_app");
        Assert.assertTrue(actual.isPresent());
        Assert.assertThat(actual.get().getAppName(), is("test_app"));
    }
    
    @Test
    public void assertRemove() {
        configService.remove("test_app");
        Mockito.verify(regCenter).remove("/config/app/test_app");
    }
}
