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

package org.apache.shardingsphere.elasticjob.lite.console.domain;

import lombok.Data;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Data
@Entity(name = "JOB_STATUS_TRACE_LOG")
public class JobStatusTraceLog {
    
    @Id
    private String id;
    
    @Column(name = "job_name")
    private String jobName;
    
    @Column(name = "original_task_id")
    private String originalTaskId;
    
    @Column(name = "task_id")
    private String taskId;
    
    @Column(name = "slave_id")
    private String slaveId;
    
    @Column(name = "source")
    private String source;
    
    @Column(name = "execution_type")
    private String executionType;
    
    @Column(name = "sharding_item")
    private String shardingItem;
    
    @Column(name = "state")
    private String state;
    
    @Column(name = "message")
    private String message;
    
    @Column(name = "creation_time")
    private Date creationTime;
    
    /**
     * JobStatusTraceLog convert to JobStatusTraceEvent.
     *
     * @return JobStatusTraceEvent entity
     */
    public JobStatusTraceEvent toJobStatusTraceEvent() {
        return new JobStatusTraceEvent(
                id,
                jobName,
                originalTaskId,
                taskId,
                slaveId,
                JobStatusTraceEvent.Source.valueOf(source),
                executionType,
                shardingItem,
                JobStatusTraceEvent.State.valueOf(state),
                message,
                creationTime
        );
    }
    
}
