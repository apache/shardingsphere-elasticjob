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

package com.dangdang.ddframe.job.cloud.Internal.task;

import lombok.RequiredArgsConstructor;

/**
 * Elastic Job Cloud任务根节点名称的常量类.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class CloudTaskNode {
    
    private static final String ROOT = "/task/%s";
    
    static final String CRON = ROOT + "/cron";
    
    static final String SHARDING_TOTAL_COUNT = ROOT + "/shardingTotalCount";
    
    static final String CPU_COUNT = ROOT + "/cpuCount";
    
    static final String MEMORY_MB = ROOT + "/memoryMB";
    
    static final String DOCKER_IMAGE_NAME = ROOT + "/dockerImageName";
    
    static final String APP_URL = ROOT + "/appURL";
    
    static final String CONNECT_STRING = ROOT + "/connectString";
    
    static final String NAMESPACE = ROOT + "/namespace";
    
    static final String DIGEST = ROOT + "/digest";
    
    private final String name;
    
    String getRootNodePath() {
        return String.format(ROOT, name);
    }
    
    String getCronNodePath() {
        return String.format(CRON, name);
    }
    
    String getShardingTotalCountNodePath() {
        return String.format(SHARDING_TOTAL_COUNT, name);
    }
    
    String getCpuCountNodePath() {
        return String.format(CPU_COUNT, name);
    }
    
    String getMemoryMBNodePath() {
        return String.format(MEMORY_MB, name);
    }
    
    String getDockerImageNameNodePath() {
        return String.format(APP_URL, name);
    }
    
    String getAppURLNodePath() {
        return String.format(DOCKER_IMAGE_NAME, name);
    }
    
    String getConnectStringNodePath() {
        return String.format(CONNECT_STRING, name);
    }
    
    String getNamespaceNodePath() {
        return String.format(NAMESPACE, name);
    }
    
    String getDigestNodePath() {
        return String.format(DIGEST, name);
    }
}
