/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.config;

import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
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
public final class ConfigurationServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @InjectMocks
    private ConfigurationService configService;
    
    private String jobConfigJson = "{\"jobName\":\"%s\",\"cron\":\"5/10 * * * * *\",\"shardingTotalCount\":10,\"cpuCount\":1.0,\"memoryMB\":128.0," +
            "\"dockerImageName\":\"dockerImage\",\"appURL\":\"http://localhost/app.jar\",\"failover\":true,\"misfire\":true}";
    
    @Test
    public void assertAdd() {
        CloudJobConfiguration jobConfig = new CloudJobConfiguration("test_job", "5/10 * * * * *", 10, 1.0d, 128.0d, "dockerImage", "http://localhost/app.jar", true, true);
        configService.add(jobConfig);
        verify(regCenter).persist("/config/test_job", String.format(jobConfigJson, "test_job"));
    }
    
    @Test
    public void assertUpdate() {
        CloudJobConfiguration jobConfig = new CloudJobConfiguration("test_job", "5/10 * * * * *", 10, 1.0d, 128.0d, "dockerImage", "http://localhost/app.jar", true, true);
        configService.update(jobConfig);
        verify(regCenter).update("/config/test_job", String.format(jobConfigJson, "test_job"));
    }
    
    @Test
    public void assertLoadAllWithoutRootNode() {
        when(regCenter.isExisted("/config")).thenReturn(false);
        assertTrue(configService.loadAll().isEmpty());
        verify(regCenter).isExisted("/config");
    }
    
    @Test
    public void assertLoadAllWithRootNode() {
        when(regCenter.isExisted("/config")).thenReturn(true);
        when(regCenter.getChildrenKeys(ConfigurationNode.ROOT)).thenReturn(Arrays.asList("test_job_1", "test_job_2"));
        when(regCenter.isExisted("/config/test_job_1")).thenReturn(true);
        when(regCenter.isExisted("/config/test_job_2")).thenReturn(false);
        when(regCenter.get("/config/test_job_1")).thenReturn(String.format(jobConfigJson, "test_job_1"));
        Collection<CloudJobConfiguration> actual = configService.loadAll();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getJobName(), is("test_job_1"));
        verify(regCenter).isExisted("/config");
        verify(regCenter).getChildrenKeys("/config");
        verify(regCenter).isExisted("/config/test_job_1");
        verify(regCenter).isExisted("/config/test_job_2");
        verify(regCenter).get("/config/test_job_1");
    }
    
    @Test
    public void assertLoadWithoutConfig() {
        when(regCenter.isExisted("/config/test_job")).thenReturn(false);
        Optional<CloudJobConfiguration> actual = configService.load("test_job");
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertLoadWithConfig() {
        when(regCenter.isExisted("/config/test_job")).thenReturn(true);
        when(regCenter.get("/config/test_job")).thenReturn(String.format(jobConfigJson, "test_job"));
        Optional<CloudJobConfiguration> actual = configService.load("test_job");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getJobName(), is("test_job"));
    }
    
    @Test
    public void assertRemove() {
        configService.remove("test_job");
        verify(regCenter).remove("/config/test_job");
    }
}
