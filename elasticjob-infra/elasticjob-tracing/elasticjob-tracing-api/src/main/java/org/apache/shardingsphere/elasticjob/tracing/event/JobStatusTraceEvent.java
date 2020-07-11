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

package org.apache.shardingsphere.elasticjob.tracing.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

/**
 * Job status trace event.
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public final class JobStatusTraceEvent implements JobEvent {
    
    private String id = UUID.randomUUID().toString();
    
    private final String jobName;
    
    @Setter
    private String originalTaskId = "";
    
    private final String taskId;
    
    private final String slaveId;
    
    private final Source source;
    
    private final String executionType;
    
    private final String shardingItems;
    
    private final State state;
    
    private final String message;
    
    private Date creationTime = new Date();
    
    public enum State {
        TASK_STAGING, TASK_RUNNING, TASK_FINISHED, TASK_KILLED, TASK_LOST, TASK_FAILED, TASK_ERROR, TASK_DROPPED, TASK_GONE, TASK_GONE_BY_OPERATOR, TASK_UNREACHABLE, TASK_UNKNOWN
    }
    
    public enum Source {
        CLOUD_SCHEDULER, CLOUD_EXECUTOR, LITE_EXECUTOR
    }
}
