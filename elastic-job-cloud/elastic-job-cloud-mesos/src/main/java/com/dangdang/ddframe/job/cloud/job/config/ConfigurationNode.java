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

import com.dangdang.ddframe.job.cloud.job.JobNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 作业配置节点路径.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigurationNode {
    
    private static final String ROOT = JobNode.JOB_ROOT + "/config";
    
    static final String CRON = ROOT + "/cron";
    
    static final String SHARDING_TOTAL_COUNT = ROOT + "/sharding_total_count";
    
    static final String CPU_COUNT = ROOT + "/cpu_count";
    
    static final String MEMORY_MB = ROOT + "/memory_mb";
    
    static final String DOCKER_IMAGE_NAME = ROOT + "/docker_image_name";
    
    static final String APP_URL = ROOT + "/app_url";
    
    static String getRootNodePath(final String jobName) {
        return String.format(ROOT, jobName);
    }
    
    static String getCronNodePath(final String jobName) {
        return String.format(CRON, jobName);
    }
    
    static String getShardingTotalCountNodePath(final String jobName) {
        return String.format(SHARDING_TOTAL_COUNT, jobName);
    }
    
    static String getCpuCountNodePath(final String jobName) {
        return String.format(CPU_COUNT, jobName);
    }
    
    static String getMemoryMBNodePath(final String jobName) {
        return String.format(MEMORY_MB, jobName);
    }
    
    static String getDockerImageNameNodePath(final String jobName) {
        return String.format(DOCKER_IMAGE_NAME, jobName);
    }
    
    static String getAppURLNodePath(final String jobName) {
        return String.format(APP_URL, jobName);
    }
}
