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

package com.dangdang.ddframe.job.cloud.Internal.running;

import lombok.RequiredArgsConstructor;

/**
 * Elastic Job Cloud运行状态根节点名称的常量类.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class RunningNode {
    
    private static final String ROOT = "/running";
    
    private static final String RUNNING_JOBS = ROOT + "/jobs/%s";
    
    private static final String RUNNING_TASKS = ROOT + "/tasks/%s";
    
    static String getRunningJobNodePath(final String jobName) {
        return String.format(RUNNING_JOBS, jobName);
    }
    
    static String getRunningTaskNodePath(final String taskId) {
        return String.format(RUNNING_TASKS, taskId);
    }
}
