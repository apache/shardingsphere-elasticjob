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

import lombok.Builder;
import org.apache.shardingsphere.elasticjob.infra.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext.MetaInfo;

@Builder
public final class TaskNode {
    
    private static final String DELIMITER = "@-@";
    
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
        return String.join(DELIMITER, null == jobName ? "test_job" : jobName, "" + shardingItem);
    }

    /**
     * Get task node value.
     * @return task node value
     */
    public String getTaskNodeValue() {
        return String.join(DELIMITER, getTaskNodePath(), null == type ? ExecutionType.READY.toString() : type.toString(), null == slaveId ? "slave-S0" : slaveId, null == uuid ? "0" : uuid);
    }

    /**
     * Get task meta info.
     * @return meta info
     */
    public MetaInfo getMetaInfo() {
        return MetaInfo.from(String.join(DELIMITER, "test_job", "0"));
    }
}
