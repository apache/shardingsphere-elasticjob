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
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Data
@Entity(name = "JOB_EXECUTION_LOG")
public class JobExecutionLog {
    
    @Id
    private String id;
    
    @Column(name = "job_name")
    private String jobName;
    
    @Column(name = "task_id")
    private String taskId;
    
    @Column(name = "hostname")
    private String hostname;
    
    @Column(name = "ip")
    private String ip;
    
    @Column(name = "sharding_item")
    private Integer shardingItem;
    
    @Column(name = "execution_source")
    private String executionSource;
    
    @Column(name = "failure_cause")
    private String failureCause;
    
    @Column(name = "is_success")
    private Boolean isSuccess;
    
    @Column(name = "start_time")
    private Date startTime;
    
    @Column(name = "complete_time")
    private Date completeTime;
    
    /**
     * JobExecutionLog convert to JobExecutionEvent.
     *
     * @return JobExecutionEvent entity
     */
    public JobExecutionEvent toJobExecutionEvent() {
        return new JobExecutionEvent(
                id,
                hostname,
                ip,
                taskId,
                jobName,
                JobExecutionEvent.ExecutionSource.valueOf(executionSource),
                shardingItem,
                startTime,
                completeTime,
                isSuccess,
                failureCause
        );
    }
    
}
