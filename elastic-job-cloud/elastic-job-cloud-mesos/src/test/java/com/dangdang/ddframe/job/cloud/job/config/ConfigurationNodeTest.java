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

package com.dangdang.ddframe.job.cloud.job.config;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public final class ConfigurationNodeTest {
    
    @Test
    public void assertGetRootNodePath() {
        assertThat(ConfigurationNode.getRootNodePath("test_job"), is("/jobs/test_job/config"));
    }
    
    @Test
    public void assertGetCronNodePath() {
        assertThat(ConfigurationNode.getCronNodePath("test_job"), is("/jobs/test_job/config/cron"));
    }
    
    @Test
    public void assertGetShardingTotalCountNodePath() {
        assertThat(ConfigurationNode.getShardingTotalCountNodePath("test_job"), is("/jobs/test_job/config/sharding_total_count"));
    }
    
    @Test
    public void assertGetCpuCountNodePath() {
        assertThat(ConfigurationNode.getCpuCountNodePath("test_job"), is("/jobs/test_job/config/cpu_count"));
    }
    
    @Test
    public void assertGetMemoryMBNodePath() {
        assertThat(ConfigurationNode.getMemoryMBNodePath("test_job"), is("/jobs/test_job/config/memory_mb"));
    }
    
    @Test
    public void assertGetDockerImageNameNodePath() {
        assertThat(ConfigurationNode.getDockerImageNameNodePath("test_job"), is("/jobs/test_job/config/docker_image_name"));
    }
    
    @Test
    public void assertGetAppURLNodePath() {
        assertThat(ConfigurationNode.getAppURLNodePath("test_job"), is("/jobs/test_job/config/app_url"));
    }
}