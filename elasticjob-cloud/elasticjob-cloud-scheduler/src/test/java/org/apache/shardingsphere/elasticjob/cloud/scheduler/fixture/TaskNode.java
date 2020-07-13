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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture;

import org.apache.shardingsphere.elasticjob.infra.context.ExecutionType;
import com.google.common.base.Joiner;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext;
import lombok.Builder;

@Builder
public final class TaskNode {
    
    private String jobName;
    
    private int shardingItem;
    
    private ExecutionType type;
    
    private String slaveId;
    
    private String uuid;

    /**
     * Get task node path.
     * @return task node path
     */
    public String getTaskNodePath() {
        return Joiner.on("@-@").join(null == jobName ? "test_job" : jobName, shardingItem);
    }

    /**
     * Get task node value.
     * @return task node value
     */
    public String getTaskNodeValue() {
        return Joiner.on("@-@").join(getTaskNodePath(), null == type ? ExecutionType.READY : type, null == slaveId ? "slave-S0" : slaveId, null == uuid ? "0" : uuid);
    }

    /**
     * Get task meta info.
     * @return meta info
     */
    public TaskContext.MetaInfo getMetaInfo() {
        return TaskContext.MetaInfo.from(Joiner.on("@-@").join("test_job", 0));
    }
}
