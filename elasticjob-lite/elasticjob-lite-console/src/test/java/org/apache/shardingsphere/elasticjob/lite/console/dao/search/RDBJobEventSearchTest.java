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

package org.apache.shardingsphere.elasticjob.lite.console.dao.search;

import org.apache.shardingsphere.elasticjob.lite.console.dto.request.FindJobExecutionEventsRequest;
import org.apache.shardingsphere.elasticjob.lite.console.dto.request.FindJobStatusTraceEventsRequest;
import org.apache.shardingsphere.elasticjob.lite.console.service.EventTraceHistoryService;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(RDBJobEventSearchTestConfiguration.class)
public final class RDBJobEventSearchTest {
    
    @Autowired
    private EventTraceHistoryService eventTraceHistoryService;
    
    @Test
    public void assertFindJobExecutionEventsWithPageSizeAndNumber() {
        Page<JobExecutionEvent> result = eventTraceHistoryService.findJobExecutionEvents(new FindJobExecutionEventsRequest());
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(10));
        result = eventTraceHistoryService.findJobExecutionEvents(new FindJobExecutionEventsRequest(50, 1));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(50));
        result = eventTraceHistoryService.findJobExecutionEvents(new FindJobExecutionEventsRequest(100, 5));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(100));
        result = eventTraceHistoryService.findJobExecutionEvents(new FindJobExecutionEventsRequest(100, 6));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(0));
    }
    
    @Test
    public void assertFindJobExecutionEventsWithErrorPageSizeAndNumber() {
        Page<JobExecutionEvent> result = eventTraceHistoryService.findJobExecutionEvents(new FindJobExecutionEventsRequest(-1, -1, null, null, null, null));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(10));
    }
    
    @Test
    public void assertFindJobExecutionEventsWithSort() {
        Page<JobExecutionEvent> result = eventTraceHistoryService.findJobExecutionEvents(new FindJobExecutionEventsRequest(10, 1, "jobName", "ASC", null, null));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(10));
        assertThat(result.getContent().get(0).getJobName(), is("test_job_1"));
        result = eventTraceHistoryService.findJobExecutionEvents(new FindJobExecutionEventsRequest(10, 1, "jobName", "DESC", null, null));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(10));
        assertThat(result.getContent().get(0).getJobName(), is("test_job_99"));
    }
    
    @Test
    public void assertFindJobExecutionEventsWithErrorSort() {
        Page<JobExecutionEvent> result = eventTraceHistoryService.findJobExecutionEvents(new FindJobExecutionEventsRequest(10, 1, "jobName", "ERROR_SORT", null, null));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(10));
        assertThat(result.getContent().get(0).getJobName(), is("test_job_1"));
        result = eventTraceHistoryService.findJobExecutionEvents(new FindJobExecutionEventsRequest(10, 1, "notExistField", "ASC", null, null));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(10));
    }
    
    @Test
    public void assertFindJobExecutionEventsWithTime() {
        Date now = new Date();
        Date tenMinutesBefore = new Date(now.getTime() - 10 * 60 * 1000);
        Page<JobExecutionEvent> result = eventTraceHistoryService.findJobExecutionEvents(new FindJobExecutionEventsRequest(10, 1, null, null, tenMinutesBefore, null));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(10));
        result = eventTraceHistoryService.findJobExecutionEvents(new FindJobExecutionEventsRequest(10, 1, null, null, now, null));
        assertThat(result.getTotalElements(), is(0L));
        assertThat(result.getContent().size(), is(0));
        result = eventTraceHistoryService.findJobExecutionEvents(new FindJobExecutionEventsRequest(10, 1, null, null, null, tenMinutesBefore));
        assertThat(result.getTotalElements(), is(0L));
        assertThat(result.getContent().size(), is(0));
        result = eventTraceHistoryService.findJobExecutionEvents(new FindJobExecutionEventsRequest(10, 1, null, null, null, now));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(10));
        result = eventTraceHistoryService.findJobExecutionEvents(new FindJobExecutionEventsRequest(10, 1, null, null, tenMinutesBefore, now));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(10));
    }
    
    @Test
    public void assertFindJobExecutionEventsWithFields() {
        FindJobExecutionEventsRequest findJobExecutionEventsRequest = new FindJobExecutionEventsRequest(10, 1, null, null, null, null);
        findJobExecutionEventsRequest.setIsSuccess(true);
        Page<JobExecutionEvent> result = eventTraceHistoryService.findJobExecutionEvents(findJobExecutionEventsRequest);
        assertThat(result.getTotalElements(), is(250L));
        assertThat(result.getContent().size(), is(10));
        findJobExecutionEventsRequest.setIsSuccess(null);
        findJobExecutionEventsRequest.setJobName("test_job_1");
        result = eventTraceHistoryService.findJobExecutionEvents(findJobExecutionEventsRequest);
        assertThat(result.getTotalElements(), is(1L));
        assertThat(result.getContent().size(), is(1));
    }
    
    @Test
    public void assertFindJobExecutionEventsWithErrorFields() {
        Page<JobExecutionEvent> result = eventTraceHistoryService.findJobExecutionEvents(new FindJobExecutionEventsRequest(10, 1));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(10));
    }
    
    @Test
    public void assertFindJobStatusTraceEventsWithPageSizeAndNumber() {
        Page<JobStatusTraceEvent> result = eventTraceHistoryService.findJobStatusTraceEvents(new FindJobStatusTraceEventsRequest(10, 1, null, null, null, null));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(10));
        result = eventTraceHistoryService.findJobStatusTraceEvents(new FindJobStatusTraceEventsRequest(50, 1, null, null, null, null));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(50));
        result = eventTraceHistoryService.findJobStatusTraceEvents(new FindJobStatusTraceEventsRequest(100, 5, null, null, null, null));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(100));
        result = eventTraceHistoryService.findJobStatusTraceEvents(new FindJobStatusTraceEventsRequest(100, 6, null, null, null, null));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(0));
    }
    
    @Test
    public void assertFindJobStatusTraceEventsWithErrorPageSizeAndNumber() {
        Page<JobStatusTraceEvent> result = eventTraceHistoryService.findJobStatusTraceEvents(new FindJobStatusTraceEventsRequest(-1, -1));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(10));
    }
    
    @Test
    public void assertFindJobStatusTraceEventsWithSort() {
        Page<JobStatusTraceEvent> result = eventTraceHistoryService.findJobStatusTraceEvents(new FindJobStatusTraceEventsRequest(10, 1, "jobName", "ASC", null, null));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(10));
        assertThat(result.getContent().get(0).getJobName(), is("test_job_1"));
        result = eventTraceHistoryService.findJobStatusTraceEvents(new FindJobStatusTraceEventsRequest(10, 1, "jobName", "DESC", null, null));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(10));
        assertThat(result.getContent().get(0).getJobName(), is("test_job_99"));
    }
    
    @Test
    public void assertFindJobStatusTraceEventsWithErrorSort() {
        Page<JobStatusTraceEvent> result = eventTraceHistoryService.findJobStatusTraceEvents(new FindJobStatusTraceEventsRequest(10, 1, "jobName", "ERROR_SORT", null, null));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(10));
        assertThat(result.getContent().get(0).getJobName(), is("test_job_1"));
        result = eventTraceHistoryService.findJobStatusTraceEvents(new FindJobStatusTraceEventsRequest(10, 1, "notExistField", "ASC", null, null));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(10));
    }
    
    @Test
    public void assertFindJobStatusTraceEventsWithTime() {
        Date now = new Date();
        Date tenMinutesBefore = new Date(now.getTime() - 10 * 60 * 1000);
        Page<JobStatusTraceEvent> result = eventTraceHistoryService.findJobStatusTraceEvents(new FindJobStatusTraceEventsRequest(10, 1, null, null, tenMinutesBefore, null));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(10));
        result = eventTraceHistoryService.findJobStatusTraceEvents(new FindJobStatusTraceEventsRequest(10, 1, null, null, now, null));
        assertThat(result.getTotalElements(), is(0L));
        assertThat(result.getContent().size(), is(0));
        result = eventTraceHistoryService.findJobStatusTraceEvents(new FindJobStatusTraceEventsRequest(10, 1, null, null, null, tenMinutesBefore));
        assertThat(result.getTotalElements(), is(0L));
        assertThat(result.getContent().size(), is(0));
        result = eventTraceHistoryService.findJobStatusTraceEvents(new FindJobStatusTraceEventsRequest(10, 1, null, null, null, now));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(10));
        result = eventTraceHistoryService.findJobStatusTraceEvents(new FindJobStatusTraceEventsRequest(10, 1, null, null, tenMinutesBefore, now));
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(10));
    }
    
    @Test
    public void assertFindJobStatusTraceEventsWithFields() {
        FindJobStatusTraceEventsRequest findJobStatusTraceEventsRequest = new FindJobStatusTraceEventsRequest(10, 1);
        findJobStatusTraceEventsRequest.setJobName("test_job_1");
        Page<JobStatusTraceEvent> result = eventTraceHistoryService.findJobStatusTraceEvents(findJobStatusTraceEventsRequest);
        assertThat(result.getTotalElements(), is(1L));
        assertThat(result.getContent().size(), is(1));
    }
    
    @Test
    public void assertFindJobStatusTraceEventsWithErrorFields() {
        FindJobStatusTraceEventsRequest findJobStatusTraceEventsRequest = new FindJobStatusTraceEventsRequest(10, 1);
        Page<JobStatusTraceEvent> result = eventTraceHistoryService.findJobStatusTraceEvents(findJobStatusTraceEventsRequest);
        assertThat(result.getTotalElements(), is(500L));
        assertThat(result.getContent().size(), is(10));
    }
}
