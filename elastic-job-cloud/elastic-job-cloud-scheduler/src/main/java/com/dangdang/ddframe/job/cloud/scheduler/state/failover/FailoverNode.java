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

package com.dangdang.ddframe.job.cloud.scheduler.state.failover;

import com.dangdang.ddframe.job.context.TaskContext;
import com.dangdang.ddframe.job.cloud.scheduler.state.StateNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 待失效转移任务队列节点路径.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class FailoverNode {
    
    static final String ROOT = StateNode.ROOT + "/failover";
    
    private static final String FAILOVER_JOB = ROOT + "/%s";
    
    private static final String FAILOVER_TASK = FAILOVER_JOB + "/%s";
    
    static String getFailoverJobNodePath(final String jobName) {
        return String.format(FAILOVER_JOB, jobName);
    }
    
    static String getFailoverTaskNodePath(final String taskMetaInfo) {
        return String.format(FAILOVER_TASK, TaskContext.MetaInfo.from(taskMetaInfo).getJobName(), taskMetaInfo);
    }
}
