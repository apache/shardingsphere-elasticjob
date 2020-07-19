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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.restful;

import org.apache.shardingsphere.elasticjob.cloud.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudAppJsonConstants;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJsonConstants;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.TaskNode;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.restful.search.JobEventRdbSearch;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.restful.search.JobEventRdbSearch.Result;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.failover.FailoverTaskInfo;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running.RunningService;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobExecutionTypeStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskResultStatistics;
import org.apache.shardingsphere.elasticjob.cloud.util.json.GsonFactory;
import org.apache.shardingsphere.elasticjob.infra.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext.MetaInfo;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CloudJobRestfulApiTest extends AbstractCloudRestfulApiTest {
    
    private static final String YAML = "appName: test_app\n"
            + "cpuCount: 1.0\n"
            + "cron: 0/30 * * * * ?\n"
            + "description: ''\n"
            + "disabled: false\n"
            + "failover: true\n"
            + "jobExecutionType: TRANSIENT\n"
            + "jobName: test_job\n"
            + "jobParameter: ''\n"
            + "maxTimeDiffSeconds: 0\n"
            + "memoryMB: 128.0\n"
            + "misfire: true\n"
            + "monitorExecution: false\n"
            + "overwrite: false\n"
            + "reconcileIntervalMinutes: 0\n"
            + "shardingItemParameters: ''\n"
            + "shardingTotalCount: 10\n";
    
    @Test
    public void assertRegister() throws Exception {
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        when(getRegCenter().isExisted("/config/job/test_job")).thenReturn(false);
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/register", "POST", CloudJsonConstants.getJobJson()), is(204));
        verify(getRegCenter()).persist("/config/job/test_job", YAML);
        RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/deregister", "DELETE", "test_job");
    }
    
    @Test
    public void assertRegisterWithoutApp() throws Exception {
        when(getRegCenter().isExisted("/config/job/test_job")).thenReturn(false);
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/register", "POST", CloudJsonConstants.getJobJson()), is(500));
    }
    
    @Test
    public void assertRegisterWithExistedName() throws Exception {
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        when(getRegCenter().isExisted("/config/test_job")).thenReturn(false);
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/register", "POST", CloudJsonConstants.getJobJson()), is(204));
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/register", "POST", CloudJsonConstants.getJobJson()), is(500));
        RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/deregister", "DELETE", "test_job");
    }
    
    @Test
    public void assertRegisterWithBadRequest() throws Exception {
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/register", "POST", "\"{\"jobName\":\"wrong_job\"}"), is(500));
    }
    
    @Test
    public void assertUpdate() throws Exception {
        when(getRegCenter().isExisted("/config/job/test_job")).thenReturn(true);
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/update", "PUT", CloudJsonConstants.getJobJson()), is(204));
        verify(getRegCenter()).update("/config/job/test_job", YAML);
        RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/deregister", "DELETE", "test_job");
    }
    
    @Test
    public void assertDeregister() throws Exception {
        when(getRegCenter().isExisted("/config/job/test_job")).thenReturn(false);
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/deregister", "DELETE", "test_job"), is(204));
        verify(getRegCenter(), times(3)).get("/config/job/test_job");
    }
    
    @Test
    public void assertTriggerWithDaemonJob() throws Exception {
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson(CloudJobExecutionType.DAEMON));
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/trigger", "POST", "test_job"), is(500));
    }
    
    @Test
    public void assertTriggerWithTransientJob() throws Exception {
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/trigger", "POST", "test_job"), is(204));
    }
    
    @Test
    public void assertDetail() throws Exception {
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/jobs/test_job"), is(CloudJsonConstants.getJobJson()));
        verify(getRegCenter()).get("/config/job/test_job");
    }
    
    @Test
    public void assertDetailWithNotExistedJob() throws Exception {
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/jobs/notExistedJobName", "GET", ""), is(404));
    }
    
    @Test
    public void assertFindAllJobs() throws Exception {
        when(getRegCenter().isExisted("/config/job")).thenReturn(true);
        when(getRegCenter().getChildrenKeys("/config/job")).thenReturn(Collections.singletonList("test_job"));
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/jobs"), is("[" + CloudJsonConstants.getJobJson() + "]"));
        verify(getRegCenter()).isExisted("/config/job");
        verify(getRegCenter()).getChildrenKeys("/config/job");
        verify(getRegCenter()).get("/config/job/test_job");
    }
    
    @Test
    public void assertFindAllRunningTasks() throws Exception {
        RunningService runningService = new RunningService(getRegCenter());
        TaskContext actualTaskContext = TaskContext.from(TaskNode.builder().build().getTaskNodeValue());
        when(getRegCenter().get("/config/job/" + actualTaskContext.getMetaInfo().getJobName())).thenReturn(CloudJsonConstants.getJobJson());
        runningService.add(actualTaskContext);
        assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/tasks/running"), is(GsonFactory.getGson().toJson(Collections.singletonList(actualTaskContext))));
    }
    
    @Test
    public void assertFindAllReadyTasks() throws Exception {
        when(getRegCenter().isExisted("/state/ready")).thenReturn(true);
        when(getRegCenter().getChildrenKeys("/state/ready")).thenReturn(Collections.singletonList("test_job"));
        when(getRegCenter().get("/state/ready/test_job")).thenReturn("1");
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("jobName", "test_job");
        expectedMap.put("times", "1");
        Collection<Map<String, String>> expectedResult = Collections.singletonList(expectedMap);
        assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/tasks/ready"), is(GsonFactory.getGson().toJson(expectedResult)));
        verify(getRegCenter()).isExisted("/state/ready");
        verify(getRegCenter()).getChildrenKeys("/state/ready");
        verify(getRegCenter()).get("/state/ready/test_job");
    }
    
    @Test
    public void assertFindAllFailoverTasks() throws Exception {
        when(getRegCenter().isExisted("/state/failover")).thenReturn(true);
        when(getRegCenter().getChildrenKeys("/state/failover")).thenReturn(Collections.singletonList("test_job"));
        when(getRegCenter().getChildrenKeys("/state/failover/test_job")).thenReturn(Collections.singletonList("test_job@-@0"));
        String originalTaskId = UUID.randomUUID().toString();
        when(getRegCenter().get("/state/failover/test_job/test_job@-@0")).thenReturn(originalTaskId);
        FailoverTaskInfo expectedFailoverTask = new FailoverTaskInfo(MetaInfo.from("test_job@-@0"), originalTaskId);
        Collection<FailoverTaskInfo> expectedResult = Collections.singletonList(expectedFailoverTask);
        assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/tasks/failover"), is(GsonFactory.getGson().toJson(expectedResult)));
        verify(getRegCenter()).isExisted("/state/failover");
        verify(getRegCenter()).getChildrenKeys("/state/failover");
        verify(getRegCenter()).getChildrenKeys("/state/failover/test_job");
        verify(getRegCenter()).get("/state/failover/test_job/test_job@-@0");
    }
    
    @Test
    public void assertFindJobExecutionEventsWhenNotConfigRDB() throws Exception {
        ReflectionUtils.setStaticFieldValue(CloudJobRestfulApi.class, "jobEventRdbSearch", null);
        assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/events/executions"), is(GsonFactory.getGson().toJson(new JobEventRdbSearch.Result<>(0,
                Collections.<JobExecutionEvent>emptyList()))));
    }
    
    @Test
    public void assertFindJobExecutionEvents() throws Exception {
        ReflectionUtils.setStaticFieldValue(CloudJobRestfulApi.class, "jobEventRdbSearch", getJobEventRdbSearch());
        JobExecutionEvent jobExecutionEvent = new JobExecutionEvent("localhost", "127.0.0.1", "fake_task_id", "test_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        when(getJobEventRdbSearch().findJobExecutionEvents(any(JobEventRdbSearch.Condition.class))).thenReturn(new JobEventRdbSearch.Result<>(0, Collections.singletonList(jobExecutionEvent)));
        assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/events/executions?" + buildFindJobEventsQueryParameter()),
                is(GsonFactory.getGson().toJson(new JobEventRdbSearch.Result<>(0, Collections.singletonList(jobExecutionEvent)))));
        verify(getJobEventRdbSearch()).findJobExecutionEvents(any(JobEventRdbSearch.Condition.class));
    }
    
    @Test
    public void assertFindJobStatusTraceEventEventsWhenNotConfigRDB() throws Exception {
        ReflectionUtils.setStaticFieldValue(CloudJobRestfulApi.class, "jobEventRdbSearch", null);
        assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/events/statusTraces"), is(GsonFactory.getGson().toJson(new JobEventRdbSearch.Result<>(0,
                Collections.<JobExecutionEvent>emptyList()))));
    }
    
    @Test
    public void assertFindJobStatusTraceEvent() throws Exception {
        ReflectionUtils.setStaticFieldValue(CloudJobRestfulApi.class, "jobEventRdbSearch", getJobEventRdbSearch());
        JobStatusTraceEvent jobStatusTraceEvent = new JobStatusTraceEvent("test-job", 
                "fake_task_id", "fake_slave_id", JobStatusTraceEvent.Source.LITE_EXECUTOR, ExecutionType.READY.toString(), "0", JobStatusTraceEvent.State.TASK_RUNNING, "message is empty.");
        when(getJobEventRdbSearch().findJobStatusTraceEvents(any(JobEventRdbSearch.Condition.class))).thenReturn(new Result<>(0, Collections.singletonList(jobStatusTraceEvent)));
        assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/events/statusTraces?" + buildFindJobEventsQueryParameter()),
                is(GsonFactory.getGson().toJson(new JobEventRdbSearch.Result<>(0, Collections.singletonList(jobStatusTraceEvent)))));
        verify(getJobEventRdbSearch()).findJobStatusTraceEvents(any(JobEventRdbSearch.Condition.class));
    }
    
    private String buildFindJobEventsQueryParameter() throws UnsupportedEncodingException {
        return "per_page=10&page=1&sort=jobName&order=DESC&jobName=test_job"
                + "&startTime=" + URLEncoder.encode("2016-12-26 10:00:00", "UTF-8") + "&endTime=" + URLEncoder.encode("2016-12-26 10:00:00", "UTF-8");
    }
    
    @Test
    public void assertGetTaskResultStatistics() throws Exception {
        assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/tasks/results"),
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertGetTaskResultStatisticsWithSinceParameter() throws Exception {
        assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/tasks/results?since=last24hours"),
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertGetTaskResultStatisticsWithPathParameter() throws Exception {
        String[] parameters = {"online", "lastWeek", "lastHour", "lastMinute"};
        for (String each : parameters) {
            String result = RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/tasks/results/" + each);
            TaskResultStatistics taskResultStatistics = GsonFactory.getGson().fromJson(result, TaskResultStatistics.class);
            assertThat(taskResultStatistics.getSuccessCount(), is(0));
            assertThat(taskResultStatistics.getFailedCount(), is(0));
        }
    }
    
    @Test
    public void assertGetTaskResultStatisticsWithErrorPathParameter() throws Exception {
        String result = RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/tasks/results/errorPath");
        TaskResultStatistics taskResultStatistics = GsonFactory.getGson().fromJson(result, TaskResultStatistics.class);
        assertThat(taskResultStatistics.getSuccessCount(), is(0));
        assertThat(taskResultStatistics.getFailedCount(), is(0));
    }
    
    @Test
    public void assertGetJobExecutionTypeStatistics() throws Exception {
        String result = RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/jobs/executionType");
        JobExecutionTypeStatistics jobExecutionTypeStatistics = GsonFactory.getGson().fromJson(result, JobExecutionTypeStatistics.class);
        assertThat(jobExecutionTypeStatistics.getDaemonJobCount(), is(0));
        assertThat(jobExecutionTypeStatistics.getTransientJobCount(), is(0));
    }
    
    @Test
    public void assertFindTaskRunningStatistics() throws Exception {
        assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/tasks/running"),
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertFindTaskRunningStatisticsWeekly() throws Exception {
        assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/tasks/running?since=lastWeek"),
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertFindJobRunningStatistics() throws Exception {
        assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/jobs/running"),
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertFindJobRunningStatisticsWeekly() throws Exception {
        assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/jobs/running?since=lastWeek"),
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertFindJobRegisterStatisticsSinceOnline() throws Exception {
        assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/statistics/jobs/register"),
                is(GsonFactory.getGson().toJson(Collections.emptyList())));
    }
    
    @Test
    public void assertIsDisabled() throws Exception {
        when(getRegCenter().isExisted("/state/disable/job/test_job")).thenReturn(true);
        assertThat(RestfulTestsUtil.sentGetRequest("http://127.0.0.1:19000/api/job/test_job/disable"), is("true"));
    }
    
    @Test
    public void assertDisable() throws Exception {
        when(getRegCenter().isExisted("/config/job")).thenReturn(true);
        when(getRegCenter().getChildrenKeys("/config/job")).thenReturn(Collections.singletonList("test_job"));
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/test_job/disable", "POST"), is(204));
        verify(getRegCenter()).persist("/state/disable/job/test_job", "test_job");
    }
    
    @Test
    public void assertEnable() throws Exception {
        when(getRegCenter().isExisted("/config/job")).thenReturn(true);
        when(getRegCenter().getChildrenKeys("/config/job")).thenReturn(Collections.singletonList("test_job"));
        when(getRegCenter().get("/config/app/test_app")).thenReturn(CloudAppJsonConstants.getAppJson("test_app"));
        when(getRegCenter().get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson());
        assertThat(RestfulTestsUtil.sentRequest("http://127.0.0.1:19000/api/job/test_job/enable", "POST", "test_job"), is(204));
        verify(getRegCenter()).remove("/state/disable/job/test_job");
    }
}
