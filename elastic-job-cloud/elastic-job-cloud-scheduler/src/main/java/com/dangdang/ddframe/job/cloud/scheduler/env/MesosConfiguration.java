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

package com.dangdang.ddframe.job.cloud.scheduler.env;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Mesos配置项.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class MesosConfiguration {
    
    /**
     * 框架名称.
     */
    public static final String FRAMEWORK_NAME = "Elastic-Job-Cloud";
    
    /**
     * 框架失效转移超时秒数. 默认为1周
     */
    public static final double FRAMEWORK_FAILOVER_TIMEOUT_SECONDS = 60 * 60 * 24 * 7D;
    
    private final String user;
    
    private final String url;
    
    private final String hostname;
}
