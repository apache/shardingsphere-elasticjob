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

import org.apache.shardingsphere.elasticjob.lite.console.dao.search.RDBJobEventSearch.Condition;
import org.apache.shardingsphere.elasticjob.lite.console.dao.search.RDBJobEventSearch.Result;
import org.apache.shardingsphere.elasticjob.lite.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.lite.tracing.event.JobStatusTraceEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(RDBJobEventSearchTestConfiguration.class)
public final class RDBJobEventSearchTest {
    
    @Autowired
    private RDBJobEventSearch repository;
    
    @Test
    public void assertFindJobExecutionEventsWithPageSizeAndNumber() {
        Result<JobExecutionEvent> result = repository.findJobExecutionEvents(new Condition(10, 1, null, null, null, null, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(10));
        result = repository.findJobExecutionEvents(new Condition(50, 1, null, null, null, null, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(50));
        result = repository.findJobExecutionEvents(new Condition(100, 5, null, null, null, null, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(100));
        result = repository.findJobExecutionEvents(new Condition(100, 6, null, null, null, null, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(0));
    }
    
    @Test
    public void assertFindJobExecutionEventsWithErrorPageSizeAndNumber() {
        Result<JobExecutionEvent> result = repository.findJobExecutionEvents(new Condition(-1, -1, null, null, null, null, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(10));
    }
    
    @Test
    public void assertFindJobExecutionEventsWithSort() {
        Result<JobExecutionEvent> result = repository.findJobExecutionEvents(new Condition(10, 1, "jobName", "ASC", null, null, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(10));
        assertThat(result.getRows().get(0).getJobName(), is("test_job_1"));
        result = repository.findJobExecutionEvents(new Condition(10, 1, "jobName", "DESC", null, null, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(10));
        assertThat(result.getRows().get(0).getJobName(), is("test_job_99"));
    }
    
    @Test
    public void assertFindJobExecutionEventsWithErrorSort() {
        Result<JobExecutionEvent> result = repository.findJobExecutionEvents(new Condition(10, 1, "jobName", "ERROR_SORT", null, null, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(10));
        assertThat(result.getRows().get(0).getJobName(), is("test_job_1"));
        result = repository.findJobExecutionEvents(new Condition(10, 1, "notExistField", "ASC", null, null, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(10));
    }
    
    @Test
    public void assertFindJobExecutionEventsWithTime() {
        Date now = new Date();
        Date tenMinutesBefore = new Date(now.getTime() - 10 * 60 * 1000);
        Result<JobExecutionEvent> result = repository.findJobExecutionEvents(new Condition(10, 1, null, null, tenMinutesBefore, null, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(10));
        result = repository.findJobExecutionEvents(new Condition(10, 1, null, null, now, null, null));
        assertThat(result.getTotal(), is(0L));
        assertThat(result.getRows().size(), is(0));
        result = repository.findJobExecutionEvents(new Condition(10, 1, null, null, null, tenMinutesBefore, null));
        assertThat(result.getTotal(), is(0L));
        assertThat(result.getRows().size(), is(0));
        result = repository.findJobExecutionEvents(new Condition(10, 1, null, null, null, now, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(10));
        result = repository.findJobExecutionEvents(new Condition(10, 1, null, null, tenMinutesBefore, now, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(10));
    }
    
    @Test
    public void assertFindJobExecutionEventsWithFields() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("isSuccess", "1");
        Result<JobExecutionEvent> result = repository.findJobExecutionEvents(new Condition(10, 1, null, null, null, null, fields));
        assertThat(result.getTotal(), is(250L));
        assertThat(result.getRows().size(), is(10));
        fields.put("isSuccess", null);
        fields.put("jobName", "test_job_1");
        result = repository.findJobExecutionEvents(new Condition(10, 1, null, null, null, null, fields));
        assertThat(result.getTotal(), is(1L));
        assertThat(result.getRows().size(), is(1));
    }
    
    @Test
    public void assertFindJobExecutionEventsWithErrorFields() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("notExistField", "some value");
        Result<JobExecutionEvent> result = repository.findJobExecutionEvents(new Condition(10, 1, null, null, null, null, fields));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(10));
    }
    
    @Test
    public void assertFindJobStatusTraceEventsWithPageSizeAndNumber() {
        Result<JobStatusTraceEvent> result = repository.findJobStatusTraceEvents(new Condition(10, 1, null, null, null, null, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(10));
        result = repository.findJobStatusTraceEvents(new Condition(50, 1, null, null, null, null, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(50));
        result = repository.findJobStatusTraceEvents(new Condition(100, 5, null, null, null, null, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(100));
        result = repository.findJobStatusTraceEvents(new Condition(100, 6, null, null, null, null, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(0));
    }
    
    @Test
    public void assertFindJobStatusTraceEventsWithErrorPageSizeAndNumber() {
        Result<JobStatusTraceEvent> result = repository.findJobStatusTraceEvents(new Condition(-1, -1, null, null, null, null, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(10));
    }
    
    @Test
    public void assertFindJobStatusTraceEventsWithSort() {
        Result<JobStatusTraceEvent> result = repository.findJobStatusTraceEvents(new Condition(10, 1, "jobName", "ASC", null, null, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(10));
        assertThat(result.getRows().get(0).getJobName(), is("test_job_1"));
        result = repository.findJobStatusTraceEvents(new Condition(10, 1, "jobName", "DESC", null, null, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(10));
        assertThat(result.getRows().get(0).getJobName(), is("test_job_99"));
    }
    
    @Test
    public void assertFindJobStatusTraceEventsWithErrorSort() {
        Result<JobStatusTraceEvent> result = repository.findJobStatusTraceEvents(new Condition(10, 1, "jobName", "ERROR_SORT", null, null, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(10));
        assertThat(result.getRows().get(0).getJobName(), is("test_job_1"));
        result = repository.findJobStatusTraceEvents(new Condition(10, 1, "notExistField", "ASC", null, null, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(10));
    }
    
    @Test
    public void assertFindJobStatusTraceEventsWithTime() {
        Date now = new Date();
        Date tenMinutesBefore = new Date(now.getTime() - 10 * 60 * 1000);
        Result<JobStatusTraceEvent> result = repository.findJobStatusTraceEvents(new Condition(10, 1, null, null, tenMinutesBefore, null, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(10));
        result = repository.findJobStatusTraceEvents(new Condition(10, 1, null, null, now, null, null));
        assertThat(result.getTotal(), is(0L));
        assertThat(result.getRows().size(), is(0));
        result = repository.findJobStatusTraceEvents(new Condition(10, 1, null, null, null, tenMinutesBefore, null));
        assertThat(result.getTotal(), is(0L));
        assertThat(result.getRows().size(), is(0));
        result = repository.findJobStatusTraceEvents(new Condition(10, 1, null, null, null, now, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(10));
        result = repository.findJobStatusTraceEvents(new Condition(10, 1, null, null, tenMinutesBefore, now, null));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(10));
    }
    
    @Test
    public void assertFindJobStatusTraceEventsWithFields() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("jobName", "test_job_1");
        Result<JobStatusTraceEvent> result = repository.findJobStatusTraceEvents(new Condition(10, 1, null, null, null, null, fields));
        assertThat(result.getTotal(), is(1L));
        assertThat(result.getRows().size(), is(1));
    }
    
    @Test
    public void assertFindJobStatusTraceEventsWithErrorFields() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("notExistField", "some value");
        Result<JobStatusTraceEvent> result = repository.findJobStatusTraceEvents(new Condition(10, 1, null, null, null, null, fields));
        assertThat(result.getTotal(), is(500L));
        assertThat(result.getRows().size(), is(10));
    }
}
