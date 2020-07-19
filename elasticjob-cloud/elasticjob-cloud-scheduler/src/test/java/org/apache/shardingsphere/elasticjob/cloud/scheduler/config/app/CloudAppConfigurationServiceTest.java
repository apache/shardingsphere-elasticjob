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

import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.pojo.CloudAppConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudAppConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudAppJsonConstants;
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
public final class CloudAppConfigurationServiceTest {
    
    private static final String YAML = "appCacheEnable: true\n"
            + "appName: test_app\n"
            + "appURL: http://localhost/app.jar\n"
            + "bootstrapScript: bin/start.sh\n"
            + "cpuCount: 1.0\n"
            + "eventTraceSamplingCount: 0\n"
            + "memoryMB: 128.0\n";
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @InjectMocks
    private CloudAppConfigurationService configService;
    
    @Test
    public void assertAdd() {
        CloudAppConfigurationPOJO appConfig = CloudAppConfigurationBuilder.createCloudAppConfiguration("test_app");
        configService.add(appConfig);
        verify(regCenter).persist("/config/app/test_app", YAML);
    }
    
    @Test
    public void assertUpdate() {
        CloudAppConfigurationPOJO appConfig = CloudAppConfigurationBuilder.createCloudAppConfiguration("test_app");
        configService.update(appConfig);
        verify(regCenter).update("/config/app/test_app", YAML);
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
        when(regCenter.get("/config/app/test_app_1")).thenReturn(CloudAppJsonConstants.getAppJson("test_app_1"));
        Collection<CloudAppConfigurationPOJO> actual = configService.loadAll();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getAppName(), is("test_app_1"));
        verify(regCenter).isExisted("/config/app");
        verify(regCenter).getChildrenKeys("/config/app");
        verify(regCenter).get("/config/app/test_app_1");
        verify(regCenter).get("/config/app/test_app_2");
    }
    
    @Test
    public void assertLoadWithoutConfig() {
        assertFalse(configService.load("test_app").isPresent());
    }
    
    @Test
    public void assertLoadWithConfig() {
        when(regCenter.get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        Optional<CloudAppConfigurationPOJO> actual = configService.load("test_app");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getAppName(), is("test_app"));
    }
    
    @Test
    public void assertRemove() {
        configService.remove("test_app");
        verify(regCenter).remove("/config/app/test_app");
    }
}
