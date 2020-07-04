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

package org.apache.shardingsphere.elasticjob.lite.console.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Request object of uri '/event-trace/execution'.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FindJobExecutionEventsRequest extends BasePageRequest {
    
    private String jobName;
    
    private String ip;
    
    private Boolean isSuccess;
    
    @JsonProperty("startTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date start;
    
    @JsonProperty("endTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date end;
    
    /**
     * Create new FindJobExecutionEventsRequest with pageSize and pageNumber.
     * @param pageNumber page number
     * @param pageSize page size
     */
    public FindJobExecutionEventsRequest(final Integer pageSize, final Integer pageNumber) {
        super(pageSize, pageNumber, null, null);
    }
    
    /**
     * Create new FindJobExecutionEventsRequest with properties.
     * @param pageNumber page number
     * @param pageSize page size
     * @param sortBy the field name sort by
     * @param orderType order type, asc or desc
     * @param startTime start time
     * @param endTime end time
     */
    public FindJobExecutionEventsRequest(final Integer pageSize, final Integer pageNumber, final String sortBy,
        final String orderType, final Date startTime, final Date endTime) {
        super(pageSize, pageNumber, sortBy, orderType);
        this.start = startTime;
        this.end = endTime;
    }
}
