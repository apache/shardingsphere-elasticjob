/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.cloud.scheduler.state.failover;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.StateNode;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext.MetaInfo;

/**
 * Failover node.
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
        return String.format(FAILOVER_TASK, MetaInfo.from(taskMetaInfo).getJobName(), taskMetaInfo);
    }
}
