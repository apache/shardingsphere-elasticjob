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

package com.dangdang.ddframe.job.cloud.scheduler.config.app;

import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudAppConfigurationBuilder;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudAppJsonConstants;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        verify(regCenter).persist("/config/app/test_app", CloudAppJsonConstants.getAppJson("test_app"));
    }
    
    @Test
    public void assertUpdate() {
        CloudAppConfiguration appConfig = CloudAppConfigurationBuilder.createCloudAppConfiguration("test_app");
        configService.update(appConfig);
        verify(regCenter).update("/config/app/test_app", CloudAppJsonConstants.getAppJson("test_app"));
    }
    
    @Test
    public void assertLoadAllWithoutRootNode() {
        when(regCenter.isExisted("/config/app")).thenReturn(false);
        assertTrue(configService.loadAll().isEmpty());
        verify(regCenter).isExisted("/config/app");
    }
    
    @Test
    public void assertLoadAllWithRootNode() {
        when(regCenter.isExisted("/config/app")).thenReturn(true);
        when(regCenter.getChildrenKeys(CloudAppConfigurationNode.ROOT)).thenReturn(Arrays.asList("test_app_1", "test_app_2"));
        when(regCenter.isExisted("/config/app/test_app_1")).thenReturn(true);
        when(regCenter.isExisted("/config/app/test_app_2")).thenReturn(false);
        when(regCenter.get("/config/app/test_app_1")).thenReturn(CloudAppJsonConstants.getAppJson("test_app_1"));
        Collection<CloudAppConfiguration> actual = configService.loadAll();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getAppName(), is("test_app_1"));
        verify(regCenter).isExisted("/config/app");
        verify(regCenter).getChildrenKeys("/config/app");
        verify(regCenter).get("/config/app/test_app_1");
        verify(regCenter).get("/config/app/test_app_2");
    }
    
    @Test
    public void assertLoadWithoutConfig() {
        when(regCenter.isExisted("/config/app/test_app")).thenReturn(false);
        Optional<CloudAppConfiguration> actual = configService.load("test_app");
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertLoadWithConfig() {
        when(regCenter.isExisted("/config/app/test_app")).thenReturn(true);
        when(regCenter.get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        Optional<CloudAppConfiguration> actual = configService.load("test_app");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getAppName(), is("test_app"));
    }
    
    @Test
    public void assertRemove() {
        configService.remove("test_app");
        verify(regCenter).remove("/config/app/test_app");
    }
}
