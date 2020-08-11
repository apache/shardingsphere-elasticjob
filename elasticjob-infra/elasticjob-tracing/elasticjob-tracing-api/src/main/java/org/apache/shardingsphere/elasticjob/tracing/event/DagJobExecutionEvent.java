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

import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;
import java.util.UUID;

/**
 * Dag job execution event.
 *
 **/
@Getter
@ToString
public class DagJobExecutionEvent implements JobEvent {
    private String id;

    private String groupName;

    private String jobName;

    private String execTime;

    private String execDate;

    private String batchNo;

    private String state;

    private String message;

    public DagJobExecutionEvent(final String groupName, final String jobName, final String batchNo, final String state,
                                final String message) {
        this(groupName, jobName, DateFormatUtils.format(new Date(), "HHmmss"),
                DateFormatUtils.format(new Date(), "yyyyMMdd"),
                batchNo, state, message);
    }

    public DagJobExecutionEvent(final String groupName, final String jobName, final String execTime, final String execDate,
                                final String batchNo, final String state, final String message) {
        this.id = UUID.randomUUID().toString();
        this.groupName = groupName;
        this.jobName = jobName;
        this.execTime = execTime;
        this.execDate = execDate;
        this.batchNo = batchNo;
        this.state = state;
        this.message = truncateMessage(message);
    }

    public DagJobExecutionEvent(final String id, final String groupName, final String jobName, final String execTime,
                                final String execDate, final String batchNo, final String state, final String message) {
        this.id = id;
        this.groupName = groupName;
        this.jobName = jobName;
        this.execTime = execTime;
        this.execDate = execDate;
        this.batchNo = batchNo;
        this.state = state;
        this.message = truncateMessage(message);
    }

    private static String truncateMessage(final String str) {
        return StringUtils.isNotEmpty(str) && str.length() > 255 ? StringUtils.substring(str, 0, 255) : str;
    }

    @Override
    public String getJobName() {
        return jobName;
    }
}
