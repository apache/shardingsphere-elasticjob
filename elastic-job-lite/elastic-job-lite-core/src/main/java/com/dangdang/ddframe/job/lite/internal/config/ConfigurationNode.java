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

package com.dangdang.ddframe.job.lite.internal.config;

import com.dangdang.ddframe.job.lite.internal.storage.JobNodePath;

/**
 * 配置节点路径.
 * 
 * @author zhangliang
 */
public final class ConfigurationNode {
    
    static final String ROOT = "config";
    
    private final JobNodePath jobNodePath;
    
    public ConfigurationNode(final String jobName) {
        jobNodePath = new JobNodePath(jobName);
    }
    
    /**
     * 判断是否为作业配置根路径.
     * 
     * @param path 节点路径
     * @return 是否为作业配置根路径
     */
    public boolean isConfigPath(final String path) {
        return jobNodePath.getConfigNodePath().equals(path);
    }
}
