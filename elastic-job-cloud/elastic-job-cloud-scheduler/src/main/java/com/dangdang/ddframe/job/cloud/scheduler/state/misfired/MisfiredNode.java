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

package com.dangdang.ddframe.job.cloud.scheduler.state.misfired;

import com.dangdang.ddframe.job.cloud.scheduler.state.StateNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 错过执行的作业队列节点路径.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MisfiredNode {
    
    static final String ROOT = StateNode.ROOT + "/misfired";
    
    private static final String MISFIRED_JOB = ROOT + "/%s";
    
    static String getMisfiredJobNodePath(final String jobName) {
        return String.format(MISFIRED_JOB, jobName);
    }
}
