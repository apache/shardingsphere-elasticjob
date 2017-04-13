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

package com.dangdang.ddframe.job.cloud.scheduler.state.disable.job;

import com.dangdang.ddframe.job.cloud.scheduler.state.StateNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 禁用作业队列节点路径.
 *
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class DisableJobNode {
    
    static final String ROOT = StateNode.ROOT + "/disable/job";
    
    private static final String DISABLE_JOB = ROOT + "/%s";
    
    static String getDisableJobNodePath(final String jobName) {
        return String.format(DISABLE_JOB, jobName);
    }
}
